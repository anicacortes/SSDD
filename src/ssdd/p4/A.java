/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: A.java
* TIEMPO: 0.5h
* DESCRIPCIÓN: Proceso que realiza una interaccion de mensajes con los procesos B y c.
*/
package ssdd.p4;

import ssdd.ms.Envelope;
import ssdd.ms.MessageSystem;
import ssdd.ms.MessageValue;
import ssdd.ms.TotalOrderMulticast;

import java.io.FileNotFoundException;


public class A {

    private  boolean debug;
    private  int idP;
    private  String fichero;

    public A(boolean debug, int idP, String fichero) {
        this.debug = debug;
        this.idP = idP;
        this.fichero = fichero;
    }

    /**
     * Realiza en envio y recepcion de mensajes
     */
    public void lanzarEjecucion() throws FileNotFoundException {

        int nMensajes = 0;
        MessageSystem ms = new MessageSystem(idP, fichero, debug);
        TotalOrderMulticast t = new TotalOrderMulticast(ms, idP);

        while(true) {
            Envelope e = t.receiveMulticast();
            System.out.println(((MessageValue) e.getPayload()).getValue());
        }
    }

}
