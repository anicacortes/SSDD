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

        String m;
        MessageSystem ms = new MessageSystem(idP, fichero, debug);
        final TotalOrderMulticast t = new TotalOrderMulticast(ms, idP);

        v = new ChatDialog(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String m = v.text();    //coge texto del campo
                if(!m.equals("")){
                    EnviaMsgRunnableC enviaC = new EnviaMsgRunnableC(t,m);  //envio desde thread
                    enviaC.start();
                    if (!m.isEmpty()) {
                        v.addMessage("Yo: " + m);
                    }
                }
            }
        }, idP);
        v.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        while(true) {
            Envelope e = t.receiveMulticast();
            //añado mensaje si no es res, ack ni es mio
            if(!(e.getPayload() instanceof REQ) && !(e.getPayload() instanceof ACK) && idP!=e.getSource()){
                m = ((MessageValue) e.getPayload()).getValue();
                v.addMessage(e.getSource() +": "+m);
            }
        }
    }

}
