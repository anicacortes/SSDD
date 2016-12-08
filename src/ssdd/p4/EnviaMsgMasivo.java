/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: EnviaMsgRunnableC.java
* TIEMPO: 0.5h
* DESCRIPCIÓN: Realiza la llamada el metodo sendMulticast para realizar en envio desde
*           un thread
*/
package ssdd.p4;

import ssdd.ms.MessageValue;
import ssdd.ms.TotalOrderMulticast;

public class EnviaMsgMasivo extends Thread{

    private TotalOrderMulticast t;
    private String s;
    public EnviaMsgMasivo(TotalOrderMulticast tom, String p){
        t = tom;
        s = p;
    }

    public void run() {
        t.sendMulticast(new MessageValue(s));
    }
}
