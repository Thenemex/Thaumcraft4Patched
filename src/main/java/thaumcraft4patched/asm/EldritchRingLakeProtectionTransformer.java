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
public class EldritchRingLakeProtectionTransformer
        implements IClassTransformer {

    private static final String TARGET =
            "net.minecraft.world.gen.ChunkProviderGenerate";

    private static final String POPULATE_NAME =
            "func_73153_a";

    private static final String POPULATE_DESCRIPTOR =
            "(Lnet/minecraft/world/chunk/IChunkProvider;II)V";

    private static final String WORLD_GEN_LAKES_OWNER =
            "net/minecraft/world/gen/feature/WorldGenLakes";

    private static final String WORLD_GEN_LAKES_GENERATE_NAME =
            "func_76484_a";

    private static final String WORLD_GEN_LAKES_GENERATE_DESCRIPTOR =
            "(Lnet/minecraft/world/World;"
                    + "Ljava/util/Random;III)Z";

    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "EldritchRingLakeProtectionPatch";

    private static final String PATCH_DESCRIPTOR =
            "(Lnet/minecraft/world/gen/feature/WorldGenLakes;"
                    + "Lnet/minecraft/world/World;"
                    + "Ljava/util/Random;III)Z";

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
        MethodNode populateMethod = null;

        for (MethodNode method : classNode.methods) {
            if (!POPULATE_NAME.equals(method.name)
                    || !POPULATE_DESCRIPTOR.equals(method.desc)) {

                continue;
            }

            if (populateMethod != null) {
                logger.error(
                        "Found multiple ChunkProviderGenerate populate methods. "
                                + "Eldritch Ring lake protection "
                                + "was not installed!"
                );

                return basicClass;
            }

            populateMethod = method;
        }

        if (populateMethod == null) {
            logger.error(
                    "Could not find ChunkProviderGenerate.populate. "
                            + "Eldritch Ring lake protection "
                            + "was not installed!"
            );

            return basicClass;
        }

        MethodInsnNode[] lakeGenerateCalls =
                new MethodInsnNode[2];

        int count = 0;

        for (AbstractInsnNode instruction
                : populateMethod.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode call =
                    (MethodInsnNode) instruction;

            if (call.getOpcode() != Opcodes.INVOKEVIRTUAL
                    || !WORLD_GEN_LAKES_OWNER.equals(call.owner)
                    || !WORLD_GEN_LAKES_GENERATE_NAME.equals(call.name)
                    || !WORLD_GEN_LAKES_GENERATE_DESCRIPTOR.equals(
                    call.desc)) {

                continue;
            }

            if (count < lakeGenerateCalls.length) {
                lakeGenerateCalls[count] = call;
            }

            count++;
        }

        if (count != lakeGenerateCalls.length) {
            logger.error(
                    "ChunkProviderGenerate has an unexpected number "
                            + "of vanilla lake generation calls. "
                            + "Eldritch Ring lake protection "
                            + "was not installed!"
            );

            return basicClass;
        }

        for (MethodInsnNode call : lakeGenerateCalls) {
            call.setOpcode(Opcodes.INVOKESTATIC);
            call.owner = PATCH_OWNER;
            call.name = "generate";
            call.desc = PATCH_DESCRIPTOR;
        }

        logger.info(
                "Successfully installed Eldritch Ring lake protection!"
        );

        return writeClass(classNode);
    }
}
