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

import ssdd.ms.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;


public class A {

    private boolean debug;
    private int idP;
    private String fichero;
    private ChatDialog v;

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
        TotalOrderMulticast t = new TotalOrderMulticast(ms, idP);
        String m;
        while(true) {
            Envelope e = t.receiveMulticast();
            if(!(e.getPayload() instanceof REQ)){
                m = ((MessageValue) e.getPayload()).getValue();
                System.out.println(m + " reloj: "+ms.getLamportClock());
            }
        }
    }

}
