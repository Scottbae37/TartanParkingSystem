package edu.cmu.tartan.service;

import edu.cmu.tartan.TartanKioskWindow;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Payment;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;

import javax.swing.*;
import java.util.HashMap;
import java.util.Vector;

/**
 * The KioskService is the Tartan service that connects the system to the user.
 *
 * Project: LG Exec Ed SDET Program
 * Copyright: 2017 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2016 - initial version
 */
public class KioskService extends TartanService {

    /** The service name */
    public final static String KIOSK_SERVICE = "KioskService";

    /** A handle to the Kiosk Window */
    private TartanKioskWindow kiosk = null;

    /**
     * Default constructor
     */
    public KioskService() {

        super.init(KIOSK_SERVICE);

        status = TartanServiceStatus.STOPPED;
    }

    /**
     * The messages that the KioskService handles.
     *
     * <ul>
     * <li> MSG_REEDEEM_RSVP: Handle reservation redemption when customer arrives.</li>
     * <li> MSG_VEHICLE_AT_ENTRY: Vehicle detected at entry gate</li>
     * <li> MSG_NEW_RSVP: Handle new reservation.</li>
     * <li> MSG_UPDATE_RSVP: Handle update reservation.</li>
     * <li> MSG_PAYMENT_VALID: Indicate that the payment was accepted. </li>
     * <li> MSG_PAYMENT_INVALID: Indicate the payment was rejected.</li>
     * <li> MSG_ENTRY_COMPLETE: Indicate that the parking transaction is done.</li>
     * <li> MSG_EXIT_COMPLETE: Indicate that the vehicle exit is done.</li>
     * </ul>
     *
     * @param message: The incoming message.
     */
    @Override
    public void handleMessage(HashMap<String, Object> message) {
        System.out.println("KioskService.handleMessage");

        String cmd = (String) message.get(TartanParams.COMMAND);

        if (cmd.equals(TartanParams.MSG_REDEEM_RSVP)) {
            handleRedeemReservation(message);
        }
        else if (cmd.equals(TartanParams.MSG_VEHICLE_AT_ENTRY)) {
            kiosk.enableRsvpRedemption();
        }
        else if (cmd.equals(TartanParams.MSG_VEHICLE_AT_EXIT)) {
            handleParkingExit(message);
        }
        else if (cmd.equals(TartanParams.MSG_WRONG_SPOT)) {
            handleParkingError(message);
        }
        else if (cmd.equals(TartanParams.MSG_NEW_RSVP)) {
           handleNewReservation(message);
        }
        else if (cmd.equals(TartanParams.MSG_UPDATE_RSVP)) {
            handleUpdateReservation(message);
        }
        else if (cmd.equals(TartanParams.MSG_PAYMENT_VALID)) {
            handlePaymentValid(message);
        }
        else if (cmd.equals(TartanParams.MSG_PAYMENT_INVALID)) {
            handlePaymentInvalid(message);
        }
        else if (cmd.equals(TartanParams.ERROR)) {
            String errorMsg = (String) message.get(TartanParams.PAYLOAD);
            kiosk.showError(errorMsg);
        }
        else if (cmd.equals(TartanParams.MSG_ENTRY_COMPLETE)) {
            kiosk.disableRsvpRedemption();
        }
        else if (cmd.equals(TartanParams.MSG_EXIT_COMPLETE)) {
            handleExitComplete(message);
        }
    }

    private void handleExitComplete(HashMap<String, Object> message) {
        Reservation rsvp = (Reservation) message.get(TartanParams.PAYLOAD);
        kiosk.showReceipt(rsvp);
    }

    private void handleParkingExit(HashMap<String, Object> message) {

//        Boolean state = false;
//        if (message.containsKey(TartanParams.PAYLOAD)) {
//            HashMap<String, Object> body = (HashMap<String, Object>) message.get(TartanParams.PAYLOAD);
//            if (body.containsKey(TartanParams.EXIT_STATE)) {
//                state = (Boolean) body.get(TartanParams.EXIT_STATE);
//            }
//        }

        String vid = JOptionPane.showInputDialog(kiosk, "Enter vehicle ID to exit", "Exit",JOptionPane.QUESTION_MESSAGE);
//        if (state == false) {
//            vid =
//        }
        if (vid != null || (vid != null && ("".equals(vid) == false))) {

            HashMap<String, Object> msg = new HashMap<String, Object>();
            msg.put(TartanParams.COMMAND, TartanParams.MSG_EXIT_GARAGE);
            msg.put(TartanParams.PAYLOAD, vid);
            sendMessage(ParkingService.PARKING_SERVICE, msg);

        } else {
            JOptionPane.showMessageDialog(kiosk,
                    "You must enter a valid vehicle ID", "Invalid Vehicle", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Display an error message if the customer parks in the wrong spot.
     *
     * @param message the received message with details about the incorrect parking.
     */
    private void handleParkingError(HashMap<String, Object> message) {

        StringBuffer buf = new StringBuffer();
        HashMap<String, Object> body = (HashMap<String, Object>) message.get(TartanParams.PAYLOAD);

        Reservation rsvp = (Reservation) body.get(TartanParams.RSVP);
        Integer wrongSpot = (Integer) body.get(TartanParams.ACTUAL_SPOT);

        buf.append("Vehicle " + rsvp.getVehicleID() + " parked in wrong spot.\nShould be in spot "
                + rsvp.getSpotId() + " but is in spot " + wrongSpot);

        kiosk.showError(buf.toString());
    }

    /**
     *  Handle updating the reservation. This method handles the situation where the reservation must be changed,
     *  for example, when the assigned spot is taken.
     *
     * @param message the message.
     */
    private void handleUpdateReservation(HashMap<String, Object> message) {
        Reservation rsvp = (Reservation) message.get(TartanParams.PAYLOAD);
        kiosk.notifyUpdatedReservation(rsvp);

        // reservation has been updated, grant entry.
        HashMap<String, Object> msg = new HashMap<String, Object>();
        msg.put(TartanParams.COMMAND, TartanParams.MSG_ENTER_GARAGE);
        msg.put(TartanParams.PAYLOAD, rsvp);
        sendMessage(ParkingService.PARKING_SERVICE, msg);
    }

    /**
     * Handle payment rejected message.
     *
     * @param message the incoming message.
     */
    private void handlePaymentInvalid(HashMap<String, Object> message) {

        kiosk.showError("Payment rejected!");
    }

    /**
     * Handle payment complete message.
     *
     * @param message The incoming message.
     */
    private void handlePaymentValid(HashMap<String, Object> message) {

        Payment payment = (Payment) message.get(TartanParams.PAYLOAD);

        if (payment.getReservation().getIsRedeemed() == false) {

            // this is a new reservation
            HashMap<String, Object> confirmation = new HashMap<String, Object>();
            confirmation.put(TartanParams.COMMAND, TartanParams.MSG_CONFIRM_RSVP);
            confirmation.put(TartanParams.PAYLOAD, payment.getReservation());

            // confirm the new reservation
            sendMessage(ReservationService.RESERVATION_SERVICE, confirmation);

            kiosk.confirmReservation(payment.getReservation());
        }
        else {
            // if this was deferred payment, then proceed with redemption
            kiosk.redeemReservation(payment.getReservation());

            HashMap<String, Object> msg = new HashMap<String, Object>();
            msg.put(TartanParams.COMMAND, TartanParams.MSG_ENTER_GARAGE);
            msg.put(TartanParams.PAYLOAD, payment.getReservation());
            sendMessage(ParkingService.PARKING_SERVICE, msg);

        }
    }

    /**
     * Send the completed payment to the PaymentService.
     *
     * @param payment The completed payment.
     */
    public void sendPaymentInfo(Payment payment) {

        HashMap<String,Object> message = new HashMap<String, Object>();
        message.put(TartanParams.COMMAND, TartanParams.MSG_VALIDATE_PAYMENT);
        message.put(TartanParams.PAYLOAD, payment);

        //  send the payment
        sendMessage(PaymentService.PAYMENT_SERVICE, message);
    }

    public void makeNewReservation(Reservation rsvp) {

        HashMap<String,Object> message = new HashMap<String, Object>();
        message.put(TartanParams.COMMAND, TartanParams.MSG_NEW_RSVP);
        message.put(TartanParams.PAYLOAD, rsvp);

        // Ask the reservation service for a reservation
        sendMessage(ReservationService.RESERVATION_SERVICE, message);
    }

    /**
     * Handle a new reservation.
     *
     * @param message The incoming message.
     */
    private void handleNewReservation(HashMap<String,Object> message) {

        Payment payment = kiosk.acceptPayment();

        // if the payment is deferred, mark that in the RSVP and proceed
        Reservation rsvp = (Reservation) message.get(TartanParams.PAYLOAD);
        if (payment == null) {
            rsvp.setIsPaid(false);

            HashMap<String,Object> confirmation = new HashMap<String, Object>();
            confirmation.put(TartanParams.COMMAND, TartanParams.MSG_CONFIRM_RSVP);
            confirmation.put(TartanParams.PAYLOAD, rsvp);

            // confirm the new reservation
            sendMessage(ReservationService.RESERVATION_SERVICE, confirmation);
            kiosk.confirmReservation(rsvp);

        }
        else {
            // Otherwise this reservation payment information is valid
            rsvp.setIsPaid(true);
            payment.setReservation(rsvp);
            sendPaymentInfo(payment);
        }
    }

    /**
     * Handle reservation redemption.
     *
     * @param message The incoming message.
     */
    private void handleRedeemReservation(HashMap<String, Object> message) {

        // reservation(s) may or may not be paid

        Reservation selectedRsvp = null;
        Vector<Reservation> rsvps = (Vector<Reservation>) message.get(TartanParams.PAYLOAD);
        if (rsvps.isEmpty()) {
            kiosk.showError("Could not find reservation");
        }
        else if (rsvps.size() > 1) { // multiple Reservations for this person

            String[] values = new String[rsvps.size()];
            for (int i=0; i< rsvps.size(); i++) {
                Reservation r = rsvps.elementAt(i);
                values[i] = i + ": Name: " + r.getCustomerName() + ", Vehicle: " + r.getVehicleID()
                              + ", " + r.getStartTime() + " to + " + r.getEndTime();
            }
            Object selected =
                    JOptionPane.showInputDialog(null,
                            "Select Reservation to Redeem?", "Redeem",
                            JOptionPane.DEFAULT_OPTION, null, values, values[0]);

            if ( selected != null ){
                char index = selected.toString().charAt(0);
                selectedRsvp = rsvps.elementAt(Character.getNumericValue(index));
            }
        }
        else {
            // only one reservation.
            selectedRsvp = rsvps.elementAt(0);
        }

        selectedRsvp.setIsRedeemed(true);

        // if the reservation has not been paid for, then pay for it now.
        if (selectedRsvp.getIsPaid() == false) {
            JOptionPane.showMessageDialog(kiosk,
                    "You must pay for this reservation now","Payment Required", JOptionPane.INFORMATION_MESSAGE);

            Payment payment = kiosk.acceptPayment();

            if (payment == null) {
                payment = kiosk.acceptPayment();
            }

            selectedRsvp.setIsPaid(true);
            payment.setReservation(selectedRsvp);

            // save the payment info for later, just in case they over stay their reservation
            selectedRsvp.setPayment(payment);

            sendPaymentInfo(payment);

        }
        else {
            kiosk.redeemReservation(selectedRsvp);

            HashMap<String, Object> msg = new HashMap<String, Object>();
            msg.put(TartanParams.COMMAND, TartanParams.MSG_ENTER_GARAGE);
            msg.put(TartanParams.PAYLOAD, selectedRsvp);
            sendMessage(ParkingService.PARKING_SERVICE, msg);

            // Mark the reservation complete
            HashMap<String,Object> completeMessage = new HashMap<String, Object>();
            completeMessage.put(TartanParams.COMMAND, TartanParams.MSG_COMPLETE_RSVP);
            completeMessage.put(TartanParams.PAYLOAD, selectedRsvp);
            sendMessage(ReservationService.RESERVATION_SERVICE, completeMessage);
        }
    }


    /**
     * Service running.
     */
    @Override
    public void run() {
        System.out.println("KioskService.run");
        status = TartanServiceStatus.RUNNING;
    }

    /**
     * On termination stop the service.
     */
    protected void finalize() {
        stop();
    }

    /**
     * Get a reservation by name or license plate
     *
     * @param name The name associated with the reservation.
     * @param licensePlate The license plate associated with the reservation.
     *
     * @return True on success.
     */
    public Boolean getReservation(String name, String licensePlate) {

        HashMap<String,Object> body = new HashMap<String, Object>();
        body.put(TartanParams.COMMAND, TartanParams.MSG_REDEEM_RSVP);

        if (name != null) {
            body.put(TartanParams.CUSTOMER, name);
        }
        else if (licensePlate != null) {
            body.put(TartanParams.VEHICLE, licensePlate);
        }

        sendMessage(ReservationService.RESERVATION_SERVICE, body);

        return true;
    }

    /**
     * Associate a kiosk with this service
     *
     * @param kiosk The kiosk associated with these services.
     */
    public void setKiosk(TartanKioskWindow kiosk) {
        this.kiosk = kiosk;
    }

    /**
     * Terminate the service.
     */
    @Override
    public void terminate() {
        stop();
    }
}
