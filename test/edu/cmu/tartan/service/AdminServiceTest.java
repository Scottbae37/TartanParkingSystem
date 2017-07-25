package edu.cmu.tartan.service;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kyungman.yu on 2017-07-19.
 */


@RunWith(PowerMockRunner.class)
@PrepareForTest({TartanServiceMessageBus.class, JOptionPane.class})
public class AdminServiceTest {
    private AdminService adminService;
    private String adminId = "admin";
    private String adminPwd = "1qaz2wsx";
    private String adminPwdEncoded = "";

    TartanServiceMessageBus msgBus;
    MessageConsumer consumer;
    MessageProducer producer;

    @Before
    public void setUp() throws Exception {
        msgBus = PowerMockito.mock(TartanServiceMessageBus.class);
        consumer = PowerMockito.mock(MessageConsumer.class);
        producer = PowerMockito.mock(MessageProducer.class);

        PowerMockito.when(msgBus.getConsumer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(consumer);
        PowerMockito.when(msgBus.getProducer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(producer);
        PowerMockito.mockStatic(TartanServiceMessageBus.class);
        PowerMockito.when(TartanServiceMessageBus.connect()).thenReturn(msgBus);
        adminService = Mockito.spy(new AdminService());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getAdminPassword_Success_AlwaysMustBe() throws Exception {
        String encodedAdminPwd = adminService.hashPassword(adminPwd);
        String expected[] = {adminId, encodedAdminPwd};
        String adminAuth[] = adminService.getAdminAuth();
        Assert.assertArrayEquals(expected, adminAuth);
    }

    @Test
    public void hashPassword_Success_IfSha256() throws Exception {
        String pwd = "testPwd";
        String expected = "8kiTENvnwhY8jPnlzzEW9yKoHVSiw1WC+n++PMIBhBE=";
        Assert.assertEquals(expected, adminService.hashPassword(pwd));
    }

    @Test
    public void hashPassword_Fail_IfIncorrectPwd() throws Exception {
        String pwd = "test";
        String expected = "8kiTENvnwhY8jPnlzzEW9yKoHVSiw1WC+n++PMIBhBE=";
        Assert.assertNotEquals(expected, adminService.hashPassword(pwd));
    }

    @Test
    public void authenticate_Success_IfAuthValid() throws Exception {
        String id = adminId;
        String pwd = adminPwd;
        Assert.assertEquals(true, adminService.authenticate(id, pwd));
    }

    @Test
    public void authenticate_Fail_IfIdInvalid() throws Exception {
        String id = "invalidId";
        String pwd = adminPwd;
        Assert.assertEquals(false, adminService.authenticate(id, pwd));
    }

    @Test
    public void authenticate_Fail_IfPwdInvalid() throws Exception {
        String id = adminId;
        String pwd = "invalidPwd";
        Assert.assertEquals(false, adminService.authenticate(id, pwd));
    }

    @Test
    public void handleMessage_CalledAuthenticateMethod_If_MSG_AUTHENTICATE_ADMIN() throws Exception {
        ArrayList authlist = new ArrayList();
        authlist.add(adminId);
        authlist.add(adminPwd);
        HashMap<String, Object> message = new HashMap<String, Object>();
        message.put(TartanParams.COMMAND, TartanParams.MSG_AUTHENTICATE_ADMIN);
        message.put(TartanParams.PAYLOAD, authlist);

        adminService.handleMessage(message);

        Mockito.verify(adminService).authenticate((String) authlist.get(0), (String) authlist.get(1));
    }

    @Test
    public void handleMessage_CalledGetStatisticalData_If_MSG_GET_STATISTICAL_DATA() throws Exception {
        HashMap<String, Object> message = new HashMap<>();
        message.put(TartanParams.COMMAND, TartanParams.MSG_GET_STATISTICAL_DATA);

        adminService.handleMessage(message);

        Mockito.verify(adminService).getRevenue();
        Mockito.verify(adminService).getAverageOccupancy();
        Mockito.verify(adminService).getPeakUsageHours();
    }

    @Test
    public void terminate() throws Exception {
    }



}