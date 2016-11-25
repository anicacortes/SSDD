/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: Envelope.java
* TIEMPO: 0h
* DESCRIPCIÓN: Objeto serializable que se envia entre procesos.
*/
package ssdd.ms;

import java.io.Serializable;

public class Envelope implements Serializable {
	private static final long serialVersionUID = 1L;
	private int source;
	private int destination;
	private Serializable payload;
	private int lamportClock;

	public Envelope(int s, int d, Serializable p, int c) {
		source = s;
		destination = d;
		payload = p;
		lamportClock = c;
	}
	
	public int getSource() { return source; }

	public int getDestination() { return destination; }
	public Serializable getPayload() { return payload; }

    public int getLamportClock() {
        return lamportClock;
    }

    @Override
	public String toString() {
		return "Envelope{" +
				"source=" + source +
				", destination=" + destination +
				", payload=" + payload +
                ", lamportClock=" + lamportClock ;
	}
}
