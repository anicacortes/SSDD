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

        MessageSystem ms = new MessageSystem(idP, fichero, debug);
        String valor;
        Envelope e;
        for(int i=0; i<12; i++){
            e = ms.receive();
            valor = ((MessageValue)e.getPayload()).getValue();
            System.out.println(valor);
        }
        ms.send(2, new MessageValue("Finalizo interaccion"));
        ms.send(3, new MessageValue("Finalizo interaccion"));

        e = ms.receive();
        ((MessageValue)e.getPayload()).getValue();
        e = ms.receive();
        ((MessageValue)e.getPayload()).getValue();
        ms.stopMailbox(idP);
    }

}
