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

            String mensaje = "Si tu magia ya no me hace efecto, \n" +
                    "¿cómo voy a continuar? \n" +
                    "Si me sueltas entre tanto viento, \n" +
                    "¿cómo voy a continuar?, \n" +
                    "¿cómo voy a continuar?";
			MessageSystem ms = new MessageSystem(idP, fichero, debug);
            ms.send(1, new MessageValue(mensaje));

            String valor;
            Envelope e;
            e = ms.receive();
            valor = ((MessageValue)e.getPayload()).getValue();
            ms.send(1,"Cerrar buzon");
            ms.stopMailbox();
	}

}
