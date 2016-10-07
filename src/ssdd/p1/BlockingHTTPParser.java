package ssdd.p1;

import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class BlockingHTTPParser {
    public void parseRequest(InputStream stream) {
        if (!readMethod(stream) || !readHeaders(stream) || !readBody(stream)) {
            state = BAD_REQUEST;
        } else {
            state = COMPLETE;
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


    private final int START = 0;
    private final int COMPLETE = 1;
    private final int BAD_REQUEST = 2;

    private int state = START;
    private ByteBuffer body;
    private String method = "";
    private String path = "";
    private String remnant = "";

    private Pattern methodPattern = Pattern.compile("\\s*(\\w+)\\s+(\\S+)\\s+HTTP/1.1\\s*");
    private Pattern bodyLengthPattern = Pattern.compile("\\s*Content-Length:\\s*(\\d+)\\s*");

    private boolean readMethod(InputStream stream) {
        String firstLine = readLine(stream);
        if (firstLine != null) {
            Matcher matcher = methodPattern.matcher(firstLine);
            if (matcher.matches()) {
                method = matcher.group(1);
                path = matcher.group(2);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean readHeaders(InputStream stream) {
        String line = readLine(stream);
        while (line != null && !line.equals("")) {
            Matcher matcher = bodyLengthPattern.matcher(line);
            if (matcher.matches()) {
                body = ByteBuffer.allocate(Integer.parseInt(matcher.group(1)));
            }
            line = readLine(stream);
        }
        return line != null;
    }

    private boolean readBody(InputStream stream) {
        if (body != null) {
            try {
                stream.read(body.array());
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    private static String readLine(InputStream stream) {
        String result = new String("");
        do {
            int nextChar = -1;
            try {
                nextChar = stream.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nextChar == -1) return null;
            char actualChar = (char)nextChar;
            if (actualChar == '\n')
                return result;
            if (actualChar != '\r') {
                result += actualChar;
            }
        } while (result.length() < 1000);
        return null;
    }
}

