package thaumcraft4patched.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;

@SuppressWarnings("unused")
public class TCPatchTransformer implements IClassTransformer {

    protected static final String
            target = "thaumcraft.common.entities.ai.interact.AIHarvestLogs",
            oldName = "FakeThaumcraftGolem",
            newName = "FakeThaumcraftGolemLumber";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!target.equals(transformedName)) return basicClass;

        logger.info("Transforming", transformedName);

        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);

        // Targeting the constructor
        for (MethodNode method : classNode.methods)
            if ("<init>".equals(method.name) && method.desc.startsWith("(Lthaumcraft/common/entities/golems/EntityGolemBase;)"))
                patchConstructor(method);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    protected void patchConstructor(MethodNode method) {
        for (AbstractInsnNode insn : method.instructions.toArray())
            if (insn instanceof LdcInsnNode) {
                LdcInsnNode ldc = (LdcInsnNode) insn;
                if (oldName.equals(ldc.cst)) {
                    ldc.cst = newName;
                    logger.info("Transforming AIHarvestLogs constructor ...");
                    return;
                }
            }
        logger.error("FakeThaumcraftGolem cannot be found in constructor. Report to author !");
    }
}
