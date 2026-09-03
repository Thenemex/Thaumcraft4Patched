package thaumcraft4patched.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;

final class ASMUtils {

    private ASMUtils() {
    }

    static ClassNode readClass(byte[] basicClass) {
        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        return classNode;
    }

    static byte[] writeClass(ClassNode classNode) {
        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    static AbstractInsnNode nextRealInstruction(
            AbstractInsnNode instruction) {

        AbstractInsnNode current = instruction.getNext();

        while (current != null
                && current.getOpcode() < 0) {

            current = current.getNext();
        }

        return current;
    }

    static AbstractInsnNode previousRealInstruction(
            AbstractInsnNode instruction) {

        AbstractInsnNode current = instruction.getPrevious();

        while (current != null
                && current.getOpcode() < 0) {

            current = current.getPrevious();
        }

        return current;
    }
}
