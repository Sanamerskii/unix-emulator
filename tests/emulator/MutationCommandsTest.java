package emulator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

/** Checks touch, chown and the requirement that the ZIP is never modified. */
public final class MutationCommandsTest {
    private MutationCommandsTest() { }

    public static void main(String[] arguments) throws Exception {
        Path archive = Path.of("build/vfs/deep.zip");
        byte[] original = Files.readAllBytes(archive);
        MemoryVfs vfs = MemoryVfs.load(archive);
        Shell shell = new Shell(Map.of(), vfs);
        require(!shell.execute("touch created.txt").failed(), "create file");
        require(vfs.node("/created.txt").size() == 0, "empty file created in VFS");
        byte[] content = vfs.node("/readme.txt").content();
        Instant modified = vfs.node("/readme.txt").modified();
        require(!shell.execute("touch readme.txt").failed(), "touch existing file");
        require(vfs.node("/readme.txt").modified().isAfter(modified), "modification time updated");
        require(Arrays.equals(content, vfs.node("/readme.txt").content()), "file content preserved");
        require(!shell.execute("chown student readme.txt").failed(), "chown");
        require(vfs.node("/readme.txt").owner().equals("student"), "virtual owner changed");
        for (String command : java.util.List.of("touch", "touch missing/new.txt", "chown", "chown student missing")) {
            require(shell.execute(command).failed(), "error: " + command);
        }
        require(Arrays.equals(original, Files.readAllBytes(archive)), "ZIP unchanged");
        MemoryVfs fresh = MemoryVfs.load(archive);
        require(fresh.node("/readme.txt").owner().equals("user"), "ownership changes only in memory");
        require(!fresh.children("/").contains("/created.txt"), "created file only in memory");
        System.out.println("PASS: required touch, chown and in-memory-only changes");
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
