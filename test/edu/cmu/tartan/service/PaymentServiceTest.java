package edu.cmu.tartan.service;

import edu.cmu.tartan.edu.cmu.tartan.reservation.Payment;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import java.util.Date;
import java.util.HashMap;


/**
 * Created by chongjae.yoo on 2017-07-18.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TartanServiceMessageBus.class)
public class PaymentServiceTest {

    PaymentService paymentService;
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

        paymentService = Mockito.spy(new PaymentService());
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    @org.junit.Test
    public void run() throws Exception {
        paymentService.run();
        Mockito.verify(paymentService).run();
    }

    @org.junit.Test
    public void handleMessage() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();

        // 1. MSG_VALIDATE_PAYMENT
        msg.put(TartanParams.COMMAND, TartanParams.MSG_VALIDATE_PAYMENT);

        // valid case
        Payment validPayment = Mockito.mock(Payment.class);
        Mockito.when(validPayment.isValid()).thenReturn(true);
        msg.put(TartanParams.PAYLOAD, validPayment);

        // execute
        paymentService.handleMessage(msg);

        msg.clear();

        // invalid case
        msg.put(TartanParams.COMMAND, TartanParams.MSG_VALIDATE_PAYMENT);
        Payment invalidPayment = Mockito.mock(Payment.class);
        Mockito.when(validPayment.isValid()).thenReturn(false);
        msg.put(TartanParams.PAYLOAD, invalidPayment);

        // execute
        paymentService.handleMessage(msg);

        // verify

        // MSG_MAKE_PAYMENT
        msg.clear();

        msg.put(TartanParams.COMMAND, TartanParams.MSG_MAKE_PAYMENT);
        Reservation reservation = Mockito.mock(Reservation.class);
        Date date = Mockito.mock(Date.class);
        Mockito.when(reservation.getStartTime()).thenReturn(date);
        Mockito.when(reservation.getEndTime()).thenReturn(date);
        msg.put(TartanParams.PAYLOAD, reservation);

        // execute
        paymentService.handleMessage(msg);

        // verify
     }

    @org.junit.Test
    public void finalize() throws Exception {
        paymentService.terminate();
        Mockito.verify(paymentService).stop();
    }

    @org.junit.Test
    public void terminate() throws Exception {
        paymentService.terminate();
        Mockito.verify(paymentService).stop();
    }

}