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
public class GravestoneDeathNoteTransformer
        implements IClassTransformer {

    private static final String TARGET =
            "de.maxhenkel.gravestone.events.DeathEvents";

    private static final String GIVE_NOTE_NAME =
            "givePlayerNote";

    private static final String GIVE_NOTE_DESCRIPTOR =
            "(Lnet/minecraft/entity/player/EntityPlayer;)V";

    private static final String INVENTORY_PLAYER_OWNER =
            "net/minecraft/entity/player/InventoryPlayer";

    private static final String ADD_ITEM_STACK_DESCRIPTOR =
            "(Lnet/minecraft/item/ItemStack;)Z";

    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "GravestoneDeathNotePatch";

    private static final String PATCH_ADD_DEATH_NOTE_NAME =
            "addDeathNote";

    private static final String PATCH_ADD_DEATH_NOTE_DESCRIPTOR =
            "(Lnet/minecraft/entity/player/InventoryPlayer;"
                    + "Lnet/minecraft/item/ItemStack;)Z";

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
            if (GIVE_NOTE_NAME.equals(method.name)
                    && GIVE_NOTE_DESCRIPTOR.equals(method.desc)
                    && (method.access & Opcodes.ACC_STATIC) != 0) {

                targetMethod = method;
                break;
            }
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find Gravestone DeathEvents.givePlayerNote. "
                            + "The death note config patch "
                            + "was not installed!"
            );

            return basicClass;
        }

        if (!patchInventoryCall(targetMethod)) {
            logger.error(
                    "Could not find the Gravestone death note inventory "
                            + "call. The death note config patch was not "
                            + "installed!"
            );

            return basicClass;
        }

        logger.info(
                "Successfully transformed the Gravestone death note to "
                        + "follow the enable_death_note config entry!"
        );

        return writeClass(classNode);
    }

    private boolean patchInventoryCall(MethodNode method) {
        MethodInsnNode inventoryCall = null;

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodCall =
                    (MethodInsnNode) instruction;

            if (methodCall.getOpcode() != Opcodes.INVOKEVIRTUAL
                    || !INVENTORY_PLAYER_OWNER.equals(methodCall.owner)
                    || !ADD_ITEM_STACK_DESCRIPTOR.equals(methodCall.desc)
                    || !("func_70441_a".equals(methodCall.name)
                    || "addItemStackToInventory".equals(
                    methodCall.name))) {

                continue;
            }

            if (inventoryCall != null) {
                return false;
            }

            inventoryCall = methodCall;
        }

        if (inventoryCall == null) {
            return false;
        }

        inventoryCall.setOpcode(Opcodes.INVOKESTATIC);
        inventoryCall.owner = PATCH_OWNER;
        inventoryCall.name = PATCH_ADD_DEATH_NOTE_NAME;
        inventoryCall.desc = PATCH_ADD_DEATH_NOTE_DESCRIPTOR;

        return true;
    }
}
