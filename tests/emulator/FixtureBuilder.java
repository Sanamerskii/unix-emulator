package emulator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Generates the ZIP variants required for demonstration under ignored build/. */
public final class FixtureBuilder {
    private FixtureBuilder() { }

    public static void main(String[] arguments) throws IOException {
        Path directory = Path.of("build/vfs");
        Files.createDirectories(directory);
        write(directory.resolve("minimal.zip"), Map.of());
        write(directory.resolve("several.zip"), Map.of("one.txt", bytes("first\n"),
                "two.txt", bytes("second\n"), "data.bin", new byte[]{0, 1, 2, (byte) 255}));
        write(directory.resolve("deep.zip"), deepFiles());
        write(directory.resolve("VFS with spaces.zip"), deepFiles());
        Files.writeString(directory.resolve("invalid.zip"), "This is not a ZIP archive");
        System.out.println("Fixtures created in " + directory.toAbsolutePath());
    }

    private static Map<String, byte[]> deepFiles() {
        Map<String, byte[]> files = new LinkedHashMap<>();
        StringBuilder lines = new StringBuilder();
        for (int index = 1; index <= 15; index++) { lines.append("line ").append(index).append('\n'); }
        files.put("readme.txt", bytes(lines.toString()));
        files.put("docs/guide.txt", bytes("Привет\nвторая строка\n"));
        files.put("docs/My Notes.txt", bytes("notes\nwithout trailing newline"));
        files.put("a/b/c/deep.txt", bytes("deep file\n"));
        files.put("data.bin", new byte[]{0, 1, 2, (byte) 255});
        return files;
    }

    private static void write(Path path, Map<String, byte[]> files) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(path))) {
            for (Map.Entry<String, byte[]> entry : files.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zipEntry.setTime(1_700_000_000_000L);
                zip.putNextEntry(zipEntry);
                zip.write(entry.getValue());
                zip.closeEntry();
            }
        }
    }

    private static byte[] bytes(String text) { return text.getBytes(StandardCharsets.UTF_8); }
}
