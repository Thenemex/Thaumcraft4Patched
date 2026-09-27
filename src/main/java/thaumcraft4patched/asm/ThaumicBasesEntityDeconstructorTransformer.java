package thaumcraft4patched.asm;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public class ThaumicBasesEntityDeconstructorTransformer
        implements IClassTransformer {

    private static final String TARGET =
            "tb.common.tile.TileEntityDeconstructor";

    private static final String ADD_WARP_OWNER =
            "tb/utils/TBUtils";

    private static final String ADD_WARP_DESCRIPTOR =
            "(Lnet/minecraft/entity/player/EntityPlayer;II)V";

    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "ThaumicBasesEntityDeconstructorPatch";

    private static final String PATCH_DESCRIPTOR =
            "(Lnet/minecraft/entity/Entity;II)V";

    private static final String PLAYER_INTERNAL =
            "net/minecraft/entity/player/EntityPlayer";

    private static final String PLAYER_MP_INTERNAL =
            "net/minecraft/entity/player/EntityPlayerMP";

    @Override
    public byte[] transform(
            String name,
            String transformedName,
            byte[] basicClass) {

        if (basicClass == null) {
            return null;
        }

        if (!TARGET.equals(name)
                && !TARGET.equals(transformedName)) {

            return basicClass;
        }

        ClassNode classNode = readClass(basicClass);

        MethodNode updateMethod = null;

        for (MethodNode method : classNode.methods) {

            if (!"()V".equals(method.desc)) {
                continue;
            }

            if (!"updateEntity".equals(method.name)
                    && !"func_145845_h".equals(method.name)) {

                continue;
            }

            if (updateMethod != null) {
                logger.error(
                        "Found multiple TileEntityDeconstructor update "
                                + "methods. Player-cast patch will not "
                                + "be applied."
                );

                return basicClass;
            }

            updateMethod = method;
        }

        if (updateMethod == null) {
            logger.error(
                    "Could not find TileEntityDeconstructor update "
                            + "method. Player-cast patch will not "
                            + "be applied."
            );

            return basicClass;
        }

        List<MethodInsnNode> warpCalls =
                new ArrayList<MethodInsnNode>();

        for (AbstractInsnNode instruction =
                updateMethod.instructions.getFirst();
                instruction != null;
                instruction = instruction.getNext()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsn =
                    (MethodInsnNode) instruction;

            if (methodInsn.getOpcode() == Opcodes.INVOKESTATIC
                    && ADD_WARP_OWNER.equals(methodInsn.owner)
                    && "addWarpToPlayer".equals(methodInsn.name)
                    && ADD_WARP_DESCRIPTOR.equals(methodInsn.desc)) {

                if (!hasExpectedPlayerCastSequence(methodInsn)) {

                    logger.error(
                            "Found an unexpected Entity Deconstructor "
                                    + "warp-call bytecode pattern. "
                                    + "Player-cast patch will not "
                                    + "be applied."
                    );

                    return basicClass;
                }

                warpCalls.add(methodInsn);
            }
        }

        if (warpCalls.size() != 3) {

            logger.error(
                    "Found {} Entity Deconstructor warp calls instead "
                            + "of the expected 3. Player-cast patch "
                            + "will not be applied.",
                    warpCalls.size()
            );

            return basicClass;
        }

        for (MethodInsnNode warpCall : warpCalls) {

            AbstractInsnNode iconst0 =
                    previousReal(warpCall);

            AbstractInsnNode iconst1 =
                    previousReal(iconst0);

            AbstractInsnNode checkcast =
                    previousReal(iconst1);

            AbstractInsnNode classCast =
                    previousReal(checkcast);

            AbstractInsnNode entityLoad =
                    previousReal(classCast);

            AbstractInsnNode playerClass =
                    previousReal(entityLoad);

            updateMethod.instructions.remove(playerClass);
            updateMethod.instructions.remove(classCast);
            updateMethod.instructions.remove(checkcast);

            warpCall.owner = PATCH_OWNER;
            warpCall.name = "addWarpIfPlayer";
            warpCall.desc = PATCH_DESCRIPTOR;
        }

        logger.info(
                "Successfully patched Thaumic Bases Entity "
                        + "Deconstructor player casts."
        );

        return writeClass(classNode);
    }

    private static boolean hasExpectedPlayerCastSequence(
            MethodInsnNode warpCall) {

        AbstractInsnNode iconst0 =
                previousReal(warpCall);

        AbstractInsnNode iconst1 =
                previousReal(iconst0);

        AbstractInsnNode checkcast =
                previousReal(iconst1);

        AbstractInsnNode classCast =
                previousReal(checkcast);

        AbstractInsnNode entityLoad =
                previousReal(classCast);

        AbstractInsnNode playerClass =
                previousReal(entityLoad);

        if (iconst0 == null
                || iconst0.getOpcode() != Opcodes.ICONST_0) {

            return false;
        }

        if (iconst1 == null
                || iconst1.getOpcode() != Opcodes.ICONST_1) {

            return false;
        }

        if (!(checkcast instanceof TypeInsnNode)
                || checkcast.getOpcode() != Opcodes.CHECKCAST
                || !PLAYER_INTERNAL.equals(
                        ((TypeInsnNode) checkcast).desc)) {

            return false;
        }

        if (!(classCast instanceof MethodInsnNode)) {
            return false;
        }

        MethodInsnNode castMethod =
                (MethodInsnNode) classCast;

        if (castMethod.getOpcode() != Opcodes.INVOKEVIRTUAL
                || !"java/lang/Class".equals(castMethod.owner)
                || !"cast".equals(castMethod.name)
                || !"(Ljava/lang/Object;)Ljava/lang/Object;"
                        .equals(castMethod.desc)) {

            return false;
        }

        if (!(entityLoad instanceof VarInsnNode)
                || entityLoad.getOpcode() != Opcodes.ALOAD
                || ((VarInsnNode) entityLoad).var != 3) {

            return false;
        }

        if (!(playerClass instanceof LdcInsnNode)) {
            return false;
        }

        Object constant =
                ((LdcInsnNode) playerClass).cst;

        return constant instanceof Type
                && Type.getObjectType(PLAYER_MP_INTERNAL)
                        .equals(constant);
    }

    private static AbstractInsnNode previousReal(
            AbstractInsnNode instruction) {

        if (instruction == null) {
            return null;
        }

        AbstractInsnNode current =
                instruction.getPrevious();

        while (current != null) {

            int type = current.getType();

            if (type != AbstractInsnNode.LABEL
                    && type != AbstractInsnNode.LINE
                    && type != AbstractInsnNode.FRAME) {

                return current;
            }

            current = current.getPrevious();
        }

        return null;
    }
}
