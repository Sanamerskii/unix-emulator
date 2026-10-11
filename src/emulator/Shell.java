package emulator;

import java.util.List;
import java.util.Map;

/** Stage-one command dispatcher, independent of the graphical interface. */
public final class Shell {
    private static final int MAX_PATH_ARGUMENTS = 1;
    private final CommandParser parser;

    public Shell(Map<String, String> environment) {
        parser = new CommandParser(environment);
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
                case "ls", "cd" -> stub(command, arguments);
                case "exit" -> exit(arguments);
                default -> Result.error("неизвестная команда: " + command);
            };
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    private Result stub(String command, List<String> arguments) {
        if (arguments.size() > MAX_PATH_ARGUMENTS || arguments.stream().anyMatch(arg -> arg.startsWith("-"))) {
            return Result.error("использование: " + command + " [путь] (опции пока не поддерживаются)");
        }
        String rendered = arguments.stream().map(Shell::quote).reduce((left, right) -> left + ", " + right)
                .orElse("");
        return new Result(command + " args=[" + rendered + "]", false);
    }

    private Result exit(List<String> arguments) {
        if (!arguments.isEmpty()) {
            return Result.error("использование: exit (без аргументов)");
        }
        return new Result("Завершение работы.", true);
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
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
        return "$ ";
    }
}
