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

public class XPTalismanParticleTransformer implements IClassTransformer {

    private static final String TARGET = "thaumic.tinkerer.common.item.ItemXPTalisman";
    private static final String WISP_DESCRIPTOR = "(Lnet/minecraft/world/World;DDDFFFF)V";
    private static final String PROXY = "thaumcraft/common/CommonProxy";
    private static final String PATCH = "thaumcraft4patched/model/patch/XPTalismanParticlePatch";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || (!TARGET.equals(name) && !TARGET.equals(transformedName))) {
            return basicClass;
        }

        ClassNode classNode = readClass(basicClass);
        MethodInsnNode wispCall = null;
        int matches = 0;
        for (MethodNode method : classNode.methods) {
            if (!"consumeXPOrb".equals(method.name)
                    || !"(Lnet/minecraft/entity/item/EntityXPOrb;)V".equals(method.desc)) {
                continue;
            }
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.getOpcode() == Opcodes.INVOKEVIRTUAL && PROXY.equals(call.owner)
                        && "wispFX".equals(call.name) && WISP_DESCRIPTOR.equals(call.desc)) {
                    wispCall = call;
                    matches++;
                }
            }
        }

        if (matches != 1) {
            logger.error("Could not identify the Thaumic Tinkerer XP talisman wisp call.");
            return basicClass;
        }

        wispCall.setOpcode(Opcodes.INVOKESTATIC);
        wispCall.owner = PATCH;
        wispCall.name = "showWisp";
        wispCall.desc = "(L" + PROXY + ";" + WISP_DESCRIPTOR.substring(1);
        logger.info("Patched Thaumic Tinkerer XP talisman particle world and thread handling.");
        return writeClass(classNode);
    }
}
