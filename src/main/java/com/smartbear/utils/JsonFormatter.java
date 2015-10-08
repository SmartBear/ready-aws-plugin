package com.smartbear.utils;

/*Implementation borrowed form the //http://programanddesign.com/cs/json-prettifier/*/
public class JsonFormatter {
    private final static String Indent = "    ";
    private StringBuilder sb = null;


    private void appendIndent(int count) {
        for (; count > 0; --count) {
            sb.append(Indent);
        }
    }

    private void appendLine() {
        sb.append("\r\n");
    }

    private boolean isEscaped(int index) {
        boolean escaped = false;
        while (index > 0 && sb.charAt(--index) == '\\') {
            escaped = !escaped;
        }
        return escaped;
    }

    public String prettyPrint(String input) {
        sb = new StringBuilder(input.length() * 2);
        Character quote = null;
        int depth = 0;

        for (int i = 0; i < input.length(); ++i) {
            char ch = input.charAt(i);

            switch (ch) {
                case '{':
                case '[':
                    sb.append(ch);
                    if (quote == null) {
                        appendLine();
                        appendIndent(++depth);
                    }
                    break;
                case '}':
                case ']':
                    if (quote != null) {
                        sb.append(ch);
                    } else {
                        appendLine();
                        appendIndent(--depth);
                        sb.append(ch);
                    }
                    break;
                case '"':
                case '\'':
                    sb.append(ch);
                    if (quote != null) {
                        if (!isEscaped(i)) {
                            quote = null;
                        }
                    } else {
                        quote = ch;
                    }
                    break;
                case ',':
                    sb.append(ch);
                    if (quote == null) {
                        appendLine();
                        appendIndent(depth);
                    }
                    break;
                case ':':
                    sb.append(quote != null ? ch : " : ");
                    break;
                default:
                    if (quote != null || !Character.isSpaceChar(ch)) {
                        sb.append(ch);
                    }
                    break;
            }
        }

        return sb.toString();
    }
}
