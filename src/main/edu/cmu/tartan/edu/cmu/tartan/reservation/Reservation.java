package edu.cmu.tartan.edu.cmu.tartan.reservation;

import edu.cmu.tartan.TartanUtils;
import edu.cmu.tartan.service.TartanParams;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 *
 * A Reservation for a parking space.
 *
 * Project: LG Exec Ed SDET Program
 * Copyright: 2017 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2016 - initial version
 */
public class Reservation implements Serializable{

    /** use serialVersionUID from JDK 1.0.2 for interoperability */
    private static final long serialVersionUID = 5678L;

    /** The start and end time for the reservation */
    private Date startTime, endTime;

    /** The name on the reservation */
    private String customerName;

    /** The vehicle identifier (license plate) */
    private String vehicleID;

    /** The spot assigned to this reservation */
    private Integer spotId;

    /** Indicates whether this reservation in paid for */
    private Boolean isPaid;

    /** Indicates whether this reservation has been redeemed */
    private Boolean isRedeemed;

    /** The date format for start and end times */
    private SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd:HH:mm");

    /** The total payment for this reservation */
    private Payment payment;

    /**
     * The default constructor.
     */
    public Reservation() {
        startTime = null;
        endTime = null;
        customerName = null;
        vehicleID = null;
        spotId = TartanParams.INVALID_SPOT;
        isPaid = false;
        isRedeemed = false;
    }

    /**
     * Create a new reservation.
     *
     * @param st start time for the reservation.
     * @param et end time for the reservation.
     * @param cn customer name for the reservation.
     * @param vid vehicle identifier for the reservation.
     */
    public Reservation(String st, String et, String cn, String vid) {

        try {
            startTime = format.parse(st);
            endTime = format.parse(et);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        customerName = cn;
        vehicleID = vid;
    }

    /**
     * Get the spot ID for this reservation.
     *
     * @return the spot ID.
     */
    public Integer getSpotId() {
        return spotId;
    }

    /**
     * Test whether the reservation has been completed.
     *
     * @return true if complete, false otherwise.
     */
    public Boolean isComplete() {
        return startTime != null && endTime != null
                && TartanUtils.IS_EMPTY.test(customerName) == false
                && TartanUtils.IS_EMPTY.test(vehicleID) == false
                && spotId.compareTo(TartanParams.INVALID_SPOT) != 0;
    }

    /**
     * Set the spot id for this reservation.
     *
     * @param sid the spot identifier.
     */
    public void setSpotId(Integer sid) {
        spotId = sid;
    }

    /**
     * Get the start time for this reservation.
     *
     * @return the start time.
     */
    public Date getStartTime() {
        Date tempStartTime = new Date(startTime.getTime());
        return tempStartTime;
    }

    /**
     * Set the start time for this reservation.
     *
     * @param startTime the start time as a Date.
     */
    public void setStartTime(Date startTime) {
        this.startTime = new Date(startTime.getTime());
    }

    /**
     * Set the start time for this reservation.
     *
     * @param startTime the start time as a String.
     */
    public void setStartTime(String startTime) {
        try {
            this.startTime = format.parse(startTime);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the end time for this reservation.
     *
     * @return the end time.
     */
    public Date getEndTime() {
        Date tempEndTime = new Date(endTime.getTime());
        return tempEndTime;
    }

    /**
     * Set the end time for this reservation.
     *
     * @param endTime the end time as a Date.
     */
    public void setEndTime(Date endTime) {
        this.endTime = new Date(endTime.getTime());
    }

    /**
     * Set the end time for this reservation.
     *
     * @param endTime the end time as a String.
     */
    public void setEndTime(String endTime) {
        try {
            this.endTime = format.parse(endTime);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set isRedeemed.
     *
     * @param r true if redeemed, false otherwise.
     */
    public void setIsRedeemed(Boolean r) {
        isRedeemed = r;
    }

    /**
     * Check whether this reservation is redeemed.
     *
     * @return the isRedeemed state
     */
    public Boolean getIsRedeemed() {
        return isRedeemed;
    }

    /**
     * Save the payment associated with this reservation
     *
     * @param p the payment to save
     */
    public void setPayment(Payment p) {
        payment = p;
    }

    /**
     * Fetch the payment info for this reservation
     *
     * @return the payment
     */
    public Payment getPayment() {
        return payment;
    }

    /**
     * Indicate the reservation has been paid or unpaid.
     *
     * @param ip true if paid, false otherwise
     */
    public void setIsPaid(Boolean ip) {
        isPaid = ip;
    }

    /**
     * Get the payment status of this reservation.
     *
     * @return true if the reservation is paid for, false otherwise.
     */
    public Boolean getIsPaid() {
        return isPaid;
    }

    /**
     * Get the name associated with this reservation.
     *
     * @return the name on the reservation.
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Set the name associated with this reservation.
     *
     * @param customerName the name on the reservation.
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * Get the vehicle ID.
     *
     * @return the vehicle ID.
     */
    public String getVehicleID() {
        return vehicleID;
    }

    /**
     * Set the vehicle ID.
     *
     * @param vehicleID the new vehicle ID
     */
    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    /**
     * Get a string representation of the reservation.
     *
     * @return a string representation of the reservation.
     */
    public String toString() {

        SimpleDateFormat fmt = new SimpleDateFormat("EEE, MMM dd:hh a yyyy");

        return  " - Customer     : " + customerName+ "\n" +
                " - Vehicle      : " + vehicleID + "\n" +
                " - Start Time   : " + fmt.format(startTime) + "\n" +
                " - End Time     : " + fmt.format(endTime) + "\n" +
                " - Parking Spot : " + spotId +
                " - " + ((isPaid) ? "Payment complete" : "Payment required on entry" );
    }

    /**
     * Compare two reservations.
     *
     * @param o the other reservation
     * @return 0 if they are equal, -1 otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reservation that = (Reservation) o;

        if (!startTime.equals(that.startTime)) return false;
        if (!endTime.equals(that.endTime)) return false;
        if (!customerName.equals(that.customerName)) return false;
        return vehicleID.equals(that.vehicleID);
    }

    @Override
    public int hashCode() {
        int result = startTime.hashCode();
        result = 31 * result + endTime.hashCode();
        result = 31 * result + customerName.hashCode();
        result = 31 * result + vehicleID.hashCode();
        return result;
    }
}

