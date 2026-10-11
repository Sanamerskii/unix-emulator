package emulator;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Required commands operating only on the memory VFS. */
public final class BasicCommands {
    private static final int HEAD_LINES = 10;
    private static final long KIBIBYTE = 1024;
    private final MemoryVfs vfs;
    private String currentDirectory = "/";

    public BasicCommands(MemoryVfs vfs) { this.vfs = vfs; }
    public String currentDirectory() { return currentDirectory; }

    public String cd(List<String> arguments) {
        String path = CommandArguments.optionalPath(arguments, "/", "cd [путь]");
        String resolved = vfs.resolve(path, currentDirectory);
        vfs.requireDirectory(resolved);
        currentDirectory = resolved;
        return "";
    }

    public String ls(List<String> arguments) {
        String path = CommandArguments.optionalPath(arguments, ".", "ls [путь]");
        String resolved = vfs.resolve(path, currentDirectory);
        if (!vfs.node(resolved).isDirectory()) { return MemoryVfs.name(resolved); }
        List<String> names = new ArrayList<>();
        for (String child : vfs.children(resolved)) {
            String name = MemoryVfs.name(child);
            if (!name.startsWith(".")) { names.add(name); }
        }
        return String.join("\n", names);
    }

    public String head(List<String> arguments) {
        CommandArguments.requireCount(arguments, 1, "head файл");
        String path = vfs.resolve(arguments.get(0), currentDirectory);
        VfsNode node = vfs.node(path);
        if (node.isDirectory()) { throw new IllegalArgumentException("head: это каталог: " + path); }
        String text = decodeText(node.content());
        int position = 0;
        for (int line = 0; line < HEAD_LINES && position < text.length(); line++) {
            int newline = text.indexOf('\n', position);
            position = newline < 0 ? text.length() : newline + 1;
        }
        return text.substring(0, position);
    }

    private static String decodeText(byte[] content) {
        try {
            return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(content)).toString();
        } catch (CharacterCodingException exception) {
            throw new IllegalArgumentException("head: файл не является текстом UTF-8");
        }
    }

    public String du(List<String> arguments) {
        String path = CommandArguments.optionalPath(arguments, ".", "du [путь]");
        String resolved = vfs.resolve(path, currentDirectory);
        List<String> output = new ArrayList<>();
        long bytes = directorySize(resolved, path, output);
        if (!vfs.node(resolved).isDirectory()) { output.add(sizeLine(bytes, path)); }
        return String.join("\n", output);
    }

    private long directorySize(String path, String label, List<String> output) {
        VfsNode node = vfs.node(path);
        long size = node.size();
        if (node.isDirectory()) {
            for (String child : vfs.children(path)) {
                size += directorySize(child, MemoryVfs.child(label, MemoryVfs.name(child)), output);
            }
            output.add(sizeLine(size, label));
        }
        return size;
    }

    private static String sizeLine(long bytes, String path) {
        return (bytes + KIBIBYTE - 1) / KIBIBYTE + "\t" + path;
    }
}
