/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: C.java
* TIEMPO: 0.5h
* DESCRIPCIÓN: Proceso que realiza una interaccion de mensajes con el proceso A.
*/
package ssdd.p4;

import ssdd.ms.*;

import java.io.FileNotFoundException;

public class C {

	private  boolean debug;
	private  int idP;
	private  String fichero;

	public C(boolean debug, int idP, String fichero) {
		this.debug = debug;
		this.idP = idP;
		this.fichero = fichero;
	}

    /**
     * Realiza en envio y recepcion de mensajes
     */
	public void lanzarEjecucion() throws FileNotFoundException{
		MessageSystem ms = new MessageSystem(idP, fichero, debug);
		TotalOrderMulticast t = new TotalOrderMulticast(ms, idP);
		String m;

        EnviaMsgRunnableC enviaC = new EnviaMsgRunnableC(t);
		enviaC.start();
		while(true) {
			Envelope e = t.receiveMulticast();
            if(!(e.getPayload() instanceof REQ) && !(e.getPayload() instanceof ACK)){
				m = ((MessageValue) e.getPayload()).getValue();
				System.out.println(m + " reloj: "+ms.getLamportClock());
            }
		}
	}
}
