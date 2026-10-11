package emulator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Checks the two launch parameters and mandatory startup-script error handling. */
public final class ConfigurationTest {
    private ConfigurationTest() { }

    public static void main(String[] arguments) throws Exception {
        Configuration config = Configuration.parse(new String[]{"--vfs", "vfs.zip", "--script", "startup.txt"});
        require(config.vfsName().equals("vfs.zip"), "VFS path");
        require(config.scriptPath().getFileName().toString().equals("startup.txt"), "script path");
        require(config.debugOutput().contains("vfs.zip") && config.debugOutput().contains("startup.txt"),
                "debug output of both parameters");
        for (String[] invalid : new String[][]{{"--unknown"}, {"--vfs"}}) {
            try {
                Configuration.parse(invalid);
                throw new AssertionError("Expected invalid parameters");
            } catch (IllegalArgumentException expected) {
                require(!expected.getMessage().isEmpty(), "parameter error");
            }
        }
        testScript();
        System.out.println("PASS: configuration and startup-script requirements");
    }

    private static void testScript() throws Exception {
        Path script = Files.createTempFile("startup", ".txt");
        try {
            Files.writeString(script, "ls\nunknown-command\nls NOT_EXECUTED\n");
            StringBuilder output = new StringBuilder();
            Shell shell = new Shell(Map.of());
            StartupScript.Report report = StartupScript.run(script, shell, output::append);
            require(report.failed() && report.line() == 2, "stop at first error with line number");
            require(output.toString().contains("$ ls\n"), "command echoed");
            require(output.toString().contains("Ошибка:"), "error displayed");
            require(!output.toString().contains("NOT_EXECUTED"), "later command skipped");
            Files.writeString(script, "exit\nunknown-command\n");
            require(StartupScript.run(script, shell, text -> { }).shouldExit(), "exit stops script");
        } finally {
            Files.deleteIfExists(script);
        }
        require(StartupScript.run(script, new Shell(Map.of()), text -> { }).failed(), "missing script");
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
