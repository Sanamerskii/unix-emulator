package emulator;

import java.util.List;

/** Required commands changing only virtual files and ownership. */
public final class MutationCommands {
    private final MemoryVfs vfs;
    private final BasicCommands basic;

    public MutationCommands(MemoryVfs vfs, BasicCommands basic) {
        this.vfs = vfs;
        this.basic = basic;
    }

    public String touch(List<String> arguments) {
        CommandArguments.requireCount(arguments, 1, "touch файл");
        String path = vfs.resolve(arguments.get(0), basic.currentDirectory(), true);
        vfs.touch(path);
        return "";
    }

    public String chown(List<String> arguments) {
        CommandArguments.requireCount(arguments, 2, "chown владелец путь");
        String owner = arguments.get(0);
        if (!owner.matches("(?:[A-Za-z_][A-Za-z0-9_.-]*|[0-9]+)")) {
            throw new IllegalArgumentException("chown: неверный владелец: " + owner);
        }
        String path = vfs.resolve(arguments.get(1), basic.currentDirectory());
        vfs.node(path).changeOwner(owner);
        return "";
    }
}
