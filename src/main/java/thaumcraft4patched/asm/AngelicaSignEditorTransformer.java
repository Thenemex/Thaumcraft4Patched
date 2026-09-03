package thaumcraft4patched.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public class AngelicaSignEditorTransformer
        implements IClassTransformer {

    private static final String TARGET =
            "com.gtnewhorizons.angelica.rendering.tesr.VanillaModelMeshes";

    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "AngelicaSignEditorCompatibilityPatch";

    private static final String RENDER_SIGN_NAME =
            "renderSign";

    private static final String RENDER_SIGN_CACHED_NAME =
            "tc4patched$renderSignCached";

    private static final String RENDER_SIGN_DESCRIPTOR =
            "(Lnet/minecraft/client/model/ModelSign;)V";

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
        MethodNode originalRenderSign = null;

        for (MethodNode method : classNode.methods) {
            if (RENDER_SIGN_CACHED_NAME.equals(method.name)
                    && RENDER_SIGN_DESCRIPTOR.equals(method.desc)) {

                return basicClass;
            }

            if (!RENDER_SIGN_NAME.equals(method.name)
                    || !RENDER_SIGN_DESCRIPTOR.equals(method.desc)) {

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
                        new String[0]
                );

        originalRenderSign.name =
                RENDER_SIGN_CACHED_NAME;

        MethodNode wrapper = new MethodNode(
                originalAccess,
                RENDER_SIGN_NAME,
                RENDER_SIGN_DESCRIPTOR,
                originalSignature,
                originalExceptions
        );

        wrapper.instructions.add(
                new VarInsnNode(Opcodes.ALOAD, 0)
        );

        //noinspection deprecation
        wrapper.instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        PATCH_OWNER,
                        RENDER_SIGN_NAME,
                        RENDER_SIGN_DESCRIPTOR
                )
        );

        wrapper.instructions.add(
                new InsnNode(Opcodes.RETURN)
        );

        wrapper.maxStack = 1;
        wrapper.maxLocals = 1;

        classNode.methods.add(wrapper);

        logger.info(
                "Successfully wrapped Angelica cached sign rendering "
                        + "for sign editor compatibility!"
        );

        return writeClass(classNode);
    }
}
