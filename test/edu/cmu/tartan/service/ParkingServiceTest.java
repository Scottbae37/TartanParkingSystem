package edu.cmu.tartan.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.powermock.api.support.membermodification.MemberMatcher.methods;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

/**
 * Created by jaeseung.bae on 7/18/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ParkingService.class)
public class ParkingServiceTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void update() throws Exception {
    }

    @Test
    public void connectToGarage() throws Exception {
    }

    @Test
    public void disconnectFromGarage() throws Exception {
    }

    @Test
    public void finalize() throws Exception {
    }

    @Test
    public void run() throws Exception {
    }

    @Test
    public void handleMessage() throws Exception {
    }

    @Test
    public void terminate() throws Exception {
    }

    @Test
    public void sendMessage() throws Exception {
    }

    @Test
    public void onMessage() throws Exception {
    }

    @Test
    public void init() throws Exception {
        /* SetUP */
        /* Put two annotations below, See above
        * @RunWith(PowerMockRunner.class)
          @PrepareForTest(ParkingService.class)
        * */
        suppress(methods(ParkingService.class, "init"));
        ParkingService mockPar = new ParkingService();
        ParkingService spy = Mockito.spy(mockPar);
        Mockito.doNothing().when(spy).sendMessage((String)any(), (HashMap)any());
        /* Exercise */
        spy.update(null, "Nothing");
        spy.sendMessage("Aaa", null);

        /* Verify */
//        fail();
        /* Tear-down */
    }
}