package edu.cmu.tartan.service;

import org.apache.activemq.broker.Connection;

import javax.jms.*;
import java.util.HashMap;


/**
 * The base class for Tartan services. This class provides the means to connect to the message bus. This class is abstract
 * and must be extended by a concrete service.
 *
 * Project: LG Exec Ed SDET Program
 * Copyright: 2017 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2016 - initial version
 */
public abstract class TartanService implements MessageListener, Runnable {

    /**
     * Service states.
     */
    public enum TartanServiceStatus {
        RUNNING,
        STOPPED,
        ERROR;
    }

    /** The current service status */
    protected TartanServiceStatus status;

    private HashMap<Integer,HashMap<String,Object> > requests = new HashMap<Integer,HashMap<String,Object> >();

    /** The output connection to the  bus. */
    protected MessageProducer outChannel;

    /** The input connection to the task bus. */
    protected MessageConsumer inChannel;

    private TartanServiceMessageBus bus;

    /** The unique ID for this service */
    private String serviceId;

    /**
     * Send a directed message to the service message bus.
     *
     * @param targetService the target of the message
     * @param messageBody The message to send
     *
     */
    public synchronized void sendMessage(String targetService, HashMap<String, Object> messageBody) {

        messageBody.put(TartanParams.SOURCE_ID, serviceId);
        ObjectMessage msg = bus.generateMessage(messageBody, targetService);
        try {
            outChannel.send(msg);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unpackage incoming messages/requests for processing
     *
     * @param message The message to handle.
     */
    @Override
    public void onMessage(Message message) {
        try {
            final ObjectMessage om = (ObjectMessage) message;
            final HashMap<String,Object> m = (HashMap<String,Object>) om.getObject();

            String id = om.getStringProperty(TartanParams.SERVICE_ID);
            if (id.equals(serviceId)) {

                // This message is meant for this service so handle it by deferring to the actual service.
                handleMessage(m);
            }

        } catch (final JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize the service.
     *
     * @param sid The service ID.
     */
    public void init(String sid) {

        bus = TartanServiceMessageBus.connect();
        try {
            inChannel = bus.getConsumer(TartanServiceMessageBus.TARTAN_TOPIC);
            inChannel.setMessageListener(this);

            outChannel = bus.getProducer(TartanServiceMessageBus.TARTAN_TOPIC);

            this.serviceId = sid;

        } catch (final JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop the service
     */
    public void stop() {
        bus.disconnect();
    }

    /**
     * Concrete services must override this message to process incoming messages.
     *
     * @param message The incomint message.
     */
    public abstract void handleMessage(HashMap<String, Object> message);

    /**
     * Concrete services must override this method to gracefully terminate.
     */
    public abstract void terminate();

}
