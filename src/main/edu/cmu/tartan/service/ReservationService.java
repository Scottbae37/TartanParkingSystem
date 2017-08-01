package edu.cmu.tartan.service;

import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import edu.cmu.tartan.edu.cmu.tartan.reservation.ReservationStore;

import java.util.*;

/**
 * Manages reservations for the system.
 * <p>
 * Project: LG Exec Ed SDET Program
 * Copyright: 2017 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2016 - initial version
 */
public class ReservationService extends TartanService {

    /**
     * The service name
     */
    public final static String RESERVATION_SERVICE = "RsvpService";

    /**
     * The list of parking spots.
     */
    ArrayList<Integer> parkingSpots = null;

    /**
     * The path to the reservation database
     */
    private String configPath = null;

    /**
     * The reservation database adapter
     */
    private ReservationStore rsvpStore;

    /**
     * The constructor for the service.
     *
     * @param cp the configuration to use.
     */
    public ReservationService(String cp) {

        super.init(RESERVATION_SERVICE);

        configPath = cp;
    }

    /**
     * Run the service. On startup query the parking service for available spots.
     */
    @Override
    public void run() {

        System.out.println("RsvpService.run");

        rsvpStore = new ReservationStore(configPath);
        try {
            rsvpStore.loadReservations();
        } catch (Exception e) {
            e.printStackTrace();
            HashMap<String, Object> response = new HashMap<String, Object>();
            response.put(TartanParams.COMMAND, TartanParams.ERROR);
            response.put(TartanParams.PAYLOAD, e.getMessage());
            sendMessage(TartanParams.SOURCE_ID, response);
        }

        // Ask the parking service about capacity
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put(TartanParams.COMMAND, TartanParams.MSG_GET_PARKING_SPOTS);
        sendMessage(ParkingService.PARKING_SERVICE, body);

        // status = TartanServiceStatus.RUNNING;
    }

    /**
     * Handle reservation messages. the following messages are handled by the reservation service:
     * <p>
     * <ul>
     * <li>MSG_NEW_RSVP: Handle a new reservation request.</li>
     * <li>MSG_CONFIRM_RSVP: Confirm a new reservation.</li>
     * <li>MSG_UPDATE_RSVP: Update an existing reservation.</li>
     * <li>MSG_GET_PARKING_SPOTS: Response to request for parking spots.</li>
     * <li>MSG_REDEEM_RSVP: Redeem a reservation.</li>
     * <li>MSG_GET_ALL_RSVP: Handle request to get all reservations.</li>
     * <li>MSG_COMPLETE_RSVP: Indicates a reservation is completed.</li>
     * </ul>
     *
     * @param message
     */
    @Override
    public void handleMessage(HashMap<String, Object> message) {
        System.out.println("ReservationService.handleMessage");

        if (Objects.isNull(message)) {
            return;
        }

        String cmd = (String) message.get(TartanParams.COMMAND);

        if (cmd.equals(TartanParams.MSG_NEW_RSVP)) {
            handleCreateNewReservation(message);
        } else if (cmd.equals(TartanParams.MSG_CONFIRM_RSVP)) {
            handleConfirmReservation(message);
        } else if (cmd.equals(TartanParams.MSG_UPDATE_RSVP)) {
            handleUpdateReservation(message);
        } else if (cmd.equals(TartanParams.MSG_GET_PARKING_SPOTS)) {
            handleGetParkingSpotsMessage(message);
        } else if (cmd.equals(TartanParams.MSG_REDEEM_RSVP)) {
            handleRedeemReservation(message);
        } else if (cmd.equals(TartanParams.MSG_GET_ALL_RSVP)) {
            handleFetchAllReservations(message);
        } else if (cmd.equals(TartanParams.MSG_COMPLETE_RSVP)) {
            handleCompleteReservation(message);
        } else if (cmd.equals(TartanParams.MSG_PAYMENT_COMPLETE)) {
            handleCompletePayment(message);
        }
    }

    public void handleCompletePayment(HashMap<String, Object> message) {
        Reservation rsvp = (Reservation) message.get(TartanParams.PAYLOAD);
        rsvpStore.saveStaticsInfo(rsvp);
    }

    /**
     * Handle reservation update.
     *
     * @param message The update message.
     */
    private void handleUpdateReservation(HashMap<String, Object> message) {

        Reservation rsvp = (Reservation) message.get(TartanParams.PAYLOAD);
        HashMap<String, Object> response = new HashMap<String, Object>();

        // get a spot for this window
        Integer spot = getParkingSpot(rsvp);


        if (!(spot.equals(TartanParams.INVALID_SPOT)) || !(spot.equals(TartanParams.SPOT_UNAVAILABLE))) {
            response.put(TartanParams.COMMAND, TartanParams.MSG_UPDATE_RSVP);
            rsvp.setSpotId(spot);
            response.put(TartanParams.PAYLOAD, rsvp);
        } else {
            response.put(TartanParams.COMMAND, TartanParams.ERROR);
            response.put(TartanParams.PAYLOAD, "Please call attendant for assistance!");
        }
        sendMessage(KioskService.KIOSK_SERVICE, response);
    }

    /**
     * Indicate that a reservation has been redeemed.
     *
     * @param payload The redeemed reservation.
     */
    private void handleCompleteReservation(HashMap<String, Object> payload) {

        Reservation rsvp = (Reservation) payload.get(TartanParams.PAYLOAD);
        rsvpStore.markReservationRedeemed(rsvp);

    }

    /**
     * Terminate the service.
     */
    @Override
    public void terminate() {
        if(rsvpStore != null)
            rsvpStore.shutdown();
        stop();
    }

    /**
     * Cleanup the service.
     */
    protected void finalize() {
        stop();
    }

    /**
     * Process the returned list of parking spots.
     *
     * @param payload The list of parking spots.
     */
    private void handleGetParkingSpotsMessage(HashMap<String, Object> payload) {
        parkingSpots = (ArrayList<Integer>) payload.get(TartanParams.PAYLOAD);
    }

    /**
     * Check to see if a reservation has a time conflict.
     *
     * @param st1 Start time for the first reservation.
     * @param et1 End time for the first reservation.
     * @param st2 Start time for the second reservation.
     * @param et2 End time for the second reservation.
     * @return true if there is an overlap, false otherwise.
     */
    private Boolean isOverlapped(Date st1, Date et1, Date st2, Date et2) {
        return st1.before(et2) && et1.after(st2);
    }

    // Get a spot for a given time window
    private Integer getParkingSpot(Reservation newRsvp) {

        ArrayList<Integer> occupiedSpots = new ArrayList<Integer>();

        Integer spot = TartanParams.INVALID_SPOT;

        Date newSt = newRsvp.getStartTime();
        Date newEt = newRsvp.getEndTime();

        // build a list of spots occupied at this time
        for (Reservation r : rsvpStore.getReservations()) {
            Date st = r.getStartTime();
            Date et = r.getEndTime();

            if (isOverlapped(st, et, newSt, newEt)) {
                occupiedSpots.add(r.getSpotId());
            }
        }

        // No spots occupied
        if (occupiedSpots.isEmpty()) {
            spot = 0;
        }
        // There are no spots available for this time
        else if (occupiedSpots.size() >= parkingSpots.size()) { /* FIXME: Maybe logical error, Should use >= for fully occupied slots */
            return TartanParams.SPOT_UNAVAILABLE;
        } else {
            Collections.sort(occupiedSpots);
            spot = occupiedSpots.get(occupiedSpots.size() - 1) + 1; // get the next spot
        }
        return spot;
    }

    /**
     * Verify a reservation. Verified reservation must meet various requirements
     *
     * @param rsvp The reservation to verify.
     * @return True if the reservation verified, false otherwise.
     */
    private Boolean verifyReservation(Reservation rsvp) {

        Date start = rsvp.getStartTime();
        Date end = rsvp.getEndTime();

        // start time must come before end time
        if (!(start.before(end))) {
            return false;
        }
        // check bounds for reservation
        long difference = end.getTime() - start.getTime();

        // max reservation is 24 hours
        if ((difference / (1000 * 60 * 60)) > 24) {
            return false;
        }

        // No reservations in the past
        if (Calendar.getInstance().getTime().after(start)) {
            return false;
        }

        // prevent reservations more than a week out
        if ((start.getTime() - System.currentTimeMillis()) >= 604800000) {
            return false;
        }
        if ((end.getTime() - System.currentTimeMillis()) >= 604800000) {
            return false;
        }

        // check other parameters
        if (rsvp.getCustomerName() == null || rsvp.getCustomerName().trim().isEmpty()) {
            return false;
        }

        if (rsvp.getVehicleID() == null || rsvp.getVehicleID().trim().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Create the new reservation.
     *
     * @param request The message to create a reservation.
     */
    private void handleCreateNewReservation(HashMap<String, Object> request) {

        Reservation newRsvp = (Reservation) request.get(TartanParams.PAYLOAD);
        HashMap<String, Object> response = new HashMap<String, Object>();
        try {

            // check for duplicate
            if (rsvpStore.isDuplicate(newRsvp)) {
                response.put(TartanParams.COMMAND, TartanParams.ERROR);

                response.put(TartanParams.PAYLOAD, "Reservation is a duplicate, please submit unique reservation");

                sendMessage((String) request.get(TartanParams.SOURCE_ID), response);
                return;
            }

            // Verify reservation parameters
            if (!verifyReservation(newRsvp)) {
                response.put(TartanParams.COMMAND, TartanParams.ERROR);
                response.put(TartanParams.PAYLOAD, "Reservation is invalid, please submit a valid reservation");
                sendMessage((String) request.get(TartanParams.SOURCE_ID), response);
                return;
            }

            // get a spot for this window
            Integer spot = getParkingSpot(newRsvp);

            if ((spot.equals(TartanParams.INVALID_SPOT)) || (spot.equals(TartanParams.SPOT_UNAVAILABLE))) {
                response.put(TartanParams.COMMAND, TartanParams.ERROR);
                response.put(TartanParams.PAYLOAD, "Parking space unavailable at desired time");
                sendMessage((String) request.get(TartanParams.SOURCE_ID), response);
                return;
            }

            newRsvp.setSpotId(spot);

            // if this new reservation is complete, notify the kiosk service
            if (newRsvp.isComplete()) {

                response.put(TartanParams.COMMAND, TartanParams.MSG_NEW_RSVP);
                response.put(TartanParams.PAYLOAD, newRsvp);
                sendMessage((String) request.get(TartanParams.SOURCE_ID), response);

                return;
            } else {
                response.put(TartanParams.COMMAND, TartanParams.ERROR);
                response.put(TartanParams.PAYLOAD, "Could not complete reservation");
                sendMessage((String) request.get(TartanParams.SOURCE_ID), response);

                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.put(TartanParams.COMMAND, TartanParams.ERROR);
        response.put(TartanParams.PAYLOAD, "Unknown Error");
        sendMessage((String) request.get(TartanParams.SOURCE_ID), response);
    }

    /**
     * Confirm the reservation was made.
     *
     * @param request The confirmation message.
     */
    private void handleConfirmReservation(HashMap<String, Object> request) {
        Reservation newRsvp = (Reservation) request.get(TartanParams.PAYLOAD);

        // spot has been reserved and paid for, save it
        rsvpStore.addReservation(newRsvp);
        rsvpStore.saveNewReservation(newRsvp);
    }

    /**
     * Send all valid reservations.
     *
     * @param request the for the set of reservations.
     */
    private void handleFetchAllReservations(HashMap<String, Object> request) {

        HashMap<String, Object> response = new HashMap<String, Object>();
        Vector<Reservation> results = rsvpStore.getReservations();

        response.put(TartanParams.PAYLOAD, results);
        response.put(TartanParams.COMMAND, TartanParams.MSG_GET_ALL_RSVP);
        sendMessage((String) request.get(TartanParams.SOURCE_ID), response);
    }

    /**
     * Redeeming a reservation requires pulling it out of the reservation store
     * and sending back for processing.
     *
     * @param request The incoming request.
     */
    private void handleRedeemReservation(HashMap<String, Object> request) {

        String errorMsg = "An error occurred";
        HashMap<String, Object> response = new HashMap<String, Object>();
        try {
            String cmd = (String) request.get(TartanParams.COMMAND);
            Vector<Reservation> results = null;

            String customer = (String) request.get(TartanParams.CUSTOMER);
            String licensePlate = (String) request.get(TartanParams.VEHICLE);

            // prefer lookup by customer over license plate
            if (customer != null) {
                results = rsvpStore.lookupByCustomer(customer);
            } else if (licensePlate != null) {
                results = rsvpStore.lookupByVehicle(licensePlate);
            }

            // found reservation for this customer, now check that it is valid
            if (results != null && !results.isEmpty()) {

                Vector<Reservation> validReservations = new Vector<Reservation>();
                Date now = Calendar.getInstance().getTime();

                // build a list of spots occupied at this time
                for (Reservation r : results) {
                    Date st = r.getStartTime();
                    Date et = r.getEndTime();

                    if (now.after(st) && now.before(et)) {
                        if (r.getIsRedeemed() == false) {
                            System.err.println("Reservation not yet redeemed!");
                            validReservations.add(r);
                        }
                    }

                }
                if (validReservations.isEmpty()) {
                    errorMsg = "No reservations valid at this time";
                }
                results = validReservations;

                if (!results.isEmpty()) {
                    response.put(TartanParams.PAYLOAD, results);
                    response.put(TartanParams.COMMAND, cmd);
                    sendMessage((String) request.get(TartanParams.SOURCE_ID), response);

                    return;
                }
            } else {
                errorMsg = "Cannot find reservation!";
            }

            // Can't find reservation!

        } catch (Exception e) {
            e.printStackTrace();
        }

        response.put(TartanParams.COMMAND, TartanParams.ERROR);
        response.put(TartanParams.PAYLOAD, errorMsg);
        sendMessage((String) request.get(TartanParams.SOURCE_ID), response);
    }
}
