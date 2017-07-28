package edu.cmu.tartan.service;

import edu.cmu.tartan.edu.cmu.tartan.reservation.Payment;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.swing.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by kyungman.yu on 2017-07-19.
 */


@RunWith(PowerMockRunner.class)
@PrepareForTest({TartanServiceMessageBus.class, JOptionPane.class})
public class AdminServiceTest {
    private AdminService adminService;
    private String adminId = "admin";
    private String adminPwd = "1qaz2wsx";
    private byte[] salt = "[B@70f43b45".getBytes(StandardCharsets.UTF_8);

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
        String currentPath = new java.io.File( "." ).getCanonicalPath();
        adminService = Mockito.spy(new AdminService(currentPath));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void generateSalt() throws Exception {
        byte[] salt = adminService.generateSalt();
        System.out.println("salt=" + salt);
    }

    @Test
    public void getAdminPassword_Success_AlwaysMustBe() throws Exception {
        String encodedAdminPwd = adminService.hashPassword(adminPwd, salt);
        String expected[] = {adminId, encodedAdminPwd};
        String adminAuth[] = adminService.getAdminAuth();
        Assert.assertArrayEquals(expected, adminAuth);
    }

    @Test
    public void hashPassword_Success_IfSha256() throws Exception {
        String pwd = "testPwd";
        String expected = "dwQnhRR0D1if+4Yigmo0zAbqLNI0QlMqbz+TpBoe4CY=";
        byte[] salt = "testSalt".getBytes(StandardCharsets.UTF_8);
        Assert.assertEquals(expected, adminService.hashPassword(pwd, salt));
    }

    @Test
    public void hashPassword_Fail_IfIncorrectPwd() throws Exception {
        String pwd = "test";
        String expected = "dwQnhRR0D1if+4Yigmo0zAbqLNI0QlMqbz+TpBoe4CY=";
        byte[] salt = "testSalt".getBytes(StandardCharsets.UTF_8);
        Assert.assertNotEquals(expected, adminService.hashPassword(pwd, salt));
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

    @Test (expected = NullPointerException.class)
    public void handleMessage_NullPointerException_If_MessageIsNull() throws Exception {
        adminService.handleMessage(null);
    }

    @Test
    public void handleMessage_CalledHandleAuthenticateMethod_If_MSG_AUTHENTICATE_ADMIN() throws Exception {
        ArrayList authlist = new ArrayList();
        authlist.add(adminId);
        authlist.add(adminPwd);
        HashMap<String, Object> message = new HashMap<String, Object>();
        message.put(TartanParams.COMMAND, TartanParams.MSG_AUTHENTICATE_ADMIN);
        message.put(TartanParams.PAYLOAD, authlist);

        adminService.handleMessage(message);

        Mockito.verify(adminService).handleAuthenticate(message);
        Mockito.verify(adminService).authenticate((String) authlist.get(0), (String) authlist.get(1));
    }

    @Test
    public void handleMessage_CalledHandleGetStatisticalData_If_MSG_GET_STATISTICAL_DATA() throws Exception {
        HashMap<String, Object> message = new HashMap<>();
        message.put(TartanParams.COMMAND, TartanParams.MSG_GET_STATISTICAL_DATA);

        adminService.handleMessage(message);
        Vector<Reservation> reservations = adminService.getReservations();
        Mockito.verify(adminService).handleGetStatisticalData(message);
        Mockito.verify(adminService).getRevenue(reservations);
        Mockito.verify(adminService).getAverageOccupancy(reservations);
        Mockito.verify(adminService).getPeakUsageHours(reservations);
    }

    @Test
    public void getRevenue_0_IfEmpty() throws Exception {
        Vector<Reservation> reservations = new Vector<>();
        long revenue = adminService.getRevenue(reservations);
        Assert.assertEquals(0, revenue);
    }

    @Test
    public void getRevenue_Sum_IfNotEmpty() throws Exception {
        Vector<Reservation> reservations = new Vector<>();
        ArrayList<Long> feeList = new ArrayList<>();
        feeList.add(10L);
        feeList.add(20L);
        feeList.add(15L);

        long expected = 0L;
        for (Long fee : feeList) {
            Reservation newReservation = new Reservation();
            newReservation.setPayment(new Payment(fee));
            reservations.add(newReservation);
            expected += fee;
        }

        long revenue = adminService.getRevenue(reservations);
        Assert.assertEquals(expected, revenue);
    }

    @Test
    public void getAverageOccupancy_0_IfEmpty() throws Exception {
        Vector<Reservation> reservations = new Vector<>();
        HashMap<String, Integer> averageOccupancy = adminService.getAverageOccupancy(reservations);
        Assert.assertEquals(0, averageOccupancy.size());
    }

    @Test
    public void getAverageOccupancy_0_IfNotEmpty() throws Exception {
        Vector<Reservation> reservations = new Vector<>();
        ArrayList<ArrayList<String>> dateList = new ArrayList<>();
        ArrayList<String> dateItem1 = new ArrayList<>();
        dateItem1.add("2017:07:25:10:45");
        dateItem1.add("2017:07:25:12:11");
        dateList.add(dateItem1);

        ArrayList<String> dateItem2 = new ArrayList<>();
        dateItem2.add("2017:07:25:11:00");
        dateItem2.add("2017:07:25:14:11");
        dateList.add(dateItem2);

        HashMap<String, Integer> occupancyMap = new HashMap<>();

        for (ArrayList<String> date : dateList) {
            Reservation newReservation = new Reservation();
            newReservation.setStartTime(date.get(0));
            newReservation.setEndTime(date.get(1));
            reservations.add(newReservation);

            Date startTime = newReservation.getStartTime();
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(startTime);
            int startHour = calendar.get(Calendar.HOUR_OF_DAY);
            Date endTime = newReservation.getEndTime();
            calendar.setTime(endTime);
            int endHour = calendar.get(Calendar.HOUR_OF_DAY);
            int usageHours = endHour - startHour + 1;
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String hashKey = String.valueOf(year) + ":";
            if (month < 10) {
                hashKey += "0";
            }
            hashKey +=  String.valueOf(month) + ":" + String.valueOf(day);
            int dailyCount = usageHours;
            if (occupancyMap.get(hashKey) != null) {
                dailyCount += occupancyMap.get(hashKey);
            }
            occupancyMap.put(hashKey, dailyCount);
        }

        HashMap<String, Integer> averageOccupancy = adminService.getAverageOccupancy(reservations);
        int expected = occupancyMap.get("2017:07:25") * 100 / (24 * 4);
        int actual = averageOccupancy.get("2017:07:25");
        System.out.println("oracle - expected: " + expected + ", actual: " + actual);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getPeakUsageHours_0_IfEmpty() throws Exception {
        Vector<Reservation> reservations = new Vector<>();
        ArrayList<Integer> peakUsageHours = adminService.getPeakUsageHours(reservations);
        Assert.assertEquals(0, peakUsageHours.size());
    }

    @Test
    public void getPeakUsageHours_PeakUsageHoursList_IfNotEmpty() throws Exception {
        Vector<Reservation> reservations = new Vector<>();
        ArrayList<ArrayList<String>> dateList = new ArrayList<>();
        ArrayList<String> dateItem1 = new ArrayList<>();
        dateItem1.add("2017:07:25:10:45");
        dateItem1.add("2017:07:25:12:11");
        dateList.add(dateItem1);

        ArrayList<String> dateItem2 = new ArrayList<>();
        dateItem2.add("2017:07:25:11:00");
        dateItem2.add("2017:07:25:14:11");
        dateList.add(dateItem2);

        for (ArrayList<String> date : dateList) {
            Reservation newReservation = new Reservation();
            newReservation.setStartTime(date.get(0));
            newReservation.setEndTime(date.get(1));
            reservations.add(newReservation);
        }
        ArrayList<Integer> peakUsageHours = adminService.getPeakUsageHours(reservations);
        System.out.println("peakUsageHours: " + peakUsageHours);
        int expected[] = {11, 12};
        Assert.assertEquals(expected[0], (int)peakUsageHours.get(0));
        Assert.assertEquals(expected[1], (int)peakUsageHours.get(1));
    }

    @Test
    public void terminate() throws Exception {
    }

    @Test
    public void loadAdminAuth_Exception_IfWrongPath() throws Exception {
        //PowerMockito.mockStatic(Logger.class);
        adminService.loadAdminAuth("--");
        //PowerMockito.verifyStatic(Mockito.times(1));
        //Logger.logMsg(any(Integer.class), any(String.class));
    }

}