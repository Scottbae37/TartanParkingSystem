package edu.cmu.tartan.edu.cmu.tartan.reservation;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
public class ReservationStoreTest {

    private ReservationStore reservationStore;
    private String customerName = "UnitTest";
    private String vehicleId = "UnitTest";
    private File file;
    private int numberOfSpot = 4;
    private static Date initDate;

    @BeforeClass
    public static void setUpForClass() {
        initDate = Calendar.getInstance().getTime();
    }

    @Before
    public void setUp() throws Exception {
        file = new File("./unit_test");
        file.mkdir();
        reservationStore = new ReservationStore(file.getPath());
    }

    @After
    public void tearDown() throws Exception {
        deleteDirectory(file);
    }

    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
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
        String currentDB = getCurrentReservationDBData();
        for (Reservation reservation : getReservations(false)) {
            reservationStore.addReservation(reservation);
            assertTrue(reservationStore.saveNewReservation(reservation));
        }
        Assert.assertNotEquals(currentDB, getCurrentReservationDBData());
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
        String currentDBData = getCurrentReservationDBData();
        reservationStore.addReservation(reservation);
        reservationStore.shutdown();
        String afterShutDownDBData = getCurrentReservationDBData();
        Assert.assertNotEquals(currentDBData, afterShutDownDBData);
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
        String currentDB = getCurrnetStaticDBData();
        for (Reservation reservation : getReservations(true)) {
            assertTrue(reservationStore.saveStaticsInfo(reservation));
        }
        Assert.assertNotEquals(currentDB, getCurrnetStaticDBData());
    }

    @org.junit.Test
    public void loadCumulativeReservations() throws Exception {
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

    private String getCurrentReservationDBData() {
        String ret = "";

        try (BufferedReader br = Files.newBufferedReader(Paths.get(file.getPath() + File.separator + "rsvp.txt"), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                ret += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    private String getCurrnetStaticDBData() {
        String ret = "";

        try (BufferedReader br = Files.newBufferedReader(Paths.get(file.getPath() + File.separator + "statics.txt"), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                ret += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
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
