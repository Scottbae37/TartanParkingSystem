package edu.cmu.tartan.service;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Handle connections to a JMS message bus.
 *
 * Project: LG Exec Ed SDET Program
 * Copyright: 2017 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2016 - initial version
 */
public class TartanServiceMessageBus {

    public static final String TARTAN_TOPIC = "TARTAN_TOPIC";

    /** The open sessions for consumers. */
    private HashMap<Integer,Session> subscriptions;

    /** The open sessions for producers. */
    private HashMap<Integer,Session> publications;

    /**
     * The connection to the bus. Unlike sessions, connections can be shared
     * between threads
     */
    private Connection conn;

    /** This is the publish session. */
    private Session pubSession;

    /** This is the subscription session. */
    private Session subSession;

    private TartanServiceMessageBus() throws JMSException {
        // create the connection to the task bus
        final ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory(
                        ActiveMQConnection.DEFAULT_BROKER_URL);

        conn = connectionFactory.createConnection();

        subscriptions = new HashMap<Integer,Session>();
        publications = new HashMap<Integer,Session>();

        // this will block until activeMQ is up and running
        conn.start();

        pubSession = null;
        subSession = null;

    }

    /**
     * Create a message consumer in a new session.
     *
     * @param topicName the name of the topic to subscribe to
     * @return the newly created MessageConsumer
     * @throws JMSException the jMS exception
     */
    public MessageConsumer getConsumer(final String topicName) throws JMSException {


        subSession = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

        final Topic topic = subSession.createTopic(topicName);
        final MessageConsumer consumer = subSession.createConsumer(topic);

        // remember the session to tear it down later
        subscriptions.put(consumer.hashCode(),subSession);

        return consumer;
    }

    /**
     * Close a consumer.
     *
     * @param consumer the consumer to close
     */
    public void releaseConsumer(MessageConsumer consumer) {

        // close the publisher and subscriber
        try {
            // close the consumer
            if (consumer != null) {
                Session session = subscriptions.get(consumer.hashCode());

                consumer.close();
                if (session != null) {
                    session.close();
                }

                subscriptions.remove(consumer.hashCode());
                consumer = null;
            }
        } catch (final JMSException e) {
            e.printStackTrace();
        }

    }
    /**
     * Close a producer.
     *
     * @param producer the producer to close
     */
    public void releaseProducer(MessageProducer producer) {
        // close the publisher
        try {
            if (producer != null) {

                Session session = publications.get(producer.hashCode());

                synchronized(producer) {
                    producer.close();
                    if (session != null) {
                        session.close();
                    }
                }
                publications.remove(producer.hashCode());
                producer = null;
            }
        } catch (final JMSException e) {
            e.printStackTrace();
        }
    }

    public MessageProducer getProducer(final String topicName)
            throws JMSException {

        if (conn != null) {
            synchronized(conn) {
                pubSession =
                        conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            }
        }
        final Topic topic = pubSession.createTopic(topicName);

        // Create a MessageProducer from the Session to the Topic or Queue
        final MessageProducer producer = pubSession.createProducer(topic);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        // remember the session to tear it down later
        publications.put(producer.hashCode(),pubSession);

        return producer;
    }

    public synchronized ObjectMessage generateMessage(
            Serializable messageBody,
            String targetService) {

        try {
            ObjectMessage message = pubSession.createObjectMessage();

            if (targetService!= null)
                message.setStringProperty(TartanParams.SERVICE_ID, targetService);

            message.setObject(messageBody);
            return message;

        } catch (final JMSException e) { }

        // On failure simply return null
        return null;
    }

    /**
     * Create a new connection.
     *
     * @return the new connection or null on error.
     */
    public synchronized static TartanServiceMessageBus connect() {
        try {
            return new TartanServiceMessageBus();
        } catch (JMSException e) {
            return null;
        }
    }
    /**
     * Explicitly disconnect from the bus. Failing to close the connection
     * results in strange behavior in the broker and possibly gingivitis
     */
    public synchronized void disconnect() {
        try {
            if (conn != null) {
                conn.close();
                conn = null;

            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
