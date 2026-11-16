package thaumcraft4patched.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public class VengefulGolemAggroMessageTransformer implements IClassTransformer {

    private static final String TARGET =
            "com.ilya3point999k.thaumicconcilium.common.entities.mobs.VengefulGolem";
    private static final String RANGED_ATTACK_DESCRIPTOR = "(Lnet/minecraft/entity/EntityLivingBase;F)V";
    private static final String PLAYER = "net/minecraft/entity/player/EntityPlayer";
    private static final String CHAT_ARGUMENT = "Lnet/minecraft/util/IChatComponent;";
    private static final String PATCH = "thaumcraft4patched/model/patch/VengefulGolemAggroMessagePatch";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || (!TARGET.equals(name) && !TARGET.equals(transformedName))) {
            return basicClass;
        }

        ClassNode classNode = readClass(basicClass);
        MethodInsnNode tauntCall = null;
        int chatCalls = 0;
        for (MethodNode method : classNode.methods) {
            if (!RANGED_ATTACK_DESCRIPTOR.equals(method.desc)
                    || !("attackEntityWithRangedAttack".equals(method.name)
                    || "func_82196_d".equals(method.name))) {
                continue;
            }

            boolean hasTauntKey = false;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof LdcInsnNode
                        && "tc.golem.taunt.".equals(((LdcInsnNode) instruction).cst)) {
                    hasTauntKey = true;
                }
                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.getOpcode() == Opcodes.INVOKEVIRTUAL && PLAYER.equals(call.owner)
                        && ("(" + CHAT_ARGUMENT + ")V").equals(call.desc)
                        && ("addChatMessage".equals(call.name) || "func_145747_a".equals(call.name))) {
                    chatCalls++;
                    if (hasTauntKey) {
                        tauntCall = call;
                    }
                }
            }
        }

        if (chatCalls != 1 || tauntCall == null) {
            logger.error("Could not identify the Thaumic Concilium Vengeful Golem chat taunt.");
            return basicClass;
        }

        tauntCall.setOpcode(Opcodes.INVOKESTATIC);
        tauntCall.owner = PATCH;
        tauntCall.name = "sendTaunt";
        tauntCall.desc = "(L" + PLAYER + ";" + CHAT_ARGUMENT + ")V";
        logger.info("Patched the Thaumic Concilium Vengeful Golem chat taunt.");
        return writeClass(classNode);
    }
}
