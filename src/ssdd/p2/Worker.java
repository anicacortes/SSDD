/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: Worker.java
* TIEMPO: 0.05h
* DESCRIPCIÓN: Interfaz que ofrece al cliente el método que obtiene los primos en el intervalor.
*/
package ssdd.p2;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Worker extends Remote {
    /**
     * Devuelve los primos que se encuentran en el intervalo min-max
     */
    java.util.ArrayList<Integer> encuentraPrimos(int min, int max) throws RemoteException;
}

