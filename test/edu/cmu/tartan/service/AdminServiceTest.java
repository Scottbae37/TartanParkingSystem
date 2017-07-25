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
//        adminService = new AdminService();

        msgBus = PowerMockito.mock(TartanServiceMessageBus.class);
        consumer = PowerMockito.mock(MessageConsumer.class);
        producer = PowerMockito.mock(MessageProducer.class);

        PowerMockito.when(msgBus.getConsumer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(consumer);
        PowerMockito.when(msgBus.getProducer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(producer);
        PowerMockito.mockStatic(TartanServiceMessageBus.class);
        PowerMockito.when(TartanServiceMessageBus.connect()).thenReturn(msgBus);
        adminService = Mockito.spy(new AdminService());
    }

    @Test
    public void run() throws Exception {
        adminService.run();
        Mockito.verify(adminService).run();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getAdminPassword() throws Exception {
        String encodedAdminPwd = adminService.hashPassword(adminPwd);
        String expected[] = {adminId, encodedAdminPwd};
        String adminAuth[] = adminService.getAdminAuth();
        Assert.assertArrayEquals(expected, adminAuth);
    }

    @Test
    public void hashPassword() throws Exception {
        String pwd = "testPwd";
        String expected = "8kiTENvnwhY8jPnlzzEW9yKoHVSiw1WC+n++PMIBhBE=";
        Assert.assertEquals(expected, adminService.hashPassword(pwd));
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
    public void handleMessage() throws Exception {

        ArrayList authlist = new ArrayList();
        authlist.add(adminId);
        authlist.add(adminPwd);
        HashMap<String, Object> message = new HashMap<String, Object>();
        message.put(TartanParams.COMMAND, TartanParams.MSG_AUTHENTICATE_ADMIN);
        message.put(TartanParams.PAYLOAD, authlist);


        adminService.handleMessage(message);

        if (message.get(TartanParams.COMMAND).equals(TartanParams.MSG_AUTHENTICATE_ADMIN)) {
            Mockito.verify(adminService).authenticate((String) authlist.get(0), (String) authlist.get(1));
        }


    }

    @Test
    public void terminate() throws Exception {
    }



}