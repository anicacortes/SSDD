/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: B.java
* TIEMPO: 0.5h
* DESCRIPCIÓN: Proceso que realiza una interaccion de mensajes con el proceso A.
*/
package ssdd.p4;

import ssdd.ms.Envelope;
import ssdd.ms.MessageSystem;
import ssdd.ms.MessageValue;

import java.io.FileNotFoundException;

public class B {

    private  boolean debug;
    private  int idP;
    private  String fichero;

    public B(boolean debug, int idP, String fichero) {
        this.debug = debug;
        this.idP = idP;
        this.fichero = fichero;
    }

    /**
     * Realiza en envio y recepcion de mensajes
     */
    public void lanzarEjecucion() throws FileNotFoundException{

        String mensaje1 = "1-Y ya lo sé, otra vez ha sucedido, \n" +
                "volaron los manteles y el domingo se hizo especial.";
        String mensaje2 = "2-Flotaba en azoteas todo mi deseo, \n" +
                "un solecito bueno y tus faldas al viento, \n" +
                "nada más.";
        String mensaje3 = "3- Aeroplanos que saludo moviendo un espejo," +
                "la ropa y tu pelo se movían al mismo compás nada más.";
        String mensaje4 = "4- Te deslizas como si fueras de viento" +
                "y al contacto con mis dedos te desvanecieras.";
        String mensaje5 = "5-Si tu magia ya no me hace efecto, \n" +
                "¿cómo voy a continuar?";
        String mensaje6= "6-Si me sueltas entre tanto viento, \n" +
                "¿cómo voy a continuar?. ";

        MessageSystem ms = new MessageSystem(idP, fichero, debug);
        ms.send(1, new MessageValue(mensaje1));
        ms.send(1, new MessageValue(mensaje2));
        ms.send(1, new MessageValue(mensaje3));
        ms.send(1, new MessageValue(mensaje4));
        ms.send(1, new MessageValue(mensaje5));
        ms.send(1, new MessageValue(mensaje6));

        Envelope e;
        e = ms.receive();
        ((MessageValue)e.getPayload()).getValue();
        ms.send(1,new MessageValue("Soy proceso " + idP + " y me voy"));
        ms.stopMailbox(idP);
    }

}
