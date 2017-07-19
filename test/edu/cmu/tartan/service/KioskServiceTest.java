package edu.cmu.tartan.service;

import edu.cmu.tartan.TartanKioskWindow;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.swing.*;
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
    TartanKioskWindow window;
    MessageConsumer consumer;
    MessageProducer producer;

    @org.junit.Before
    public void setUp() throws Exception {
        msgBus = PowerMockito.mock(TartanServiceMessageBus.class);
        consumer = PowerMockito.mock(MessageConsumer.class);
        producer = PowerMockito.mock(MessageProducer.class);
        window = Mockito.mock(TartanKioskWindow.class);
        PowerMockito.when(msgBus.getConsumer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(consumer);
        PowerMockito.when(msgBus.getProducer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(producer);
        PowerMockito.mockStatic(TartanServiceMessageBus.class);
        PowerMockito.when(TartanServiceMessageBus.connect()).thenReturn(msgBus);
        kioskService = new KioskService();
        kioskService.setKiosk(window);
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    @org.junit.Test
    public void handleMessage() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_REDEEM_RSVP);
        Vector<Reservation> reservations = new Vector<Reservation>();
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservation.getIsPaid()).thenReturn(true);
        reservations.add(reservation);
        msg.put(TartanParams.PAYLOAD, reservations);
        kioskService.handleMessage(msg);
        Mockito.verify(window).redeemReservation(reservation);
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