package emulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/** Swing terminal with an editable command line. */
public final class ShellWindow extends JFrame {
    private static final int WINDOW_WIDTH = 850;
    private static final int WINDOW_HEIGHT = 520;
    private static final int FONT_SIZE = 16;
    private final Shell shell = new Shell(System.getenv());
    private final Configuration configuration;
    private final JTextArea transcript = new JTextArea();
    private final JTextField input = new JTextField();

    public ShellWindow(Configuration configuration) {
        super("UNIX Emulator — VFS: " + configuration.vfsName());
        this.configuration = configuration;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        configureTranscript();
        JPanel prompt = new JPanel(new BorderLayout(8, 0));
        prompt.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        prompt.add(new JLabel("user@" + configuration.vfsName() + ":~$"), BorderLayout.WEST);
        prompt.add(input, BorderLayout.CENTER);
        add(prompt, BorderLayout.SOUTH);
        input.setFont(new Font(Font.MONOSPACED, Font.PLAIN, FONT_SIZE));
        input.addActionListener(event -> submit());
    }

    private void configureTranscript() {
        transcript.setEditable(false);
        transcript.setFont(new Font(Font.MONOSPACED, Font.PLAIN, FONT_SIZE));
        transcript.setBackground(new Color(24, 28, 35));
        transcript.setForeground(new Color(225, 235, 225));
        transcript.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        transcript.setText(configuration.debugOutput() + "Этап 2. Команды: ls [путь], cd [путь], exit.\n"
                + "ls и cd — заглушки. Переменные: $HOME, ${HOME}.\n\n");
        add(new JScrollPane(transcript), BorderLayout.CENTER);
    }

    private void submit() {
        String command = input.getText();
        transcript.append("$ " + command + "\n");
        input.setText("");
        Shell.Result result = shell.execute(command);
        if (!result.output().isEmpty()) {
            transcript.append(result.output() + "\n");
        }
        transcript.setCaretPosition(transcript.getDocument().getLength());
        if (result.shouldExit()) {
            dispose();
        }
    }

    /** Displays the window and focuses the command line. */
    public void open() {
        setVisible(true);
        input.requestFocusInWindow();
        runStartup();
    }

    private void runStartup() {
        if (configuration.scriptPath() == null) {
            return;
        }
        input.setEnabled(false);
        new SwingWorker<StartupScript.Report, String>() {
            @Override
            protected StartupScript.Report doInBackground() {
                return StartupScript.run(configuration.scriptPath(), shell, text -> publish(text));
            }

            @Override
            protected void process(List<String> chunks) {
                chunks.forEach(transcript::append);
                transcript.setCaretPosition(transcript.getDocument().getLength());
            }

            @Override
            protected void done() {
                finishStartup(this);
            }
        }.execute();
    }

    private void finishStartup(SwingWorker<StartupScript.Report, String> worker) {
        try {
            if (worker.get().shouldExit()) {
                dispose();
                return;
            }
        } catch (Exception exception) {
            transcript.append("Ошибка стартового скрипта: " + exception.getMessage() + "\n");
        }
        input.setEnabled(true);
        input.requestFocusInWindow();
    }
}
