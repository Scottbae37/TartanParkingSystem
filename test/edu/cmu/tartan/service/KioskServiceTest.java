package edu.cmu.tartan.service;

import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by chongjae.yoo on 2017-07-18.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(TartanServiceMessageBus.class)
public class KioskServiceTest {

    KioskService kioskService;
    TartanServiceMessageBus msgBus;
    MessageConsumer consumer;
    MessageProducer producer;

    @org.junit.Before
    public void setUp() throws Exception {
        msgBus = PowerMockito.mock(TartanServiceMessageBus.class);
        consumer = PowerMockito.mock(MessageConsumer.class);
        producer = PowerMockito.mock(MessageProducer.class);
        PowerMockito.when(msgBus.getConsumer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(consumer);
        PowerMockito.when(msgBus.getProducer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(producer);
        PowerMockito.mockStatic(TartanServiceMessageBus.class);
        PowerMockito.when(TartanServiceMessageBus.connect()).thenReturn(msgBus);
        kioskService = new KioskService();
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    @org.junit.Test
    public void handleMessage() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_REDEEM_RSVP);
        msg.put(TartanParams.PAYLOAD, new Vector<Reservation>());
        kioskService.handleMessage(msg);
    }

    @org.junit.Test
    public void sendPaymentInfo() throws Exception {
    }

    @org.junit.Test
    public void makeNewReservation() throws Exception {
    }

    @org.junit.Test
    public void run() throws Exception {
    }

    @org.junit.Test
    public void finalize() throws Exception {
    }

    @org.junit.Test
    public void getReservation() throws Exception {
    }

    @org.junit.Test
    public void setKiosk() throws Exception {
    }

    @org.junit.Test
    public void terminate() throws Exception {
    }

}