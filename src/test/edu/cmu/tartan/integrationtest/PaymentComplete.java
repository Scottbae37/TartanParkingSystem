package edu.cmu.tartan.integrationtest;

import edu.cmu.tartan.edu.cmu.tartan.reservation.Payment;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import edu.cmu.tartan.edu.cmu.tartan.reservation.ReservationStore;
import edu.cmu.tartan.edu.cmu.tartan.reservation.ReservationStoreTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.time.Year;
import java.time.YearMonth;

/**
 * @author sanghyuck.na
 * <p>
 * integartion teset between reservation and payment
 */
@RunWith(PowerMockRunner.class)
public class PaymentComplete {

    File mFile;
    ReservationStore mRs;
    private YearMonth mYearMonth;
    private Year mYear;

    @Before
    public void before() {
        mFile = new File("./unit_test");
        mFile.mkdir();
        mRs = new ReservationStore(mFile.getPath());

    }

    @After
    public void after() {
        ReservationStoreTest.deleteDirectory(mFile);
    }

    /**
     * Test Case ID	TS03-TC01 - Payment try with invalid card number
     */
    @PrepareForTest({Year.class, Payment.class})
    @Test
    public void testInvalidCardNumber() {
        // Current time is 17-08-09 15:00
        mYear = Year.of(2017);
        PowerMockito.mockStatic(Year.class);
        PowerMockito.when(Year.now()).thenReturn(mYear);

        mYearMonth = YearMonth.of(2017, 8);
        PowerMockito.mockStatic(YearMonth.class);
        PowerMockito.when(YearMonth.now()).thenReturn(mYearMonth);

        Assert.assertTrue(mRs.getReservations().isEmpty());

        Reservation res = new Reservation();
        res.setCustomerName("somebody");
        res.setIsPaid(false);
        res.setStartTime("2017:08:09:16:00");
        res.setEndTime("2017:08:09:17:00");
        res.setVehicleID("abcd1234");
        mRs.addReservation(res);
        Assert.assertTrue(mRs.getReservations().size() == 1);
        mRs.saveNewReservation(res);

        // 1. Car is detected at entry
        // 2. Payment window displayed
        // 3. Try to complete payment with card number=" ", name="somebody", exp. date="20-12-31"

        Payment payment = new Payment();
        payment.setCcNum(" ");
        payment.setCcName("somebody");
        payment.setCcExpDate("December\\2020");
        Assert.assertEquals(Boolean.TRUE, payment.isValid());

        // 4. Payment fail notified
    }

    /**
     * TS03-TC02 - Payment try with invalid user name
     */
    @PrepareForTest({Year.class, Payment.class})
    @Test
    public void testInvalidUsername() {
        // Current time is 17-08-09 15:00
        mYear = Year.of(2017);
        PowerMockito.mockStatic(Year.class);
        PowerMockito.when(Year.now()).thenReturn(mYear);

        mYearMonth = YearMonth.of(2017, 8);
        PowerMockito.mockStatic(YearMonth.class);
        PowerMockito.when(YearMonth.now()).thenReturn(mYearMonth);

        Assert.assertTrue(mRs.getReservations().isEmpty());

        Reservation res = new Reservation();
        res.setCustomerName("somebody");
        res.setIsPaid(false);
        res.setStartTime("2017:08:09:16:00");
        res.setEndTime("2017:08:09:17:00");
        res.setVehicleID("abcd1234");
        mRs.addReservation(res);
        Assert.assertTrue(mRs.getReservations().size() == 1);
        mRs.saveNewReservation(res);

        // 1. Car is detected at entry
        // 2. Payment window displayed
        // 3. Try to complete payment with card number=" ", name="somebody", exp. date="20-12-31"

        Payment payment = new Payment();
        payment.setCcNum("0000 1111 2222 3333");
        payment.setCcName("anybody");
        payment.setCcExpDate("December\\2020");
        Assert.assertEquals(Boolean.TRUE, payment.isValid());

        // 4. Payment fail notified
    }

    /**
     * TS03-TC03 - Payment try with expired card
     */
    @PrepareForTest({Year.class, Payment.class})
    @Test
    public void testExpiredCard() {
        // Current time is 17-08-09 15:00
        mYear = Year.of(2017);
        PowerMockito.mockStatic(Year.class);
        PowerMockito.when(Year.now()).thenReturn(mYear);

        mYearMonth = YearMonth.of(2017, 8);
        PowerMockito.mockStatic(YearMonth.class);
        PowerMockito.when(YearMonth.now()).thenReturn(mYearMonth);

        Assert.assertTrue(mRs.getReservations().isEmpty());

        Reservation res = new Reservation();
        res.setCustomerName("somebody");
        res.setIsPaid(false);
        res.setStartTime("2017:08:09:16:00");
        res.setEndTime("2017:08:09:17:00");
        res.setVehicleID("abcd1234");
        mRs.addReservation(res);
        Assert.assertTrue(mRs.getReservations().size() == 1);
        mRs.saveNewReservation(res);

        // 1. Car is detected at entry
        // 2. Payment window displayed
        // 3. Try to complete payment with card number=" ", name="somebody", exp. date="20-12-31"

        Payment payment = new Payment();
        payment.setCcNum("0000 1111 2222 3333");
        payment.setCcName("somebody");
        payment.setCcExpDate("December\\2016");
        Assert.assertEquals(Boolean.FALSE, payment.isValid());
        // 4. Payment fail notified
    }


    /**
     * TS03-TC04 - Payment try with adequate card info. at reservation making time
     */
    @PrepareForTest({Year.class, Payment.class})
    @Test
    public void testValidPaymentTime() {
        // Current time is 17-08-09 15:00
        mYear = Year.of(2017);
        PowerMockito.mockStatic(Year.class);
        PowerMockito.when(Year.now()).thenReturn(mYear);

        mYearMonth = YearMonth.of(2017, 8);
        PowerMockito.mockStatic(YearMonth.class);
        PowerMockito.when(YearMonth.now()).thenReturn(mYearMonth);

        Assert.assertTrue(mRs.getReservations().isEmpty());

        Reservation res = new Reservation();
        res.setCustomerName("somebody");
        res.setIsPaid(false);
        res.setStartTime("2017:08:09:16:00");
        res.setEndTime("2017:08:09:17:00");
        res.setVehicleID("abcd1234");
        mRs.addReservation(res);
        Assert.assertTrue(mRs.getReservations().size() == 1);
        mRs.saveNewReservation(res);

        // 1. Car is detected at entry
        // 2. Payment window displayed
        // 3. Try to complete payment with card number=" ", name="somebody", exp. date="20-12-31"

        Payment payment = new Payment();
        payment.setCcNum("0000 1111 2222 3333");
        payment.setCcName("somebody");
        payment.setCcExpDate("December\\2020");
        Assert.assertEquals(Boolean.TRUE, payment.isValid());
        // 4. Payment fail notified
    }

}

