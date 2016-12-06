package ssdd.p4;

import ssdd.ms.MessageValue;
import ssdd.ms.TotalOrderMulticast;

public class EnviaMsgRunnableC extends Thread{

    private TotalOrderMulticast t;
    private String s;
    public EnviaMsgRunnableC(TotalOrderMulticast tom, String m){
        s = m;
        t = tom;
    }

    public void run() {
        t.sendMulticast(new MessageValue(s));
    }
}
