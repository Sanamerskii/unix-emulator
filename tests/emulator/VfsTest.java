package emulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/** Checks the mandatory ZIP variants, binary storage and loading errors. */
public final class VfsTest {
    private VfsTest() { }

    public static void main(String[] arguments) throws Exception {
        FixtureBuilder.main(new String[0]);
        Path archive = Path.of("build/vfs/deep.zip");
        byte[] original = Files.readAllBytes(archive);
        MemoryVfs vfs = MemoryVfs.load(archive);
        require(vfs.children("/a/b/c").equals(List.of("/a/b/c/deep.txt")), "three directory levels");
        require(Arrays.equals(vfs.node("/data.bin").content(), new byte[]{0, 1, 2, (byte) 255}), "binary data");
        require(MemoryVfs.load(Path.of("build/vfs/minimal.zip")).children("/").isEmpty(), "minimal VFS");
        require(MemoryVfs.load(Path.of("build/vfs/several.zip")).children("/").size() == 3, "several files");
        require(Arrays.equals(original, Files.readAllBytes(archive)), "ZIP unchanged");
        require(!Files.exists(Path.of("build/vfs/a")), "no extraction");
        for (String name : List.of("missing.zip", "invalid.zip")) {
            try {
                MemoryVfs.load(Path.of("build/vfs", name));
                throw new AssertionError("Expected loading error");
            } catch (IOException expected) {
                require(expected.getMessage() != null, "loading diagnostic");
            }
        }
        System.out.println("PASS: required ZIP VFS behavior");
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
