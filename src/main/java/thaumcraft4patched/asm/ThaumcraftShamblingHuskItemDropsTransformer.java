package thaumcraft4patched.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.previousRealInstruction;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public final class ThaumcraftShamblingHuskItemDropsTransformer implements IClassTransformer {

    private static final String TARGET =
            "thaumcraft.common.entities.monster.EntityInhabitedZombie";
    private static final String TARGET_INTERNAL =
            "thaumcraft/common/entities/monster/EntityInhabitedZombie";
    private static final String CRAB_INTERNAL =
            "thaumcraft/common/entities/monster/EntityEldritchCrab";
    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/ThaumcraftShamblingHuskItemDropsPatch";
    private static final String PATCH_DESCRIPTOR =
            "(Lnet/minecraft/entity/EntityLiving;)Z";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }
        if (!TARGET.equals(transformedName) && !TARGET.equals(name)) {
            return basicClass;
        }

        ClassNode node = readClass(basicClass);
        MethodNode deathUpdate = null;
        MethodNode death = null;

        for (MethodNode method : node.methods) {
            if ("()V".equals(method.desc)
                    && ("onDeathUpdate".equals(method.name)
                    || "func_70609_aI".equals(method.name))) {
                if (deathUpdate != null) {
                    logger.error("Multiple Shambling Husk death-update methods. Item-drop patch not applied.");
                    return basicClass;
                }
                deathUpdate = method;
            }
            if ("(Lnet/minecraft/util/DamageSource;)V".equals(method.desc)
                    && ("onDeath".equals(method.name)
                    || "func_70645_a".equals(method.name))) {
                if (death != null) {
                    logger.error("Multiple Shambling Husk death methods. Item-drop patch not applied.");
                    return basicClass;
                }
                death = method;
            }
        }

        if (deathUpdate == null || death == null || !isEmptyDeath(death)) {
            logger.error("Unexpected Shambling Husk death handling. Item-drop patch not applied.");
            return basicClass;
        }

        MethodInsnNode removal = null;
        int crabs = 0;
        for (AbstractInsnNode insn = deathUpdate.instructions.getFirst();
                insn != null; insn = insn.getNext()) {
            if (insn instanceof TypeInsnNode && insn.getOpcode() == Opcodes.NEW
                    && CRAB_INTERNAL.equals(((TypeInsnNode) insn).desc)) {
                crabs++;
            }
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) insn;
                if (call.getOpcode() == Opcodes.INVOKEVIRTUAL
                        && ("setDead".equals(call.name)
                        || "func_70106_y".equals(call.name))
                        && "()V".equals(call.desc)) {
                    if (removal != null) {
                        logger.error("Multiple Shambling Husk removal calls. Item-drop patch not applied.");
                        return basicClass;
                    }
                    removal = call;
                }
            }
        }

        AbstractInsnNode receiver = removal == null ? null : previousRealInstruction(removal);
        if (crabs != 1 || !(receiver instanceof VarInsnNode)
                || receiver.getOpcode() != Opcodes.ALOAD
                || ((VarInsnNode) receiver).var != 0) {
            logger.error("Unexpected Shambling Husk death-update bytecode. Item-drop patch not applied.");
            return basicClass;
        }

        LabelNode skip = new LabelNode();
        InsnList added = new InsnList();
        added.add(new VarInsnNode(Opcodes.ALOAD, 0));
        added.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                PATCH_OWNER, "shouldRestoreDrops", PATCH_DESCRIPTOR, false));
        added.add(new JumpInsnNode(Opcodes.IFEQ, skip));
        added.add(new VarInsnNode(Opcodes.ALOAD, 0));
        added.add(new InsnNode(Opcodes.ICONST_0));
        added.add(new InsnNode(Opcodes.ICONST_0));
        added.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                TARGET_INTERNAL,
                "setDead".equals(removal.name) ? "dropEquipment" : "func_82160_b",
                "(ZI)V", false));
        added.add(skip);
        added.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        deathUpdate.instructions.insertBefore(receiver, added);
        deathUpdate.maxStack = Math.max(deathUpdate.maxStack, 3);

        logger.info("Restored picked-up equipment drops for Shambling Husks.");
        return writeClass(node);
    }

    private static boolean isEmptyDeath(MethodNode death) {
        int count = 0;
        for (AbstractInsnNode insn = death.instructions.getFirst();
                insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() >= 0) {
                count++;
                if (insn.getOpcode() != Opcodes.RETURN) {
                    return false;
                }
            }
        }
        return count == 1;
    }
}
