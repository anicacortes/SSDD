package ssdd.p4;

import ssdd.ms.MessageValue;
import ssdd.ms.TotalOrderMulticast;

public class EnviaMsgRunnable extends Thread{

    TotalOrderMulticast t;
    String s;
    public EnviaMsgRunnable(TotalOrderMulticast tom,String msg){
        t = tom;
        s = msg;
    }

    public void run(){
        t.sendMulticast(new MessageValue(s));

    }

}