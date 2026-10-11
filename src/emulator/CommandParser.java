package emulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Splits shell words and expands environment variables without executing host commands. */
public final class CommandParser {
    private final Map<String, String> environment;

    public CommandParser(Map<String, String> environment) {
        this.environment = Map.copyOf(environment);
    }

    /** Parses quotes, escapes, $NAME and ${NAME}; single quotes preserve literal text. */
    public List<String> parse(String input) {
        State state = new State(input);
        while (state.index < input.length()) {
            consume(state);
        }
        if (state.quote != 0) {
            throw new IllegalArgumentException("Незакрытая кавычка");
        }
        state.finishWord();
        return List.copyOf(state.words);
    }

    private void consume(State state) {
        char character = state.input.charAt(state.index++);
        if (character == state.quote) {
            state.quote = 0;
        } else if (state.quote == '\'') {
            state.word.append(character);
        } else if (character == '\\') {
            escape(state);
        } else if (character == '$') {
            state.word.append(expand(state));
            state.started = true;
        } else if (state.quote == 0 && (character == '\'' || character == '"')) {
            state.quote = character;
            state.started = true;
        } else if (state.quote == 0 && Character.isWhitespace(character)) {
            state.finishWord();
        } else {
            state.word.append(character);
            state.started = true;
        }
    }

    private void escape(State state) {
        if (state.index == state.input.length()) {
            throw new IllegalArgumentException("После обратной косой черты ожидается символ");
        }
        char next = state.input.charAt(state.index++);
        if (state.quote == '"' && next != '$' && next != '"' && next != '\\') {
            state.word.append('\\');
        }
        state.word.append(next);
        state.started = true;
    }

    private String expand(State state) {
        if (state.index == state.input.length()) {
            return "$";
        }
        if (state.input.charAt(state.index) == '{') {
            return expandBraced(state);
        }
        int start = state.index;
        if (!isNameStart(state.input.charAt(start))) {
            return "$";
        }
        while (state.index < state.input.length() && isNamePart(state.input.charAt(state.index))) {
            state.index++;
        }
        return environment.getOrDefault(state.input.substring(start, state.index), "");
    }

    private String expandBraced(State state) {
        int start = ++state.index;
        int end = state.input.indexOf('}', start);
        if (end < 0) {
            throw new IllegalArgumentException("Незакрытая переменная ${...}");
        }
        String name = state.input.substring(start, end);
        if (!name.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Неверное имя переменной: " + name);
        }
        state.index = end + 1;
        return environment.getOrDefault(name, "");
    }

    private static boolean isNameStart(char character) {
        return character == '_' || character >= 'A' && character <= 'Z'
                || character >= 'a' && character <= 'z';
    }

    private static boolean isNamePart(char character) {
        return isNameStart(character) || character >= '0' && character <= '9';
    }

    private static final class State {
        private final String input;
        private final List<String> words = new ArrayList<>();
        private final StringBuilder word = new StringBuilder();
        private int index;
        private char quote;
        private boolean started;

        private State(String input) {
            this.input = input;
        }

        private void finishWord() {
            if (started) {
                words.add(word.toString());
                word.setLength(0);
                started = false;
            }
        }
    }
}
