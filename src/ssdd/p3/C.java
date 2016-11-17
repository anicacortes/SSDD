package ssdd.p3;

import ssdd.ms.Envelope;
import ssdd.ms.MessageSystem;
import ssdd.ms.MessageValue;

import java.io.FileNotFoundException;

//import ssdd.ms.Envelope;
//import ssdd.ms.MessageSystem;

public class C {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean debug = true;
		String networkFile = "peers.txt";
		
		for (String arg : args) {
			if (arg.equals("-d"))
				debug = true;
			else
				networkFile = arg;
		}
		
		try {
			MessageSystem ms = new MessageSystem(3, networkFile, debug);
			int valor;
			Envelope e;
			e = ms.receive();
			valor = ((MessageValue)e.getPayload()).getValue();
			e = ms.receive();
			valor = ((MessageValue)e.getPayload()).getValue();
			System.out.println("El valor almacenado finalemente es " + valor);
			ms.stopMailbox();
		} catch (FileNotFoundException e) {
			System.err.println("El fichero " + networkFile + " no existe.");
		}
	}

}
