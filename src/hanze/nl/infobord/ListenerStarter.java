package hanze.nl.infobord;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

public class ListenerStarter implements Runnable, ExceptionListener {

    public ListenerStarter(String selector) {

    }

    @Override
    public void run() {

    }

    @Override
    public void onException(JMSException exception) {

    }
    //TODO	Implementeer de starter voor de messagelistener:
//	Zet de verbinding op met de messagebroker en start de listener met 
//	de juiste selector.

}