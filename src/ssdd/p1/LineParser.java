package ssdd.p1;

import java.nio.ByteBuffer;

public class LineParser {
    private ByteBuffer buffer;

    public LineParser(ByteBuffer b) {
        buffer = b;
    }

    public String readLine() {
        String result = null;
        int newLine = findNewLine();
        if (newLine < buffer.limit()) {
            byte[] line = new byte[newLine - buffer.position()];
            buffer.get(line);
            result = new String(line);
            removeNewLine();
        }
        return result;
    }

    private int findNewLine() {
        int currentPos = buffer.position();
        while (currentPos < buffer.limit() && !isNewLine(buffer.get(currentPos))) {
            ++currentPos;
        }
        return currentPos;
    }

    private boolean isNewLine(byte c) {
        return c == (byte)'\n' || c == (byte)'\r';
    }

    private void removeNewLine() {
        byte n = buffer.get();
        if (n == (byte)'\r') {
            n = buffer.get(buffer.position());
            if (n == (byte)'\n') {
                buffer.get();
            }
        }
    }
}

