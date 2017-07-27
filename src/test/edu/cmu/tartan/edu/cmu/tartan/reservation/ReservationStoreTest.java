package edu.cmu.tartan.edu.cmu.tartan.reservation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import static org.junit.Assert.assertEquals;

/**
 * Created by jaeseung.bae on 7/17/2017.
 */
@RunWith(PowerMockRunner.class)
public class ReservationStoreTest {
    private ReservationStore reservationStore;

    private static final String RESERVATION_STORE = "rsvp.txt";
    private static final String STATICS_STORE = "statics.txt";

    /**
     * The path to the reservation database.
     */
    private final String settingsPath = "./";

    private BufferedWriter bufferedWriter;
    private BufferedWriter reservationWriter;
    private BufferedWriter staticsWriter;
    private BufferedReader bufferedReader;
    private BufferedReader reservationReader;
    private BufferedReader staticsReader;

    @Before
    public void setUp() throws Exception {
        reservationStore = PowerMockito.mock(ReservationStore.class);
        PowerMockito.whenNew(ReservationStore.class).withArguments(Mockito.anyString()).thenReturn(reservationStore);


        bufferedWriter = PowerMockito.mock(BufferedWriter.class);
        PowerMockito.whenNew(BufferedWriter.class).withAnyArguments().thenReturn(bufferedWriter);

        reservationWriter = Files.newBufferedWriter(Paths.get(settingsPath + File.separator + RESERVATION_STORE), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        staticsWriter = Files.newBufferedWriter(Paths.get(settingsPath + File.separator + STATICS_STORE), StandardCharsets.UTF_8, StandardOpenOption.APPEND);


        bufferedReader = PowerMockito.mock(BufferedReader.class);
        PowerMockito.whenNew(BufferedReader.class).withAnyArguments().thenReturn(bufferedReader);

        reservationReader = Files.newBufferedReader(Paths.get(settingsPath + File.separator + RESERVATION_STORE), StandardCharsets.UTF_8);
        staticsReader = Files.newBufferedReader(Paths.get(settingsPath + File.separator + STATICS_STORE), StandardCharsets.UTF_8);

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getReservations() throws Exception {
//        fail("Intended Fail @ getReservations()");
    }

    @Test
    public void lookupByCustomer() throws Exception {
//        fail("Intended Fail @ lookupByCustomer()");
    }

    @Test
    public void lookupByVehicle() throws Exception {
//        fail("Intended Fail @ lookupByVehicle()");
    }

    @Test
    public void addReservation() throws Exception {
//        fail("Intended Fail @ addReservation()");
    }

    @Test
    public void loadReservations() throws Exception {
//        fail("Intended Fail @ loadReservations()");
    }

    @Test
    public void shutdown() throws Exception {
//        fail("Intended Fail @ shutdown()");
    }

    @Test
    public void isDuplicate() throws Exception {
//        fail("Intended Fail @ isDuplicate()");
    }

    @Test
    public void saveNewReservation() throws Exception {
//        fail("Intended Fail @ saveNewReservation()");
    }

    @Test
    public void markReservationRedeemed() throws Exception {
//        fail("Intended Fail @ markReservationRedeemed()");
    }

    @org.junit.Test
    public void loadCumulativeReservationsTest() throws Exception {


        Vector<Reservation> reservations = new Vector<>();

        Reservation reserve = Mockito.mock(Reservation.class);

        Reservation reservation = Mockito.mock(Reservation.class);


        String line;
        while ((line = staticsReader.readLine()) != null) { // one reservation per line

            String[] entries = line.split(",");
            for (String entry : entries) {

                String[] item = entry.split("=");
                String key = item[0];
                String val = item[1];

                if (key.equals("name")) {
                    reservation.setCustomerName(val);
                } else if (key.equals("start")) {
                    reservation.setStartTime(val);
                } else if (key.equals("end")) {
                    reservation.setEndTime(val);
                } else if (key.equals("vehicle")) {
                    reservation.setVehicleID(val);
                } else if (key.equals("spot")) {
                    reservation.setSpotId(Integer.parseInt(val));
                } else if (key.equals("paid")) {
                    reservation.setIsPaid(Boolean.valueOf(val));
                }
            }

        }
        Mockito.when(reservationStore.getReservations()).thenReturn(reservations);
        Mockito.verify(reservation).setSpotId(Mockito.anyInt());

    }

    @org.junit.Test
    public void saveStaticsInfoTest() throws Exception {

        File mockFile = PowerMockito.mock(File.class);

        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(mockFile);
        mockFile = new File("./" + File.separator + "unit.txt");

        FileWriter wr = PowerMockito.mock(FileWriter.class);
        PowerMockito.whenNew(FileWriter.class).withAnyArguments().thenReturn(wr);
        wr = new FileWriter(mockFile);
        BufferedWriter bw = PowerMockito.mock(BufferedWriter.class);
        PowerMockito.whenNew(BufferedWriter.class).withAnyArguments().thenReturn(bw);
        bw = new BufferedWriter(wr);

        Reservation reservation = Mockito.mock(Reservation.class);

        Mockito.when(reservation.getCustomerName()).thenReturn("unitname");
        Mockito.when(reservation.getCustomerName()).thenReturn("unitvehicle");
        Date startDate = Calendar.getInstance().getTime();
        Date endDate = new Date();
        startDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        endDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        Mockito.when(reservation.getStartTime()).thenReturn(startDate);
        Mockito.when(reservation.getEndTime()).thenReturn(endDate);

        Payment payment = Mockito.mock(Payment.class);
        Mockito.when(reservation.getPayment()).thenReturn(payment);
        Mockito.when(payment.getFee()).thenReturn(50L);
        reservation.setPayment(payment);

        System.out.println("reservation.getCustomerName() = " + reservation.getCustomerName());
        wr.write("name=" + reservation.getCustomerName() + ",vehicle=" + reservation.getVehicleID() + ",start= " + startDate + ",end=" + endDate + ",paid=" + String.valueOf(reservation.getIsPaid()) + ",spot=" + reservation.getSpotId().toString() + ",fee=" + reservation.getPayment().getFee().toString() + "\n");


        FileReader fr = PowerMockito.mock(FileReader.class);
        PowerMockito.whenNew(FileReader.class).withAnyArguments().thenReturn(fr);
        fr = new FileReader(mockFile);

        BufferedReader br = PowerMockito.mock(BufferedReader.class);
        PowerMockito.whenNew(BufferedReader.class).withAnyArguments().thenReturn(br);
        br = new BufferedReader(fr);


        String line;
        while ((line = br.readLine()) != null) { // one reservation per line

            String[] entries = line.split(",");
            for (String entry : entries) {

                String[] item = entry.split("=");
                String key = item[0];
                String val = item[1];

                if (key.equals("name")) {
                    assertEquals(reservation.getCustomerName(), val);
                }
                if (key.equals("vehicle")) {
                    assertEquals(reservation.getVehicleID(), val);
                }
                if (key.equals("fee")) {
                    assertEquals(reservation.getPayment().getFee(), val);
                }
            }
        }
    }

}