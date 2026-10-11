package emulator;

import java.util.List;

/** Validates the argument counts for the required command forms. */
public final class CommandArguments {
    private CommandArguments() { }

    public static String optionalPath(List<String> arguments, String defaultPath, String usage) {
        if (arguments.size() > 1) { throw new IllegalArgumentException("использование: " + usage); }
        rejectOptions(arguments);
        return arguments.isEmpty() ? defaultPath : arguments.get(0);
    }

    public static void requireCount(List<String> arguments, int count, String usage) {
        if (arguments.size() != count) { throw new IllegalArgumentException("использование: " + usage); }
        rejectOptions(arguments);
    }

    private static void rejectOptions(List<String> arguments) {
        for (String argument : arguments) {
            if (argument.startsWith("-")) {
                throw new IllegalArgumentException("Опции не поддерживаются: " + argument);
            }
        }
    }
}
