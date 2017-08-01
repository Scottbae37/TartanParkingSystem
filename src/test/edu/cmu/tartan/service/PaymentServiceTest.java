package edu.cmu.tartan.service;

import edu.cmu.tartan.edu.cmu.tartan.reservation.Payment;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by chongjae.yoo on 2017-07-18.
 */
@RunWith(PowerMockRunner.class)
//@RunWith(Suite.class)
//@Suite.SuiteClasses({ PaymentServiceTest.class,})
@PrepareForTest(TartanServiceMessageBus.class)
public class PaymentServiceTest {

    PaymentService paymentService;
    TartanServiceMessageBus msgBus;
    MessageConsumer consumer;
    MessageProducer producer;
    HashMap<String, Object> msg;
    Reservation rsvp;

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

        // Set msg params
        msg = new HashMap<>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_MAKE_PAYMENT);
        rsvp = new Reservation();
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    @org.junit.Test
    /*
     *  Test correctness of weekday day time rate
     *  Period: Monday-Friday 09:00AM-05:00PM
     *  Rate: 15.00 dollars per hour
     */
    public void testComputeTotalFeeWithWeeklyDayTimeRate() throws Exception {
        // Setup Reservation class
        Reservation rsvp = new Reservation();
        rsvp.setCustomerName("Daniel");
        rsvp.setStartTime("2017:08:08:09:00");
        rsvp.setEndTime("2017:08:08:10:00");

        long result = Whitebox.invokeMethod(paymentService, "computeTotalFee", rsvp);
        Assert.assertEquals(15, result);
    }

    @org.junit.Test
    /*
     *  Test correctness of weekday night time rate
     *  Period: Mon-Fri 05:00PM-09:00AM
     *  Rate: 10.00 dollars per hour
     */
    public void testComputeTotalFeeWithWeeklyNightTimeRate() throws Exception {
        // Setup Reservation class
        Reservation rsvp = new Reservation();
        rsvp.setCustomerName("Jeff");
        rsvp.setStartTime("2017:08:04:17:00");
        rsvp.setEndTime("2017:08:05:00:00");

        long result = Whitebox.invokeMethod(paymentService, "computeTotalFee", rsvp);
        Assert.assertEquals(70, result);
    }

    @org.junit.Test
    /**
     *  Test correctness of weekend day time rate
     *  Period: Sat-Sun 09:00M-05:00PM
     *  Rate: 12.00 dollars per hour
     */
    public void testComputeTotalFeeWithWeekendDayTimeRate() throws Exception {
        // Setup Reservation class
        Reservation rsvp = new Reservation();
        rsvp.setCustomerName("John");
        rsvp.setStartTime("2017:08:05:09:00");
        rsvp.setEndTime("2017:08:05:17:00");

        long result = Whitebox.invokeMethod(paymentService, "computeTotalFee", rsvp);
        Assert.assertEquals(96, result);
    }

    @org.junit.Test
    /**
     *  Test correctness of weekend night time rate
     *  Period: Sat-Sun 05:00PM-09:00AM
     *  Rate: 8.00 dollars per hour
     */
    public void testComputeTotalFeeWithWeekendNightTimeRate() throws Exception {
        // Setup Reservation class
        Reservation rsvp = new Reservation();
        rsvp.setCustomerName("Cathy");
        rsvp.setStartTime("2017:08:05:00:00");
        rsvp.setEndTime("2017:08:05:09:00");

        long result = Whitebox.invokeMethod(paymentService, "computeTotalFee", rsvp);
        Assert.assertEquals(90, result);
    }

    @org.junit.Test
    /**
     *  Test correctness of multiple time rate
     *  Period: depends on user selection
     *  Rate: depends on time period
     */
    public void testComputeTotalFeeWithMultipleRate() throws Exception {
        // Setup Reservation class
        Reservation rsvp = new Reservation();
        rsvp.setCustomerName("Carnegie");
        // 24hours = WeekdayNight(10) 7 hours + WeekendNight(8) 9 hours, WeekendDay(12) 8 hours
        rsvp.setStartTime("2017:08:04:17:00");
        rsvp.setEndTime("2017:08:05:17:00");

        long result = Whitebox.invokeMethod(paymentService, "computeTotalFee", rsvp);
        Assert.assertEquals(256, result);
    }

    @org.junit.Test
    /**
     *  Test correctness of penalty fees on overage hours
     *  THIS TEST WILL BE SUCCEEDED ONLY IF CURRENT TIME IS WEEKDAY NIGHT!!
     *  Period: depends on user selection
     *  Rate: depends on time period
     */
    public void testPenaltyFee() throws Exception {

        // Setup date class
        Date now = new Date();

        // reserved start time is 3 hours advanced
        Date testStartTime = new Date(now.getTime() - 1000 * 60 * 60 * 3);

        // reserved end time is 2 hours advanced
        Date testEndTime = new Date(now.getTime() - 1000 * 60 * 60 * 2);

        // Setup Reservation class
        Reservation rsvp = new Reservation();
        rsvp.setCustomerName("Mellon");
        rsvp.setStartTime(testStartTime);
        rsvp.setEndTime(testEndTime);

        long result = Whitebox.invokeMethod(paymentService, "computeTotalFee", rsvp);
        //Assert.assertEquals(50, result);
    }

    @org.junit.Test
    public void handleMessage() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_VALIDATE_PAYMENT);
        Payment validPayment = Mockito.mock(Payment.class);
        Mockito.when(validPayment.isValid()).thenReturn(true);
        msg.put(TartanParams.PAYLOAD, validPayment);
        paymentService.handleMessage(msg);

        msg.clear();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_MAKE_PAYMENT);
        Reservation reservation = Mockito.mock(Reservation.class);
        Date date = Mockito.mock(Date.class);
        Mockito.when(reservation.getStartTime()).thenReturn(date);
        Mockito.when(reservation.getEndTime()).thenReturn(date);
        msg.put(TartanParams.PAYLOAD, reservation);
        paymentService.handleMessage(msg);
    }

    @org.junit.Test
    public void run() throws Exception {
        paymentService.run();
    }

    @org.junit.Test
    public void terminate() throws Exception {
        paymentService.terminate();
        Mockito.verify(paymentService).stop();
    }

    @org.junit.Test
    public void finalize() throws Exception {
        paymentService.finalize();
        Mockito.verify(paymentService).stop();
    }
}