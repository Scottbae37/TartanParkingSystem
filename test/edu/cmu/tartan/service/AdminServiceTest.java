package edu.cmu.tartan.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

/**
 * Created by kyungman.yu on 2017-07-19.
 */
public class AdminServiceTest {
    private AdminService adminService;
    private String adminId = "admin";
    private String adminPwd = "1qaz2wsx";
    private String adminPwdEncoded = "";

    @Before
    public void setUp() throws Exception {
        adminService = new AdminService();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getAdminPassword() throws Exception {
        String encodedAdminPwd = adminService.hashPassword(adminPwd);
        String expected[] = { adminId, encodedAdminPwd };
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
    }

    @Test
    public void terminate() throws Exception {
    }

    @Test
    public void run() throws Exception {
    }

}