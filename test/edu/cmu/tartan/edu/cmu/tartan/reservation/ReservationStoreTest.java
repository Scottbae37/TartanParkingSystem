package edu.cmu.tartan.edu.cmu.tartan.reservation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;

import static org.junit.Assert.*;

/**
 * Created by jaeseung.bae on 7/17/2017.
 */
public class ReservationStoreTest {
    private ReservationStore cut;

    @Before
    public void setUp() throws Exception {
        cut = new ReservationStore(null);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getReservations() throws Exception {
        fail("Intended Fail @ getReservations()");
    }

    @Test
    public void lookupByCustomer() throws Exception {
        fail("Intended Fail @ lookupByCustomer()");
    }

    @Test
    public void lookupByVehicle() throws Exception {
        fail("Intended Fail @ lookupByVehicle()");
    }

    @Test
    public void addReservation() throws Exception {
        fail("Intended Fail @ addReservation()");
    }

    @Test
    public void loadReservations() throws Exception {
        fail("Intended Fail @ loadReservations()");
    }

    @Test
    public void shutdown() throws Exception {
        fail("Intended Fail @ shutdown()");
    }

    @Test
    public void isDuplicate() throws Exception {
        fail("Intended Fail @ isDuplicate()");
    }

    @Test
    public void saveNewReservation() throws Exception {
        fail("Intended Fail @ saveNewReservation()");
    }

    @Test
    public void markReservationRedeemed() throws Exception {
        fail("Intended Fail @ markReservationRedeemed()");
    }

}