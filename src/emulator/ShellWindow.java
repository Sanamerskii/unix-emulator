package emulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/** Swing terminal with an editable command line; not intended for serialization. */
@SuppressWarnings("serial")
public final class ShellWindow extends JFrame {
    private static final int WINDOW_WIDTH = 850;
    private static final int WINDOW_HEIGHT = 520;
    private static final int FONT_SIZE = 16;
    private final Shell shell;
    private final JLabel promptLabel = new JLabel();
    private final Configuration configuration;
    private final JTextArea transcript = new JTextArea();
    private final JTextField input = new JTextField();
    private final JButton submitButton = new JButton("Ввод");

    public ShellWindow(Configuration configuration, MemoryVfs vfs) {
        super("UNIX Emulator — VFS: " + configuration.vfsName());
        this.configuration = configuration;
        shell = new Shell(System.getenv(), vfs);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        configureTranscript();
        transcript.append("VFS загружена в память.\n");
        JPanel prompt = new JPanel(new BorderLayout(8, 0));
        prompt.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        promptLabel.setText(shell.prompt());
        prompt.add(promptLabel, BorderLayout.WEST);
        prompt.add(input, BorderLayout.CENTER);
        prompt.add(submitButton, BorderLayout.EAST);
        add(prompt, BorderLayout.SOUTH);
        input.setFont(new Font(Font.MONOSPACED, Font.PLAIN, FONT_SIZE));
        input.addActionListener(event -> submit());
        submitButton.addActionListener(event -> submit());
    }

    private void configureTranscript() {
        transcript.setEditable(false);
        transcript.setFont(new Font(Font.MONOSPACED, Font.PLAIN, FONT_SIZE));
        transcript.setBackground(new Color(24, 28, 35));
        transcript.setForeground(new Color(225, 235, 225));
        transcript.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        transcript.setText(configuration.debugOutput() + "Этап 5. Команды: ls, cd, head, du, touch, chown, exit.\n"
                + "Переменные: $HOME, ${HOME}. Все пути команд относятся к VFS.\n\n");
        add(new JScrollPane(transcript), BorderLayout.CENTER);
    }

    private void submit() {
        String command = input.getText();
        transcript.append(shell.prompt() + command + "\n");
        input.setText("");
        Shell.Result result = shell.execute(command);
        promptLabel.setText(shell.prompt());
        if (!result.output().isEmpty()) {
            transcript.append(result.output() + "\n");
        }
        transcript.setCaretPosition(transcript.getDocument().getLength());
        if (result.shouldExit()) {
            dispose();
        } else {
            input.requestFocusInWindow();
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
        submitButton.setEnabled(false);
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
        submitButton.setEnabled(true);
        promptLabel.setText(shell.prompt());
        input.requestFocusInWindow();
    }
}
