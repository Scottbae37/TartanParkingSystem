package edu.cmu.tartan.integrationtest;


import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import edu.cmu.tartan.edu.cmu.tartan.reservation.ReservationStore;
import edu.cmu.tartan.service.ReservationService;
import edu.cmu.tartan.service.TartanServiceMessageBus;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * Created by kyungman.yu on 2017-08-01.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TartanServiceMessageBus.class)
public class MultiReservationTest {
    TartanServiceMessageBus msgBus;
    MessageConsumer consumer;
    MessageProducer producer;
    Date startDate;
    Date endDate;
    ReservationService reservationService;
    ReservationStore reservationStore;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd:HH:mm");

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

        reservationStore = Mockito.spy(new ReservationStore("./"));
        Whitebox.setInternalState(reservationService, "rsvpStore", reservationStore);

    }


    @org.junit.Test
    public void singleReservationTest() throws Exception {
        Reservation reserve = Mockito.mock(Reservation.class);
        setReservation(reserve);
        Object ret;

        //Normal Case. CurrentTime + 1 Hour ~ CurrentTime + 2 Hour
        ret = Whitebox.invokeMethod(reservationService, "verifyReservation", reserve);
        Assert.assertTrue((Boolean) ret);
    }

    @org.junit.Test
    public void multiReservationTest_equal() throws Exception {
        Reservation reserve = Mockito.mock(Reservation.class);
        Vector<Reservation> reservations = new Vector<>();
        setReservation(reserve);
        for(int i=0;i<2;i++){
            reservations.add(reserve);
        }
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservation.getCustomerName()).thenReturn("UnitTest");
        Mockito.when(reservation.getVehicleID()).thenReturn("UnitTest");
        Mockito.when(reservation.getStartTime()).thenReturn(format.parse("2017:08:06:16:00"));
        Mockito.when(reservation.getEndTime()).thenReturn(format.parse("2017:08:07:16:00"));

        Object ret;
        for (Reservation reser : reservations) {
            reservationStore.addReservation(reser);
        }
        ret = Whitebox.invokeMethod(reservationStore, "isDuplicate", reservation);
        Assert.assertFalse((Boolean) ret);
    }


    @org.junit.Test
    public void multiReservationTest_nonequal() throws Exception {
        Reservation reserve = Mockito.mock(Reservation.class);
        Vector<Reservation> reservations = new Vector<>();
        setReservation(reserve);
        for(int i=0;i<2;i++){
            reservations.add(reserve);
        }
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservation.getCustomerName()).thenReturn("UnitTest");
        //vehicleID가 같지 않음 테스트
        Mockito.when(reservation.getVehicleID()).thenReturn("UnitTest1");
        Mockito.when(reservation.getStartTime()).thenReturn(format.parse("2017:08:06:16:00"));
        Mockito.when(reservation.getEndTime()).thenReturn(format.parse("2017:08:07:16:00"));

        Object ret;
        for (Reservation reser : reservations) {
            reservationStore.addReservation(reser);
        }
        ret = Whitebox.invokeMethod(reservationStore, "isDuplicate", reservation);
        Assert.assertFalse((Boolean) ret);
    }

    private void setReservation(Reservation reservation) throws Exception {
        Mockito.when(reservation.getCustomerName()).thenReturn("UnitTest");
        Mockito.when(reservation.getVehicleID()).thenReturn("UnitTest");
        Mockito.when(reservation.getStartTime()).thenReturn(format.parse("2017:08:06:16:00"));
        Mockito.when(reservation.getEndTime()).thenReturn(format.parse("2017:08:07:16:00"));

    }



}
