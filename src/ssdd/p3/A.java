package ssdd.p3;

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


    public void lanzarEjecucion() throws FileNotFoundException{

            MessageSystem ms = new MessageSystem(idP, fichero, debug);
            String valor;
            Envelope e;
            e = ms.receive();
            valor = ((MessageValue)e.getPayload()).getValue();
            System.out.println("Primer valor recibido:" + valor);
            e = ms.receive();
            valor = ((MessageValue)e.getPayload()).getValue();
            System.out.println("Segundo valor recibido " + valor);

            ms.send(2, new MessageValue("Finalizo interaccion"));
            ms.receive();
            valor = ((MessageValue)e.getPayload()).getValue();
            System.out.println("Me envian mensaje de finalizacion 1: " + valor);
            ms.receive();
            valor = ((MessageValue)e.getPayload()).getValue();
            System.out.println("Me envian mensaje de finalizacion 2: " + valor);
            ms.stopMailbox();

    }

}
