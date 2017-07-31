package edu.cmu.tartan.service;

import edu.cmu.tartan.hardware.TartanGarageManager;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import java.util.HashMap;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.times;
import static org.powermock.api.support.membermodification.MemberMatcher.methods;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

/**
 * Created by jaeseung.bae on 7/18/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ParkingService.class)
public class ParkingServiceTest {
    private ParkingService cutSpy;
    private ParkingService cutSpyPower;

    @Before
    public void setUp() throws Exception {
        suppress(methods(ParkingService.class, "init"));
        cutSpy = Mockito.spy(ParkingService.class);
        cutSpyPower = PowerMockito.spy(new ParkingService());
//        msgBus = PowerMockito.mock(TartanServiceMessageBus.class);
//        consumer = PowerMockito.mock(MessageConsumer.class);
//        producer = PowerMockito.mock(MessageProducer.class);
//
//        PowerMockito.when(msgBus.getConsumer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(consumer);
//        PowerMockito.when(msgBus.getProducer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(producer);
//        PowerMockito.mockStatic(TartanServiceMessageBus.class);
//        PowerMockito.when(TartanServiceMessageBus.connect()).thenReturn(msgBus);
    }

    @After
    public void tearDown() throws Exception {
        /* Tear-down */
        cutSpy = null;
        cutSpyPower = null;
    }
    /**
     *
     * For public method
     *
     */
    @Test
    public void test_update_invocate_signalVehicleArrived_when_MSG_VEHICLE_AT_ENTRY() throws Exception {
        /* Setup */
        String cmd = TartanParams.MSG_VEHICLE_AT_ENTRY;
        PowerMockito.doNothing().when(cutSpyPower, "signalVehicleArrived");
        /* Exercise */
        cutSpyPower.update(null, cmd);
        /* Verify */
        PowerMockito.verifyPrivate(cutSpyPower, times(1)).invoke("signalVehicleArrived");
        /* Tear-down */
    }
    @Test
    public void test_update_not_invocate_signalVehicleReadyToLeave_when_MSG_VEHICLE_AT_ENTRY() throws Exception {
        /* Setup */
        String cmd = TartanParams.MSG_VEHICLE_AT_ENTRY;
        PowerMockito.doNothing().when(cutSpyPower, "signalVehicleArrived");
        /* Exercise */
        cutSpyPower.update(null, cmd);
        /* Verify */
        PowerMockito.verifyPrivate(cutSpyPower, times(0)).invoke("signalVehicleReadyToLeave");
        /* Tear-down */
    }
    @Test
    public void test_update_invocate_signalVehicleReadyToLeave_when_MSG_VEHICLE_AT_EXIT() throws Exception {
        /* Setup */
        String cmd = TartanParams.MSG_VEHICLE_AT_EXIT;
        PowerMockito.doNothing().when(cutSpyPower, "signalVehicleReadyToLeave");
        /* Exercise */
        cutSpyPower.update(null, cmd);
        /* Verify */
        PowerMockito.verifyPrivate(cutSpyPower, times(1)).invoke("signalVehicleReadyToLeave");
        /* Tear-down */
    }
    @Test
    public void test_update_not_invocate_signalVehicleArrived_when_MSG_VEHICLE_AT_EXIT() throws Exception {
        /* Setup */
        String cmd = TartanParams.MSG_VEHICLE_AT_EXIT;
        PowerMockito.doNothing().when(cutSpyPower, "signalVehicleReadyToLeave");
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
        PowerMockito.doNothing().when(cutSpyPower, "signalVehicleArrived");
        PowerMockito.doNothing().when(cutSpyPower, "signalVehicleReadyToLeave");
        /* Exercise */
        cutSpyPower.update(null, cmd);
        /* Verify */
        PowerMockito.verifyPrivate(cutSpyPower, times(0)).invoke("signalVehicleArrived");
        PowerMockito.verifyPrivate(cutSpyPower, times(0)).invoke("signalVehicleReadyToLeave");
        /* Tear-down */
    }

    /**
     *
     * For private method
     *
     */
    @Test
    public void test_signalVehicleReadyToLeave_invocate_sendMessage_with_vehicle_exit_cmd_data_to_KIOSK_SERVICE() throws Exception {
        /* SetUP */
        TartanGarageManager garageMgrMock = Mockito.mock(TartanGarageManager.class);
        Integer[] occupiedState = new Integer[]{1,0,1,0};
        Mockito.doReturn(occupiedState).when(garageMgrMock).getSpotOccupiedState();

        Whitebox.setInternalState(cutSpyPower, "garageManager", garageMgrMock);
        PowerMockito.doAnswer(new Answer<RuntimeException>() {
            @Override
            public RuntimeException answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments(); // arguments
                String cmd = (String) args[0];
                HashMap<String, Object> map = (HashMap<String, Object>) args[1];

                if(!cmd.equals(KioskService.KIOSK_SERVICE))
                    throw new RuntimeException("Test Fail");
                if(!TartanParams.MSG_VEHICLE_AT_EXIT.equals(map.get(TartanParams.COMMAND)))
                    throw new RuntimeException("Test Fail");

                Integer[] expected = (Integer[])map.get(TartanParams.ACTUAL_SPOT);
                if(expected == null || expected.length != 4)
                    throw new RuntimeException("Test Fail");

                if(expected[0] != 1 || expected[1] != 0 || expected[2] != 1 || expected[3] != 0)
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
        Integer[] occupiedState = new Integer[]{1,0,1,0};
        Mockito.doReturn(occupiedState).when(garageMgrMock).getSpotOccupiedState();

        Whitebox.setInternalState(cutSpyPower, "garageManager", garageMgrMock);
        PowerMockito.doAnswer(new Answer<RuntimeException>() {
            @Override
            public RuntimeException answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments(); // arguments
                String cmd = (String) args[0];
                HashMap<String, Object> map = (HashMap<String, Object>) args[1];

                if(!cmd.equals(KioskService.KIOSK_SERVICE))
                    throw new RuntimeException("Test Fail");
                if(!TartanParams.MSG_VEHICLE_AT_ENTRY.equals(map.get(TartanParams.COMMAND)))
                    throw new RuntimeException("Test Fail");

                Integer[] expected = (Integer[])map.get(TartanParams.ACTUAL_SPOT);
                if(expected == null || expected.length != 4)
                    throw new RuntimeException("Test Fail");

                if(expected[0] != 1 || expected[1] != 0 || expected[2] != 1 || expected[3] != 0)
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