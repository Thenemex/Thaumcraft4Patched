package thaumcraft4patched.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.previousRealInstruction;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public class ThaumicTinkererElementalFireTransformer
        implements IClassTransformer {

    private static final String TARGET =
            "thaumic.tinkerer.common.block.fire.BlockFireBase";

    private static final String UPDATE_TICK_DESCRIPTOR =
            "(Lnet/minecraft/world/World;IIILjava/util/Random;)V";

    private static final String GAME_RULES_OWNER =
            "net/minecraft/world/GameRules";

    private static final String GET_GAME_RULE_BOOLEAN_DESCRIPTOR =
            "(Ljava/lang/String;)Z";

    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "ThaumicTinkererElementalFireTickPatch";

    private static final String PATCH_GET_GAME_RULE_BOOLEAN_DESCRIPTOR =
            "(Lnet/minecraft/world/GameRules;Ljava/lang/String;)Z";

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
            if (UPDATE_TICK_DESCRIPTOR.equals(method.desc)
                    && ("func_149674_a".equals(method.name)
                    || "updateTick".equals(method.name))) {

                if (targetMethod != null) {
                    logger.error(
                            "Found multiple Thaumic Tinkerer BlockFireBase "
                                    + "updateTick methods. The elemental fire "
                                    + "tick patch was not installed!"
                    );

                    return basicClass;
                }

                targetMethod = method;
            }
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find Thaumic Tinkerer BlockFireBase.updateTick. "
                            + "The elemental fire tick patch was not installed!"
            );

            return basicClass;
        }

        if (!patchDoFireTickCall(targetMethod)) {
            logger.error(
                    "Could not find the unique Thaumic Tinkerer BlockFireBase "
                            + "doFireTick game rule call. The elemental fire "
                            + "tick patch was not installed!"
            );

            return basicClass;
        }

        logger.info(
                "Successfully transformed Thaumic Tinkerer elemental fire "
                        + "to tick when doFireTick is off!"
        );

        return writeClass(classNode);
    }

    private boolean patchDoFireTickCall(MethodNode method) {
        MethodInsnNode gameRuleCall = null;

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodCall =
                    (MethodInsnNode) instruction;

            if (methodCall.getOpcode() != Opcodes.INVOKEVIRTUAL
                    || !GAME_RULES_OWNER.equals(methodCall.owner)
                    || !GET_GAME_RULE_BOOLEAN_DESCRIPTOR.equals(
                    methodCall.desc)
                    || !("func_82766_b".equals(methodCall.name)
                    || "getGameRuleBooleanValue".equals(
                    methodCall.name))) {

                continue;
            }

            AbstractInsnNode previous =
                    previousRealInstruction(methodCall);

            if (!(previous instanceof LdcInsnNode)
                    || !"doFireTick".equals(
                    ((LdcInsnNode) previous).cst)) {

                continue;
            }

            if (gameRuleCall != null) {
                return false;
            }

            gameRuleCall = methodCall;
        }

        if (gameRuleCall == null) {
            return false;
        }

        gameRuleCall.setOpcode(Opcodes.INVOKESTATIC);
        gameRuleCall.owner = PATCH_OWNER;
        gameRuleCall.name = "getGameRuleBooleanValue";
        gameRuleCall.desc =
                PATCH_GET_GAME_RULE_BOOLEAN_DESCRIPTOR;

        return true;
    }
}
