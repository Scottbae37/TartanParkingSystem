package edu.cmu.tartan.service;

import edu.cmu.tartan.edu.cmu.tartan.reservation.Payment;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import edu.cmu.tartan.edu.cmu.tartan.reservation.ReservationStore;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by chongjae.yoo on 2017-07-18.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({TartanServiceMessageBus.class})
public class ReservationServiceTest {

    TartanServiceMessageBus msgBus;
    MessageConsumer consumer;
    MessageProducer producer;
    ReservationService reservationService;
    ReservationStore reservationStore;
    Date startDate;
    Date endDate;

    @org.junit.Before
    public void setUp() throws Exception {
        msgBus = PowerMockito.mock(TartanServiceMessageBus.class);
        consumer = PowerMockito.mock(MessageConsumer.class);
        producer = PowerMockito.mock(MessageProducer.class);
        reservationStore = PowerMockito.mock(ReservationStore.class);
        PowerMockito.when(msgBus.getConsumer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(consumer);
        PowerMockito.when(msgBus.getProducer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(producer);
        PowerMockito.mockStatic(TartanServiceMessageBus.class);
        PowerMockito.when(TartanServiceMessageBus.connect()).thenReturn(msgBus);
        PowerMockito.whenNew(ReservationStore.class).withArguments(Mockito.anyString()).thenReturn(reservationStore);

        reservationService = Mockito.spy(new ReservationService("./"));
        Whitebox.setInternalState(reservationService, "rsvpStore", reservationStore);
        //run();
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    @org.junit.Test
    public void newReservationHandleMeg() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservation.getCustomerName()).thenReturn("UnitTest");
        Mockito.when(reservation.getVehicleID()).thenReturn("UnitTest");
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

    @org.junit.Test
    public void handleCompleteReservationTest() throws Exception {

        Reservation reservation = Mockito.mock(Reservation.class);
        startDate = Calendar.getInstance().getTime();
        endDate = new Date();
        startDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        endDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        setDate(reservation, startDate, endDate);

        Payment payment = Mockito.mock(Payment.class);
        Mockito.when(reservation.getPayment()).thenReturn(payment);
        Mockito.when(payment.getFee()).thenReturn(50L);
        reservation.setPayment(payment);
        Mockito.when(reservationStore.saveStaticsInfo(reservation)).thenReturn(true);
    }

    private void setReservation(Reservation reservation) throws Exception {
        Mockito.when(reservation.getCustomerName()).thenReturn("UnitTest");
        Mockito.when(reservation.getVehicleID()).thenReturn("UnitTest");
        startDate = Calendar.getInstance().getTime();
        endDate = new Date();
        startDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        endDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        setDate(reservation, startDate, endDate);
    }

    @org.junit.Test
    public void verifyReservationPass() throws Exception {
        Reservation reservation = Mockito.mock(Reservation.class);
        setReservation(reservation);
        Object ret;

        //Normal Case. CurrentTime + 1 Hour ~ CurrentTime + 2 Hour
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertTrue((Boolean) ret);
    }

    @org.junit.Test
    public void verifyReservationStartTimeLargerThanEndTime() throws Exception {
        Reservation reservation = Mockito.mock(Reservation.class);
        setReservation(reservation);
        Object ret;

        //endTime <= startTime
        startDate = Calendar.getInstance().getTime();
        endDate = startDate;
        setDate(reservation, startDate, endDate);
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertFalse((Boolean) ret);
        startDate.setTime(endDate.getTime() + 1000 * 60 * 60);
        setDate(reservation, startDate, endDate);
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertFalse((Boolean) ret);
    }

    @org.junit.Test
    public void verifyReservationDiffernceOfEndTimeAndStartTimeLargerThan24Hours() throws Exception {
        Reservation reservation = Mockito.mock(Reservation.class);
        setReservation(reservation);
        Object ret;

        //endTime - startTime > 24 hours
        startDate = Calendar.getInstance().getTime();
        endDate = Calendar.getInstance().getTime();
        endDate.setTime(startDate.getTime() + 1000 * 60 * 60 * 24 + 1000 * 60 * 60);
        setDate(reservation, startDate, endDate);
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertFalse((Boolean) ret);
    }

    @org.junit.Test
    public void verifyReservationStartTimeSmallerThanCurrentTime() throws Exception {
        Reservation reservation = Mockito.mock(Reservation.class);
        setReservation(reservation);
        Object ret;

        //Current time > startTime
        startDate = Calendar.getInstance().getTime();
        endDate = Calendar.getInstance().getTime();
        startDate.setTime(startDate.getTime() - 1000 * 60 * 60);
        setDate(reservation, startDate, endDate);
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertFalse((Boolean) ret);
    }

    @org.junit.Test
    public void verifyReservationStartTimeLargerThanWeek() throws Exception {
        Reservation reservation = Mockito.mock(Reservation.class);
        setReservation(reservation);
        Object ret;

        //Startime more than a week out
        startDate = Calendar.getInstance().getTime();
        endDate = Calendar.getInstance().getTime();
        startDate.setTime(startDate.getTime() + 604900000);
        endDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        setDate(reservation, startDate, endDate);
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertFalse((Boolean) ret);
    }

    @org.junit.Test
    public void verifyReservationEndTimeLargerThanWeek() throws Exception {
        Reservation reservation = Mockito.mock(Reservation.class);
        setReservation(reservation);
        Object ret;

        //Endime more than a week out
        startDate = Calendar.getInstance().getTime();
        endDate = Calendar.getInstance().getTime();
        startDate.setTime(startDate.getTime() + 604700000);
        endDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        setDate(reservation, startDate, endDate);
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertFalse((Boolean) ret);
    }

    @org.junit.Test
    public void verifyReservationCustomerNameIsNull() throws Exception {
        Reservation reservation = Mockito.mock(Reservation.class);
        setReservation(reservation);
        Object ret;

        //Customer Name is null
        startDate = Calendar.getInstance().getTime();
        endDate = new Date();
        startDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        endDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        setDate(reservation, startDate, endDate);
        Mockito.when(reservation.getCustomerName()).thenReturn(null);
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertFalse((Boolean) ret);

    }

    @org.junit.Test
    public void verifyReservationVehicleIdIsNull() throws Exception {
        Reservation reservation = Mockito.mock(Reservation.class);
        setReservation(reservation);
        Object ret;


        //VehicleId is null
        Mockito.when(reservation.getCustomerName()).thenReturn("");
        Mockito.when(reservation.getVehicleID()).thenReturn(null);
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reservation);
        Assert.assertFalse((Boolean) ret);
    }

    private void setDate(Reservation reservation, Date start, Date end) throws Exception {
        Mockito.when(reservation.getStartTime()).thenReturn(start);
        Mockito.when(reservation.getEndTime()).thenReturn(end);
    }

    @org.junit.Test
    public void confirmReservationHandleMeg() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        Reservation reservation = Mockito.mock(Reservation.class);
        Date startDate = Calendar.getInstance().getTime();
        Date endDate = new Date();
        startDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        endDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        setDate(reservation, startDate, endDate);
        msg.put(TartanParams.PAYLOAD, reservation);
        msg.put(TartanParams.COMMAND, TartanParams.MSG_CONFIRM_RSVP);
        reservationService.handleMessage(msg);
    }

    @org.junit.Test
    public void updateReservationHandleMeg() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        Vector<Reservation> reservations = new Vector<>();
        Reservation reservation = Mockito.mock(Reservation.class);
        msg.put(TartanParams.PAYLOAD, reservation);
        msg.put(TartanParams.COMMAND, TartanParams.MSG_UPDATE_RSVP);
        Mockito.when(reservationStore.getReservations()).thenReturn(reservations);
        reservationService.handleMessage(msg);
        Mockito.verify(reservation).setSpotId(Mockito.anyInt());
        Mockito.verify(reservationService).sendMessage(Mockito.eq(KioskService.KIOSK_SERVICE), Mockito.any(HashMap.class));
    }

    @org.junit.Test
    public void getParkingSpotHandleMeg() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_GET_PARKING_SPOTS);

        //It just save parking spots
        reservationService.handleMessage(msg);
    }

    @org.junit.Test
    public void redeemHandleMegFailBecauseOfStartTime() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        Vector<Reservation> results = new Vector<>();
        Reservation reservation = Mockito.mock(Reservation.class);
        setReservation(reservation);
        results.add(reservation);
        msg.put(TartanParams.CUSTOMER, "UnitTest");
        msg.put(TartanParams.VEHICLE, "UnitTest");
        msg.put(TartanParams.COMMAND, TartanParams.MSG_REDEEM_RSVP);
        Mockito.when(reservationStore.lookupByCustomer(Mockito.anyString())).thenReturn(results);

        reservationService.handleMessage(msg);

        ArgumentCaptor<String> keyMsg = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HashMap> responseMsg = ArgumentCaptor.forClass(HashMap.class);

        Mockito.verify(reservationService).sendMessage(keyMsg.capture(), responseMsg.capture());
        HashMap<String, Object> response = (HashMap<String, Object>) responseMsg.getValue();
        Assert.assertEquals(TartanParams.ERROR, response.get(TartanParams.COMMAND));
    }

    @org.junit.Test
    public void redeemHandleMegSuccess() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        Vector<Reservation> results = new Vector<>();
        Reservation reservation = Mockito.mock(Reservation.class);
        setReservation(reservation);
        startDate.setTime(startDate.getTime() - 1000 * 60 * 60 * 2);
        results.add(reservation);
        msg.put(TartanParams.CUSTOMER, "UnitTest");
        msg.put(TartanParams.VEHICLE, "UnitTest");
        msg.put(TartanParams.COMMAND, TartanParams.MSG_REDEEM_RSVP);
        Mockito.when(reservationStore.lookupByCustomer(Mockito.anyString())).thenReturn(results);

        reservationService.handleMessage(msg);

        ArgumentCaptor<String> keyMsg = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HashMap> responseMsg = ArgumentCaptor.forClass(HashMap.class);

        Mockito.verify(reservationService).sendMessage(keyMsg.capture(), responseMsg.capture());
        HashMap<String, Object> response = (HashMap<String, Object>) responseMsg.getValue();
        Assert.assertEquals(TartanParams.MSG_REDEEM_RSVP, response.get(TartanParams.COMMAND));
    }


    @org.junit.Test
    public void getAllReservationHandleMeg() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_GET_ALL_RSVP);

        reservationService.handleMessage(msg);
        Mockito.verify(reservationService).sendMessage(Mockito.anyString(), Mockito.any(HashMap.class));
    }

    @org.junit.Test
    public void completeReservationHandleMeg() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        Reservation reservation = Mockito.mock(Reservation.class);
        msg.put(TartanParams.COMMAND, TartanParams.MSG_COMPLETE_RSVP);
        msg.put(TartanParams.PAYLOAD, reservation);
        reservationService.handleMessage(msg);
        Mockito.verify(reservationStore).markReservationRedeemed(reservation);
    }

    @org.junit.Test
    public void handleMessage_handleCompletePayment_If_MSG_PAYMENT_COMPLETE() throws Exception {
        HashMap<String, Object> message = new HashMap<>();
        message.put(TartanParams.COMMAND, TartanParams.MSG_PAYMENT_COMPLETE);
        Reservation reservation = Mockito.mock(Reservation.class);

        Date startDate = Calendar.getInstance().getTime();
        Date endDate = new Date();
        startDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        endDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        setDate(reservation, startDate, endDate);

        Payment payment = Mockito.mock(Payment.class);
        Mockito.when(reservation.getPayment()).thenReturn(payment);
        Mockito.when(payment.getFee()).thenReturn(50L);
        reservation.setPayment(payment);


        message.put(TartanParams.PAYLOAD, reservation);
        reservationService.handleMessage(message);


        Mockito.verify(reservationService).handleCompletePayment(message);


    }

    @org.junit.Test
    public void terminate() throws Exception {
        reservationService.terminate();
        Mockito.verify(reservationService).stop();
    }

    @org.junit.Test
    public void finalize() throws Exception {
        reservationService.terminate();
        Mockito.verify(reservationService).stop();
    }

}