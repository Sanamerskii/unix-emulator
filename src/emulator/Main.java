package emulator;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import javax.swing.SwingUtilities;

/** GUI entry point with validated configuration and startup diagnostics. */
public final class Main {
    private Main() { }

    public static void main(String[] arguments) {
        try {
            Configuration configuration = Configuration.parse(arguments);
            System.out.print(configuration.debugOutput());
            MemoryVfs vfs = configuration.vfsPath() == null ? new MemoryVfs()
                    : MemoryVfs.load(configuration.vfsPath());
            if (GraphicsEnvironment.isHeadless()) {
                throw new IllegalArgumentException("Для запуска требуется графическая среда");
            }
            SwingUtilities.invokeLater(() -> new ShellWindow(configuration, vfs).open());
        } catch (IOException | IllegalArgumentException exception) {
            System.err.println("Ошибка запуска: " + exception.getMessage());
            System.err.println(Configuration.USAGE);
            System.exit(1);
        }
    }
}
