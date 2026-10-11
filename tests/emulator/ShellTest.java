package emulator;

import java.util.List;
import java.util.Map;

/** Checks environment expansion, parsing errors and the required REPL commands. */
public final class ShellTest {
    private ShellTest() { }

    public static void main(String[] arguments) {
        CommandParser parser = new CommandParser(Map.of("HOME", "/home/student"));
        require(parser.parse("cd $HOME").equals(List.of("cd", "/home/student")), "environment variable");
        require(parser.parse("ls \"" + "$" + "{HOME}/My Documents\"").equals(
                List.of("ls", "/home/student/My Documents")), "braces and quoted path");
        require(parser.parse("ls '$HOME'").equals(List.of("ls", "$HOME")), "single quotes");
        Shell shell = new Shell(System.getenv());
        require(!shell.execute("ls").failed(), "ls");
        require(!shell.execute("cd").failed(), "cd");
        require(shell.execute("pwd").failed(), "unknown command");
        require(shell.execute("cd a b").failed(), "invalid arguments");
        require(shell.execute("ls \"broken").failed(), "parser error");
        require(!shell.execute("ls").failed(), "REPL continues after error");
        require(shell.execute("exit").shouldExit(), "exit");
        require(shell.execute("exit now").failed(), "exit arguments");
        System.out.println("PASS: required parser and REPL behavior");
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
