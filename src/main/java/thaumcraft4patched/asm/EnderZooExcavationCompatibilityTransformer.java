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
public class EnderZooExcavationCompatibilityTransformer
        implements IClassTransformer {

    private static final String TARGET =
            "crazypants.enderzoo.spawn.MobSpawnEventHandler";

    private static final String FORGE_HOOKS_OWNER =
            "net/minecraftforge/common/ForgeHooks";

    private static final String FORGE_TOOL_CHECK_NAME =
            "isToolEffective";

    private static final String TOOL_CHECK_DESCRIPTOR =
            "(Lnet/minecraft/item/ItemStack;"
                    + "Lnet/minecraft/block/Block;I)Z";

    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "HlcExcavationFocusToolPatch";

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
            if (!"onBlockHarvest".equals(method.name)
                    || !("(Lnet/minecraftforge/event/world/"
                    + "BlockEvent$HarvestDropsEvent;)V")
                    .equals(method.desc)) {

                continue;
            }

            patched = patchToolCheck(method);
            break;
        }

        if (patched) {
            logger.info(
                    "Successfully transformed Ender Zoo's harvest "
                            + "tool check for Excavation Focus "
                            + "compatibility!"
            );
        } else {
            logger.error(
                    "Could not find Ender Zoo's ForgeHooks."
                            + "isToolEffective call in onBlockHarvest. "
                            + "The compatibility patch was not applied!"
            );
        }

        return writeClass(classNode);
    }

    private boolean patchToolCheck(MethodNode method) {
        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodCall =
                    (MethodInsnNode) instruction;

            if (methodCall.getOpcode() != Opcodes.INVOKESTATIC
                    || !FORGE_HOOKS_OWNER.equals(methodCall.owner)
                    || !FORGE_TOOL_CHECK_NAME.equals(methodCall.name)
                    || !TOOL_CHECK_DESCRIPTOR.equals(methodCall.desc)) {

                continue;
            }

            methodCall.owner = PATCH_OWNER;
            methodCall.name = FORGE_TOOL_CHECK_NAME;
            methodCall.desc = TOOL_CHECK_DESCRIPTOR;

            return true;
        }

        return false;
    }
}
