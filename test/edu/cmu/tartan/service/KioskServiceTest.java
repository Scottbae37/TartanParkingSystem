package edu.cmu.tartan.service;

import edu.cmu.tartan.TartanKioskWindow;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Payment;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import junit.framework.Assert;

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
@PrepareForTest({ TartanServiceMessageBus.class, JOptionPane.class })
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
		PowerMockito.doNothing().when(JOptionPane.class, "showMessageDialog", Mockito.any(String.class),
				Mockito.any(String.class));

		Mockito.when(window.acceptPayment()).thenReturn(null).thenReturn(payment);
		kioskService = Mockito.spy(new KioskService());
		kioskService.setKiosk(window);
	}

	@org.junit.After
	public void tearDown() throws Exception {
	}

	@org.junit.Test
	public void reedemReservation() throws Exception {
		HashMap<String, Object> msg = new HashMap<String, Object>();

		// MSG_REDEEM_RSVP handle
		// Already paid.
		msg.put(TartanParams.COMMAND, TartanParams.MSG_REDEEM_RSVP);
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
		reservations.clear();
		reservations.addElement(reservation);
		msg.clear();
		msg.put(TartanParams.COMMAND, TartanParams.MSG_REDEEM_RSVP);
		msg.put(TartanParams.PAYLOAD, reservations);
		kioskService.handleMessage(msg);

		// Reservation size larger than 1
		int size = 5;
		reservations.clear();
		for (int i = 0; i < size; i++) {
			reservations.addElement(Mockito.mock(Reservation.class));
		}
		msg.clear();
		msg.put(TartanParams.COMMAND, TartanParams.MSG_REDEEM_RSVP);
		msg.put(TartanParams.PAYLOAD, reservations);
		kioskService.handleMessage(msg);
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
	public void getReservation() throws Exception {
		Assert.assertTrue(kioskService.getReservation("", null));
		Mockito.verify(kioskService).sendMessage(Mockito.eq("RsvpService"), Mockito.any(HashMap.class));
	}

	@org.junit.Test
	public void terminate() throws Exception {
		kioskService.terminate();
		Mockito.verify(kioskService).stop();
	}

}