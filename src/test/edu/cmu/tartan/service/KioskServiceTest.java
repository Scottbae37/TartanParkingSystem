package edu.cmu.tartan.service;

import edu.cmu.tartan.TartanKioskWindow;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Payment;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import junit.framework.Assert;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by chongjae.yoo on 2017-07-18.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({TartanServiceMessageBus.class, JOptionPane.class})
public class KioskServiceTest {

    KioskService kioskService;
    TartanServiceMessageBus msgBus;
    TartanKioskWindow window;
    Payment payment;
    MessageConsumer consumer;
    MessageProducer producer;

    @org.junit.Before
    public void setUp() throws Exception {
        msgBus = PowerMockito.mock(TartanServiceMessageBus.class);
        consumer = PowerMockito.mock(MessageConsumer.class);
        producer = PowerMockito.mock(MessageProducer.class);
        window = Mockito.mock(TartanKioskWindow.class);
        payment = Mockito.mock(Payment.class);

        PowerMockito.when(msgBus.getConsumer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(consumer);
        PowerMockito.when(msgBus.getProducer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(producer);
        PowerMockito.mockStatic(TartanServiceMessageBus.class);
        PowerMockito.when(TartanServiceMessageBus.connect()).thenReturn(msgBus);
        PowerMockito.mockStatic(JOptionPane.class);
        PowerMockito.doNothing().when(JOptionPane.class, "showMessageDialog", Mockito.any(Component.class),
                Mockito.any(Object.class), Mockito.anyString(), Mockito.anyInt());

        kioskService = Mockito.spy(new KioskService());
        kioskService.setKiosk(window);
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    @org.junit.Test
    public void sendPaymentInfo() throws Exception {
        kioskService.sendPaymentInfo(payment);
        Mockito.verify(kioskService).sendMessage(Mockito.eq("PaymentService"), Mockito.any(HashMap.class));
    }

    @org.junit.Test
    public void makeNewReservation() throws Exception {
        Reservation reservation = Mockito.mock(Reservation.class);
        kioskService.makeNewReservation(reservation);
        Mockito.verify(kioskService).sendMessage(Mockito.eq("RsvpService"), Mockito.any(HashMap.class));
    }

    @org.junit.Test
    public void getReservationWithName() throws Exception {
        Assert.assertTrue(kioskService.getReservation("", null));
        Mockito.verify(kioskService).sendMessage(Mockito.eq("RsvpService"), Mockito.any(HashMap.class));
    }

    @org.junit.Test
    public void getReservationWithLicense() throws Exception {
        Assert.assertTrue(kioskService.getReservation(null, ""));
        Mockito.verify(kioskService).sendMessage(Mockito.eq("RsvpService"), Mockito.any(HashMap.class));
    }

    @org.junit.Test
    public void terminate() throws Exception {
        kioskService.terminate();
        Mockito.verify(kioskService).stop();
    }

    @org.junit.Test
    public void authenicate() throws Exception {
        Assert.assertTrue(kioskService.authenicate(Mockito.any(ArrayList.class)));
        Mockito.verify(kioskService).sendMessage(Mockito.eq(AdminService.ADMIN_SERVICE), Mockito.any(HashMap.class));
    }

    @org.junit.Test
    public void finalize() throws Exception {
        kioskService.finalize();
        Mockito.verify(kioskService).stop();
    }

    @org.junit.Test
    public void run() throws Exception {
        kioskService.run();
    }


    @org.junit.Test
    public void reedemReservationMsgHandle() throws Exception {
        // MSG_REDEEM_RSVP handle
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_REDEEM_RSVP);

        // Already paid.
        Vector<Reservation> reservations = new Vector<Reservation>();
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservation.getIsPaid()).thenReturn(true);
        reservations.add(reservation);
        msg.put(TartanParams.PAYLOAD, reservations);
        kioskService.handleMessage(msg);
        Mockito.verify(window).redeemReservation(reservation);
        Mockito.verify(kioskService).sendMessage(Mockito.eq("ParkingService"), Mockito.any(HashMap.class));
        Mockito.verify(kioskService).sendMessage(Mockito.eq("RsvpService"), Mockito.any(HashMap.class));

        // Not Paid.
        reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservation.getIsPaid()).thenReturn(false);
        Mockito.when(window.acceptPayment(reservation)).thenReturn(payment);
        reservations.clear();
        reservations.addElement(reservation);
        msg.clear();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_REDEEM_RSVP);
        msg.put(TartanParams.PAYLOAD, reservations);
        kioskService.handleMessage(msg);
        Mockito.verify(reservation).setIsPaid(true);
        Mockito.verify(kioskService).sendPaymentInfo(payment);


        // Reservation size larger than 1
        int size = 5;
        reservations.clear();
        for (int i = 0; i < size; i++) {
            reservations.addElement(Mockito.mock(Reservation.class));
        }
        msg.clear();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_REDEEM_RSVP);
        msg.put(TartanParams.PAYLOAD, reservations);
        PowerMockito.doReturn("1").when(JOptionPane.class, "showInputDialog", Mockito.any(Component.class),
                Mockito.any(Object.class), Mockito.anyString(), Mockito.anyInt(), Mockito.any(Icon.class), Mockito.any(Object.class), Mockito.any(Object.class));
        kioskService.handleMessage(msg);


        //Reservation is empty
        msg.clear();
        reservations.clear();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_REDEEM_RSVP);
        msg.put(TartanParams.PAYLOAD, reservations);
        kioskService.handleMessage(msg);
        Mockito.verify(window).showError(Mockito.anyString());
    }

    @org.junit.Test
    public void vehicleEntryMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_VEHICLE_AT_ENTRY);

        kioskService.handleMessage(msg);

        Mockito.verify(window).enableRsvpRedemption();
    }

    @org.junit.Test
    public void vehicleExitMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_VEHICLE_AT_EXIT);

        //Valid Parking number
        PowerMockito.doReturn("1").when(JOptionPane.class, "showInputDialog", Mockito.any(Component.class),
                Mockito.any(Object.class), Mockito.anyString(), Mockito.anyInt());

        kioskService.handleMessage(msg);

        Mockito.verify(kioskService).sendMessage(Mockito.eq(ParkingService.PARKING_SERVICE), Mockito.any(HashMap.class));
    }

    @org.junit.Test
    public void vehicleExitWithoutInputMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_VEHICLE_AT_EXIT);

        //Valid Parking number
        PowerMockito.doReturn("").when(JOptionPane.class, "showInputDialog", Mockito.any(Component.class),
                Mockito.any(Object.class), Mockito.anyString(), Mockito.anyInt());

        kioskService.handleMessage(msg);
    }

    @org.junit.Test
    public void wrongSpotMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_WRONG_SPOT);
        HashMap<String, Object> body = new HashMap<>();
        body.put(TartanParams.RSVP, Mockito.mock(Reservation.class));
        body.put(TartanParams.ACTUAL_SPOT, Mockito.anyInt());
        msg.put(TartanParams.PAYLOAD, body);

        kioskService.handleMessage(msg);

        Mockito.verify(window).showError(Mockito.anyString());
    }

    @org.junit.Test
    public void newReservationMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_NEW_RSVP);
        Reservation reservation = Mockito.mock(Reservation.class);
        msg.put(TartanParams.PAYLOAD, reservation);

        //No payment Info.
        Mockito.when(window.acceptPayment(reservation)).thenReturn(null);
        kioskService.handleMessage(msg);
        Mockito.verify(reservation).setIsPaid(false);
        Mockito.verify(kioskService).sendMessage(Mockito.eq(ReservationService.RESERVATION_SERVICE), Mockito.any(HashMap.class));
        Mockito.verify(window).confirmReservation(reservation);

        //Payment Info was set
        Mockito.when(window.acceptPayment(reservation)).thenReturn(payment);
        kioskService.handleMessage(msg);
        Mockito.verify(reservation).setIsPaid(true);
        Mockito.verify(payment).setReservation(reservation);
        Mockito.verify(kioskService).sendPaymentInfo(payment);
    }


    @org.junit.Test
    public void updateReservationMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_UPDATE_RSVP);
        Reservation reservation = Mockito.mock(Reservation.class);
        msg.put(TartanParams.PAYLOAD, reservation);

        kioskService.handleMessage(msg);

        Mockito.verify(window).notifyUpdatedReservation(reservation);

        Mockito.verify(kioskService).sendMessage(Mockito.eq(ParkingService.PARKING_SERVICE), Mockito.any(HashMap.class));
    }

    @org.junit.Test
    public void paymentValidWithoutRedeemedMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_PAYMENT_VALID);
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(payment.getReservation()).thenReturn(reservation);
        msg.put(TartanParams.PAYLOAD, payment);

        //Not redeemed
        Mockito.when(reservation.getIsRedeemed()).thenReturn(false);
        kioskService.handleMessage(msg);
        Mockito.verify(kioskService).sendMessage(Mockito.eq(ReservationService.RESERVATION_SERVICE), Mockito.any(HashMap.class));
        Mockito.verify(window).confirmReservation(reservation);
    }

    @org.junit.Test
    public void paymentValidWithRedeemedMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_PAYMENT_VALID);
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(payment.getReservation()).thenReturn(reservation);
        msg.put(TartanParams.PAYLOAD, payment);

        //Redeemed
        Mockito.when(reservation.getIsRedeemed()).thenReturn(true);
        kioskService.handleMessage(msg);
        Mockito.verify(window).redeemReservation(reservation);
        Mockito.verify(kioskService).sendMessage(Mockito.eq(ParkingService.PARKING_SERVICE), Mockito.any(HashMap.class));
    }

    @org.junit.Test
    public void paymentInvalidMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_PAYMENT_INVALID);

        kioskService.handleMessage(msg);

        Mockito.verify(window).showError(Mockito.anyString());
    }

    @org.junit.Test
    public void errorMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.ERROR);

        kioskService.handleMessage(msg);

        Mockito.verify(window).showError(Mockito.any());
    }

    @org.junit.Test
    public void entryCompleteMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_ENTRY_COMPLETE);

        kioskService.handleMessage(msg);

        Mockito.verify(window).disableRsvpRedemption();
    }

    @org.junit.Test
    public void exitCompleteMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_EXIT_COMPLETE);
        Reservation reservation = Mockito.mock(Reservation.class);
        msg.put(TartanParams.PAYLOAD, reservation);
        kioskService.handleMessage(msg);

        Mockito.verify(window).showReceipt(reservation);
    }

    @org.junit.Test
    public void authenticationResultMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_AUTHENTICATION_RESULT);
        msg.put(TartanParams.PAYLOAD, true);

        kioskService.handleMessage(msg);

        Mockito.verify(kioskService).sendMessage(Mockito.eq(AdminService.ADMIN_SERVICE), Mockito.any(HashMap.class));

        msg.put(TartanParams.PAYLOAD, false);
        kioskService.handleMessage(msg);

        Mockito.verify(window).showError(Mockito.anyString());
    }

    @org.junit.Test
    public void statisticalDataResultMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_STATISTICAL_DATA_RESULT);

        kioskService.handleMessage(msg);

        Mockito.verify(window).showAdminConsole(Mockito.any(HashMap.class));
    }
}