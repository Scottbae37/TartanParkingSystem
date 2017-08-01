package edu.cmu.tartan.integrationtest;

import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import edu.cmu.tartan.edu.cmu.tartan.reservation.ReservationStore;
import edu.cmu.tartan.service.ReservationService;
import edu.cmu.tartan.service.TartanParams;
import edu.cmu.tartan.service.TartanServiceMessageBus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import java.util.*;

@RunWith(PowerMockRunner.class)
public class ReservationTest {

    TartanServiceMessageBus msgBus;
    MessageConsumer consumer;
    MessageProducer producer;
    ReservationService reservationService;
    ReservationStore reservationStore;

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

        reservationService = Mockito.spy(new ReservationService("./"));
        Whitebox.setInternalState(reservationService, "rsvpStore", reservationStore);
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }


    private HashMap<String, Object> newMessage_MSG_NEW_RSVP(Reservation r) {
        HashMap<String, Object> msg = new HashMap<>();
        msg.put(TartanParams.PAYLOAD, r);
        msg.put(TartanParams.COMMAND, TartanParams.MSG_NEW_RSVP);
        return msg;
    }

    private HashMap<String, Object> newMessage_MSG_GET_PARKING_SPOTS() {
        final int capacity = 4;
        ArrayList<Integer> parkingSpots = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            parkingSpots.add(i);
        }

        HashMap<String, Object> msg = new HashMap<>();
        msg.put(TartanParams.PAYLOAD, parkingSpots);
        msg.put(TartanParams.COMMAND, TartanParams.MSG_GET_PARKING_SPOTS);
        return msg;
    }

    /**
     * TS01-TC01 - Reservation try when all spots are already full.
     */
    @PrepareForTest({Calendar.class, ReservationService.class, TartanServiceMessageBus.class, ReservationStore.class})
    @Test
    public void test_Reservation_try_when_all_spots_are_already_full() {
        Calendar cal = Calendar.getInstance(); //  Current date is 07-27 15:00.
        cal.set(2017, 5, 27, 15, 0);
        Date cur = cal.getTime();
        Vector list = new Vector<Reservation>();
        PowerMockito.mockStatic(Calendar.class);
        PowerMockito.when(Calendar.getInstance()).thenReturn(cal);

        // set available total garage
        reservationService.handleMessage(newMessage_MSG_GET_PARKING_SPOTS());

        Reservation aaa = new Reservation();
        aaa.setCustomerName("aaa");
        aaa.setVehicleID("1qaz");
        aaa.setStartTime("2017:07:27:17:00");//"yyyy:MM:dd:HH:mm"
        aaa.setEndTime("2017:07:27:18:00");
        Mockito.when(reservationStore.getReservations()).thenReturn(list);
        reservationService.handleMessage(newMessage_MSG_NEW_RSVP(aaa));
        Assert.assertEquals(Boolean.TRUE, aaa.isComplete());
        Mockito.verify(reservationStore, Mockito.times(1)).getReservations();
        list.add(aaa);
        // --------------------

        Reservation bbb = new Reservation();
        bbb.setCustomerName("bbb");
        bbb.setVehicleID("2wsx");
        bbb.setStartTime("2017:07:27:17:00");//"yyyy:MM:dd:HH:mm"
        bbb.setEndTime("2017:07:27:18:00");
        Mockito.when(reservationStore.getReservations()).thenReturn(list);
        reservationService.handleMessage(newMessage_MSG_NEW_RSVP(bbb));
        Assert.assertEquals(Boolean.TRUE, bbb.isComplete());
        Mockito.verify(reservationStore, Mockito.times(2)).getReservations();
        list.add(bbb);
        // -------------------


        Reservation ccc = new Reservation();
        ccc.setCustomerName("ccc");
        ccc.setVehicleID("3edc");
        ccc.setStartTime("2017:07:27:17:00");//"yyyy:MM:dd:HH:mm"
        ccc.setEndTime("2017:07:27:18:00");
        Mockito.when(reservationStore.getReservations()).thenReturn(list);
        reservationService.handleMessage(newMessage_MSG_NEW_RSVP(ccc));
        Assert.assertEquals(Boolean.TRUE, ccc.isComplete());
        Mockito.verify(reservationStore, Mockito.times(3)).getReservations();
        list.add(ccc);

        Reservation ddd = new Reservation();
        ddd.setCustomerName("ddd");
        ddd.setVehicleID("4rfv");
        ddd.setStartTime("2017:07:27:17:00");//"yyyy:MM:dd:HH:mm"
        ddd.setEndTime("2017:07:27:18:00");
        Mockito.when(reservationStore.getReservations()).thenReturn(list);
        reservationService.handleMessage(newMessage_MSG_NEW_RSVP(ddd));
        Assert.assertEquals(Boolean.TRUE, ddd.isComplete());
        Mockito.verify(reservationStore, Mockito.times(4)).getReservations();
        list.add(ddd);

        // check logic is in reservation service
        Reservation eee = new Reservation();
        eee.setCustomerName("somebody");
        eee.setVehicleID("5tgb");
        eee.setStartTime("2017:07:27:17:00");//"yyyy:MM:dd:HH:mm"
        eee.setEndTime("2017:07:27:18:00");
        Mockito.when(reservationStore.getReservations()).thenReturn(list);
        reservationService.handleMessage(newMessage_MSG_NEW_RSVP(eee));
        Assert.assertEquals(Boolean.FALSE, eee.isComplete());
        Mockito.verify(reservationStore, Mockito.times(5)).getReservations();

    }

    /**
     * TS01-TC02 - Reservation try with invalid name
     */
    @PrepareForTest({Calendar.class, ReservationService.class, TartanServiceMessageBus.class, ReservationStore.class})
    @Test
    public void test_Reservation_try_with_invalid_name() {
        Calendar cal = Calendar.getInstance(); //  Current date is 07-27 15:00.
        cal.set(2017, 5, 27, 15, 0);
        Date cur = cal.getTime();
        Vector list = new Vector<Reservation>();
        PowerMockito.mockStatic(Calendar.class);
        PowerMockito.when(Calendar.getInstance()).thenReturn(cal);

        // set available total garage
        reservationService.handleMessage(newMessage_MSG_GET_PARKING_SPOTS());

        Reservation aaa = new Reservation();
        aaa.setCustomerName("");
        aaa.setVehicleID("5tgb");
        aaa.setStartTime("2017:07:27:17:00");//"yyyy:MM:dd:HH:mm"
        aaa.setEndTime("2017:07:27:18:00");
        Mockito.when(reservationStore.getReservations()).thenReturn(list);
        reservationService.handleMessage(newMessage_MSG_NEW_RSVP(aaa));
        Assert.assertEquals(Boolean.FALSE, aaa.isComplete());
        Mockito.verify(reservationService).sendMessage(null, newMessage_Error_ReservationService_verifyReservation());
    }

    private HashMap<String, Object> newMessage_Error_ReservationService_verifyReservation(){
        HashMap<String, Object> response = new HashMap<String, Object>();

        response.put(TartanParams.SOURCE_ID,"RsvpService");
        response.put(TartanParams.COMMAND, TartanParams.ERROR);
        response.put(TartanParams.PAYLOAD, "Reservation is invalid, please submit a valid reservation");
        return response;
    }
}
