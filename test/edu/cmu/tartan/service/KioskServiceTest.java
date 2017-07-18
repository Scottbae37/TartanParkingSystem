package edu.cmu.tartan.service;
import edu.cmu.tartan.TartanKioskWindow;

import java.util.HashMap;

import static org.mockito.Mockito.*;

/**
 * Created by chongjae.yoo on 2017-07-18.
 */
public class KioskServiceTest {
    @org.junit.Before
    public void setUp() throws Exception {
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    @org.junit.Test
    public void handleMessage() throws Exception {
        ParkingService parkingService = mock(ParkingService.class);
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_REDEEM_RSVP);
        parkingService.handleMessage(msg);
        verify(parkingService).handleMessage(msg);
    }

    @org.junit.Test
    public void sendPaymentInfo() throws Exception {
    }

    @org.junit.Test
    public void makeNewReservation() throws Exception {
    }

    @org.junit.Test
    public void run() throws Exception {
    }

    @org.junit.Test
    public void finalize() throws Exception {
    }

    @org.junit.Test
    public void getReservation() throws Exception {
    }

    @org.junit.Test
    public void setKiosk() throws Exception {
    }

    @org.junit.Test
    public void terminate() throws Exception {
    }

}