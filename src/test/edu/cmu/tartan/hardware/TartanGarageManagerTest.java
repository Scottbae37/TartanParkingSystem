package edu.cmu.tartan.hardware;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * @author sanghyuck.na@lge.com
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TartanGarageConnection.class)
public class TartanGarageManagerTest {
    private TartanGarageManager garageManager;
    private TartanGarageConnection conn;

/*
    TartanGarageManager.
            getSpotOccupiedState
            isConnected
            setParkingSpotLights
            startUpdateThread
            toggleExitGate
            updateGarageState
  */

    @Before
    public void before() {
        conn = Mockito.spy(TartanGarageConnection.class);
        garageManager = new TartanGarageManagerSpy(conn);
    }


    @Test
    public void testSetEntryLight() throws Exception {

        StringBuffer cmd = new StringBuffer().append(TartanSensors.ENTRY_LIGHT)
                .append(TartanSensors.PARAM_EQ)
                .append(TartanSensors.RED)
                .append(TartanSensors.MSG_END);

        Mockito.doReturn(TartanSensors.OK)
                .when(conn)
                .sendMessageToGarage(cmd.toString());

        Boolean r = garageManager.setEntryLight(TartanSensors.RED);

        Assert.assertTrue(r);
        Mockito.verify(conn).sendMessageToGarage(cmd.toString());
    }


    @Test
    public void testSetEntryLightError() throws Exception {

        StringBuffer cmd = new StringBuffer().append(TartanSensors.ENTRY_LIGHT)
                .append(TartanSensors.PARAM_EQ)
                .append(TartanSensors.RED)
                .append(TartanSensors.MSG_END);


        Mockito.doReturn(null)
                .when(conn)
                .sendMessageToGarage(cmd.toString());


        boolean r = garageManager.setEntryLight(TartanSensors.RED);

        Assert.assertFalse(r);
        Mockito.verify(conn).sendMessageToGarage(cmd.toString());
    }

    @Test
    public void testSetExitLight() throws Exception {

        StringBuffer cmd = new StringBuffer().append(TartanSensors.EXIT_LIGHT)
                .append(TartanSensors.PARAM_EQ)
                .append(TartanSensors.RED)
                .append(TartanSensors.MSG_END);

        Mockito.doReturn(TartanSensors.OK)
                .when(conn)
                .sendMessageToGarage(cmd.toString());


        boolean r = garageManager.setExitLight(TartanSensors.RED);

        Assert.assertTrue(r);
        Mockito.verify(conn)
                .sendMessageToGarage(cmd.toString());
    }

    @Test
    public void testSetExitLightError() throws Exception {

        StringBuffer cmd = new StringBuffer().append(TartanSensors.EXIT_LIGHT)
                .append(TartanSensors.PARAM_EQ)
                .append(TartanSensors.RED)
                .append(TartanSensors.MSG_END);


        Mockito.doReturn(null)
                .when(conn)
                .sendMessageToGarage(cmd.toString());


        boolean r = garageManager.setExitLight(TartanSensors.RED);

        Assert.assertFalse(r);
        Mockito.verify(conn)
                .sendMessageToGarage(cmd.toString());
    }

    @Test
    public void testOpenEntryGate() throws Exception {

        StringBuffer cmd = new StringBuffer()
                .append(TartanSensors.ENTRY_GATE)
                .append(TartanSensors.PARAM_EQ)
                .append(TartanSensors.OPEN)
                .append(TartanSensors.MSG_END);


        Mockito.doReturn(TartanSensors.OK)
                .when(conn)
                .sendMessageToGarage(cmd.toString());


        garageManager.openEntryGate();

        Mockito.verify(conn)
                .sendMessageToGarage(cmd.toString());
    }

    @Test
    public void testOpenEntryGateError() throws Exception {

        StringBuffer cmd = new StringBuffer()
                .append(TartanSensors.ENTRY_GATE)
                .append(TartanSensors.PARAM_EQ)
                .append(TartanSensors.OPEN)
                .append(TartanSensors.MSG_END);


        Mockito.doReturn(null)
                .when(conn)
                .sendMessageToGarage(cmd.toString());


        garageManager.openEntryGate();

        Mockito.verify(conn)
                .sendMessageToGarage(cmd.toString());
    }


    @Test
    public void testCloseEntryGate() throws Exception {

        StringBuffer cmd = new StringBuffer()
                .append(TartanSensors.ENTRY_GATE)
                .append(TartanSensors.PARAM_EQ)
                .append(TartanSensors.CLOSE)
                .append(TartanSensors.MSG_END);

        Mockito.doReturn(TartanSensors.OK)
                .when(conn)
                .sendMessageToGarage(cmd.toString());

        garageManager.closeEntryGate();

        Mockito.verify(conn)
                .sendMessageToGarage(cmd.toString());
    }


    @Test
    public void testCloseEntryGateError() throws Exception {

        StringBuffer cmd = new StringBuffer()
                .append(TartanSensors.ENTRY_GATE)
                .append(TartanSensors.PARAM_EQ)
                .append(TartanSensors.CLOSE)
                .append(TartanSensors.MSG_END);


        Mockito.doReturn(null)
                .when(conn)
                .sendMessageToGarage(cmd.toString());

        garageManager.closeEntryGate();

        Mockito.verify(conn)
                .sendMessageToGarage(cmd.toString());
    }


    @Test
    public void testOpenExitGate() throws Exception {

        StringBuffer cmd = new StringBuffer()
                .append(TartanSensors.EXIT_GATE)
                .append(TartanSensors.PARAM_EQ)
                .append(TartanSensors.OPEN)
                .append(TartanSensors.MSG_END);


        Mockito.doReturn(TartanSensors.OK)
                .when(conn)
                .sendMessageToGarage(cmd.toString());

        garageManager.openExitGate();

        Mockito.verify(conn)
                .sendMessageToGarage(cmd.toString());
    }


    @Test
    public void testOpenExitGateError() throws Exception {

        StringBuffer cmd = new StringBuffer()
                .append(TartanSensors.EXIT_GATE)
                .append(TartanSensors.PARAM_EQ)
                .append(TartanSensors.OPEN)
                .append(TartanSensors.MSG_END);


        Mockito.doReturn(null)
                .when(conn)
                .sendMessageToGarage(cmd.toString());

        garageManager.openExitGate();

        Mockito.verify(conn)
                .sendMessageToGarage(cmd.toString());
    }


    @Test
    public void testCloseExitGate() throws Exception {

        StringBuffer cmd = new StringBuffer()
                .append(TartanSensors.EXIT_GATE)
                .append(TartanSensors.PARAM_EQ)
                .append(TartanSensors.CLOSE)
                .append(TartanSensors.MSG_END);


        Mockito.doReturn(TartanSensors.OK)
                .when(conn)
                .sendMessageToGarage(cmd.toString());

        garageManager.closeExitGate();

        Mockito.verify(conn)
                .sendMessageToGarage(cmd.toString());
    }

    @Test
    public void testCloseExitGateError() throws Exception {

        StringBuffer cmd = new StringBuffer()
                .append(TartanSensors.EXIT_GATE)
                .append(TartanSensors.PARAM_EQ)
                .append(TartanSensors.CLOSE)
                .append(TartanSensors.MSG_END);


        Mockito.doReturn(null)
                .when(conn)
                .sendMessageToGarage(cmd.toString());
        garageManager.closeExitGate();

        Mockito.verify(conn)
                .sendMessageToGarage(cmd.toString());
    }

    @Test
    public void testGetParkingSpots() {

        ArrayList<Integer> spots = garageManager.getParkingSpots();

        Assert.assertNotNull(spots);
        Assert.assertEquals(4, spots.size());
        IntStream.rangeClosed(0, 3).forEach((i) -> Assert.assertEquals(i, spots.get(i).intValue()));
    }

    @Test
    public void testGetCapacity() {

        Integer r = garageManager.getCapacity();

        Assert.assertNotNull(r);
        Assert.assertEquals(4, r.intValue());

    }

    @Test
    public void testDisconnectFromGarage() {

        Mockito.doNothing().when(conn)
                .disconnect();
        garageManager.disconnectFromGarage();

        Mockito.verify(conn).disconnect();
    }


    /**
     * startUpdateThread
     * > alertVehicleAtEntry()
     * > alertVehicleAtExit()
     */
    @Test
    public void testStartUpdateThreadIsNotConnected() {
        Mockito.doReturn(false)
                .when(conn)
                .isConnected();
        garageManager.startUpdateThread();
        Mockito.verify(conn).isConnected();
    }

    @Test
    public void testStartUpdateThreadUpdateGarageState() {
        Mockito.doReturn(true)
                .when(conn)
                .isConnected();

        StringBuffer cmd = new StringBuffer()
                .append(TartanSensors.GET_STATE )
                .append(TartanSensors.MSG_END);

        Mockito.doReturn(null)
                .when(conn)
                .sendMessageToGarage(cmd.toString());

        garageManager.startUpdateThread();

        Mockito.verify(conn, Mockito.atMost(2)).isConnected();

        Mockito.verify(conn)
                .sendMessageToGarage(cmd.toString());

    }


    public void testEn() {
        ArrayList<String> lightState = new ArrayList<String>();
        for (int i = 0; i < garageManager.getCapacity(); i++) {
            lightState.add(TartanSensors.OFF);
        }
        garageManager.setParkingSpotLights(lightState);

        garageManager.startUpdateThread();
    }
}
