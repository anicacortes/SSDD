/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: WorkerFactory.java
* TIEMPO: 0.05h
* DESCRIPCIÓN: Interfaz que ofrece al cliente el método que obtiene los n servidores de calculo.
*/
package ssdd.p2;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface WorkerFactory extends Remote {
    /**
     * Devuelve n servidores de calculo si estan en ejecucion.
     * En caso contrario devuelve null y un mensaje que indica que no hay tantos
     * servidores en ejecución
     */
    ArrayList<Worker> dameWorkers(int n) throws RemoteException;
}
