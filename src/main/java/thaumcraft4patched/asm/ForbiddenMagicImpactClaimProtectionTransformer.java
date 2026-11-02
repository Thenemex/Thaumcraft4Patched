package thaumcraft4patched.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.List;

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public final class ForbiddenMagicImpactClaimProtectionTransformer
        implements IClassTransformer {

    private static final String SHOVEL =
            "fox.spiteful.forbidden.items.tools.ItemMorphShovel";
    private static final String PICKAXE =
            "fox.spiteful.forbidden.items.tools.ItemMorphPickaxe";
    private static final String TARGET_DESCRIPTOR =
            "(Lnet/minecraft/item/ItemStack;"
                    + "Lnet/minecraft/world/World;"
                    + "Lnet/minecraft/block/Block;"
                    + "IIILnet/minecraft/entity/EntityLivingBase;)Z";
    private static final String FORGE_HOOKS =
            "net/minecraftforge/common/ForgeHooks";
    private static final String IS_EFFECTIVE_DESCRIPTOR =
            "(Lnet/minecraft/item/ItemStack;"
                    + "Lnet/minecraft/block/Block;I)Z";
    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/ForbiddenMagicImpactClaimProtectionPatch";
    private static final String PATCH_DESCRIPTOR =
            "(Lnet/minecraft/item/ItemStack;"
                    + "Lnet/minecraft/block/Block;I"
                    + "Lnet/minecraft/world/World;"
                    + "Lnet/minecraft/entity/EntityLivingBase;III)Z";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }
        String target = SHOVEL.equals(transformedName) || PICKAXE.equals(transformedName)
                ? transformedName : name;
        if (!SHOVEL.equals(target) && !PICKAXE.equals(target)) {
            return basicClass;
        }

        ClassNode node = readClass(basicClass);
        MethodNode method = null;
        for (MethodNode candidate : node.methods) {
            if (TARGET_DESCRIPTOR.equals(candidate.desc)
                    && ("onBlockDestroyed".equals(candidate.name)
                    || "func_150894_a".equals(candidate.name))) {
                if (method != null) {
                    logger.error("Multiple onBlockDestroyed methods in {}. "
                            + "Forbidden Magic Impact protection was not installed.", target);
                    return basicClass;
                }
                method = candidate;
            }
        }
        if (method == null) {
            logger.error("Missing onBlockDestroyed in {}. "
                    + "Forbidden Magic Impact protection was not installed.", target);
            return basicClass;
        }

        List<MethodInsnNode> effectiveChecks = new ArrayList<MethodInsnNode>();
        for (AbstractInsnNode insn = method.instructions.getFirst();
                insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) insn;
            if (call.getOpcode() == Opcodes.INVOKESTATIC
                    && FORGE_HOOKS.equals(call.owner)
                    && "isToolEffective".equals(call.name)
                    && IS_EFFECTIVE_DESCRIPTOR.equals(call.desc)) {
                effectiveChecks.add(call);
            }
        }

        if (effectiveChecks.size() != 2
                || !isExpectedNeighbourCheck(effectiveChecks.get(1))) {
            logger.error("Unexpected Impact mining bytecode in {}. "
                    + "Forbidden Magic Impact protection was not installed.", target);
            return basicClass;
        }

        MethodInsnNode neighbourCheck = effectiveChecks.get(1);
        InsnList added = new InsnList();
        added.add(new VarInsnNode(Opcodes.ALOAD, 2));
        added.add(new VarInsnNode(Opcodes.ALOAD, 7));
        added.add(new VarInsnNode(Opcodes.ILOAD, 4));
        added.add(new VarInsnNode(Opcodes.ILOAD, 11));
        added.add(new org.objectweb.asm.tree.InsnNode(Opcodes.IADD));
        added.add(new VarInsnNode(Opcodes.ILOAD, 5));
        added.add(new VarInsnNode(Opcodes.ILOAD, 12));
        added.add(new org.objectweb.asm.tree.InsnNode(Opcodes.IADD));
        added.add(new VarInsnNode(Opcodes.ILOAD, 6));
        added.add(new VarInsnNode(Opcodes.ILOAD, 13));
        added.add(new org.objectweb.asm.tree.InsnNode(Opcodes.IADD));
        method.instructions.insertBefore(neighbourCheck, added);
        neighbourCheck.owner = PATCH_OWNER;
        neighbourCheck.name = "isEffectiveAndPermitted";
        neighbourCheck.desc = PATCH_DESCRIPTOR;

        method.maxStack = Math.max(method.maxStack, 10);
        logger.info("Protected Forbidden Magic Impact neighbour mining in {}", target);
        return writeClass(node);
    }

    private static boolean isExpectedNeighbourCheck(MethodInsnNode call) {
        AbstractInsnNode next = nextReal(call);
        if (!(next instanceof JumpInsnNode) || next.getOpcode() != Opcodes.IFEQ) {
            return false;
        }
        AbstractInsnNode metadata = previousReal(call);
        AbstractInsnNode block = previousReal(metadata);
        AbstractInsnNode stack = previousReal(block);
        return matchesLoad(metadata, Opcodes.ILOAD, 8)
                && matchesLoad(block, Opcodes.ALOAD, 14)
                && matchesLoad(stack, Opcodes.ALOAD, 1);
    }

    private static boolean matchesLoad(AbstractInsnNode insn, int opcode, int local) {
        return insn instanceof VarInsnNode && insn.getOpcode() == opcode
                && ((VarInsnNode) insn).var == local;
    }

    private static AbstractInsnNode previousReal(AbstractInsnNode insn) {
        if (insn == null) {
            return null;
        }
        AbstractInsnNode previous = insn.getPrevious();
        while (previous != null && previous.getOpcode() < 0) {
            previous = previous.getPrevious();
        }
        return previous;
    }

    private static AbstractInsnNode nextReal(AbstractInsnNode insn) {
        AbstractInsnNode next = insn.getNext();
        while (next != null && next.getOpcode() < 0) {
            next = next.getNext();
        }
        return next;
    }
}
