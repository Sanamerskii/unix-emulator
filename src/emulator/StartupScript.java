package emulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/** Executes UTF-8 startup commands sequentially, stopping at the first error or exit. */
public final class StartupScript {
    private StartupScript() { }

    public static Report run(Path path, Shell shell, Consumer<String> display) {
        int line = 0;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String command;
            while ((command = reader.readLine()) != null) {
                line++;
                if (command.isBlank() || command.stripLeading().startsWith("#")) {
                    continue;
                }
                display.accept(shell.prompt() + command + "\n");
                Shell.Result result = shell.execute(command);
                if (!result.output().isEmpty()) {
                    display.accept(result.output() + "\n");
                }
                if (result.failed()) {
                    return failure(path, line, "ошибка команды", display);
                }
                if (result.shouldExit()) {
                    return new Report(false, true, line);
                }
            }
            display.accept("Стартовый скрипт завершен.\n");
            return new Report(false, false, line);
        } catch (IOException exception) {
            return failure(path, line, exception.getMessage(), display);
        }
    }

    private static Report failure(Path path, int line, String reason, Consumer<String> display) {
        display.accept("Ошибка стартового скрипта " + path + ": строка " + line + ": " + reason + "\n");
        return new Report(true, false, line);
    }

    /** Includes physical line number for diagnostics, counting blank lines and comments. */
    public record Report(boolean failed, boolean shouldExit, int line) { }
}
