package ssdd.p4;

import ssdd.ms.MessageValue;
import ssdd.ms.TotalOrderMulticast;

public class EnviaMsgRunnableD extends Thread{

    TotalOrderMulticast t;
    public EnviaMsgRunnableD(TotalOrderMulticast tom){
        t = tom;
    }

    public void run(){
        for (int i = 0; i < 5; i++) {
            t.sendMulticast(new MessageValue("D - mensaje " + i));
        }
    }

}
