package thaumcraft4patched.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public final class SpecialAIShamblingHuskBlacklistTransformer implements IClassTransformer {

    private static final String TARGET = "toast.specialAI.ai.AIHandler";
    private static final String JOIN_EVENT = "net/minecraftforge/event/entity/EntityJoinWorldEvent";
    private static final String DEATH_EVENT = "net/minecraftforge/event/entity/living/LivingDeathEvent";
    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/SpecialAIShamblingHuskBlacklistPatch";
    private static final String PATCH_DESCRIPTOR = "(Lnet/minecraft/entity/Entity;)Z";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }
        if (!TARGET.equals(transformedName) && !TARGET.equals(name)) {
            return basicClass;
        }

        ClassNode node = readClass(basicClass);
        MethodNode join = null;
        MethodNode death = null;
        for (MethodNode method : node.methods) {
            if ("onEntityJoinWorld".equals(method.name)
                    && "(Lnet/minecraftforge/event/entity/EntityJoinWorldEvent;)V".equals(method.desc)) {
                if (join != null) {
                    logger.error("Unexpected duplicate Special AI join handler. Husk blacklist not installed.");
                    return basicClass;
                }
                join = method;
            }
            if ("onLivingDeath".equals(method.name)
                    && "(Lnet/minecraftforge/event/entity/living/LivingDeathEvent;)V".equals(method.desc)) {
                if (death != null) {
                    logger.error("Unexpected duplicate Special AI death handler. Husk blacklist not installed.");
                    return basicClass;
                }
                death = method;
            }
        }

        if (join == null || death == null
                || !hasEventField(join, JOIN_EVENT, "entity", "Lnet/minecraft/entity/Entity;")
                || !hasEventField(death, DEATH_EVENT, "entityLiving", "Lnet/minecraft/entity/EntityLivingBase;")
                || containsHook(join) || containsHook(death)) {
            logger.error("Unexpected Special AI 1.1.2 handler bytecode. Husk blacklist not installed.");
            return basicClass;
        }

        guard(join, JOIN_EVENT, "entity", "Lnet/minecraft/entity/Entity;");
        guard(death, DEATH_EVENT, "entityLiving", "Lnet/minecraft/entity/EntityLivingBase;");
        logger.info("Excluded Thaumcraft Shambling Husks from Special AI entity and death handlers.");
        return writeClass(node);
    }

    private static boolean hasEventField(MethodNode method, String eventOwner,
            String fieldName, String fieldDesc) {
        for (AbstractInsnNode insn = method.instructions.getFirst();
                insn != null; insn = insn.getNext()) {
            if (insn instanceof FieldInsnNode && insn.getOpcode() == Opcodes.GETFIELD) {
                FieldInsnNode field = (FieldInsnNode) insn;
                if (eventOwner.equals(field.owner) && fieldName.equals(field.name)
                        && fieldDesc.equals(field.desc)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean containsHook(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst();
                insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode
                    && PATCH_OWNER.equals(((MethodInsnNode) insn).owner)) {
                return true;
            }
        }
        return false;
    }

    private static void guard(MethodNode method, String eventOwner,
            String fieldName, String fieldDesc) {
        LabelNode proceed = new LabelNode();
        InsnList added = new InsnList();
        added.add(new VarInsnNode(Opcodes.ALOAD, 1));
        added.add(new FieldInsnNode(Opcodes.GETFIELD, eventOwner, fieldName, fieldDesc));
        added.add(new MethodInsnNode(Opcodes.INVOKESTATIC, PATCH_OWNER,
                "shouldSkip", PATCH_DESCRIPTOR, false));
        added.add(new JumpInsnNode(Opcodes.IFEQ, proceed));
        added.add(new InsnNode(Opcodes.RETURN));
        added.add(proceed);
        added.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(added);
        method.maxStack = Math.max(method.maxStack, 1);
    }
}
