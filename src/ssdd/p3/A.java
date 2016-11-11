package ssdd.p3;

import java.io.FileNotFoundException;

//import ssdd.ms.MessageSystem;

public class A {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean debug = false;
		String networkFile = "peers.txt";
		
		for (String arg : args) {
			if (arg.equals("-d"))
				debug = true;
			else
				networkFile = arg;
		}
		
		try {
			MessageSystem ms = new MessageSystem(1, networkFile, debug);
			ms.send(3, new MessageValue(1));
			ms.stopMailbox();
		} catch (FileNotFoundException e) {
			System.err.println("El fichero " + networkFile + " no existe.");
		}
	}

}
