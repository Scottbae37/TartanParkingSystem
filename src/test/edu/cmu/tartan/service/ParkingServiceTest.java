package edu.cmu.tartan.service;

import edu.cmu.tartan.edu.cmu.tartan.reservation.Payment;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import edu.cmu.tartan.hardware.TartanGarageManager;
import edu.cmu.tartan.hardware.TartanSensors;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.times;

/**
 * Created by jaeseung.bae on 7/18/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TartanServiceMessageBus.class)
public class ParkingServiceTest {
    private ParkingService cutSpyPower;
    private TartanServiceMessageBus msgBus;
    private MessageConsumer consumer;
    private MessageProducer producer;
    private TartanGarageManager garageMgrMock;

    @Before
    public void setUp() throws Exception {
        msgBus = PowerMockito.mock(TartanServiceMessageBus.class);
        consumer = PowerMockito.mock(MessageConsumer.class);
        producer = PowerMockito.mock(MessageProducer.class);
        garageMgrMock = Mockito.mock(TartanGarageManager.class);
        PowerMockito.when(msgBus.getConsumer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(consumer);
        PowerMockito.when(msgBus.getProducer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(producer);
        PowerMockito.mockStatic(TartanServiceMessageBus.class);
        PowerMockito.when(TartanServiceMessageBus.connect()).thenReturn(msgBus);
        cutSpyPower = PowerMockito.spy(new ParkingService());
        Whitebox.setInternalState(cutSpyPower, "garageManager", garageMgrMock);
    }

    @After
    public void tearDown() throws Exception {
        /* Tear-down */
        cutSpyPower = null;
    }

    @Test
    public void getParkingSpotMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_GET_PARKING_SPOTS);
        cutSpyPower.handleMessage(msg);
        Mockito.verify(cutSpyPower).sendMessage(Mockito.eq(ReservationService.RESERVATION_SERVICE), Mockito.any(HashMap.class));
    }

    @Test
    public void enterGarageMsgHandleFailBecauseOfAlreadyOccupied() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        Reservation reservation = Mockito.mock(Reservation.class);
        msg.put(TartanParams.COMMAND, TartanParams.MSG_ENTER_GARAGE);
        msg.put(TartanParams.PAYLOAD, reservation);

        Mockito.when(garageMgrMock.isConnected()).thenReturn(true);
        Mockito.when(reservation.getSpotId()).thenReturn(0);
        Mockito.when(garageMgrMock.getSpotOccupiedState()).thenReturn(new Integer[]{1, 0, 0, 0});
        cutSpyPower.handleMessage(msg);

        Mockito.verify(cutSpyPower).sendMessage(Mockito.eq(ReservationService.RESERVATION_SERVICE), Mockito.any(HashMap.class));
    }

    @Test
    public void enterGarageMsgHandleFailBecauseParkInvalidLocation() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        Reservation reservation = Mockito.mock(Reservation.class);
        msg.put(TartanParams.COMMAND, TartanParams.MSG_ENTER_GARAGE);
        msg.put(TartanParams.PAYLOAD, reservation);

        Mockito.when(garageMgrMock.isConnected()).thenReturn(true);
        Mockito.when(reservation.getSpotId()).thenReturn(0);
        Mockito.when(garageMgrMock.getSpotOccupiedState()).thenReturn(new Integer[]{0, 0, 0, 0});

        cutSpyPower.handleMessage(msg);

        Mockito.verify(garageMgrMock).setEntryLight(TartanSensors.GREEN);
        Mockito.verify(garageMgrMock).openEntryGate();
        Mockito.verify(garageMgrMock).setEntryLight(TartanSensors.RED);
        Mockito.verify(garageMgrMock).closeEntryGate();
        Mockito.verify(cutSpyPower, Mockito.times(2)).sendMessage(Mockito.eq(KioskService.KIOSK_SERVICE), Mockito.any(HashMap.class));

        ArgumentCaptor<String> keyMsg = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HashMap> responseMsg = ArgumentCaptor.forClass(HashMap.class);

        Mockito.verify(cutSpyPower, Mockito.times(2)).sendMessage(keyMsg.capture(), responseMsg.capture());
        HashMap<String, Object> response = (HashMap<String, Object>) responseMsg.getValue();

        Assert.assertEquals(TartanParams.MSG_WRONG_SPOT, response.get(TartanParams.COMMAND));
    }

    @Test
    public void enterGarageMsgHandleSuccess() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        Reservation reservation = Mockito.mock(Reservation.class);
        msg.put(TartanParams.COMMAND, TartanParams.MSG_ENTER_GARAGE);
        msg.put(TartanParams.PAYLOAD, reservation);

        Mockito.when(garageMgrMock.isConnected()).thenReturn(true);
        Mockito.when(reservation.getSpotId()).thenReturn(0);
        Mockito.when(garageMgrMock.getSpotOccupiedState()).thenReturn(new Integer[]{0, 0, 0, 0}).thenReturn(new Integer[]{1, 0, 0, 0});

        cutSpyPower.handleMessage(msg);

        Mockito.verify(garageMgrMock).setEntryLight(TartanSensors.GREEN);
        Mockito.verify(garageMgrMock).openEntryGate();
        Mockito.verify(garageMgrMock).setEntryLight(TartanSensors.RED);
        Mockito.verify(garageMgrMock).closeEntryGate();
        Mockito.verify(cutSpyPower).sendMessage(Mockito.eq(KioskService.KIOSK_SERVICE), Mockito.any(HashMap.class));
    }

    @Test
    public void exitGarageMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_EXIT_GARAGE);
        msg.put(TartanParams.PAYLOAD, "UnitTest");
        Vector<Reservation> mockOccupancy = new Vector<>();
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservation.getVehicleID()).thenReturn("UnitTest");
        mockOccupancy.add(reservation);
        Whitebox.setInternalState(cutSpyPower, "occupancy", mockOccupancy);

        cutSpyPower.handleMessage(msg);

        Mockito.verify(cutSpyPower).sendMessage(Mockito.eq(PaymentService.PAYMENT_SERVICE), Mockito.any(HashMap.class));
    }

    @Test
    public void paymentCompleteMsgHandle() throws Exception {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        Reservation reservation = Mockito.mock(Reservation.class);
        Payment payment = Mockito.mock(Payment.class);
        Mockito.when(payment.getFee()).thenReturn((long) 1000);
        Mockito.when(reservation.getPayment()).thenReturn(payment);
        Mockito.when(garageMgrMock.isConnected()).thenReturn(true);
        msg.put(TartanParams.COMMAND, TartanParams.MSG_PAYMENT_COMPLETE);
        msg.put(TartanParams.PAYLOAD, reservation);

        cutSpyPower.handleMessage(msg);

        Mockito.verify(garageMgrMock).setExitLight(TartanSensors.GREEN);
        Mockito.verify(garageMgrMock).openExitGate();
        Mockito.verify(garageMgrMock).setExitLight(TartanSensors.RED);
        Mockito.verify(garageMgrMock).closeExitGate();

        Mockito.verify(cutSpyPower).sendMessage(Mockito.eq(KioskService.KIOSK_SERVICE), Mockito.any(HashMap.class));
    }

    /**
     * For public method
     */
    @Test
    public void test_update_not_invocate_signalVehicleReadyToLeave_when_MSG_VEHICLE_AT_ENTRY() throws Exception {
        /* Setup */
        String cmd = TartanParams.MSG_VEHICLE_AT_ENTRY;
        /* Exercise */
        cutSpyPower.update(null, cmd);
        /* Verify */
        PowerMockito.verifyPrivate(cutSpyPower, times(0)).invoke("signalVehicleReadyToLeave");
        /* Tear-down */
    }

    @Test
    public void test_update_not_invocate_signalVehicleArrived_when_MSG_VEHICLE_AT_EXIT() throws Exception {
        /* Setup */
        String cmd = TartanParams.MSG_VEHICLE_AT_EXIT;
        /* Exercise */
        cutSpyPower.update(null, cmd);
        /* Verify */
        PowerMockito.verifyPrivate(cutSpyPower, times(0)).invoke("signalVehicleArrived");
        /* Tear-down */
    }

    @Test
    public void test_update_not_invocate_both_methods_when_negative_cmd() throws Exception {
        /* Setup */
        String cmd = "Invalid_CMD";
        /* Exercise */
        cutSpyPower.update(null, cmd);
        /* Verify */
        PowerMockito.verifyPrivate(cutSpyPower, times(0)).invoke("signalVehicleArrived");
        PowerMockito.verifyPrivate(cutSpyPower, times(0)).invoke("signalVehicleReadyToLeave");
        /* Tear-down */
    }

    /**
     * For private method
     */
    @Test
    public void test_signalVehicleReadyToLeave_invocate_sendMessage_with_vehicle_exit_cmd_data_to_KIOSK_SERVICE() throws Exception {
        /* SetUP */
        Integer[] occupiedState = new Integer[]{1, 0, 1, 0};
        Mockito.doReturn(occupiedState).when(garageMgrMock).getSpotOccupiedState();

        PowerMockito.doAnswer(new Answer<RuntimeException>() {
            @Override
            public RuntimeException answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments(); // arguments
                String cmd = (String) args[0];
                HashMap<String, Object> map = (HashMap<String, Object>) args[1];

                if (!cmd.equals(KioskService.KIOSK_SERVICE))
                    throw new RuntimeException("Test Fail");
                if (!TartanParams.MSG_VEHICLE_AT_EXIT.equals(map.get(TartanParams.COMMAND)))
                    throw new RuntimeException("Test Fail");

                Integer[] expected = (Integer[]) map.get(TartanParams.ACTUAL_SPOT);
                if (expected == null || expected.length != 4)
                    throw new RuntimeException("Test Fail");

                if (expected[0] != 1 || expected[1] != 0 || expected[2] != 1 || expected[3] != 0)
                    throw new RuntimeException("Test Fail");
                return null;
            }
        }).when(cutSpyPower, "sendMessage", anyString(), anyMap());

        /* Exercise */
        Whitebox.invokeMethod(cutSpyPower, "signalVehicleReadyToLeave");
        /* Verify */
          /* By Exception */
        /* Tear-down */
    }

    @Test
    public void test_signalVehicleArrived_invocate_sendMessage_with_vehicle_entry_cmd_data_to_KIOSK_SERVICE() throws Exception {
        /* SetUP */
        TartanGarageManager garageMgrMock = Mockito.mock(TartanGarageManager.class);
        Integer[] occupiedState = new Integer[]{1, 0, 1, 0};
        Mockito.doReturn(occupiedState).when(garageMgrMock).getSpotOccupiedState();

        Whitebox.setInternalState(cutSpyPower, "garageManager", garageMgrMock);
        PowerMockito.doAnswer(new Answer<RuntimeException>() {
            @Override
            public RuntimeException answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments(); // arguments
                String cmd = (String) args[0];
                HashMap<String, Object> map = (HashMap<String, Object>) args[1];

                if (!cmd.equals(KioskService.KIOSK_SERVICE))
                    throw new RuntimeException("Test Fail");
                if (!TartanParams.MSG_VEHICLE_AT_ENTRY.equals(map.get(TartanParams.COMMAND)))
                    throw new RuntimeException("Test Fail");

                Integer[] expected = (Integer[]) map.get(TartanParams.ACTUAL_SPOT);
                if (expected == null || expected.length != 4)
                    throw new RuntimeException("Test Fail");

                if (expected[0] != 1 || expected[1] != 0 || expected[2] != 1 || expected[3] != 0)
                    throw new RuntimeException("Test Fail");
                return null;
            }
        }).when(cutSpyPower, "sendMessage", anyString(), anyMap());

        /* Exercise */
        Whitebox.invokeMethod(cutSpyPower, "signalVehicleArrived");
        /* Verify */
          /* By Exception */
        /* Tear-down */
    }


    @Test
    public void disconnectFromGarage() throws Exception {
        /* SetUP */
        /* Exercise */
        /* Verify */
        /* Tear-down */
    }

    @Test
    public void handleMessage() throws Exception {
        /* SetUP */
        /* Exercise */
        /* Verify */
        /* Tear-down */
    }

    @Test
    public void terminate() throws Exception {
        /* SetUP */
        /* Exercise */
        /* Verify */
        /* Tear-down */
    }

    @Test
    public void sendMessage() throws Exception {
        /* SetUP */
        /* Exercise */
        /* Verify */
        /* Tear-down */
    }

    @Test
    public void onMessage() throws Exception {
        /* SetUP */
        /* Exercise */
        /* Verify */
        /* Tear-down */
    }
}