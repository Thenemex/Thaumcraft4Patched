package thaumcraft4patched.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.nextRealInstruction;
import static thaumcraft4patched.asm.ASMUtils.previousRealInstruction;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public class MagicCookiesDarkShrineTransformer
        implements IClassTransformer {

    private static final String TARGET =
            "tschallacka.magiccookies.worldgen.WorldGenDarkShrine";

    private static final String GENERATE_NAME =
            "generateDungeonAt";

    private static final String WORLD_OWNER =
            "net/minecraft/world/World";

    private static final String GET_BLOCK_DESCRIPTOR =
            "(III)Lnet/minecraft/block/Block;";

    private static final String BLOCKS_OWNER =
            "net/minecraft/init/Blocks";

    private static final String BLOCK_FIELD_DESCRIPTOR =
            "Lnet/minecraft/block/Block;";

    private static final String PATCH_OWNER =
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

        if (!TARGET.equals(transformedName)) {
            return basicClass;
        }

        ClassNode classNode = readClass(basicClass);
        MethodNode targetMethod = null;

        for (MethodNode method : classNode.methods) {
            if (GENERATE_NAME.equals(method.name)) {
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

        if (!patchFoundationLoop(targetMethod)) {
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

    private boolean patchFoundationLoop(MethodNode method) {
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
        foundationBlockRead.owner = PATCH_OWNER;
        foundationBlockRead.name = "getBlock";
        foundationBlockRead.desc = PATCH_GET_BLOCK_DESCRIPTOR;

        method.maxStack = method.maxStack + 1;

        return true;
    }

    private static boolean isAirLoopTest(
            MethodInsnNode blockRead) {

        AbstractInsnNode airRead =
                nextRealInstruction(blockRead);

        if (!(airRead instanceof FieldInsnNode)) {
            return false;
        }

        FieldInsnNode airField =
                (FieldInsnNode) airRead;

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

    private static int findFoundationCounter(
            MethodInsnNode blockRead) {

        int[] expectedOpcodes = {
                Opcodes.IADD, Opcodes.ILOAD, Opcodes.ILOAD,
                Opcodes.IADD, Opcodes.ILOAD, Opcodes.ILOAD,
                Opcodes.IADD, Opcodes.ILOAD, Opcodes.ILOAD,
                Opcodes.ALOAD
        };

        AbstractInsnNode current = blockRead;
        int counter = -1;

        for (int index = 0;
                index < expectedOpcodes.length;
                index++) {

            current = previousRealInstruction(current);

            if (current == null
                    || current.getOpcode()
                    != expectedOpcodes[index]) {

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
}
