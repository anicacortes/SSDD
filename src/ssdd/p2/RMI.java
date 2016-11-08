/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: RMI.java
* TIEMPO: 0.5h
* DESCRIPCIÓN: Clase principal que recoge los argumentos y ejecuta el servidor o cliente indicado.
*/
package ssdd.p2;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class RMI {

    public static void main (String[]args)throws RemoteException {
        String tipo;
        //Si no tiene IP, se pone por defecto
        String IP = "localhost";
        tipo = args[0];
        switch (tipo) {
            //Lanza WorkerServer
            case "-c":
                if(args.length == 2) {
                    IP = args[1];
                }
                WorkerServer ws = new WorkerServer(IP);
                ws.registrar();
                break;

            //Lanza WorkerFactoryServer
            case "-a":
                if(args.length == 2) {
                    IP = args[1];
                }

                WorkerFactoryServer wfs = new WorkerFactoryServer(IP);
                wfs.registrar();
                break;

            //Lanza cliente
            case "-u":
                int min,max,n;
                min = Integer.parseInt(args[1]);
                max = Integer.parseInt(args[2]);
                n = Integer.parseInt(args[3]);
                if(args.length == 5) {
                    IP = args[4];
                }
                Client c = new Client(min, max, n, IP);
                c.interaccion();
                break;

            //Opcion incorrecta
            default:
                System.out.println("Opción no soportada.");
                System.out.println("Servidor calculo: java -jar SSDDp2.jar -c [IP_registro]");
                System.out.println("Servidor asignación: java -jar SSDDp2.jar -a [IP_registro]");
                System.out.println("Cliente: java -jar SSDDp2.jar -u min max n [IP_registro]");
                break;
        }
    }
}
