package edu.cmu.tartan.service;

import edu.cmu.tartan.edu.cmu.tartan.reservation.Payment;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;

import java.util.*;

/**
 * The Payment is the Tartan service that manages payments.
 *
 * Project: LG Exec Ed SDET Program
 * Copyright: 2017 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2016 - initial version
 */
public class PaymentService extends TartanService {

    /** Parking rates */
    private final Long WEEK_DAY_RATE = Long.valueOf(15);
    private final Long WEEK_NIGHT_RATE = Long.valueOf(10);
    private final Long WEEKEND_DAY_RATE = Long.valueOf(12);
    private final Long WEEKEND_NIGHT_RATE = Long.valueOf(8);

    /** The value for calculating parking hours */
    private final int MILLI_TO_HOUR = 1000 * 60 * 60;

    /** The rate for overstaying your reservation */
    private final Integer PENALTY_RATE = 20;

    /** The name of this service */
    public final static String PAYMENT_SERVICE = "PaymentService";

    /**
     * Default constructor.
     */
    public PaymentService() {

        super.init(PAYMENT_SERVICE);

        // status = TartanServiceStatus.STOPPED;
    }

    /** get reserved rates */
    // ADDED to calculate various rates
    private ArrayList<Long> getRates(Reservation rsvp) {

        ArrayList<Long> rates = new ArrayList<Long>();

        Date startTime = rsvp.getStartTime();
        Date endTime = rsvp.getEndTime();
        Long rsvpDuration = (endTime.getTime() - startTime.getTime()) / MILLI_TO_HOUR;

        Calendar cal = Calendar.getInstance();
        Date currentTime = startTime;

        int maxDuration = (int)rsvpDuration.longValue();
        for(int i = 1; i <= maxDuration; i++)
        {
            cal.setTime(currentTime);
            Long rate = setRate(currentTime);
            rates.add(rate);
            currentTime = new Date(currentTime.getTime() + MILLI_TO_HOUR);
        }

        return rates;
    }

    /** set rates */
    // CHANGED to get Date parameter
    private Long setRate(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int day = cal.get(Calendar.DAY_OF_WEEK);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        boolean isWeekday = ((day >= Calendar.MONDAY) && (day <= Calendar.FRIDAY));

        // FIXED about hour limit
        boolean isDaytime = ((hour >= 9) && (hour < 17)); // day time is 9 AM to 5 PM

        if (isWeekday && isDaytime) {
            return WEEK_DAY_RATE;
        }
        else if (isWeekday && !isDaytime) {
            return WEEK_NIGHT_RATE;
        }
        else if (!isWeekday && isDaytime) {
            return WEEKEND_DAY_RATE;
        }
        else if (!isWeekday && !isDaytime) {
            return WEEKEND_NIGHT_RATE;
        }
        return WEEK_DAY_RATE;
    }

    /**
     * Run the service.
     */
    @Override
    public void run() {
        System.out.println("PaymentService.run");
        // status = TartanServiceStatus.RUNNING;
    }

    /**
     * Cleanup the service.
     */
    protected void finalize() {
        stop();
    }

    /**
     * Handle payment messages. The only incoming message to handle concerns making a payment.
     * The following messages are handled:
     *
     * <ul>
     *     <li>MSG_MAKE_PAYMENT: Calculate the total fee and execute the payment.</li>
     *     <li>MSG_VALIDATE_PAYMENT: Check that payment information is valid.</li>
     * </ul>
     *
     * @param message The incoming message.
     */
    @Override
    public void handleMessage( HashMap<String, Object> message) {

        String cmd = (String) message.get(TartanParams.COMMAND);

        if (cmd.equals(TartanParams.MSG_VALIDATE_PAYMENT)) {
            handleValidatePayment(message); // validate the payment
        }
        else if (cmd.equals(TartanParams.MSG_MAKE_PAYMENT)) {
            handleMakePayment(message);  // actually execute the payment

        }
    }

    /**
     * Handle making a payment. Notably, this method requires work to make more realistic until real payment system
     * connected.
     *
     * @param message The payment message.
     */
    private void handleMakePayment(HashMap<String, Object> message) {

        HashMap<String,Object> response = new HashMap<String, Object>();

        Reservation rsvp = (Reservation) message.get(TartanParams.PAYLOAD);

        Long fee = computeTotalFee(rsvp);
        Payment payment = new Payment();
        payment.setFee(fee);
        rsvp.setPayment(payment);

        response.put(TartanParams.COMMAND, TartanParams.MSG_PAYMENT_COMPLETE);
        response.put(TartanParams.PAYLOAD, rsvp);

        sendMessage((String) message.get(TartanParams.SOURCE_ID), response);
        sendMessage(ReservationService.RESERVATION_SERVICE, response);
    }

    /**
     * Handle making a payment. Notably, this method requires work to make more realistic until real payment system
     * connected.
     *
     * @param rsvp The payment message.
     */

    private Long computeTotalFee(Reservation rsvp) {

        Long reservedFee = Long.valueOf(0);
        ArrayList<Long> rates = getRates(rsvp);
        for(int i = 0; i < rates.size(); i++)
            reservedFee += rates.get(i);

        Date startTime = rsvp.getStartTime();
        Date endTime = rsvp.getEndTime();
        Date now = new Date();

        // The number of hours the vehicle has been parked (hours)
        Long totalDuration  = (now.getTime() - startTime.getTime()) / MILLI_TO_HOUR;

        // How long the reservation was scheduled for (hours)
        Long rsvpDuration = (endTime.getTime() - startTime.getTime()) / MILLI_TO_HOUR;

        // Did the total duration exceed the reserved period? If so, apply the penalty rate for the overage
        Long penaltyHours = Long.valueOf(0);
        if ((totalDuration - rsvpDuration) > 0) {
            penaltyHours = totalDuration - rsvpDuration;
        }

        // compute the standard fee for the reservation and the penalty
        return reservedFee + (penaltyHours * PENALTY_RATE);
    }

    /**
     * validate that the required payment information is present. Currently this just checks that
     * all required fields have been specified.
     *
     * @param message the message containing the payment to validate.
     */
    private void handleValidatePayment(HashMap<String, Object> message) {

        HashMap<String,Object> response = new HashMap<String, Object>();

        Payment payment = (Payment) message.get(TartanParams.PAYLOAD);
        if (payment.isValid()) {
            response.put(TartanParams.COMMAND, TartanParams.MSG_PAYMENT_VALID);
        }
        else {
            response.put(TartanParams.COMMAND, TartanParams.MSG_PAYMENT_INVALID);
        }
        response.put(TartanParams.PAYLOAD, payment);
        sendMessage((String) message.get(TartanParams.SOURCE_ID), response);
    }

    /**
     * Terminate the payment service.
     */
    @Override
    public void terminate() {
        stop();
    }
}
