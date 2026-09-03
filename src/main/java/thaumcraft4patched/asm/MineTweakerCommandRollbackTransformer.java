package thaumcraft4patched.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public class MineTweakerCommandRollbackTransformer
        implements IClassTransformer {

    private static final String MC_SERVER_TARGET =
            "minetweaker.mc1710.server.MCServer";

    private static final String REMOVE_COMMAND_ACTION_TARGET =
            "minetweaker.mc1710.server.MCServer$RemoveCommandAction";

    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "MineTweakerServerCommandRollbackPatch";

    private static final String GUARD_NAME =
            "shouldSkipCommandRollback";

    private static final String GUARD_DESCRIPTOR =
            "()Z";

    private static final String REMOVE_COMMAND_DESCRIPTOR =
            "(Lnet/minecraft/command/ICommand;)V";

    @Override
    public byte[] transform(
            String name,
            String transformedName,
            byte[] basicClass) {

        if (basicClass == null) {
            return null;
        }

        if (MC_SERVER_TARGET.equals(transformedName)) {
            return transformMcServer(basicClass);
        }

        if (REMOVE_COMMAND_ACTION_TARGET.equals(
                transformedName)) {

            return transformRemoveCommandAction(basicClass);
        }

        return basicClass;
    }

    private byte[] transformMcServer(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        MethodNode targetMethod = null;

        for (MethodNode method : classNode.methods) {
            if ("removeCommand".equals(method.name)
                    && REMOVE_COMMAND_DESCRIPTOR.equals(method.desc)
                    && (method.access & Opcodes.ACC_STATIC) != 0) {

                if (targetMethod != null) {
                    logger.error(
                            "Found multiple MineTweaker MCServer "
                                    + "command-removal methods. "
                                    + "The server command rollback patch "
                                    + "was not installed!"
                    );

                    return basicClass;
                }

                targetMethod = method;
            }
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find MineTweaker "
                            + "MCServer.removeCommand(ICommand). "
                            + "The server command rollback patch "
                            + "was not installed!"
            );

            return basicClass;
        }

        if (!hasServerLookupPrefix(targetMethod)) {
            logger.error(
                    "MineTweaker MCServer.removeCommand(ICommand) "
                            + "no longer matches the expected implementation. "
                            + "The server command rollback patch "
                            + "was not installed!"
            );

            return basicClass;
        }

        insertGuard(targetMethod);

        logger.info(
                "Installed MineTweaker server command rollback guard."
        );

        return writeClass(classNode);
    }

    private byte[] transformRemoveCommandAction(
            byte[] basicClass) {

        ClassNode classNode = readClass(basicClass);
        MethodNode targetMethod = null;

        for (MethodNode method : classNode.methods) {
            if ("undo".equals(method.name)
                    && "()V".equals(method.desc)) {

                if (targetMethod != null) {
                    logger.error(
                            "Found multiple MineTweaker "
                                    + "RemoveCommandAction undo methods. "
                                    + "The server command rollback patch "
                                    + "was not installed!"
                    );

                    return basicClass;
                }

                targetMethod = method;
            }
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find MineTweaker "
                            + "RemoveCommandAction.undo(). "
                            + "The server command rollback patch "
                            + "was not installed!"
            );

            return basicClass;
        }

        if (!hasServerLookupPrefix(targetMethod)) {
            logger.error(
                    "MineTweaker RemoveCommandAction.undo() no longer "
                            + "matches the expected implementation. "
                            + "The server command rollback patch "
                            + "was not installed!"
            );

            return basicClass;
        }

        insertGuard(targetMethod);

        logger.info(
                "Installed MineTweaker removed-command undo guard."
        );

        return writeClass(classNode);
    }

    private boolean hasServerLookupPrefix(MethodNode method) {
        AbstractInsnNode first = null;
        AbstractInsnNode second = null;
        AbstractInsnNode third = null;
        int found = 0;

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (instruction.getOpcode() < 0) {
                continue;
            }

            if (found == 0) {
                first = instruction;
            } else if (found == 1) {
                second = instruction;
            } else if (found == 2) {
                third = instruction;
                break;
            }

            found++;
        }

        if (!(first instanceof MethodInsnNode)
                || !(second instanceof MethodInsnNode)
                || !(third instanceof TypeInsnNode)) {

            return false;
        }

        MethodInsnNode getServer =
                (MethodInsnNode) first;

        MethodInsnNode getCommandManager =
                (MethodInsnNode) second;

        TypeInsnNode commandHandlerCast =
                (TypeInsnNode) third;

        boolean validGetServer =
                getServer.getOpcode() == Opcodes.INVOKESTATIC
                        && "net/minecraft/server/MinecraftServer".equals(
                        getServer.owner)
                        && ("func_71276_C".equals(getServer.name)
                        || "getServer".equals(getServer.name))
                        && "()Lnet/minecraft/server/MinecraftServer;".equals(
                        getServer.desc);

        boolean validGetCommandManager =
                getCommandManager.getOpcode() == Opcodes.INVOKEVIRTUAL
                        && "net/minecraft/server/MinecraftServer".equals(
                        getCommandManager.owner)
                        && ("func_71187_D".equals(
                        getCommandManager.name)
                        || "getCommandManager".equals(
                        getCommandManager.name))
                        && "()Lnet/minecraft/command/ICommandManager;".equals(
                        getCommandManager.desc);

        boolean validCommandHandlerCast =
                commandHandlerCast.getOpcode() == Opcodes.CHECKCAST
                        && "net/minecraft/command/CommandHandler".equals(
                        commandHandlerCast.desc);

        return validGetServer
                && validGetCommandManager
                && validCommandHandlerCast;
    }

    private void insertGuard(MethodNode method) {
        LabelNode continueOriginal = new LabelNode();
        InsnList guard = new InsnList();

        //noinspection deprecation
        guard.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        PATCH_OWNER,
                        GUARD_NAME,
                        GUARD_DESCRIPTOR
                )
        );

        guard.add(
                new JumpInsnNode(
                        Opcodes.IFEQ,
                        continueOriginal
                )
        );

        guard.add(new InsnNode(Opcodes.RETURN));
        guard.add(continueOriginal);

        method.instructions.insertBefore(
                method.instructions.getFirst(),
                guard
        );
    }
}
