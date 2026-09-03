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
public class ThaumonomiconRecipeLightingTransformer
        implements IClassTransformer {

    private static final String TARGET =
            "thaumcraft.client.gui.GuiResearchRecipe";

    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "ThaumonomiconRecipeLightingPatch";

    private static final String RENDER_ITEM_OWNER =
            "net/minecraft/client/renderer/entity/RenderItem";

    private static final String RENDER_ITEM_GUI_NAME =
            "func_82406_b";

    private static final String RENDER_ITEM_GUI_DESCRIPTOR =
            "(Lnet/minecraft/client/gui/FontRenderer;"
                    + "Lnet/minecraft/client/renderer/texture/TextureManager;"
                    + "Lnet/minecraft/item/ItemStack;II)V";

    private static final String PATCH_RENDER_ITEM_GUI_DESCRIPTOR =
            "(Lnet/minecraft/client/renderer/entity/RenderItem;"
                    + "Lnet/minecraft/client/gui/FontRenderer;"
                    + "Lnet/minecraft/client/renderer/texture/TextureManager;"
                    + "Lnet/minecraft/item/ItemStack;II)V";

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
        MethodInsnNode[] calls = new MethodInsnNode[16];
        int count = 0;

        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode instruction
                    : method.instructions.toArray()) {

                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }

                MethodInsnNode call =
                        (MethodInsnNode) instruction;

                if (call.getOpcode() != Opcodes.INVOKEVIRTUAL
                        || !RENDER_ITEM_OWNER.equals(call.owner)
                        || !RENDER_ITEM_GUI_NAME.equals(call.name)
                        || !RENDER_ITEM_GUI_DESCRIPTOR.equals(call.desc)) {

                    continue;
                }

                if (count < calls.length) {
                    calls[count] = call;
                }

                count++;
            }
        }

        if (count != calls.length) {
            logger.error(
                    "GuiResearchRecipe has an unexpected number of "
                            + "item render calls. Thaumonomicon recipe "
                            + "lighting patch was not installed!"
            );

            return basicClass;
        }

        for (MethodInsnNode call : calls) {
            call.setOpcode(Opcodes.INVOKESTATIC);
            call.owner = PATCH_OWNER;
            call.name = "renderItem";
            call.desc =
                    PATCH_RENDER_ITEM_GUI_DESCRIPTOR;
        }

        logger.info(
                "Successfully patched Thaumonomicon recipe item lighting!"
        );

        return writeClass(classNode);
    }
}
