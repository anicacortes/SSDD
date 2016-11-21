package ssdd.p3;

import ssdd.ms.Envelope;
import ssdd.ms.MessageSystem;
import ssdd.ms.MessageValue;

import java.io.FileNotFoundException;

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

		String mensaje7 = "7- Recuerdo que sopló la luna y era en pleno día" +
				"y entre aquellas nubes vislumbraste la estrella polar,	y algo más";

		String mensaje8 = "8- Madelmans haciendo slalom por tu cuello, " +
				"aire que se lleva tus misterios, hacia el Sur se van.";

		String mensaje9 = "9- Y sé que a veces piensas que estoy algo ido," +
				"pero nunca pierdo una sola oportunidad de admirar cómo ...";

		String mensaje10 = "10- Te deslizas como si fueras de viento " +
				" al contacto con mis dedos te desvanecieras.";
		String mensaje11 = "11- Si tu magia ya no me hace efecto," +
				"¿cómo voy a continuar?";
		String mensaje12 = "12- ¿cómo voy a continuar?";

		MessageSystem ms = new MessageSystem(idP, fichero, debug);
		ms.send(1, new MessageValue(mensaje7));
		ms.send(1, new MessageValue(mensaje8));
		ms.send(1, new MessageValue(mensaje9));
		ms.send(1, new MessageValue(mensaje10));
		ms.send(1, new MessageValue(mensaje11));
		ms.send(1, new MessageValue(mensaje12));

        for(int i=13; i<33; i++){
            ms.send(1, new MessageValue("mesaje "+ i));
        }

		Envelope e;
		e = ms.receive();
		((MessageValue)e.getPayload()).getValue();
		ms.send(1,new MessageValue("Soy proceso " + idP + " y me voy"));
		ms.stopMailbox(idP);
	}
}
