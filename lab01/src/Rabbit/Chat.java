package Rabbit;

public class Chat {
    static public void main(String[] args) throws Exception {
        new Thread(new ReceiveMsgs()).start();
        new Thread(new SendMsgs()).start();
    }
}
