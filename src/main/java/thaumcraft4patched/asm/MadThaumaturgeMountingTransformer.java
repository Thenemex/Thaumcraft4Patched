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
public class MadThaumaturgeMountingTransformer
        implements IClassTransformer {

    private static final String TARGET =
            "com.ilya3point999k.thaumicconcilium.common.entities.mobs.MadThaumaturge";

    private static final String MOUNT_DESCRIPTOR =
            "(Lnet/minecraft/entity/Entity;)V";

    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/MadThaumaturgeMountingPatch";

    private static final String PATCH_DESCRIPTOR =
            "(Lnet/minecraft/entity/Entity;"
                    + "Lnet/minecraft/entity/Entity;)V";

    @Override
    public byte[] transform(
            String name,
            String transformedName,
            byte[] basicClass) {

        if (basicClass == null) {
            return null;
        }

        if (!TARGET.equals(name)
                && !TARGET.equals(transformedName)) {

            return basicClass;
        }

        ClassNode classNode = readClass(basicClass);

        MethodNode updateMethod = null;

        for (MethodNode method : classNode.methods) {

            if (!"()V".equals(method.desc)) {
                continue;
            }

            if (!"onUpdate".equals(method.name)
                    && !"func_70071_h_".equals(method.name)) {

                continue;
            }

            if (updateMethod != null) {
                logger.error(
                        "Found multiple MadThaumaturge update methods. "
                                + "Mounting patch will not be applied."
                );

                return basicClass;
            }

            updateMethod = method;
        }

        if (updateMethod == null) {
            logger.error(
                    "Could not find MadThaumaturge update method. "
                            + "Mounting patch will not be applied."
            );

            return basicClass;
        }

        MethodInsnNode mountCall = null;
        int matches = 0;

        for (AbstractInsnNode instruction =
                updateMethod.instructions.getFirst();
                instruction != null;
                instruction = instruction.getNext()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsn =
                    (MethodInsnNode) instruction;

            if (methodInsn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                continue;
            }

            if (!MOUNT_DESCRIPTOR.equals(methodInsn.desc)) {
                continue;
            }

            if (!"mountEntity".equals(methodInsn.name)
                    && !"func_70078_a".equals(methodInsn.name)) {

                continue;
            }

            mountCall = methodInsn;
            matches++;
        }

        if (matches != 1 || mountCall == null) {
            logger.error(
                    "Found {} Mad Thaumaturge mount calls instead "
                            + "of the expected 1. Mounting patch "
                            + "will not be applied.",
                    matches
            );

            return basicClass;
        }


        mountCall.setOpcode(Opcodes.INVOKESTATIC);
        mountCall.owner = PATCH_OWNER;
        mountCall.name = "handleMount";
        mountCall.desc = PATCH_DESCRIPTOR;

        logger.info(
                "Successfully disabled Mad Thaumaturge target mounting."
        );

        return writeClass(classNode);
    }
}
