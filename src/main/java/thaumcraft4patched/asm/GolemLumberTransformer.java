package thaumcraft4patched.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public class GolemLumberTransformer implements IClassTransformer {

    private static final String TARGET =
            "thaumcraft.common.entities.ai.interact.AIHarvestLogs";

    private static final String OLD_FAKE_PLAYER_NAME =
            "FakeThaumcraftGolem";

    private static final String NEW_FAKE_PLAYER_NAME =
            "FakeThaumcraftGolemLumber";

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
        boolean patched = false;

        for (MethodNode method : classNode.methods) {
            if ("<init>".equals(method.name)
                    && method.desc.startsWith(
                    "(Lthaumcraft/common/entities/golems/"
                            + "EntityGolemBase;)")) {

                patched = patchConstructor(method);
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

    private boolean patchConstructor(MethodNode method) {
        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof LdcInsnNode)) {
                continue;
            }

            LdcInsnNode constantInstruction =
                    (LdcInsnNode) instruction;

            if (!OLD_FAKE_PLAYER_NAME.equals(
                    constantInstruction.cst)) {

                continue;
            }

            constantInstruction.cst =
                    NEW_FAKE_PLAYER_NAME;

            logger.info(
                    "Transforming AIHarvestLogs constructor ..."
            );

            return true;
        }

        return false;
    }
}
