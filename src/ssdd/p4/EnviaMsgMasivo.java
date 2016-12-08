/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: EnviaMsgMasivo.java
* TIEMPO: 0.5h
* DESCRIPCIÓN: Realiza la llamada el metodo sendMulticast para realizar en 5 envio desde
*           un thread
*/
package ssdd.p4;

import ssdd.ms.MessageValue;
import ssdd.ms.TotalOrderMulticast;

import java.util.Random;

public class EnviaMsgMasivo extends Thread{

    private TotalOrderMulticast t;
    private String s;
    private int pid;

    public EnviaMsgMasivo(TotalOrderMulticast tom, String p, int i){
        t = tom;
        s = p;
        pid = i;
    }

    public void run() {
        Random rnd;
        rnd = new Random((long) pid);

        for(int i=0; i<5; i++){
            try {
                Thread.sleep(rnd.nextInt(500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            s = "Proceso " + pid + " : mensaje " + i;
            t.sendMulticast(new MessageValue(s));
        }
    }
}
