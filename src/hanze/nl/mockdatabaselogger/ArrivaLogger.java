package hanze.nl.mockdatabaselogger;

import com.thoughtworks.xstream.XStream;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ArrivaLogger {

    private String queueName = "ARRIVALOGGER";
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;

    public ArrivaLogger() {
        try {
            setupConnection();
        } catch (JMSException e) {
            System.out.println("Something went wrong setting up connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ArrivaLogger logger = new ArrivaLogger();
        try {
            logger.processMessages();
        } catch (JMSException e) {
            System.out.println("Something went wrong during processing messages: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setupConnection() throws JMSException {
        ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue(queueName);
        this.consumer = session.createConsumer(destination);
    }

    public void processMessages() throws JMSException {
        boolean newMessage = true;
        int aantalBerichten = 0, aantalETAs = 0;
        while (newMessage) {
            Message message = this.consumer.receive(2000);
            newMessage = false;
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                newMessage = true;
                Bericht bericht = convertMessage(textMessage);
                aantalBerichten++;
                aantalETAs += bericht.ETAs.size();
            } else {
                System.out.println("Received: " + message);
            }
        }
        this.closeConnection();
        System.out.println(aantalBerichten + " berichten met " + aantalETAs + " ETAs verwerkt.");
    }

    private Bericht convertMessage(TextMessage textMessage) throws JMSException {
        String text = textMessage.getText();
        XStream xstream = new XStream();
        xstream.alias("Bericht", Bericht.class);
        xstream.alias("ETA", ETA.class);
        return (Bericht) xstream.fromXML(text);
    }


    private void closeConnection() throws JMSException {
        consumer.close();
        session.close();
        connection.close();
    }
}
