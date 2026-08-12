package thaumcraft4patched.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;

@SuppressWarnings("unused")
public class TCPatchTransformer implements IClassTransformer {

    private static final String GOLEM_LUMBER_TARGET =
            "thaumcraft.common.entities.ai.interact.AIHarvestLogs";

    private static final String EXCAVATION_FOCUS_TARGET =
            "thaumcraft.common.items.wands.foci.ItemFocusExcavation";

    private static final String ENDER_ZOO_SPAWN_HANDLER_TARGET =
            "crazypants.enderzoo.spawn.MobSpawnEventHandler";

    private static final String OLD_GOLEM_FAKE_PLAYER_NAME =
            "FakeThaumcraftGolem";

    private static final String NEW_GOLEM_FAKE_PLAYER_NAME =
            "FakeThaumcraftGolemLumber";

    private static final String EXCAVATE_METHOD_NAME =
            "excavate";

    private static final String EXCAVATE_METHOD_DESCRIPTOR =
            "(Lnet/minecraft/world/World;"
                    + "Lnet/minecraft/item/ItemStack;"
                    + "Lnet/minecraft/entity/player/EntityPlayer;"
                    + "Lnet/minecraft/block/Block;IIII)Z";

    private static final String HARVEST_CONTEXT_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "ExcavationFocusHarvestContext";

    private static final String FORGE_EVENT_FACTORY_OWNER =
            "net/minecraftforge/event/ForgeEventFactory";

    private static final String FIRE_BLOCK_HARVESTING_NAME =
            "fireBlockHarvesting";

    private static final String FIRE_BLOCK_HARVESTING_DESCRIPTOR =
            "(Ljava/util/ArrayList;"
                    + "Lnet/minecraft/world/World;"
                    + "Lnet/minecraft/block/Block;"
                    + "IIIIIFZ"
                    + "Lnet/minecraft/entity/player/EntityPlayer;)F";

    private static final String THAUMCRAFT_BLOCK_UTILS_OWNER =
            "thaumcraft/common/lib/utils/BlockUtils";

    private static final String DROP_BLOCK_WITH_CHANCE_NAME =
            "dropBlockAsItemWithChance";

    private static final String DROP_BLOCK_WITH_CHANCE_DESCRIPTOR =
            "(Lnet/minecraft/world/World;"
                    + "Lnet/minecraft/block/Block;"
                    + "IIIIFI"
                    + "Lnet/minecraft/entity/player/EntityPlayer;)V";

    private static final String FORGE_HOOKS_OWNER =
            "net/minecraftforge/common/ForgeHooks";

    private static final String FORGE_TOOL_CHECK_NAME =
            "isToolEffective";

    private static final String TOOL_CHECK_DESCRIPTOR =
            "(Lnet/minecraft/item/ItemStack;"
                    + "Lnet/minecraft/block/Block;I)Z";

    private static final String EXCAVATION_TOOL_PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "HlcExcavationFocusToolPatch";

    @Override
    public byte[] transform(
            String name,
            String transformedName,
            byte[] basicClass) {

        if (basicClass == null) {
            return null;
        }

        if (GOLEM_LUMBER_TARGET.equals(transformedName)) {
            return transformGolemLumberClass(basicClass);
        }

        if (EXCAVATION_FOCUS_TARGET.equals(transformedName)) {
            return transformExcavationFocusClass(basicClass);
        }

        if (ENDER_ZOO_SPAWN_HANDLER_TARGET.equals(transformedName)) {
            return transformEnderZooSpawnHandler(basicClass);
        }

        return basicClass;
    }

    /**
     * Preserves the existing FakeThaumcraftGolem lumber patch.
     */
    private byte[] transformGolemLumberClass(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        boolean patched = false;

        for (MethodNode method : classNode.methods) {
            if ("<init>".equals(method.name)
                    && method.desc.startsWith(
                    "(Lthaumcraft/common/entities/golems/"
                            + "EntityGolemBase;)")) {

                patched = patchGolemConstructor(method);
                break;
            }
        }

        if (!patched) {
            logger.error(
                    "FakeThaumcraftGolem cannot be found in "
                            + "AIHarvestLogs constructor. Report to author!"
            );
        }

        return writeClass(classNode);
    }

    private boolean patchGolemConstructor(MethodNode method) {
        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof LdcInsnNode)) {
                continue;
            }

            LdcInsnNode constantInstruction =
                    (LdcInsnNode) instruction;

            if (OLD_GOLEM_FAKE_PLAYER_NAME.equals(
                    constantInstruction.cst)) {

                constantInstruction.cst =
                        NEW_GOLEM_FAKE_PLAYER_NAME;

                logger.info(
                        "Transforming AIHarvestLogs constructor ..."
                );

                return true;
            }
        }

        return false;
    }

    /**
     * Wraps only the harvest calls inside the Excavation Focus's private
     * excavate method.
     * <p>
     * Physically hitting a block with the wand does not use this method and
     * therefore does not activate the focus-magic harvest context.
     */
    private byte[] transformExcavationFocusClass(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        boolean foundExcavateMethod = false;
        boolean patched = false;

        for (MethodNode method : classNode.methods) {
            if (!EXCAVATE_METHOD_NAME.equals(method.name)
                    || !EXCAVATE_METHOD_DESCRIPTOR.equals(method.desc)) {
                continue;
            }

            foundExcavateMethod = true;
            patched = patchExcavationHarvestCalls(method);
            break;
        }

        if (!foundExcavateMethod) {
            logger.error(
                    "Could not find ItemFocusExcavation.excavate. "
                            + "The Excavation Focus harvest context "
                            + "was not installed!"
            );
        } else if (!patched) {
            logger.error(
                    "Could not find both Excavation Focus harvest calls. "
                            + "The Excavation Focus harvest context "
                            + "was not installed!"
            );
        } else {
            logger.info(
                    "Successfully transformed Excavation Focus harvest "
                            + "calls to track active focus magic!"
            );
        }

        return writeClass(classNode);
    }

    private boolean patchExcavationHarvestCalls(MethodNode method) {
        MethodInsnNode silkHarvestCall = null;
        MethodInsnNode normalHarvestCall = null;

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodCall =
                    (MethodInsnNode) instruction;

            if (methodCall.getOpcode() != Opcodes.INVOKESTATIC) {
                continue;
            }

            if (FORGE_EVENT_FACTORY_OWNER.equals(methodCall.owner)
                    && FIRE_BLOCK_HARVESTING_NAME.equals(methodCall.name)
                    && FIRE_BLOCK_HARVESTING_DESCRIPTOR.equals(
                    methodCall.desc)) {

                silkHarvestCall = methodCall;
                continue;
            }

            if (THAUMCRAFT_BLOCK_UTILS_OWNER.equals(methodCall.owner)
                    && DROP_BLOCK_WITH_CHANCE_NAME.equals(methodCall.name)
                    && DROP_BLOCK_WITH_CHANCE_DESCRIPTOR.equals(
                    methodCall.desc)) {

                normalHarvestCall = methodCall;
            }
        }

        /*
         * Do not partially patch the method. Both harvest paths must be found
         * before either call is redirected.
         */
        if (silkHarvestCall == null || normalHarvestCall == null) {
            return false;
        }

        silkHarvestCall.owner = HARVEST_CONTEXT_OWNER;
        silkHarvestCall.name = FIRE_BLOCK_HARVESTING_NAME;
        silkHarvestCall.desc = FIRE_BLOCK_HARVESTING_DESCRIPTOR;

        normalHarvestCall.owner = HARVEST_CONTEXT_OWNER;
        normalHarvestCall.name = DROP_BLOCK_WITH_CHANCE_NAME;
        normalHarvestCall.desc = DROP_BLOCK_WITH_CHANCE_DESCRIPTOR;

        return true;
    }

    /**
     * Redirects only Ender Zoo's dirt/grass tool-effectiveness check.
     */
    private byte[] transformEnderZooSpawnHandler(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        boolean patched = false;

        for (MethodNode method : classNode.methods) {
            if (!"onBlockHarvest".equals(method.name)
                    || !("(Lnet/minecraftforge/event/world/"
                    + "BlockEvent$HarvestDropsEvent;)V")
                    .equals(method.desc)) {

                continue;
            }

            patched = patchEnderZooToolCheck(method);
            break;
        }

        if (patched) {
            logger.info(
                    "Successfully transformed Ender Zoo's harvest "
                            + "tool check for Excavation Focus "
                            + "compatibility!"
            );
        } else {
            logger.error(
                    "Could not find Ender Zoo's ForgeHooks."
                            + "isToolEffective call in onBlockHarvest. "
                            + "The compatibility patch was not applied!"
            );
        }

        return writeClass(classNode);
    }

    private boolean patchEnderZooToolCheck(MethodNode method) {
        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodCall =
                    (MethodInsnNode) instruction;

            if (methodCall.getOpcode() != Opcodes.INVOKESTATIC
                    || !FORGE_HOOKS_OWNER.equals(methodCall.owner)
                    || !FORGE_TOOL_CHECK_NAME.equals(methodCall.name)
                    || !TOOL_CHECK_DESCRIPTOR.equals(methodCall.desc)) {

                continue;
            }

            methodCall.owner = EXCAVATION_TOOL_PATCH_OWNER;
            methodCall.name = FORGE_TOOL_CHECK_NAME;
            methodCall.desc = TOOL_CHECK_DESCRIPTOR;

            return true;
        }

        return false;
    }

    private static ClassNode readClass(byte[] basicClass) {
        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        return classNode;
    }

    private static byte[] writeClass(ClassNode classNode) {
        /*
         * These transformations replace constants or same-signature static
         * method calls, so the existing stack frames remain valid.
         */
        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}