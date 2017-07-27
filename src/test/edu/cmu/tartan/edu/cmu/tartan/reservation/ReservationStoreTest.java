package edu.cmu.tartan.edu.cmu.tartan.reservation;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by jaeseung.bae on 7/17/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ReservationStore.class, Files.class})
public class ReservationStoreTest {

    private ReservationStore reservationStore;
    private static BufferedReader bufferedReader;
    private static BufferedWriter bufferedWriter;
    private static DBReplacer dbReplacer;
    private String customerName = "UnitTest";
    private String vehicleId = "UnitTest";
    private int numberOfSpot = 4;
    private static Date initDate;

    @BeforeClass
    public static void setUpForClass() {
        initDate = Calendar.getInstance().getTime();
    }

    @Before
    public void setUp() throws Exception {
        reservationStore = new ReservationStore("./");
        dbReplacer = new DBReplacer();
        bufferedReader = Mockito.mock(BufferedReader.class);
        bufferedWriter = Mockito.mock(BufferedWriter.class);
        PowerMockito.mockStatic(Files.class);
        PowerMockito.when(Files.newBufferedReader(Mockito.any(Path.class), Mockito.any(Charset.class))).thenReturn(bufferedReader);
        PowerMockito.when(Files.newBufferedWriter(Mockito.any(Path.class), Mockito.any(Charset.class), Mockito.any(OpenOption.class))).thenReturn(bufferedWriter);
        PowerMockito.when(Files.newBufferedWriter(Mockito.any(Path.class), Mockito.any(Charset.class))).thenReturn(bufferedWriter);
        Mockito.when(bufferedReader.readLine()).thenAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) {
                return dbReplacer.readLine();
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        dbReplacer.initPtr();
    }

    @Test
    public void addAndGetReservations() throws Exception {
        Reservation reservation = Mockito.mock(Reservation.class);
        Assert.assertTrue(reservationStore.getReservations().isEmpty());
        reservationStore.addReservation(reservation);
        Assert.assertTrue(reservationStore.getReservations().size() == 1);
    }

    @Test
    public void lookupByCustomer() throws Exception {
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservation.getCustomerName()).thenReturn(customerName);
        reservationStore.addReservation(reservation);
        for (Reservation res : reservationStore.lookupByCustomer(customerName)) {
            Assert.assertTrue(customerName.equals(res.getCustomerName()));
        }
    }

    @Test
    public void lookupByVehicle() throws Exception {
        Reservation reservation = Mockito.mock(Reservation.class);
        Mockito.when(reservation.getVehicleID()).thenReturn(vehicleId);
        reservationStore.addReservation(reservation);
        for (Reservation res : reservationStore.lookupByVehicle(vehicleId)) {
            Assert.assertTrue(vehicleId.equals(res.getVehicleID()));
        }
    }

    @Test
    public void saveNewReservation() throws Exception {
        for (Reservation reservation : getReservations(false)) {
            reservationStore.addReservation(reservation);
            assertTrue(reservationStore.saveNewReservation(reservation));
        }
        saveDBToMemory(false);
    }

    @Test
    public void loadReservations() throws Exception {
        saveNewReservation();
        reservationStore.loadReservations();
        ArrayList<Reservation> dumpReservation = getReservations(false);
        Vector<Reservation> loadedReservation = reservationStore.getReservations();
        for (int i = 0; i < numberOfSpot; i++) {
            assertTrue(dumpReservation.get(i).getCustomerName().equals(loadedReservation.get(i).getCustomerName()));
            assertTrue(dumpReservation.get(i).getVehicleID().equals(loadedReservation.get(i).getVehicleID()));
            assertTrue(dumpReservation.get(i).getIsPaid() == loadedReservation.get(i).getIsPaid());
            assertTrue(dumpReservation.get(i).getSpotId().equals(loadedReservation.get(i).getSpotId()));
            assertTrue(dumpReservation.get(i).getStartTime().equals(loadedReservation.get(i).getStartTime()));
            assertTrue(dumpReservation.get(i).getEndTime().equals(loadedReservation.get(i).getEndTime()));
        }
    }

    @Test
    public void shutdown() throws Exception {
        Reservation reservation = Mockito.mock(Reservation.class);
        Date startDate = new Date();
        Date endDate = new Date();
        startDate.setTime(initDate.getTime());
        endDate.setTime(initDate.getTime());
        startDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        endDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        Mockito.when(reservation.getStartTime()).thenReturn(startDate);
        Mockito.when(reservation.getEndTime()).thenReturn(endDate);
        Mockito.when(reservation.getIsRedeemed()).thenReturn(false);
        reservationStore.addReservation(reservation);
        reservationStore.shutdown();

        ArgumentCaptor<String> writeMsg = ArgumentCaptor.forClass(String.class);
        Mockito.verify(bufferedWriter).write(writeMsg.capture());
        assertTrue(writeMsg.getValue() != null && !writeMsg.getValue().equals(""));
        dbReplacer.addReservation(writeMsg.getValue());
    }

    @Test
    public void isDuplicate() throws Exception {
        Reservation reservation = Mockito.mock(Reservation.class);
        Reservation reservation1 = Mockito.mock(Reservation.class);
        assertFalse(reservationStore.isDuplicate(reservation));
        reservationStore.addReservation(reservation);
        assertTrue(reservationStore.isDuplicate(reservation));
        assertFalse(reservationStore.isDuplicate(reservation1));
    }


    @Test
    public void markReservationRedeemed() throws Exception {
        Reservation reservation = Mockito.mock(Reservation.class);

        //Reservation is empty
        reservationStore.markReservationRedeemed(Mockito.mock(Reservation.class));


        reservationStore.addReservation(reservation);

        //Not matched reservation
        reservationStore.markReservationRedeemed(Mockito.mock(Reservation.class));

        //Matched reservation
        reservationStore.markReservationRedeemed(reservation);
        Mockito.verify(reservation).setIsRedeemed(true);
    }


    @org.junit.Test
    public void saveStaticsInfoTest() throws Exception {
        long i = 1000;
        for (Reservation reservation : getReservations(true)) {
            assertTrue(reservationStore.saveStaticsInfo(reservation));
        }
        saveDBToMemory(true);
    }

    @org.junit.Test
    public void loadCumulativeReservations() throws Exception {
        Mockito.when(bufferedReader.readLine()).thenAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) {
                return dbReplacer.readLineFromStatics();
            }
        });
        saveStaticsInfoTest();
        reservationStore.loadCumulativeReservations();
        ArrayList<Reservation> dumpReservation = getReservations(true);
        Vector<Reservation> loadedReservation = reservationStore.getReservations();
        for (int i = 0; i < numberOfSpot; i++) {
            assertTrue(dumpReservation.get(i).getCustomerName().equals(loadedReservation.get(i).getCustomerName()));
            assertTrue(dumpReservation.get(i).getVehicleID().equals(loadedReservation.get(i).getVehicleID()));
            assertTrue(dumpReservation.get(i).getIsPaid() == loadedReservation.get(i).getIsPaid());
            assertTrue(dumpReservation.get(i).getSpotId().equals(loadedReservation.get(i).getSpotId()));
            //assertTrue(dumpReservation.get(i).getStartTime().equals(loadedReservation.get(i).getStartTime()));
            //assertTrue(dumpReservation.get(i).getEndTime().equals(loadedReservation.get(i).getEndTime()));
            //assertTrue(dumpReservation.get(i).getPayment().getFee() == loadedReservation.get(i).getPayment().getFee());
        }
    }

    private void saveDBToMemory(boolean isStatic) throws Exception {
        ArgumentCaptor<String> writeMsg = ArgumentCaptor.forClass(String.class);
        Mockito.verify(bufferedWriter, Mockito.times(numberOfSpot)).write(writeMsg.capture());
        List<String> capturedMsg = writeMsg.getAllValues();

        for (String msg : capturedMsg) {
            assertTrue(msg != null && !msg.equals(""));
            if (isStatic) {
                dbReplacer.addReservationToStatics(msg);
            } else {
                dbReplacer.addReservation(msg);
            }
        }
    }

    private ArrayList<Reservation> getReservations(boolean isStatic) throws Exception {
        ArrayList<Reservation> rets = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
        for (int i = 0; i < numberOfSpot; i++) {
            Reservation reservation = Mockito.mock(Reservation.class);
            Date startDate = new Date();
            Date endDate = new Date();
            startDate.setTime(initDate.getTime());
            endDate.setTime(initDate.getTime());
            startDate.setTime(startDate.getTime() + 1000 * 60 * 60 * (i + 1));
            endDate.setTime(startDate.getTime() + 1000 * 60 * 60 * (i + 1));
            startDate.setSeconds(0);
            endDate.setSeconds(0);
            Mockito.when(reservation.getStartTime()).thenReturn(startDate);
            Mockito.when(reservation.getEndTime()).thenReturn(endDate);
            Mockito.when(reservation.getCustomerName()).thenReturn(customerName + i);
            Mockito.when(reservation.getVehicleID()).thenReturn(vehicleId + i);
            Mockito.when(reservation.getSpotId()).thenReturn(i);
            if (i % 2 == 0) {
                Mockito.when(reservation.getIsPaid()).thenReturn(true);
            }
            if (isStatic) {
                Payment payment = Mockito.mock(Payment.class);
                Mockito.when(payment.getFee()).thenReturn((long) i * 1000);
                Mockito.when(reservation.getPayment()).thenReturn(payment);
            }
            rets.add(reservation);
        }

        return rets;
    }
}

class DBReplacer {

    private ArrayList<String> db;
    private ArrayList<String> staticsDB;
    private int ptr;
    private int ptrForStatics;

    public DBReplacer() {
        db = new ArrayList<>();
        staticsDB = new ArrayList<>();
        ptr = 0;
        ptrForStatics = 0;
    }

    public void addReservation(String msg) {
        db.add(msg.trim());
    }

    public void addReservationToStatics(String msg) {
        staticsDB.add(msg.trim());
    }

    public String readLine() {
        if (db.size() == ptr) {
            return null;
        }
        String ret = db.get(ptr);
        ptr++;
        return ret;
    }

    public String readLineFromStatics() {
        if (staticsDB.size() == ptrForStatics) {
            return null;
        }
        String ret = staticsDB.get(ptrForStatics);
        ptrForStatics++;
        return ret;
    }

    public void initPtr() {
        ptr = 0;
        ptrForStatics = 0;
    }


}