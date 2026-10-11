package emulator;

import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

/** GUI entry point with validated configuration and startup diagnostics. */
public final class Main {
    private Main() { }

    public static void main(String[] arguments) {
        try {
            Configuration configuration = Configuration.parse(arguments);
            System.out.print(configuration.debugOutput());
            if (GraphicsEnvironment.isHeadless()) {
                throw new IllegalArgumentException("Для запуска требуется графическая среда");
            }
            SwingUtilities.invokeLater(() -> new ShellWindow(configuration).open());
        } catch (IllegalArgumentException exception) {
            System.err.println("Ошибка запуска: " + exception.getMessage());
            System.err.println(Configuration.USAGE);
            System.exit(1);
        }
    }
}
