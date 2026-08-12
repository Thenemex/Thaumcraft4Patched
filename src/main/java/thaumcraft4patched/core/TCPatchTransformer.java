package thaumcraft4patched.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;

@SuppressWarnings("unused")
public class TCPatchTransformer implements IClassTransformer {

    private static final String GOLEM_LUMBER_TARGET =
            "thaumcraft.common.entities.ai.interact.AIHarvestLogs";

    private static final String EXCAVATION_FOCUS_TARGET =
            "thaumcraft.common.items.wands.foci.ItemFocusExcavation";

    private static final String ENDER_ZOO_SPAWN_HANDLER_TARGET =
            "crazypants.enderzoo.spawn.MobSpawnEventHandler";

    private static final String WITCHERY_RAISE_LAND_TARGET =
            "com.emoniph.witchery.brewing.action.effect.BrewActionRaiseLand";

    private static final String WITCHERY_RAISE_LAND_METHOD =
            "doApplyToBlock";

    private static final String WITCHERY_RAISE_LAND_METHOD_DESCRIPTOR =
            "(Lnet/minecraft/world/World;"
                    + "IIILnet/minecraftforge/common/util/ForgeDirection;"
                    + "ILcom/emoniph/witchery/brewing/ModifiersEffect;"
                    + "Lnet/minecraft/item/ItemStack;)V";

    private static final String WORLD_OWNER =
            "net/minecraft/world/World";

    private static final String SET_BLOCK_TO_AIR_DESCRIPTOR =
            "(III)Z";

    private static final String SET_BLOCK_DESCRIPTOR =
            "(IIILnet/minecraft/block/Block;II)Z";

    private static final String RAISE_LAND_PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "WitcheryRaiseLandProtectionPatch";

    private static final String PATCH_SET_BLOCK_TO_AIR_DESCRIPTOR =
            "(Lnet/minecraft/world/World;III)Z";

    private static final String PATCH_SET_BLOCK_DESCRIPTOR =
            "(Lnet/minecraft/world/World;"
                    + "IIILnet/minecraft/block/Block;II)Z";

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

    private static final String ANGELICA_MODEL_MESHES_TARGET =
            "com.gtnewhorizons.angelica.rendering.tesr.VanillaModelMeshes";

    private static final String ANGELICA_SIGN_PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "AngelicaSignEditorCompatibilityPatch";

    private static final String ANGELICA_RENDER_SIGN_NAME =
            "renderSign";

    private static final String ANGELICA_RENDER_SIGN_CACHED_NAME =
            "tc4patched$renderSignCached";

    private static final String ANGELICA_RENDER_SIGN_DESCRIPTOR =
            "(Lnet/minecraft/client/model/ModelSign;)V";

    private static final String MAGIC_COOKIES_DARK_SHRINE_TARGET =
            "tschallacka.magiccookies.worldgen.WorldGenDarkShrine";

    private static final String DARK_SHRINE_GENERATE_NAME =
            "generateDungeonAt";

    private static final String GET_BLOCK_DESCRIPTOR =
            "(III)Lnet/minecraft/block/Block;";

    private static final String BLOCKS_OWNER =
            "net/minecraft/init/Blocks";

    private static final String BLOCK_FIELD_DESCRIPTOR =
            "Lnet/minecraft/block/Block;";

    private static final String DARK_SHRINE_PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "MagicCookiesDarkShrineFillPatch";

    private static final String PATCH_GET_BLOCK_DESCRIPTOR =
            "(Lnet/minecraft/world/World;IIII)"
                    + "Lnet/minecraft/block/Block;";

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

        if (WITCHERY_RAISE_LAND_TARGET.equals(transformedName)) {
            return transformWitcheryRaiseLandClass(basicClass);
        }

        if (ANGELICA_MODEL_MESHES_TARGET.equals(transformedName)) {
            return transformAngelicaModelMeshes(basicClass);
        }

        if (MAGIC_COOKIES_DARK_SHRINE_TARGET.equals(transformedName)) {
            return transformMagicCookiesDarkShrine(basicClass);
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

    /**
     * Redirects only Witchery Raise Land's source removal and destination
     * placement calls through our protection helper.
     *
     * Both calls must be found before either one is modified. This prevents a
     * partial transformation from creating block loss or duplication.
     */
    private byte[] transformWitcheryRaiseLandClass(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        MethodNode targetMethod = null;

        for (MethodNode method : classNode.methods) {
            if (WITCHERY_RAISE_LAND_METHOD.equals(method.name)
                    && WITCHERY_RAISE_LAND_METHOD_DESCRIPTOR.equals(
                    method.desc)) {

                targetMethod = method;
                break;
            }
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find Witchery BrewActionRaiseLand."
                            + "doApplyToBlock. Raise Land protection "
                            + "was not installed!"
            );

            return basicClass;
        }

        if (!patchWitcheryRaiseLandBlockMoves(targetMethod)) {
            logger.error(
                    "Could not find both Witchery Raise Land block "
                            + "movement calls. Raise Land protection "
                            + "was not installed!"
            );

            return basicClass;
        }

        logger.info(
                "Successfully transformed Witchery Raise Land block "
                        + "movement calls for protected-block handling!"
        );

        return writeClass(classNode);
    }

    private boolean patchWitcheryRaiseLandBlockMoves(
            MethodNode method) {

        MethodInsnNode removeBlockCall = null;
        MethodInsnNode placeBlockCall = null;

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodCall =
                    (MethodInsnNode) instruction;

            if (methodCall.getOpcode() != Opcodes.INVOKEVIRTUAL
                    || !WORLD_OWNER.equals(methodCall.owner)) {
                continue;
            }

            if (SET_BLOCK_TO_AIR_DESCRIPTOR.equals(methodCall.desc)
                    && ("func_147468_f".equals(methodCall.name)
                    || "setBlockToAir".equals(methodCall.name))) {

                removeBlockCall = methodCall;
                continue;
            }

            if (SET_BLOCK_DESCRIPTOR.equals(methodCall.desc)
                    && ("func_147465_d".equals(methodCall.name)
                    || "setBlock".equals(methodCall.name))) {

                placeBlockCall = methodCall;
            }
        }

        /*
         * Never partially transform Raise Land. Preventing only one half of
         * the move could destroy blocks or duplicate protected blocks.
         */
        if (removeBlockCall == null || placeBlockCall == null) {
            return false;
        }

        removeBlockCall.setOpcode(Opcodes.INVOKESTATIC);
        removeBlockCall.owner = RAISE_LAND_PATCH_OWNER;
        removeBlockCall.name = "setBlockToAir";
        removeBlockCall.desc =
                PATCH_SET_BLOCK_TO_AIR_DESCRIPTOR;

        placeBlockCall.setOpcode(Opcodes.INVOKESTATIC);
        placeBlockCall.owner = RAISE_LAND_PATCH_OWNER;
        placeBlockCall.name = "setBlock";
        placeBlockCall.desc =
                PATCH_SET_BLOCK_DESCRIPTOR;

        return true;
    }

    /**
     * Wraps Angelica's cached vanilla sign renderer with our compatibility
     * helper while preserving the original cached implementation.
     *
     * The original renderSign(ModelSign) method is renamed and a same-signature
     * wrapper is installed in its place. Normal rendering therefore continues
     * to use Angelica's cached implementation, while the helper can selectively
     * fall back to vanilla rendering inside the sign editor.
     */
    private byte[] transformAngelicaModelMeshes(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        MethodNode originalRenderSign = null;

        for (MethodNode method : classNode.methods) {
            if (ANGELICA_RENDER_SIGN_CACHED_NAME.equals(method.name)
                    && ANGELICA_RENDER_SIGN_DESCRIPTOR.equals(method.desc)) {

                /*
                 * Already transformed. Avoid ever wrapping the class twice.
                 */
                return basicClass;
            }

            if (!ANGELICA_RENDER_SIGN_NAME.equals(method.name)
                    || !ANGELICA_RENDER_SIGN_DESCRIPTOR.equals(method.desc)) {

                continue;
            }

            if (originalRenderSign != null) {
                logger.error(
                        "Found multiple Angelica renderSign(ModelSign) methods. "
                                + "Sign editor compatibility patch "
                                + "was not installed!"
                );

                return basicClass;
            }

            originalRenderSign = method;
        }

        if (originalRenderSign == null) {
            logger.error(
                    "Could not find Angelica VanillaModelMeshes.renderSign"
                            + "(ModelSign). Sign editor compatibility patch "
                            + "was not installed!"
            );

            return basicClass;
        }

        int originalAccess = originalRenderSign.access;
        String originalSignature = originalRenderSign.signature;
        String[] originalExceptions =
                originalRenderSign.exceptions == null
                        ? null
                        : originalRenderSign.exceptions.toArray(
                        new String[originalRenderSign.exceptions.size()]
                );

        /*
         * Preserve Angelica's complete original cached implementation under
         * another name. The compatibility helper calls this method whenever
         * the vanilla sign-editor fallback is not required.
         */
        originalRenderSign.name =
                ANGELICA_RENDER_SIGN_CACHED_NAME;

        MethodNode wrapper = new MethodNode(
                originalAccess,
                ANGELICA_RENDER_SIGN_NAME,
                ANGELICA_RENDER_SIGN_DESCRIPTOR,
                originalSignature,
                originalExceptions
        );

        wrapper.instructions.add(
                new VarInsnNode(Opcodes.ALOAD, 0)
        );

        wrapper.instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        ANGELICA_SIGN_PATCH_OWNER,
                        ANGELICA_RENDER_SIGN_NAME,
                        ANGELICA_RENDER_SIGN_DESCRIPTOR
                )
        );

        wrapper.instructions.add(
                new InsnNode(Opcodes.RETURN)
        );

        /*
         * ClassWriter(0) does not calculate these values.
         * The wrapper only holds one ModelSign reference.
         */
        wrapper.maxStack = 1;
        wrapper.maxLocals = 1;

        classNode.methods.add(wrapper);

        logger.info(
                "Successfully wrapped Angelica cached sign rendering "
                        + "for sign editor compatibility!"
        );

        return writeClass(classNode);
    }

    /**
     * Sends the block read of the Dark Shrine foundation loop through our
     * helper, so the loop gets a floor.
     *
     * The loop keeps the vertical offset in a local variable that starts at -8
     * and goes down by one for each layer. The helper needs that offset, so the
     * offset is pushed as an extra argument and the read becomes a static call.
     *
     * Only a read that feeds a comparison with air, and that uses a counter the
     * method lowers by one, is treated as the foundation loop. A method with
     * more than one read of that shape is left alone.
     */
    private byte[] transformMagicCookiesDarkShrine(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        MethodNode targetMethod = null;

        for (MethodNode method : classNode.methods) {
            if (DARK_SHRINE_GENERATE_NAME.equals(method.name)) {
                targetMethod = method;
                break;
            }
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find Magic Cookies WorldGenDarkShrine."
                            + "generateDungeonAt. The endless foundation "
                            + "patch was not installed!"
            );

            return basicClass;
        }

        if (!patchDarkShrineFoundationLoop(targetMethod)) {
            logger.error(
                    "Could not find the Magic Cookies Dark Shrine "
                            + "foundation loop. The endless foundation "
                            + "patch was not installed!"
            );

            return basicClass;
        }

        logger.info(
                "Successfully transformed the Magic Cookies Dark Shrine "
                        + "foundation loop to stop at a set depth!"
        );

        return writeClass(classNode);
    }

    private boolean patchDarkShrineFoundationLoop(MethodNode method) {
        MethodInsnNode foundationBlockRead = null;
        int foundationCounter = -1;

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodCall =
                    (MethodInsnNode) instruction;

            if (methodCall.getOpcode() != Opcodes.INVOKEVIRTUAL
                    || !WORLD_OWNER.equals(methodCall.owner)
                    || !GET_BLOCK_DESCRIPTOR.equals(methodCall.desc)
                    || !("func_147439_a".equals(methodCall.name)
                    || "getBlock".equals(methodCall.name))) {

                continue;
            }

            if (!isAirLoopTest(methodCall)) {
                continue;
            }

            int counter = findFoundationCounter(methodCall);

            if (counter < 0
                    || !hasDecrement(method, counter)) {

                continue;
            }

            /*
             * A second loop of the same shape would make the counter
             * unsafe to guess. Leave the class as it is.
             */
            if (foundationBlockRead != null) {
                return false;
            }

            foundationBlockRead = methodCall;
            foundationCounter = counter;
        }

        if (foundationBlockRead == null) {
            return false;
        }

        method.instructions.insertBefore(
                foundationBlockRead,
                new VarInsnNode(Opcodes.ILOAD, foundationCounter)
        );

        foundationBlockRead.setOpcode(Opcodes.INVOKESTATIC);
        foundationBlockRead.owner = DARK_SHRINE_PATCH_OWNER;
        foundationBlockRead.name = "getBlock";
        foundationBlockRead.desc = PATCH_GET_BLOCK_DESCRIPTOR;

        /*
         * ClassWriter(0) keeps the stored maxStack, and the extra
         * argument holds one more slot.
         */
        method.maxStack = method.maxStack + 1;

        return true;
    }

    /**
     * Tells if this block read feeds the air test of the foundation loop.
     */
    private static boolean isAirLoopTest(MethodInsnNode blockRead) {
        AbstractInsnNode airRead = nextRealInstruction(blockRead);

        if (!(airRead instanceof FieldInsnNode)) {
            return false;
        }

        FieldInsnNode airField = (FieldInsnNode) airRead;

        if (airField.getOpcode() != Opcodes.GETSTATIC
                || !BLOCKS_OWNER.equals(airField.owner)
                || !BLOCK_FIELD_DESCRIPTOR.equals(airField.desc)
                || !("field_150350_a".equals(airField.name)
                || "air".equals(airField.name))) {

            return false;
        }

        AbstractInsnNode comparison =
                nextRealInstruction(airRead);

        return comparison != null
                && comparison.getOpcode() == Opcodes.IF_ACMPNE;
    }

    /**
     * Reads back the arguments of the block read and gives the local variable
     * that holds the vertical offset of the loop.
     *
     * The expected shape, in normal order, is the world, then three sums of two
     * local variables. The second variable of the middle sum is the offset.
     */
    private static int findFoundationCounter(MethodInsnNode blockRead) {
        int[] expectedOpcodes = {
                Opcodes.IADD, Opcodes.ILOAD, Opcodes.ILOAD,
                Opcodes.IADD, Opcodes.ILOAD, Opcodes.ILOAD,
                Opcodes.IADD, Opcodes.ILOAD, Opcodes.ILOAD,
                Opcodes.ALOAD
        };

        AbstractInsnNode current = blockRead;
        int counter = -1;

        for (int index = 0; index < expectedOpcodes.length; index++) {
            current = previousRealInstruction(current);

            if (current == null
                    || current.getOpcode() != expectedOpcodes[index]) {

                return -1;
            }

            if (index == 4) {
                counter = ((VarInsnNode) current).var;
            }
        }

        return counter;
    }

    private static boolean hasDecrement(
            MethodNode method,
            int variable) {

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof IincInsnNode)) {
                continue;
            }

            IincInsnNode increment =
                    (IincInsnNode) instruction;

            if (increment.var == variable
                    && increment.incr < 0) {

                return true;
            }
        }

        return false;
    }

    private static AbstractInsnNode nextRealInstruction(
            AbstractInsnNode instruction) {

        AbstractInsnNode current = instruction.getNext();

        while (current != null
                && current.getOpcode() < 0) {

            current = current.getNext();
        }

        return current;
    }

    private static AbstractInsnNode previousRealInstruction(
            AbstractInsnNode instruction) {

        AbstractInsnNode current = instruction.getPrevious();

        while (current != null
                && current.getOpcode() < 0) {

            current = current.getPrevious();
        }

        return current;
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