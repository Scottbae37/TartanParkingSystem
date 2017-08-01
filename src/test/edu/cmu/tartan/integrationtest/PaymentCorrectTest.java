package edu.cmu.tartan.integrationtest;


import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import edu.cmu.tartan.service.PaymentService;
import edu.cmu.tartan.service.TartanServiceMessageBus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import java.util.HashMap;

import static org.junit.Assert.*;
/**
 * Created by kyungman.yu on 2017-08-01.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TartanServiceMessageBus.class)
public class PaymentCorrectTest {


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
    @org.junit.Test
    public void paymentCorrectTest_weekdayToweekend() throws Exception{
        Reservation rsvp = new Reservation();
        rsvp.setCustomerName("somebody");
        rsvp.setVehicleID("abcd1234");
        rsvp.setStartTime("2017:08:04:16:00");
        rsvp.setEndTime("2017:08:05:16:00");

        long result = Whitebox.invokeMethod(paymentService, "computeTotalFee", rsvp);
        Assert.assertEquals(259, result);
    }

    @org.junit.Test
    public void paymentCorrectTest_weekendToweekday() throws Exception{
        Reservation rsvp = new Reservation();
        rsvp.setCustomerName("somebody");
        rsvp.setVehicleID("abcd1234");
        rsvp.setStartTime("2017:08:06:16:00");
        rsvp.setEndTime("2017:08:07:16:00");

        long result = Whitebox.invokeMethod(paymentService, "computeTotalFee", rsvp);
        Assert.assertEquals(245, result);
    }

    @org.junit.Test
    public void paymentCorrectTest_weekday_daytonight() throws Exception{
        Reservation rsvp = new Reservation();
        rsvp.setCustomerName("somebody");
        rsvp.setVehicleID("abcd1234");
        rsvp.setStartTime("2017:08:07:16:00");
        rsvp.setEndTime("2017:08:07:17:00");

        long result = Whitebox.invokeMethod(paymentService, "computeTotalFee", rsvp);
        Assert.assertEquals(15, result);
    }

    @org.junit.Test
    public void paymentCorrectTest_weekday_nightToday() throws Exception{
        Reservation rsvp = new Reservation();
        rsvp.setCustomerName("somebody");
        rsvp.setVehicleID("abcd1234");
        rsvp.setStartTime("2017:08:07:17:00");
        rsvp.setEndTime("2017:08:07:18:00");

        long result = Whitebox.invokeMethod(paymentService, "computeTotalFee", rsvp);
        Assert.assertEquals(10, result);
    }




}
