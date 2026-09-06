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

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public class VillagerTradeStackMatchingTransformer
        implements IClassTransformer {

    private static final String TARGET =
            "net.minecraft.village.MerchantRecipeList";

    private static final String METHOD_DESCRIPTOR =
            "(Lnet/minecraft/item/ItemStack;"
                    + "Lnet/minecraft/item/ItemStack;I)"
                    + "Lnet/minecraft/village/MerchantRecipe;";

    private static final String ITEM_STACK_OWNER =
            "net/minecraft/item/ItemStack";

    private static final String MERCHANT_RECIPE_OWNER =
            "net/minecraft/village/MerchantRecipe";

    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "VillagerTradeStackMatchingPatch";

    private static final String PATCH_DESCRIPTOR =
            "(Lnet/minecraft/item/ItemStack;"
                    + "Lnet/minecraft/item/ItemStack;)Z";

    private static final int EXPECTED_COMPARISONS = 4;

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
        MethodNode targetMethod = findTargetMethod(classNode);

        if (targetMethod == null) {
            return basicClass;
        }

        int patchedComparisons = 0;

        for (AbstractInsnNode instruction =
                targetMethod.instructions.getFirst();
                instruction != null;) {

            AbstractInsnNode next = instruction.getNext();

            if (instruction instanceof JumpInsnNode
                    && instruction.getOpcode() == Opcodes.IF_ACMPNE
                    && patchItemComparison(
                    targetMethod,
                    (JumpInsnNode) instruction)) {

                patchedComparisons++;
            }

            instruction = next;
        }

        if (patchedComparisons != EXPECTED_COMPARISONS) {
            logger.error(
                    "Found {} of {} expected villager trade item "
                            + "comparisons. The villager trade stack "
                            + "matching patch was not installed!",
                    patchedComparisons,
                    EXPECTED_COMPARISONS
            );

            return basicClass;
        }

        targetMethod.maxStack =
                Math.max(targetMethod.maxStack, 2);

        logger.info(
                "Successfully transformed villager trades "
                        + "to respect item metadata and NBT!"
        );

        return writeClass(classNode);
    }

    private static MethodNode findTargetMethod(
            ClassNode classNode) {

        MethodNode targetMethod = null;

        for (MethodNode method : classNode.methods) {
            if (!METHOD_DESCRIPTOR.equals(method.desc)
                    || !("func_77203_a".equals(method.name)
                    || "canRecipeBeUsed".equals(method.name))) {

                continue;
            }

            if (targetMethod != null) {
                logger.error(
                        "Found multiple MerchantRecipeList "
                                + "canRecipeBeUsed methods. "
                                + "The villager trade stack matching "
                                + "patch was not installed!"
                );

                return null;
            }

            targetMethod = method;
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find MerchantRecipeList.canRecipeBeUsed. "
                            + "The villager trade stack matching patch "
                            + "was not installed!"
            );
        }

        return targetMethod;
    }

    private static boolean patchItemComparison(
            MethodNode method,
            JumpInsnNode comparison) {

        AbstractInsnNode previous =
                previousCodeInstruction(comparison);

        if (!(previous instanceof MethodInsnNode)
                || !isItemGet((MethodInsnNode) previous)) {

            return false;
        }

        MethodInsnNode secondGetItem =
                (MethodInsnNode) previous;

        MethodInsnNode firstGetItem =
                findPreviousItemGet(secondGetItem);

        if (firstGetItem == null) {
            return false;
        }

        StackSource firstSource =
                findStackSource(firstGetItem);
        StackSource secondSource =
                findStackSource(secondGetItem);

        if (firstSource == null || secondSource == null) {
            return false;
        }

        StackSource offered;
        StackSource required;

        if (firstSource.recipeGetter == null
                && secondSource.recipeGetter != null) {

            offered = firstSource;
            required = secondSource;
        } else if (firstSource.recipeGetter != null
                && secondSource.recipeGetter == null) {

            offered = secondSource;
            required = firstSource;
        } else {
            return false;
        }

        InsnList stackCheck = new InsnList();

        stackCheck.add(new VarInsnNode(
                Opcodes.ALOAD,
                offered.local
        ));

        stackCheck.add(new VarInsnNode(
                Opcodes.ALOAD,
                required.local
        ));

        stackCheck.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                required.recipeGetter.owner,
                required.recipeGetter.name,
                required.recipeGetter.desc,
                required.recipeGetter.itf
        ));

        stackCheck.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                PATCH_OWNER,
                "matches",
                PATCH_DESCRIPTOR,
                false
        ));

        stackCheck.add(new JumpInsnNode(
                Opcodes.IFEQ,
                comparison.label
        ));

        method.instructions.insert(
                comparison,
                stackCheck
        );

        return true;
    }

    private static MethodInsnNode findPreviousItemGet(
            MethodInsnNode from) {

        AbstractInsnNode instruction =
                previousCodeInstruction(from);

        for (int i = 0;
                instruction != null && i < 8;
                i++) {

            if (instruction instanceof MethodInsnNode
                    && isItemGet(
                    (MethodInsnNode) instruction)) {

                return (MethodInsnNode) instruction;
            }

            instruction =
                    previousCodeInstruction(instruction);
        }

        return null;
    }

    private static StackSource findStackSource(
            MethodInsnNode getItemCall) {

        AbstractInsnNode receiver =
                previousCodeInstruction(getItemCall);

        if (receiver instanceof VarInsnNode
                && receiver.getOpcode() == Opcodes.ALOAD) {

            return new StackSource(
                    ((VarInsnNode) receiver).var,
                    null
            );
        }

        if (!(receiver instanceof MethodInsnNode)) {
            return null;
        }

        MethodInsnNode getter =
                (MethodInsnNode) receiver;

        if (!isRecipeInputGetter(getter)) {
            return null;
        }

        AbstractInsnNode recipeLoad =
                previousCodeInstruction(getter);

        if (!(recipeLoad instanceof VarInsnNode)
                || recipeLoad.getOpcode() != Opcodes.ALOAD) {

            return null;
        }

        return new StackSource(
                ((VarInsnNode) recipeLoad).var,
                getter
        );
    }

    private static boolean isItemGet(
            MethodInsnNode call) {

        return ITEM_STACK_OWNER.equals(call.owner)
                && "()Lnet/minecraft/item/Item;"
                .equals(call.desc)
                && ("getItem".equals(call.name)
                || "func_77973_b".equals(call.name));
    }

    private static boolean isRecipeInputGetter(
            MethodInsnNode call) {

        if (!MERCHANT_RECIPE_OWNER.equals(call.owner)
                || !"()Lnet/minecraft/item/ItemStack;"
                .equals(call.desc)) {

            return false;
        }

        return "getItemToBuy".equals(call.name)
                || "func_77394_a".equals(call.name)
                || "getSecondItemToBuy".equals(call.name)
                || "func_77396_b".equals(call.name);
    }

    private static AbstractInsnNode previousCodeInstruction(
            AbstractInsnNode instruction) {

        AbstractInsnNode previous =
                instruction.getPrevious();

        while (previous != null
                && previous.getOpcode() < 0) {

            previous = previous.getPrevious();
        }

        return previous;
    }

    private static final class StackSource {

        private final int local;
        private final MethodInsnNode recipeGetter;

        private StackSource(
                int local,
                MethodInsnNode recipeGetter) {

            this.local = local;
            this.recipeGetter = recipeGetter;
        }
    }
}