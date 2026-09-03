package thaumcraft4patched.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public class ExcavationFocusTransformer implements IClassTransformer {

    private static final String TARGET =
            "thaumcraft.common.items.wands.foci.ItemFocusExcavation";

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

    @Override
    public byte[] transform(
            String name,
            String transformedName,
            byte[] basicClass) {

        if (basicClass == null) {
            return null;
        }

        if (!TARGET.equals(transformedName)) {
            return basicClass;
        }

        ClassNode classNode = readClass(basicClass);
        boolean foundExcavateMethod = false;
        boolean patched = false;

        for (MethodNode method : classNode.methods) {
            if (!EXCAVATE_METHOD_NAME.equals(method.name)
                    || !EXCAVATE_METHOD_DESCRIPTOR.equals(method.desc)) {

                continue;
            }

            foundExcavateMethod = true;
            patched = patchHarvestCalls(method);
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

    private boolean patchHarvestCalls(MethodNode method) {
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
}
