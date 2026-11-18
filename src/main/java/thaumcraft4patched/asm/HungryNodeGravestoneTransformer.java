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

public class HungryNodeGravestoneTransformer implements IClassTransformer {

    private static final String TARGET = "thaumcraft.common.tiles.TileNode";
    private static final String BREAK_BLOCK = "(IIIZ)Z";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || (!TARGET.equals(name) && !TARGET.equals(transformedName))) {
            return basicClass;
        }

        ClassNode node = readClass(basicClass);
        MethodNode target = null;
        MethodInsnNode breakCall = null;
        int matches = 0;
        for (MethodNode method : node.methods) {
            if (!"handleHungryNodeSecond".equals(method.name) || !"(Z)Z".equals(method.desc)) {
                continue;
            }
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.getOpcode() == Opcodes.INVOKEVIRTUAL
                        && "net/minecraft/world/World".equals(call.owner)
                        && "func_147480_a".equals(call.name)
                        && BREAK_BLOCK.equals(call.desc)) {
                    target = method;
                    breakCall = call;
                    matches++;
                }
            }
        }
        if (matches != 1) {
            logger.error("Could not identify the Hungry Node block break call for Gravestone protection.");
            return basicClass;
        }

        target.instructions.set(breakCall, new MethodInsnNode(Opcodes.INVOKESTATIC,
                "thaumcraft4patched/model/patch/HungryNodeGravestonePatch",
                "breakBlockUnlessGrave", "(Lnet/minecraft/world/World;IIIZ)Z", false));
        logger.info("Patched Hungry Node block break call for Gravestone graves.");
        return writeClass(node);
    }
}
