package emulator;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/** The two command-line parameters required by stage two. */
public record Configuration(Path vfsPath, Path scriptPath) {
    public static final String USAGE = "Использование: ./run.sh [--vfs ZIP] [--script FILE]";

    public static Configuration parse(String[] arguments) {
        Path vfs = null;
        Path script = null;
        for (int index = 0; index < arguments.length; index++) {
            String option = arguments[index];
            if (!option.equals("--vfs") && !option.equals("--script")) {
                throw new IllegalArgumentException("Неизвестный параметр: " + option);
            }
            if (++index == arguments.length || arguments[index].startsWith("--")) {
                throw new IllegalArgumentException("Не задано значение " + option);
            }
            Path path = parsePath(arguments[index]);
            if (option.equals("--vfs")) {
                rejectDuplicate(vfs, option);
                vfs = path;
            } else {
                rejectDuplicate(script, option);
                script = path;
            }
        }
        return new Configuration(vfs, script);
    }

    private static Path parsePath(String value) {
        if (value.isBlank()) { throw new IllegalArgumentException("Путь не может быть пустым"); }
        try {
            return Path.of(value).toAbsolutePath().normalize();
        } catch (InvalidPathException exception) {
            throw new IllegalArgumentException("Неверный путь: " + value);
        }
    }

    private static void rejectDuplicate(Path previous, String option) {
        if (previous != null) { throw new IllegalArgumentException("Параметр задан повторно: " + option); }
    }

    public String vfsName() {
        return vfsPath == null ? "memory-vfs"
                : vfsPath.getFileName() == null ? vfsPath.toString() : vfsPath.getFileName().toString();
    }

    public String debugOutput() {
        return "Конфигурация:\n  VFS: " + (vfsPath == null ? "не задана" : vfsPath)
                + "\n  Стартовый скрипт: " + (scriptPath == null ? "не задан" : scriptPath) + "\n";
    }
}
