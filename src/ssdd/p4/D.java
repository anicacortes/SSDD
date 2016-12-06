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

import ssdd.ms.Envelope;
import ssdd.ms.MessageSystem;
import ssdd.ms.MessageValue;
import ssdd.ms.TotalOrderMulticast;

import java.io.FileNotFoundException;

public class D {

	private  boolean debug;
	private  int idP;
	private  String fichero;

	public D(boolean debug, int idP, String fichero) {
		this.debug = debug;
		this.idP = idP;
		this.fichero = fichero;
	}

    /**
     * Realiza en envio y recepcion de mensajes
     */
	public void lanzarEjecucion() throws FileNotFoundException{

		Integer nMensajes = 0;
		MessageSystem ms = new MessageSystem(idP, fichero, debug);
		TotalOrderMulticast t = new TotalOrderMulticast(ms, idP);

		EnviaMsgRunnable envia = new EnviaMsgRunnable(t, "D - mensaje inicial");
		envia.start();
        int i=0;
		while(true) {
            if(i<5) {
                envia = new EnviaMsgRunnable(t, "D - mensaje "+i);
                envia.start();
                i++;
            }
			Envelope e = t.receiveMulticast();
			System.out.println(((MessageValue) e.getPayload()).getValue());
		}
	}
}