package emulator;

import javax.swing.SwingUtilities;

/** GUI entry point; the optional argument is a display name for the future VFS. */
public final class Main {
    private Main() { }

    public static void main(String[] arguments) {
        if (arguments.length > 1) {
            System.err.println("Использование: ./run.sh [имя-VFS]");
            System.exit(1);
        }
        String name = arguments.length == 0 ? "vfs-stage1" : arguments[0];
        SwingUtilities.invokeLater(() -> new ShellWindow(name).open());
    }
}
