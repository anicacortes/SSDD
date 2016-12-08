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


public class EnvioMasivo {

    private boolean debug;
    private int idP;
    private String fichero;
    private ChatDialog v;

    public EnvioMasivo(boolean debug, int idP, String fichero) {
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
               //
            }
        }, idP);
        v.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            if(idP==1){
                Thread.sleep(1000);
            }else if(idP==2){
                Thread.sleep(750);
            }else if(idP==3){
                Thread.sleep(500);
            }else{
                Thread.sleep(250);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int nMensaje=0;
        while(true) {
            if(nMensaje<5) {
                m = "Proceso " + idP + " : mensaje " + nMensaje;
                EnviaMsgMasivo enviaC = new EnviaMsgMasivo(t, m, idP);  //envio desde thread
                enviaC.start();
                //v.addMessage("Yo: " + m);
                nMensaje++;
            }
            Envelope e = t.receiveMulticast();
            //añado mensaje si no es res, ack ni es mio
            if(!(e.getPayload() instanceof REQ) && !(e.getPayload() instanceof ACK)){
                m = ((MessageValue) e.getPayload()).getValue();
                if(e.getSource()==idP){
                    v.addMessage("Yo: " + m);
                }else{
                    v.addMessage(e.getSource() +": "+m);
                }
            }
        }
    }

}
