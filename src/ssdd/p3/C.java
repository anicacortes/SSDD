package ssdd.p3;

import ssdd.ms.Envelope;
import ssdd.ms.MessageSystem;
import ssdd.ms.MessageValue;

import java.io.FileNotFoundException;

//import ssdd.ms.Envelope;
//import ssdd.ms.MessageSystem;

public class C {

	private  boolean debug;
	private  int idP;
	private  String fichero;

	public C(boolean debug, int idP, String fichero) {
		this.debug = debug;
		this.idP = idP;
		this.fichero = fichero;
	}

	public void lanzarEjecucion() throws FileNotFoundException{

            String mensaje3 = "3-Si tu magia ya no me hace efecto, \n" +
                    "¿cómo voy a continuar?";
            String mensaje4= "4-Si me sueltas entre tanto viento, \n" +
                    "¿cómo voy a continuar?. ";
			MessageSystem ms = new MessageSystem(idP, fichero, debug);
            ms.send(1, new MessageValue(mensaje3));
            ms.send(1, new MessageValue(mensaje4));

            String valor;
            Envelope e;
            e = ms.receive();
            valor = ((MessageValue)e.getPayload()).getValue();
            //System.out.println("Soy proceso " + idP + " y recibo: " + valor);
            ms.send(1,new MessageValue("Soy proceso " + idP + " y me voy"));
            ms.stopMailbox();
	}

}
