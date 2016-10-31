package ssdd.p2;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Worker extends Remote {
    // Devuelve un vector con los primos entre min y max.
    java.util.ArrayList<Integer> encuentraPrimos(int min, int max) throws RemoteException;
}

