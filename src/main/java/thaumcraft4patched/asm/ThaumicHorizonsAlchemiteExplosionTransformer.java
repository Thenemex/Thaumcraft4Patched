package thaumcraft4patched.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public class ThaumicHorizonsAlchemiteExplosionTransformer
        implements IClassTransformer {

    private static final String TARGET =
            "com.kentington.thaumichorizons.common.lib.ExplosionAlchemite";

    private static final String METHOD_DESCRIPTOR = "()V";

    private static final String ALCHEMITE_OWNER =
            "com/kentington/thaumichorizons/common/lib/ExplosionAlchemite";

    private static final String WORLD_OWNER =
            "net/minecraft/world/World";

    private static final String ENTITY_QUERY_DESCRIPTOR =
            "(Lnet/minecraft/entity/Entity;"
                    + "Lnet/minecraft/util/AxisAlignedBB;)"
                    + "Ljava/util/List;";

    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "ThaumicHorizonsAlchemiteExplosionPatch";

    private static final String PATCH_DESCRIPTOR =
            "(Lnet/minecraft/world/World;"
                    + "Lnet/minecraft/world/Explosion;"
                    + "Ljava/util/List;"
                    + "Ljava/util/List;D)V";

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

        int injections = 0;

        for (AbstractInsnNode instruction =
                targetMethod.instructions.getFirst();
                instruction != null;
                instruction = instruction.getNext()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsn = (MethodInsnNode) instruction;

            if (!isEntityQuery(methodInsn)) {
                continue;
            }

            AbstractInsnNode storeInstruction =
                    nextRealInstruction(instruction);

            if (!(storeInstruction instanceof VarInsnNode)
                    || storeInstruction.getOpcode() != Opcodes.ASTORE) {

                logger.error(
                        "Found Thaumic Horizons Alchemite entity query "
                                + "without the expected ASTORE. "
                                + "The Alchemite explosion compatibility "
                                + "patch will not be applied.");

                return basicClass;
            }

            int affectedEntitiesLocal =
                    ((VarInsnNode) storeInstruction).var;

            InsnList patch = new InsnList();

            patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
            patch.add(new FieldInsnNode(
                    Opcodes.GETFIELD,
                    ALCHEMITE_OWNER,
                    "worldObj",
                    "Lnet/minecraft/world/World;"));

            patch.add(new VarInsnNode(Opcodes.ALOAD, 0));

            patch.add(new VarInsnNode(
                    Opcodes.ALOAD,
                    affectedEntitiesLocal));

            patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
            patch.add(new FieldInsnNode(
                    Opcodes.GETFIELD,
                    ALCHEMITE_OWNER,
                    "field_77281_g",
                    "Ljava/util/List;"));

            patch.add(new VarInsnNode(Opcodes.ALOAD, 0));
            patch.add(new FieldInsnNode(
                    Opcodes.GETFIELD,
                    ALCHEMITE_OWNER,
                    "field_77280_f",
                    "F"));

            patch.add(new InsnNode(Opcodes.F2D));

            patch.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    PATCH_OWNER,
                    "onDetonate",
                    PATCH_DESCRIPTOR,
                    false));

            targetMethod.instructions.insert(
                    storeInstruction,
                    patch);

            injections++;
        }

        if (injections != 1) {
            logger.error(
                    "Found {} Alchemite entity-query injection points "
                            + "instead of the expected 1. "
                            + "The Alchemite explosion compatibility "
                            + "patch will not be applied.",
                    injections);

            return basicClass;
        }

        targetMethod.maxStack =
                Math.max(targetMethod.maxStack, 6);

        logger.info(
                "Successfully transformed Thaumic Horizons Alchemite "
                        + "explosions to fire the normal Forge "
                        + "detonation event!");

        return writeClass(classNode);
    }

    private static MethodNode findTargetMethod(
            ClassNode classNode) {

        MethodNode targetMethod = null;

        for (MethodNode method : classNode.methods) {

            if (!METHOD_DESCRIPTOR.equals(method.desc)) {
                continue;
            }

            if (!"doExplosionA".equals(method.name)
                    && !"func_77278_a".equals(method.name)) {

                continue;
            }

            if (targetMethod != null) {
                logger.error(
                        "Found multiple ExplosionAlchemite "
                                + "doExplosionA methods. "
                                + "The Alchemite explosion compatibility "
                                + "patch will not be applied.");

                return null;
            }

            targetMethod = method;
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find ExplosionAlchemite.doExplosionA. "
                            + "The Alchemite explosion compatibility "
                            + "patch will not be applied.");
        }

        return targetMethod;
    }

    private static boolean isEntityQuery(
            MethodInsnNode methodInsn) {

        if (methodInsn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
            return false;
        }

        if (!WORLD_OWNER.equals(methodInsn.owner)) {
            return false;
        }

        if (!ENTITY_QUERY_DESCRIPTOR.equals(methodInsn.desc)) {
            return false;
        }

        return "getEntitiesWithinAABBExcludingEntity"
                .equals(methodInsn.name)
                || "func_72839_b".equals(methodInsn.name);
    }

    private static AbstractInsnNode nextRealInstruction(
            AbstractInsnNode instruction) {

        AbstractInsnNode next = instruction.getNext();

        while (next != null && next.getOpcode() < 0) {
            next = next.getNext();
        }

        return next;
    }
}
