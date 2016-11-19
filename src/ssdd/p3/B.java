package ssdd.p3;

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

	public void lanzarEjecucion() throws FileNotFoundException{

            String mensaje1 = "1-Y ya lo sé, otra vez ha sucedido, \n" +
                    "volaron los manteles y el domingo se hizo especial.";
            String mensaje2 = "2-Flotaba en azoteas todo mi deseo, \n" +
                    "un solecito bueno y tus faldas al viento, \n" +
                    "nada más.";
			MessageSystem ms = new MessageSystem(idP, fichero, debug);
			ms.send(1, new MessageValue(mensaje1));
            ms.send(1, new MessageValue(mensaje2));

            String valor;
            Envelope e;
            e = ms.receive();
            valor = ((MessageValue)e.getPayload()).getValue();
            //System.out.println("Soy proceso " + idP + " y recibo: " + valor);
            ms.send(1,new MessageValue("Soy proceso " + idP + " y me voy"));
			ms.stopMailbox();
	}

}
