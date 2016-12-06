package ssdd.p4;

import ssdd.ms.MessageValue;
import ssdd.ms.TotalOrderMulticast;

public class EnviaMsgRunnableC extends Thread{

    TotalOrderMulticast t;
    String s;
    public EnviaMsgRunnableC(TotalOrderMulticast tom, String msg){
        t = tom;
        s = msg;
    }

    public void run(){
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t.sendMulticast(new MessageValue(s));

    }

}
