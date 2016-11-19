package ssdd.p3;

import ssdd.ms.Envelope;
import ssdd.ms.MessageSystem;
import ssdd.ms.MessageValue;
import sun.util.resources.cldr.as.LocaleNames_as;

import java.io.FileNotFoundException;

//import ssdd.ms.MessageSystem;

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

            String mensaje = "Y ya lo sé, otra vez ha sucedido, \n" +
                    "volaron los manteles y el domingo se hizo especial. \n" +
                    "Flotaba en azoteas todo mi deseo, \n" +
                    "un solecito bueno y tus faldas al viento, \n" +
                    "nada más...";
			MessageSystem ms = new MessageSystem(idP, fichero, debug);
			ms.send(1, new MessageValue(mensaje));

            String valor;
            Envelope e;
            e = ms.receive();
            valor = ((MessageValue)e.getPayload()).getValue();
            ms.send(3,"Cerrar buzon");
			ms.stopMailbox();
	}

}
