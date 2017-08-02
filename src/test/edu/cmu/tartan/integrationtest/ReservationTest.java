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
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.*;

@PrepareForTest({ReservationService.class, TartanServiceMessageBus.class, ReservationStore.class})
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

        reservationService = new ReservationService("./");
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
    @Test
    public void test_Reservation_try_when_all_spots_are_already_full() {
        Vector list = new Vector<Reservation>();

        LocalDateTime ldt = LocalDateTime.of(2017, Month.JULY, 27, 15, 0);
        Date d = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        PowerMockito.mockStatic(Calendar.class);
        Mockito.when(Calendar.getInstance().getTime()).thenReturn(d);

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
        Mockito.verify(msgBus).generateMessage(newMessage_Error_ReservationService_INVALID_SPOT(), null);
    }

    /**
     * TS01-TC02 - Reservation try with invalid name
     */
    @Test
    public void test_Reservation_try_with_invalid_name() {
        Vector list = new Vector<Reservation>();

        LocalDateTime ldt = LocalDateTime.of(2017, Month.JULY, 27, 15, 0);
        Date d = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        PowerMockito.mockStatic(Calendar.class);
        Mockito.when(Calendar.getInstance().getTime()).thenReturn(d);

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
        Mockito.verify(msgBus).generateMessage(newMessage_Error_ReservationService_verifyReservation(), null);
    }

    /**
     * TS01-TC03 - Reservation try with invalid vehicle ID
     */
    @Test
    public void test_Reservation_try_with_invalid_vehicle_ID() {
        Vector list = new Vector<Reservation>();

        LocalDateTime ldt = LocalDateTime.of(2017, Month.JULY, 27, 15, 0);
        Date d = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        PowerMockito.mockStatic(Calendar.class);
        Mockito.when(Calendar.getInstance().getTime()).thenReturn(d);

        // set available total garage
        reservationService.handleMessage(newMessage_MSG_GET_PARKING_SPOTS());

        Reservation aaa = new Reservation();
        aaa.setCustomerName("somebody");
        aaa.setVehicleID(" ");
        aaa.setStartTime("2017:07:27:17:00");//"yyyy:MM:dd:HH:mm"
        aaa.setEndTime("2017:07:27:18:00");
        Mockito.when(reservationStore.getReservations()).thenReturn(list);
        reservationService.handleMessage(newMessage_MSG_NEW_RSVP(aaa));
        Assert.assertEquals(Boolean.FALSE, aaa.isComplete());
        Mockito.verify(msgBus).generateMessage(newMessage_Error_ReservationService_verifyReservation(), null);
    }


    /**
     * TS01-TC04 - Reservation try when start time < current time
     */
    @Test
    public void test_Reservation_try_when_start_time_is_less_than_current_time() {
        Vector list = new Vector<Reservation>();

        LocalDateTime ldt = LocalDateTime.of(2017, Month.JULY, 27, 15, 0);
        Date d = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        PowerMockito.mockStatic(Calendar.class);
        Mockito.when(Calendar.getInstance().getTime()).thenReturn(d);

        // set available total garage
        reservationService.handleMessage(newMessage_MSG_GET_PARKING_SPOTS());

        Reservation aaa = new Reservation();
        aaa.setCustomerName("somebody");
        aaa.setVehicleID(" ");
        aaa.setStartTime("2017:07:27:13:00");//"yyyy:MM:dd:HH:mm"
        aaa.setEndTime("2017:07:27:18:00");
        Mockito.when(reservationStore.getReservations()).thenReturn(list);
        reservationService.handleMessage(newMessage_MSG_NEW_RSVP(aaa));
        Assert.assertEquals(Boolean.FALSE, aaa.isComplete());
        Mockito.verify(msgBus).generateMessage(newMessage_Error_ReservationService_verifyReservation(), null);
    }

    private HashMap<String, Object> newMessage_Error_ReservationService_verifyReservation() {
        HashMap<String, Object> response = new HashMap<String, Object>();

        response.put(TartanParams.SOURCE_ID, "RsvpService");
        response.put(TartanParams.COMMAND, TartanParams.ERROR);
        response.put(TartanParams.PAYLOAD, "Reservation is invalid, please submit a valid reservation");
        return response;
    }

    private HashMap<String, Object> newMessage_Error_ReservationService_INVALID_SPOT() {
        HashMap<String, Object> response = new HashMap<String, Object>();

        response.put(TartanParams.SOURCE_ID, "RsvpService");
        response.put(TartanParams.COMMAND, TartanParams.ERROR);
        response.put(TartanParams.PAYLOAD, "Parking space unavailable at desired time");
        return response;
    }


    /**
     * TS01-TC05 - Reservation try when start time >= current time + week
     */
    @Test
    public void test_Reservation_try_when_start_time_or_more_current_time_week() {
        Vector list = new Vector<Reservation>();

        LocalDateTime ldt = LocalDateTime.of(2017, Month.JULY, 27, 15, 0);
        Date d = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());

        PowerMockito.mockStatic(Calendar.class);
        Mockito.when(Calendar.getInstance().getTime()).thenReturn(d);

        PowerMockito.mockStatic(System.class);
        Mockito.when(System.currentTimeMillis()).thenReturn(d.getTime());

        // set available total garage
        reservationService.handleMessage(newMessage_MSG_GET_PARKING_SPOTS());

        Reservation aaa = new Reservation();
        aaa.setCustomerName("somebody");
        aaa.setVehicleID("5tgb");
        aaa.setStartTime("2017:08:03:13:00");//"yyyy:MM:dd:HH:mm"
        aaa.setEndTime("2017:08:03:18:00");
        Mockito.when(reservationStore.getReservations()).thenReturn(list);
        reservationService.handleMessage(newMessage_MSG_NEW_RSVP(aaa));
        Assert.assertEquals(Boolean.FALSE, aaa.isComplete());
        Mockito.verify(msgBus).generateMessage(newMessage_Error_ReservationService_verifyReservation(), null);
    }

    /**
     * TS01-TC06 - Reservation try when start time >= current time + week
     */
    @Test
    public void test_Reservation_try_when_start_time_or_more_current_time_week2() {
        Vector list = new Vector<Reservation>();

        LocalDateTime ldt = LocalDateTime.of(2017, Month.AUGUST, 1, 15, 0);
        Date d = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());

        PowerMockito.mockStatic(Calendar.class);
        Mockito.when(Calendar.getInstance().getTime()).thenReturn(d);

        PowerMockito.mockStatic(System.class);
        Mockito.when(System.currentTimeMillis()).thenReturn(d.getTime());

        // set available total garage
        reservationService.handleMessage(newMessage_MSG_GET_PARKING_SPOTS());

        Reservation aaa = new Reservation();
        aaa.setCustomerName("somebody");
        aaa.setVehicleID("5tgb");
        aaa.setStartTime("2017:08:08:15:00");//"yyyy:MM:dd:HH:mm"
        aaa.setEndTime("2017:08:08:16:00");
        Mockito.when(reservationStore.getReservations()).thenReturn(list);
        reservationService.handleMessage(newMessage_MSG_NEW_RSVP(aaa));
        Assert.assertEquals(Boolean.FALSE, aaa.isComplete());
        Mockito.verify(msgBus).generateMessage(newMessage_Error_ReservationService_verifyReservation(), null);
    }


    /**
     * TS01-TC07 - Reservation try when start time >= end time
     */
    @Test
    public void test_Reservation_try_when_start_time_or_more_than_end_time() {
        Vector list = new Vector<Reservation>();

        LocalDateTime ldt = LocalDateTime.of(2017, Month.AUGUST, 1, 15, 0);
        Date d = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());

        PowerMockito.mockStatic(Calendar.class);
        Mockito.when(Calendar.getInstance().getTime()).thenReturn(d);

        PowerMockito.mockStatic(System.class);
        Mockito.when(System.currentTimeMillis()).thenReturn(d.getTime());

        // set available total garage
        reservationService.handleMessage(newMessage_MSG_GET_PARKING_SPOTS());

        Reservation aaa = new Reservation();
        aaa.setCustomerName("somebody");
        aaa.setVehicleID("5tgb");
        aaa.setStartTime("2017:08:01:15:00");//"yyyy:MM:dd:HH:mm"
        aaa.setEndTime("2017:08:01:15:00");
        Mockito.when(reservationStore.getReservations()).thenReturn(list);
        reservationService.handleMessage(newMessage_MSG_NEW_RSVP(aaa));
        Assert.assertEquals(Boolean.FALSE, aaa.isComplete());
        Mockito.verify(msgBus).generateMessage(newMessage_Error_ReservationService_verifyReservation(), null);
    }

    /**
     * TS01-TC08 - Reservation try when end time > current time + week
     */
    @Test
    public void test_Reservation_try_when_end_time_or_more_than_cur_plus_week() {
        Vector list = new Vector<Reservation>();

        LocalDateTime ldt = LocalDateTime.of(2017, Month.AUGUST, 1, 15, 0);
        Date d = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());

        PowerMockito.mockStatic(Calendar.class);
        Mockito.when(Calendar.getInstance().getTime()).thenReturn(d);

        PowerMockito.mockStatic(System.class);
        Mockito.when(System.currentTimeMillis()).thenReturn(d.getTime());

        // set available total garage
        reservationService.handleMessage(newMessage_MSG_GET_PARKING_SPOTS());

        Reservation aaa = new Reservation();
        aaa.setCustomerName("somebody");
        aaa.setVehicleID("5tgb");
        aaa.setStartTime("2017:08:08:14:00");//"yyyy:MM:dd:HH:mm"
        aaa.setEndTime("2017:08:08:16:00");
        Mockito.when(reservationStore.getReservations()).thenReturn(list);
        reservationService.handleMessage(newMessage_MSG_NEW_RSVP(aaa));
        Assert.assertEquals(Boolean.FALSE, aaa.isComplete());
        Mockito.verify(msgBus).generateMessage(newMessage_Error_ReservationService_verifyReservation(), null);
    }

    /**
     * TS01-TC09 - Reservation try when end time > current time + week
     */
    @Test
    public void test_Reservation_try_when_end_time_more_than_cur_plus_week() {
        Vector list = new Vector<Reservation>();

        LocalDateTime ldt = LocalDateTime.of(2017, Month.AUGUST, 1, 15, 0);
        Date d = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());

        PowerMockito.mockStatic(Calendar.class);
        Mockito.when(Calendar.getInstance().getTime()).thenReturn(d);

        PowerMockito.mockStatic(System.class);
        Mockito.when(System.currentTimeMillis()).thenReturn(d.getTime());

        // set available total garage
        reservationService.handleMessage(newMessage_MSG_GET_PARKING_SPOTS());

        Reservation aaa = new Reservation();
        aaa.setCustomerName("somebody");
        aaa.setVehicleID("5tgb");
        aaa.setStartTime("2017:08:04:15:00");//"yyyy:MM:dd:HH:mm"
        aaa.setEndTime("2017:08:05:12:00");
        Mockito.when(reservationStore.getReservations()).thenReturn(list);
        reservationService.handleMessage(newMessage_MSG_NEW_RSVP(aaa));
        Assert.assertEquals(Boolean.TRUE, aaa.isComplete());
        Mockito.verify(msgBus,Mockito.never()).generateMessage(newMessage_Error_ReservationService_verifyReservation(), null);
    }
}
