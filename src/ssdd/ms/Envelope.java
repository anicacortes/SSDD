package ssdd.ms;

import java.io.Serializable;

public class Envelope implements Serializable {
	private static final long serialVersionUID = 1L;
	private int source;
	private int destination;
	private Serializable payload;

	public Envelope(int s, int d, Serializable p) {
		source = s;
		destination = d;
		payload = p;
	}
	
	public int getSource() { return source; }

	public int getDestination() { return destination; }
	public Serializable getPayload() { return payload; }

	@Override
	public String toString() {
		return "Envelope{" +
				"source=" + source +
				", destination=" + destination +
				", payload=" + payload;
	}
}
