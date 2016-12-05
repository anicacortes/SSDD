package ssdd.p4;

import ssdd.ms.MessageValue;
import ssdd.ms.TotalOrderMulticast;

public class EnviaMsgRunnable extends Thread{

    TotalOrderMulticast t;
    public EnviaMsgRunnable(TotalOrderMulticast tom){
        t = tom;
    }

    public void run(){
        for(int i=0; i<3 ; i++){
            t.sendMulticast(new MessageValue("B - Mensaje numero " + i));
        }

    }

}
