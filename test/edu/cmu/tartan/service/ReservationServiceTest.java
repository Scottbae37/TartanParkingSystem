package edu.cmu.tartan.service;

import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by chongjae.yoo on 2017-07-18.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TartanServiceMessageBus.class)
public class ReservationServiceTest {

    TartanServiceMessageBus msgBus;
    MessageConsumer consumer;
    MessageProducer producer;
    ReservationService reservationService;

    @org.junit.Before
    public void setUp() throws Exception {
        msgBus = PowerMockito.mock(TartanServiceMessageBus.class);
        consumer = PowerMockito.mock(MessageConsumer.class);
        producer = PowerMockito.mock(MessageProducer.class);
        PowerMockito.when(msgBus.getConsumer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(consumer);
        PowerMockito.when(msgBus.getProducer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(producer);
        PowerMockito.mockStatic(TartanServiceMessageBus.class);
        PowerMockito.when(TartanServiceMessageBus.connect()).thenReturn(msgBus);

        reservationService = Mockito.spy(new ReservationService("./"));
        run();
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    public void run() throws Exception {
        PrintWriter writer = new PrintWriter("rsvp.txt", "UTF-8");
        writer.close();
        reservationService.run();
        Mockito.verify(reservationService).sendMessage(Mockito.eq(ParkingService.PARKING_SERVICE), Mockito.any(HashMap.class));
    }

    @org.junit.Test
    public void newReservationHandleMeg() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservation.getCustomerName()).thenReturn("");
        Mockito.when(reservation.getVehicleID()).thenReturn("");
        Date normalStartDate = Calendar.getInstance().getTime();
        Date normalEndDate = new Date();
        normalStartDate.setTime(normalStartDate.getTime() + 1000 * 60 * 60);
        normalEndDate.setTime(normalStartDate.getTime() + 1000 * 60 * 60);
        Mockito.when(reservation.getStartTime()).thenReturn(normalStartDate);
        Mockito.when(reservation.getEndTime()).thenReturn(normalEndDate);
        msg.put(TartanParams.PAYLOAD, reservation);
        msg.put(TartanParams.COMMAND, TartanParams.MSG_NEW_RSVP);

        reservationService.handleMessage(msg);

    }

    public void verifyReservation() throws Exception {
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservation.getCustomerName()).thenReturn("");
        Mockito.when(reservation.getVehicleID()).thenReturn("");
        Date normalStartDate = Calendar.getInstance().getTime();
        Date normalEndDate = new Date();
        normalStartDate.setTime(normalStartDate.getTime() + 1000 * 60 * 60);
        normalEndDate.setTime(normalStartDate.getTime() + 1000 * 60 * 60);
        Object ret;

        //Normal Case. CurrentTime + 1 Hour ~ CurrentTime + 2 Hour
        Mockito.when(reservation.getStartTime()).thenReturn(normalStartDate);
        Mockito.when(reservation.getEndTime()).thenReturn(normalEndDate);
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertTrue((Boolean) ret);

        //endTime <= startTime
        setDate(reservation, "Wed, Jul 26 11 AM 2017", "Wed, Jul 26 11 AM 2017");
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertFalse((Boolean) ret);
        setDate(reservation, "Wed, Jul 26 1 PM 2017", "Wed, Jul 26 11 AM 2017");
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertFalse((Boolean) ret);


        //endTime - startTime > 24 hours
        setDate(reservation, "Wed, Jul 26 10 AM 2017", "Thu, Jul 27 11 AM 2017");
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertFalse((Boolean) ret);


        //Current time > startTime
        setDate(reservation, "Wed, Jul 26 1 AM 2017", "Wed, Jul 26 11 AM 2017");
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertFalse((Boolean) ret);


        //Startime more than a week out
        Date moreThanOneWeekStart = Calendar.getInstance().getTime();
        Date moreThanOneWeekEnd = Calendar.getInstance().getTime();
        moreThanOneWeekStart.setTime(moreThanOneWeekStart.getTime() + 604900000);
        moreThanOneWeekEnd.setTime(moreThanOneWeekStart.getTime() + 1000 * 60 * 60);
        Mockito.when(reservation.getStartTime()).thenReturn(moreThanOneWeekStart);
        Mockito.when(reservation.getEndTime()).thenReturn(moreThanOneWeekEnd);
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertFalse((Boolean) ret);


        //Customer Name is null
        Mockito.when(reservation.getCustomerName()).thenReturn(null);
        Mockito.when(reservation.getStartTime()).thenReturn(normalStartDate);
        Mockito.when(reservation.getEndTime()).thenReturn(normalEndDate);
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertFalse((Boolean) ret);


        //VehicleId is null
        Mockito.when(reservation.getCustomerName()).thenReturn("");
        Mockito.when(reservation.getVehicleID()).thenReturn(null);
        Mockito.when(reservation.getStartTime()).thenReturn(normalStartDate);
        Mockito.when(reservation.getEndTime()).thenReturn(normalEndDate);
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertFalse((Boolean) ret);
    }

    private void setDate(Reservation reservation, String start, String end) throws Exception {
        SimpleDateFormat parser = new SimpleDateFormat("EEE, MMM dd hh a yyyy");
        Date startDate = parser.parse(start);
        Date endDate = parser.parse(end);
        Mockito.when(reservation.getStartTime()).thenReturn(startDate);
        Mockito.when(reservation.getEndTime()).thenReturn(endDate);
    }

    @org.junit.Test
    public void confirmReservationHandleMeg() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_CONFIRM_RSVP);
    }

    @org.junit.Test
    public void updateReservationHandleMeg() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_UPDATE_RSVP);
    }

    @org.junit.Test
    public void getParkingSpotHandleMeg() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_GET_PARKING_SPOTS);
    }

    @org.junit.Test
    public void redeemHandleMeg() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_REDEEM_RSVP);
    }

    @org.junit.Test
    public void getAllReservationHandleMeg() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_GET_ALL_RSVP);
    }

    @org.junit.Test
    public void completeReservationHandleMeg() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_COMPLETE_RSVP);
    }

    @org.junit.Test
    public void terminate() throws Exception {
    }

    @org.junit.Test
    public void finalize() throws Exception {
    }

}