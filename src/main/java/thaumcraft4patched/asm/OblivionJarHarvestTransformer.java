package thaumcraft4patched.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public class OblivionJarHarvestTransformer
        implements IClassTransformer {

    private static final String BLOCK_JAR_TARGET =
            "thaumcraft.common.blocks.BlockJar";

    private static final String OBLIVION_JAR_TARGET =
            "flaxbeard.thaumicexploration.block.BlockTrashJar";

    private static final String GET_DROPS_NAME =
            "getDrops";

    private static final String GET_DROPS_DESCRIPTOR =
            "(Lnet/minecraft/world/World;IIIII)"
                    + "Ljava/util/ArrayList;";

    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "ThaumicExplorationOblivionJarHarvestPatch";

    private static final String PATCH_GET_DROPS_DESCRIPTOR =
            "(Lnet/minecraft/block/Block;I)"
                    + "Ljava/util/ArrayList;";

    private static final String BLOCK_JAR_HARVEST_DESCRIPTOR =
            "(Lnet/minecraft/world/World;IIII"
                    + "Lnet/minecraft/entity/player/EntityPlayer;)V";

    private static final String BLOCK_DROP_AS_ITEM_DESCRIPTOR =
            "(Lnet/minecraft/world/World;IIIII)V";

    private static final String PATCH_DROP_AS_ITEM_DESCRIPTOR =
            "(Lnet/minecraft/block/Block;"
                    + "Lnet/minecraft/world/World;IIIII)V";

    @Override
    public byte[] transform(
            String name,
            String transformedName,
            byte[] basicClass) {

        if (basicClass == null) {
            return null;
        }

        if (BLOCK_JAR_TARGET.equals(transformedName)) {
            return transformBlockJar(basicClass);
        }

        if (OBLIVION_JAR_TARGET.equals(transformedName)) {
            return transformOblivionJar(basicClass);
        }

        return basicClass;
    }

    private byte[] transformBlockJar(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        MethodNode harvestMethod = null;

        for (MethodNode method : classNode.methods) {
            if (BLOCK_JAR_HARVEST_DESCRIPTOR.equals(method.desc)
                    && ("func_149681_a".equals(method.name)
                    || "harvestBlock".equals(method.name))) {

                harvestMethod = method;
                break;
            }
        }

        if (harvestMethod == null) {
            logger.error(
                    "Could not find Thaumcraft BlockJar.harvestBlock. "
                            + "Oblivion Jar duplicate-drop protection "
                            + "was not installed!"
            );

            return basicClass;
        }

        MethodInsnNode dropCall = null;

        for (AbstractInsnNode instruction
                : harvestMethod.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodCall =
                    (MethodInsnNode) instruction;

            if (methodCall.getOpcode() != Opcodes.INVOKEVIRTUAL
                    || !BLOCK_DROP_AS_ITEM_DESCRIPTOR.equals(
                    methodCall.desc)
                    || !("func_149697_b".equals(methodCall.name)
                    || "dropBlockAsItem".equals(methodCall.name))) {

                continue;
            }

            if (dropCall != null) {
                logger.error(
                        "Found multiple BlockJar dropBlockAsItem calls. "
                                + "Oblivion Jar duplicate-drop protection "
                                + "was not installed!"
                );

                return basicClass;
            }

            dropCall = methodCall;
        }

        if (dropCall == null) {
            logger.error(
                    "Could not find BlockJar's early dropBlockAsItem call. "
                            + "Oblivion Jar duplicate-drop protection "
                            + "was not installed!"
            );

            return basicClass;
        }

        dropCall.setOpcode(Opcodes.INVOKESTATIC);
        dropCall.owner = PATCH_OWNER;
        dropCall.name = "dropBlockAsItem";
        dropCall.desc = PATCH_DROP_AS_ITEM_DESCRIPTOR;

        logger.info(
                "Successfully wrapped Thaumcraft BlockJar's early drop "
                        + "for Oblivion Jar compatibility!"
        );

        return writeClass(classNode);
    }

    private byte[] transformOblivionJar(
            byte[] basicClass) {

        ClassNode classNode = readClass(basicClass);
        MethodNode getDropsMethod = null;

        for (MethodNode method : classNode.methods) {
            if (GET_DROPS_NAME.equals(method.name)
                    && GET_DROPS_DESCRIPTOR.equals(method.desc)) {

                getDropsMethod = method;
                break;
            }
        }

        if (getDropsMethod == null) {
            logger.error(
                    "Could not find Thaumic Exploration "
                            + "BlockTrashJar.getDrops. Oblivion Jar harvest "
                            + "patch was not installed!"
            );

            return basicClass;
        }

        if (!isOriginalEmptyDropsMethod(getDropsMethod)) {
            logger.error(
                    "Thaumic Exploration BlockTrashJar.getDrops no longer "
                            + "matches the expected empty-drop implementation. "
                            + "Oblivion Jar harvest patch was not installed!"
            );

            return basicClass;
        }

        getDropsMethod.instructions.clear();
        getDropsMethod.tryCatchBlocks.clear();

        if (getDropsMethod.localVariables != null) {
            getDropsMethod.localVariables.clear();
        }

        getDropsMethod.instructions.add(
                new VarInsnNode(Opcodes.ALOAD, 0)
        );

        getDropsMethod.instructions.add(
                new VarInsnNode(Opcodes.ILOAD, 5)
        );

        //noinspection deprecation
        getDropsMethod.instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        PATCH_OWNER,
                        GET_DROPS_NAME,
                        PATCH_GET_DROPS_DESCRIPTOR
                )
        );

        getDropsMethod.instructions.add(
                new InsnNode(Opcodes.ARETURN)
        );

        getDropsMethod.maxStack = 2;
        getDropsMethod.maxLocals = Math.max(
                getDropsMethod.maxLocals,
                7
        );

        logger.info(
                "Successfully transformed Thaumic Exploration's "
                        + "Oblivion Jar drop handling! yippeee"
        );

        return writeClass(classNode);
    }

    private static boolean isOriginalEmptyDropsMethod(
            MethodNode method) {

        int[] expectedOpcodes = {
                Opcodes.NEW,
                Opcodes.DUP,
                Opcodes.INVOKESPECIAL,
                Opcodes.ASTORE,
                Opcodes.ALOAD,
                Opcodes.ARETURN
        };

        int opcodeIndex = 0;

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            int opcode = instruction.getOpcode();

            if (opcode < 0) {
                continue;
            }

            if (opcodeIndex >= expectedOpcodes.length
                    || opcode != expectedOpcodes[opcodeIndex]) {

                return false;
            }

            if (opcode == Opcodes.INVOKESPECIAL) {
                if (!(instruction instanceof MethodInsnNode)) {
                    return false;
                }

                MethodInsnNode constructorCall =
                        (MethodInsnNode) instruction;

                if (!"java/util/ArrayList".equals(
                        constructorCall.owner)
                        || !"<init>".equals(constructorCall.name)
                        || !"()V".equals(constructorCall.desc)) {

                    return false;
                }
            }

            opcodeIndex++;
        }

        return opcodeIndex == expectedOpcodes.length;
    }
}
