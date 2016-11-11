package ssdd.ms;

import java.io.Serializable;

public class MessageValue implements Serializable {
	private static final long serialVersionUID = 1L;

	private int value;
	public MessageValue(int v) { value = v; }
	public int getValue() { return value; }
	public String toString() { return "Asignar valor " + value; }
}
