package emulator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Loads ZIP files into memory without extraction or host-file modification. */
public final class MemoryVfs {
    private final TreeMap<String, VfsNode> nodes = new TreeMap<>();

    public MemoryVfs() {
        nodes.put("/", new VfsNode(true, new byte[0], Instant.now()));
    }

    public static MemoryVfs load(Path archive) throws IOException {
        MemoryVfs vfs = new MemoryVfs();
        Set<String> paths = new HashSet<>();
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String path = archivePath(entry.getName());
                if (!paths.add(path)) { throw new IOException("Повторный путь ZIP: " + path); }
                byte[] content = readEntry(zip, entry);
                Instant modified = entry.getLastModifiedTime() == null ? Instant.EPOCH
                        : entry.getLastModifiedTime().toInstant();
                vfs.insert(path, new VfsNode(entry.isDirectory(), content, modified));
            }
        } catch (IllegalArgumentException exception) {
            throw new IOException("Неверный формат ZIP: " + exception.getMessage(), exception);
        }
        return vfs;
    }

    private static byte[] readEntry(ZipFile zip, ZipEntry entry) throws IOException {
        byte[] content;
        try (InputStream stream = zip.getInputStream(entry)) { content = stream.readAllBytes(); }
        CRC32 checksum = new CRC32();
        checksum.update(content);
        if (checksum.getValue() != entry.getCrc() || content.length != entry.getSize()) {
            throw new IOException("Поврежден файл ZIP: " + entry.getName());
        }
        return content;
    }

    private static String archivePath(String name) throws IOException {
        if (name.isEmpty() || name.startsWith("/") || name.contains("\\") || name.indexOf('\0') >= 0) {
            throw new IOException("Недопустимый путь ZIP: " + name);
        }
        String clean = name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
        for (String part : clean.split("/", -1)) {
            if (part.isEmpty() || part.equals(".") || part.equals("..")) {
                throw new IOException("Недопустимый путь ZIP: " + name);
            }
        }
        return "/" + clean;
    }

    private void insert(String path, VfsNode node) throws IOException {
        String parent = parent(path);
        if (!nodes.containsKey(parent)) { insert(parent, new VfsNode(true, new byte[0], node.modified())); }
        if (!nodes.get(parent).isDirectory()) { throw new IOException("Родитель не является каталогом: " + parent); }
        VfsNode previous = nodes.get(path);
        if (previous != null && (!previous.isDirectory() || !node.isDirectory())) {
            throw new IOException("Конфликт файлов и каталогов: " + path);
        }
        nodes.put(path, node);
    }

    public String resolve(String path, String currentDirectory) {
        return resolve(path, currentDirectory, false);
    }

    public String resolve(String path, String currentDirectory, boolean allowMissingLeaf) {
        if (path.isEmpty() || path.indexOf('\0') >= 0) { throw new IllegalArgumentException("Неверный путь: " + path); }
        String current = path.startsWith("/") ? "/" : currentDirectory;
        String[] parts = path.split("/");
        for (int index = 0; index < parts.length; index++) {
            if (!parts[index].isEmpty()) {
                current = resolveComponent(current, parts[index], allowMissingLeaf && index == parts.length - 1);
            }
        }
        if (path.endsWith("/")) { requireDirectory(current); }
        return current;
    }

    private String resolveComponent(String current, String part, boolean allowMissing) {
        requireDirectory(current);
        if (part.equals("..")) { return parent(current); }
        if (part.equals(".")) { return current; }
        String result = child(current, part);
        if (!allowMissing) { node(result); }
        return result;
    }

    public VfsNode node(String absolutePath) {
        VfsNode node = nodes.get(absolutePath);
        if (node == null) { throw new IllegalArgumentException("Нет такого файла или каталога: " + absolutePath); }
        return node;
    }

    public void requireDirectory(String path) {
        if (!node(path).isDirectory()) { throw new IllegalArgumentException("Не является каталогом: " + path); }
    }

    public List<String> children(String path) {
        requireDirectory(path);
        List<String> children = new ArrayList<>();
        for (String candidate : nodes.keySet()) {
            if (!candidate.equals(path) && parent(candidate).equals(path)) { children.add(candidate); }
        }
        return children;
    }

    public static String parent(String path) {
        int slash = path.lastIndexOf('/');
        return slash <= 0 ? "/" : path.substring(0, slash);
    }

    public static String name(String path) {
        return path.equals("/") ? "/" : path.substring(path.lastIndexOf('/') + 1);
    }

    public static String child(String parent, String name) {
        return parent.equals("/") ? "/" + name : parent + "/" + name;
    }

    /** Creates an empty virtual file or updates its modification time. */
    public void touch(String path) {
        Instant now = Instant.now();
        if (!nodes.containsKey(path)) {
            requireDirectory(parent(path));
            nodes.put(path, new VfsNode(false, new byte[0], now));
        }
        node(path).touch(now);
    }
}
