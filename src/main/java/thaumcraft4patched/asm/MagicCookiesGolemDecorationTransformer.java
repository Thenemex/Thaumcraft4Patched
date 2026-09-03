package thaumcraft4patched.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public class MagicCookiesGolemDecorationTransformer
        implements IClassTransformer {

    private static final String TARGET =
            "tschallacka.magiccookies.entities.living.golem."
                    + "ItemGolemDecoration";

    private static final String ICON_DESCRIPTOR =
            "(I)Lnet/minecraft/util/IIcon;";

    private static final String ICON_FIELD_DESCRIPTOR =
            "[Lnet/minecraft/util/IIcon;";

    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "MagicCookiesGolemDecorationIconPatch";

    private static final String PATCH_DESCRIPTOR =
            "([Lnet/minecraft/util/IIcon;I)"
                    + "Lnet/minecraft/util/IIcon;";

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
            if (ICON_DESCRIPTOR.equals(method.desc)
                    && ("func_77617_a".equals(method.name)
                    || "getIconFromDamage".equals(method.name))) {

                if (targetMethod != null) {
                    logger.error(
                            "Found multiple Magic Cookies Golem Decoration "
                                    + "icon lookup methods. The icon bounds "
                                    + "patch was not installed!"
                    );

                    return basicClass;
                }

                targetMethod = method;
            }
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find Magic Cookies ItemGolemDecoration "
                            + "icon lookup. The icon bounds patch "
                            + "was not installed!"
            );

            return basicClass;
        }

        AbstractInsnNode[] instructions =
                targetMethod.instructions.toArray();

        AbstractInsnNode[] realInstructions =
                new AbstractInsnNode[5];

        int realCount = 0;

        for (AbstractInsnNode instruction : instructions) {
            if (instruction.getOpcode() < 0) {
                continue;
            }

            if (realCount >= realInstructions.length) {
                logger.error(
                        "Magic Cookies ItemGolemDecoration icon lookup "
                                + "has an unexpected bytecode shape. "
                                + "The icon bounds patch was not installed!"
                );

                return basicClass;
            }

            realInstructions[realCount++] = instruction;
        }

        if (realCount != 5
                || realInstructions[0].getOpcode()
                != Opcodes.ALOAD
                || realInstructions[1].getOpcode()
                != Opcodes.GETFIELD
                || realInstructions[2].getOpcode()
                != Opcodes.ILOAD
                || realInstructions[3].getOpcode()
                != Opcodes.AALOAD
                || realInstructions[4].getOpcode()
                != Opcodes.ARETURN) {

            logger.error(
                    "Magic Cookies ItemGolemDecoration icon lookup "
                            + "no longer matches the expected implementation. "
                            + "The icon bounds patch was not installed!"
            );

            return basicClass;
        }

        VarInsnNode loadThis =
                (VarInsnNode) realInstructions[0];

        FieldInsnNode iconField =
                (FieldInsnNode) realInstructions[1];

        VarInsnNode loadMetadata =
                (VarInsnNode) realInstructions[2];

        if (loadThis.var != 0
                || loadMetadata.var != 1
                || !"icon".equals(iconField.name)
                || !ICON_FIELD_DESCRIPTOR.equals(
                iconField.desc)) {

            logger.error(
                    "Magic Cookies ItemGolemDecoration icon lookup "
                            + "uses an unexpected field or argument layout. "
                            + "The icon bounds patch was not installed!"
            );

            return basicClass;
        }

        //noinspection deprecation
        targetMethod.instructions.set(
                realInstructions[3],
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        PATCH_OWNER,
                        "getIcon",
                        PATCH_DESCRIPTOR
                )
        );

        logger.info(
                "Successfully transformed Magic Cookies Golem Decoration "
                        + "icon lookup with metadata bounds protection!"
        );

        return writeClass(classNode);
    }
}
