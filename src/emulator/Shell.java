package emulator;

import java.util.List;
import java.util.Map;

/** Command dispatcher, independent of the graphical interface. */
public final class Shell {
    private final CommandParser parser;
    private final BasicCommands commands;
    private final MutationCommands mutations;

    public Shell(Map<String, String> environment) {
        this(environment, new MemoryVfs());
    }

    public Shell(Map<String, String> environment, MemoryVfs vfs) {
        parser = new CommandParser(environment);
        commands = new BasicCommands(vfs);
        mutations = new MutationCommands(vfs, commands);
    }

    /** Processes a single command and returns output plus a window-close signal. */
    public Result execute(String input) {
        try {
            List<String> words = parser.parse(input);
            if (words.isEmpty()) {
                return new Result("", false);
            }
            String command = words.get(0);
            List<String> arguments = words.subList(1, words.size());
            return switch (command) {
                case "ls" -> new Result(commands.ls(arguments), false);
                case "cd" -> new Result(commands.cd(arguments), false);
                case "head" -> new Result(commands.head(arguments), false);
                case "du" -> new Result(commands.du(arguments), false);
                case "touch" -> new Result(mutations.touch(arguments), false);
                case "chown" -> new Result(mutations.chown(arguments), false);
                case "exit" -> exit(arguments);
                default -> Result.error("неизвестная команда: " + command);
            };
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    private Result exit(List<String> arguments) {
        if (!arguments.isEmpty()) {
            return Result.error("использование: exit (без аргументов)");
        }
        return new Result("Завершение работы.", true);
    }

    /** Result of one REPL iteration. */
    public record Result(String output, boolean shouldExit, boolean failed) {
        public Result(String output, boolean shouldExit) {
            this(output, shouldExit, false);
        }

        public static Result error(String message) {
            return new Result("Ошибка: " + message, false, true);
        }
    }

    public String prompt() {
        return "user@vfs:" + commands.currentDirectory() + "$ ";
    }
}
