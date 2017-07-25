package edu.cmu.tartan.edu.cmu.tartan.reservation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

/**
 * The data store for reservations.
 * <p>
 * Project: LG Exec Ed SDET Program
 * Copyright: 2017 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2016 - initial version
 */
public class ReservationStore {

    /**
     * The flat file that contains all the reservations.
     */
    private static final String RESERVATION_STORE = "rsvp.txt";

    /**
     * The path to the reservation database.
     */
    private final String settingsPath;

    /**
     * The list of all reservations.
     */
    private Vector<Reservation> reservations = new Vector<Reservation>();

    /**
     * Fetch all reservations.
     *
     * @return The list of reservations.
     */
    public Vector<Reservation> getReservations() {
        return reservations;
    }

    public ReservationStore(String path) {
        settingsPath = path;
    }

    /**
     * Get the set of reservations for a given customer.
     *
     * @param name The name of the customer.
     * @return The reservations associated with a customer.
     */
    public Vector<Reservation> lookupByCustomer(String name) {
        Vector<Reservation> results = new Vector<Reservation>();
        for (Reservation r : reservations) {
            if (r.getCustomerName().equals(name)) {
                results.add(r);
            }
        }
        return results;
    }

    /**
     * Get the reservations for a given vehicle
     *
     * @param name the id of the vehicle (license plate).
     * @return The list of reservations for a given vehicle.
     */
    public Vector<Reservation> lookupByVehicle(String name) {
        Vector<Reservation> results = new Vector<Reservation>();
        for (Reservation r : reservations) {
            if (r.getCustomerName().equals(name)) { /* FIXME: Maybe Copy & Paste bug, should check vehicle */
                results.add(r);
            }
        }
        return results;
    }

    /**
     * Add a reservation to the database.
     *
     * @param r the new reservation.
     * @return True when the reservation is added.
     */
    public Boolean addReservation(Reservation r) {
        reservations.add(r);
        return true;
    }

    /**
     * Load reservations from a file. This occurs on start up.
     *
     * @throws Exception
     */
    public void loadReservations() throws Exception {

        try (BufferedReader br = Files.newBufferedReader(Paths.get(settingsPath + File.separator + RESERVATION_STORE), StandardCharsets.UTF_8)) {

            String line;
            while ((line = br.readLine()) != null) { // one reservation per line
                Reservation reservation = new Reservation();
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
                addReservation(reservation);
            }
        }
    }

    /**
     * On shutdown, save the updated reservation list.
     */
    public void shutdown() {

        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(settingsPath + File.separator + RESERVATION_STORE), StandardCharsets.UTF_8)) {

            DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm");

            for (Reservation rsvp : reservations) {

                if (!rsvp.getIsRedeemed()) {
                    //to convert Date to String, use format method of SimpleDateFormat class.
                    String startDate = dateFormat.format(rsvp.getStartTime());
                    String endDate = dateFormat.format(rsvp.getEndTime());

                    bw.write("name=" + rsvp.getCustomerName() +
                            ",vehicle=" + rsvp.getVehicleID() +
                            ",start= " + startDate + ",end=" + endDate +
                            ",paid=" + String.valueOf(rsvp.getIsPaid()) +
                            ",spot=" + rsvp.getSpotId().toString() + "\n");
                }
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check for duplicate reservation.
     *
     * @param rsvp The reservation to check for.
     * @return true if duplicate found, false otherwise.
     */
    public Boolean isDuplicate(Reservation rsvp) {

        for (Reservation r : reservations) {
            if (r.equals(rsvp)) {
                return true;
            }
        }
        return false;

    }

    /**
     * Save a new reservation to the database.
     *
     * @param newRsvp The new reservation.
     * @return True if successfully saved, false otherwise.
     */
    public Boolean saveNewReservation(Reservation newRsvp) {

        if (!isDuplicate(newRsvp)) {
            return false;
        }

        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(settingsPath + File.separator + RESERVATION_STORE), StandardCharsets.UTF_8)){

            DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm");

            //to convert Date to String, use format method of SimpleDateFormat class.
            String startDate = dateFormat.format(newRsvp.getStartTime());
            String endDate = dateFormat.format(newRsvp.getEndTime());

            bw.write("name=" + newRsvp.getCustomerName() +
                    ",vehicle=" + newRsvp.getVehicleID() +
                    ",start= " + startDate + ",end=" + endDate +
                    ",paid=" + String.valueOf(newRsvp.getIsPaid()) +
                    ",spot=" + newRsvp.getSpotId().toString() + "\n");

            return true;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /***
     * Indicate that a reservation has been redeemed so that it cannot be redeemed twice.
     *
     * @param rsvp The redeemed reservation.
     */
    public void markReservationRedeemed(Reservation rsvp) {

        for (Reservation r : reservations) {
            if (r.equals(rsvp)) {
                r.setIsRedeemed(true);
            }
        }
    }
}

