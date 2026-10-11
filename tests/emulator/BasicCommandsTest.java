package emulator;

import java.nio.file.Path;
import java.util.Map;

/** Checks the basic ls, cd, head and du behavior required by stage four. */
public final class BasicCommandsTest {
    private BasicCommandsTest() { }

    public static void main(String[] arguments) throws Exception {
        MemoryVfs vfs = MemoryVfs.load(Path.of("build/vfs/deep.zip"));
        Shell shell = new Shell(Map.of(), vfs);
        require(shell.execute("ls").output().contains("docs"), "list VFS root");
        require(!shell.execute("cd /docs").failed(), "absolute cd");
        require(shell.execute("ls").output().contains("guide.txt"), "list current directory");
        require(!shell.execute("cd ../a/b/c").failed(), "relative cd and parents");
        require(shell.execute("ls").output().contains("deep.txt"), "three nested directories");
        require(!shell.execute("cd /").failed(), "return to root");
        String head = shell.execute("head readme.txt").output();
        require(head.endsWith("line 10\n") && !head.contains("line 11"), "first ten lines");
        require(shell.execute("head 'docs/My Notes.txt'").output().contains("without trailing newline"),
                "quoted text filename");
        require(shell.execute("du readme.txt").output().equals("1\treadme.txt"), "file size in KiB");
        require(shell.execute("du a").output().contains("a/b/c"), "recursive directory sizes");
        for (String command : java.util.List.of("ls missing", "ls a b", "cd readme.txt",
                "head", "head docs", "du a b")) {
            require(shell.execute(command).failed(), "error: " + command);
        }
        System.out.println("PASS: required ls, cd, head and du behavior");
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
