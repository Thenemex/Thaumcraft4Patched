package thaumcraft4patched.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public class WitcheryRaiseLandTransformer
        implements IClassTransformer {

    private static final String TARGET =
            "com.emoniph.witchery.brewing.action.effect.BrewActionRaiseLand";

    private static final String METHOD_NAME =
            "doApplyToBlock";

    private static final String METHOD_DESCRIPTOR =
            "(Lnet/minecraft/world/World;"
                    + "IIILnet/minecraftforge/common/util/ForgeDirection;"
                    + "ILcom/emoniph/witchery/brewing/ModifiersEffect;"
                    + "Lnet/minecraft/item/ItemStack;)V";

    private static final String WORLD_OWNER =
            "net/minecraft/world/World";

    private static final String SET_BLOCK_TO_AIR_DESCRIPTOR =
            "(III)Z";

    private static final String SET_BLOCK_DESCRIPTOR =
            "(IIILnet/minecraft/block/Block;II)Z";

    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "WitcheryRaiseLandProtectionPatch";

    private static final String PATCH_SET_BLOCK_TO_AIR_DESCRIPTOR =
            "(Lnet/minecraft/world/World;III)Z";

    private static final String PATCH_SET_BLOCK_DESCRIPTOR =
            "(Lnet/minecraft/world/World;"
                    + "IIILnet/minecraft/block/Block;II)Z";

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
            if (METHOD_NAME.equals(method.name)
                    && METHOD_DESCRIPTOR.equals(method.desc)) {

                targetMethod = method;
                break;
            }
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find Witchery BrewActionRaiseLand."
                            + "doApplyToBlock. Raise Land protection "
                            + "was not installed!"
            );

            return basicClass;
        }

        if (!patchBlockMoves(targetMethod)) {
            logger.error(
                    "Could not find both Witchery Raise Land block "
                            + "movement calls. Raise Land protection "
                            + "was not installed!"
            );

            return basicClass;
        }

        logger.info(
                "Successfully transformed Witchery Raise Land block "
                        + "movement calls for protected-block handling!"
        );

        return writeClass(classNode);
    }

    private boolean patchBlockMoves(MethodNode method) {
        MethodInsnNode removeBlockCall = null;
        MethodInsnNode placeBlockCall = null;

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodCall =
                    (MethodInsnNode) instruction;

            if (methodCall.getOpcode() != Opcodes.INVOKEVIRTUAL
                    || !WORLD_OWNER.equals(methodCall.owner)) {

                continue;
            }

            if (SET_BLOCK_TO_AIR_DESCRIPTOR.equals(methodCall.desc)
                    && ("func_147468_f".equals(methodCall.name)
                    || "setBlockToAir".equals(methodCall.name))) {

                removeBlockCall = methodCall;
                continue;
            }

            if (SET_BLOCK_DESCRIPTOR.equals(methodCall.desc)
                    && ("func_147465_d".equals(methodCall.name)
                    || "setBlock".equals(methodCall.name))) {

                placeBlockCall = methodCall;
            }
        }

        if (removeBlockCall == null || placeBlockCall == null) {
            return false;
        }

        removeBlockCall.setOpcode(Opcodes.INVOKESTATIC);
        removeBlockCall.owner = PATCH_OWNER;
        removeBlockCall.name = "setBlockToAir";
        removeBlockCall.desc =
                PATCH_SET_BLOCK_TO_AIR_DESCRIPTOR;

        placeBlockCall.setOpcode(Opcodes.INVOKESTATIC);
        placeBlockCall.owner = PATCH_OWNER;
        placeBlockCall.name = "setBlock";
        placeBlockCall.desc =
                PATCH_SET_BLOCK_DESCRIPTOR;

        return true;
    }
}
