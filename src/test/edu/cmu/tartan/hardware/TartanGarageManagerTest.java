package edu.cmu.tartan.hardware;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.StringJoiner;

/**
 * @author sanghyuck.na@lge.com
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TartanGarageConnection.class)
public class TartanGarageManagerTest {
    private TartanGarageManager garageManager;
    private TartanGarageConnection conn;


    @Test
    public void testSetEntryLight() throws Exception {
        StringBuffer cmd = new StringBuffer().append(TartanSensors.ENTRY_LIGHT)
                .append(TartanSensors.PARAM_EQ)
                .append(TartanSensors.RED)
                .append(TartanSensors.MSG_END);
        Boolean r;
        conn = Mockito.spy(TartanGarageConnection.class);

        Mockito.doReturn(TartanSensors.OK)
                .when(conn)
                .sendMessageToGarage(cmd.toString());

        garageManager = new TartanGarageManager(conn);
        r = garageManager.setEntryLight(TartanSensors.RED);

        Mockito.verify(conn).sendMessageToGarage(cmd.toString());
        Assert.assertTrue(r);

    }

    @Test
    public void testSetExitLight() throws Exception {
        Boolean r;
        StringBuffer cmd = new StringBuffer().append(TartanSensors.EXIT_LIGHT)
                .append(TartanSensors.PARAM_EQ)
                .append(TartanSensors.RED)
                .append(TartanSensors.MSG_END);

        conn = Mockito.spy(TartanGarageConnection.class);

        Mockito.doReturn(TartanSensors.OK)
                .when(conn)
                .sendMessageToGarage(cmd.toString());

        garageManager = new TartanGarageManager(conn);
        r = garageManager.setExitLight(TartanSensors.RED);

        Mockito.verify(conn)
                .sendMessageToGarage(cmd.toString());
        Assert.assertTrue(r);
    }

    @Test
    public void testCloseEntryGate() throws Exception {
        StringBuffer cmd = new StringBuffer()
                .append(TartanSensors.ENTRY_GATE)
                .append(TartanSensors.PARAM_EQ)
                .append(TartanSensors.CLOSE)
                .append(TartanSensors.MSG_END);

        conn = Mockito.spy(TartanGarageConnection.class);

        Mockito.doReturn(TartanSensors.OK)
                .when(conn)
                .sendMessageToGarage(cmd.toString());

        garageManager = new TartanGarageManager(conn);
        garageManager.closeEntryGate();

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

        conn = Mockito.spy(TartanGarageConnection.class);

        Mockito.doReturn(TartanSensors.OK)
                .when(conn)
                .sendMessageToGarage(cmd.toString());

        garageManager = new TartanGarageManager(conn);
        garageManager.closeExitGate();

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
