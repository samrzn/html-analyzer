import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlAnalyzer {

    private static final Pattern OPEN_TAG = Pattern.compile("^<([A-Za-z][A-Za-z0-9]*)>$");
    private static final Pattern CLOSE_TAG = Pattern.compile("^</([A-Za-z][A-Za-z0-9]*)>$");

    private static final String OUTPUT_MALFORMED = "malformed HTML";
    private static final String OUTPUT_URL_ERROR = "URL connection error";

    public static void main(String[] args) {

        if (args == null || args.length != 1) {
            System.out.println(OUTPUT_URL_ERROR);
            return;
        }

        String urlString = args[0];

        ParseResult result;
        try {
            result = analyzeUrl(urlString);
        } catch (IOException | IllegalArgumentException e) {
            System.out.println(OUTPUT_URL_ERROR);
            return;
        }

        if (result.malformed || result.deepestText == null) {
            System.out.println(OUTPUT_MALFORMED);
        } else {
            System.out.println(result.deepestText);
        }
    }

    private static ParseResult analyzeUrl(String urlString) throws IOException {
        URL url = createUrl(urlString);

        Deque<String> stack = new ArrayDeque<>();
        int maxDepth = -1;
        String deepestText = null;
        boolean malformed = false;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {

            String rawLine;
            while ((rawLine = reader.readLine()) != null) {
                String line = rawLine.trim();
                if (line.isEmpty()) {
                    continue;
                }

                LineType type = classifyLine(line);

                switch (type.kind) {
                    case OPEN_TAG -> stack.push(type.value);
                    case CLOSE_TAG -> {
                        if (stack.isEmpty()) {
                            malformed = true;
                            break;
                        }
                        String open = stack.pop();
                        if (!open.equals(type.value)) {
                            malformed = true;
                            break;
                        }
                    }
                    case TEXT -> {
                        if (stack.isEmpty()) {
                            malformed = true;
                            break;
                        }

                        int depth = stack.size();
                        if (depth > maxDepth) {
                            maxDepth = depth;
                            deepestText = line;
                        }
                    }
                    case INVALID -> {
                        malformed = true;
                        break;
                    }
                }

                if (malformed) {
                    break;
                }
            }
        }

        if (!malformed && !stack.isEmpty()) {
            malformed = true;
        }

        return new ParseResult(malformed, deepestText);
    }

    private static URL createUrl(String urlString) throws MalformedURLException {

        return new URL(urlString);
    }

    private static LineType classifyLine(String line) {
        if (line.startsWith("<")) {
            Matcher open = OPEN_TAG.matcher(line);
            if (open.matches()) {
                return LineType.openTag(open.group(1));
            }

            Matcher close = CLOSE_TAG.matcher(line);
            if (close.matches()) {
                return LineType.closeTag(close.group(1));
            }

            return LineType.invalid();
        }

        return LineType.text(line);
    }

    private enum Kind {
        OPEN_TAG, CLOSE_TAG, TEXT, INVALID
    }

    private static final class LineType {
        final Kind kind;
        final String value;

        private LineType(Kind kind, String value) {
            this.kind = kind;
            this.value = value;
        }

        static LineType openTag(String name) {
            return new LineType(Kind.OPEN_TAG, name);
        }

        static LineType closeTag(String name) {
            return new LineType(Kind.CLOSE_TAG, name);
        }

        static LineType text(String text) {
            return new LineType(Kind.TEXT, text);
        }

        static LineType invalid() {
            return new LineType(Kind.INVALID, null);
        }
    }

    private static final class ParseResult {
        final boolean malformed;
        final String deepestText;

        private ParseResult(boolean malformed, String deepestText) {
            this.malformed = malformed;
            this.deepestText = deepestText;
        }
    }
}
