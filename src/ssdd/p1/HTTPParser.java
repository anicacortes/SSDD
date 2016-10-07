package ssdd.p1;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class HTTPParser {
    public void parseRequest(ByteBuffer buffer) {
        if (state == READ_METHOD) {
            readMethod(buffer);
        }
        if (state == READ_HEADS) {
            readHeaders(buffer);
        }
        if (state == READ_BODY) {
            readBody(buffer);
        }
    }

    public boolean isComplete() {
        return state == COMPLETE;
    }

    public boolean failed() {
        return state == BAD_REQUEST;
    }

    public String getMethod() {
        return method;
    }
    
    public String getPath() {
        return path;
    }

    public ByteBuffer getBody() {
        return body;
    }


    private final int READ_METHOD = 0;
    private final int READ_HEADS = 1;
    private final int READ_BODY = 2;
    private final int COMPLETE = 3;
    private final int BAD_REQUEST = 4;

    private int state = READ_METHOD;
    private ByteBuffer body;
    private String method = "";
    private String path = "";
    private String remnant = "";

    private Pattern methodPattern = Pattern.compile("\\s*(\\w+)\\s+(\\S+)\\s+HTTP/1.1\\s*");
    private Pattern bodyLengthPattern = Pattern.compile("\\s*Content-Length:\\s*(\\d+)\\s*");

    private void readMethod(ByteBuffer buffer) {
        LineParser lp = new LineParser(buffer);
        String firstLine = lp.readLine();
        if (firstLine != null) {
            firstLine = remnant + firstLine;
            remnant = "";
            Matcher matcher = methodPattern.matcher(firstLine);
            if (matcher.matches()) {
                method = matcher.group(1);
                path = matcher.group(2);
                state = READ_HEADS;
            } else {
                state = BAD_REQUEST;
            }
        } else {
            remnant = remnant + asString(buffer);
            buffer.clear();
        }
    }

    private void readHeaders(ByteBuffer buffer) {
        LineParser lp = new LineParser(buffer);
        String line = lp.readLine();
        if (line != null) {
            line = remnant + line;
            remnant = "";
        }
        while (line != null && !line.equals("")) {
            Matcher matcher = bodyLengthPattern.matcher(line);
            if (matcher.matches()) {
                body = ByteBuffer.allocate(Integer.parseInt(matcher.group(1)));
            }
            line = lp.readLine();
        }
        if (line == null) {
            remnant = remnant + asString(buffer);
            buffer.clear();
        } else {
            state = body != null ? READ_BODY : COMPLETE;
        }
    }

    private void readBody(ByteBuffer buffer) {
        if (buffer.remaining() <= body.remaining()) {
            body.put(buffer);
        } else {
            byte[] array = new byte[body.remaining()];
            buffer.get(array);
            body.put(array);
        }
        if (!body.hasRemaining()) {
            body.flip();
            state = COMPLETE;
        }
    }

    private static String asString(ByteBuffer buffer) {
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        return new String(result);
    }
}

