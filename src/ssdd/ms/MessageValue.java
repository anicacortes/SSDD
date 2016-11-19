package ssdd.ms;

import java.io.Serializable;

public class MessageValue implements Serializable {
	private static final long serialVersionUID = 1L;

	private String value;
	public MessageValue(String v) { value = v; }
	public String getValue() { return value; }

	@Override
	public String toString() {
		return "Message:" + value;
	}
}
