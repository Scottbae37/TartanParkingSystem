package edu.cmu.tartan.edu.cmu.tartan.reservation;

import edu.cmu.tartan.TartanUtils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Payments represent payment information, such as credit card number, expiration date
 * and name.
 *
    *
 * Project: LG Exec Ed SDET Program
 * Copyright: 2017 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2016 - initial version
 */
public class Payment implements Serializable {

    /** use serialVersionUID from JDK 1.0.2 for interoperability */
    private static final long serialVersionUID = 1234L;

    /** Credit card number */
    private String ccNum = null;

    /** Credit card expiration date */
    private String ccExpDate = null;

    /** Credit card name */
    private String ccName = null;

    /** The reservation associated with the payment */
    private Reservation rsvp = null;

    /** The key used to protect data */
    private static final String key = "TARTAN";

    private Long fee = null;

    /**
     * Encode and protect a message
     *
     * @param param the message to encode
     * @return the encoded message
     */
    private String protect(String param) {

        char[] keys = key.toCharArray();
        char[] p = param.toCharArray();

        int m = p.length;
        int k = keys.length;
        char[] result = new char[m];

        for (int i = 0; i < m; i++) {
            result[i] = (char)(p[i] ^ keys[i % k]);
        }

        return new String(result);
    }

    /**
     * Default constructor
     */
    public Payment() {  }

    public Payment(Long amt) { fee = amt; }

    /**
     * Reservation setter
     *
     * @param r the reservation
     */
    public void setReservation(Reservation r) {
        this.rsvp = r;
    }

    /**
     * Reservation getter
     *
     * @return the payment's reservation
     */
    public Reservation getReservation() {
        return this.rsvp;
    }


    public void setFee(Long amt) {
        fee = amt;
    }

    public Long getFee() {
        return fee;
    }

    /**
     * Check to see if the payment is valid. This method simply checks that all the required information
     * has been supplied
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        // original logic
        if (TartanUtils.IS_EMPTY.test(ccNum)
                || TartanUtils.IS_EMPTY.test(ccExpDate)
                || TartanUtils.IS_EMPTY.test(ccName))
            return false;

        int localMonth = YearMonth.now().getMonthValue();
        int localYear = Year.now().getValue();

        // cardString returns like "January\2019"
        String cardString = getCcExpDate().replace("\\", "-");
        String[] parts = cardString.split("-");
        String monthString = parts[0];
        String yearString = parts[1];

        Date date;
        int cardMonth = 0;
        int cardYear = Integer.parseInt(yearString);
        try {
            date = new SimpleDateFormat("MMM", Locale.ENGLISH).parse(monthString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cardMonth = cal.get(Calendar.MONTH) + 1;
        } catch (ParseException e) {
        }

        if((cardYear > localYear) || (cardYear == localYear && cardMonth >= localMonth))
            return true;

        return false;
    }

    /**
     * Credit card number getter
     *
     * @return the credit card number
     */
    public String getCcNum() {
        return protect(ccNum);
    }

    /**
     * Credit card number setter
     *
     * @param ccNum the new credit card number
     */
    public void setCcNum(String ccNum) {
        this.ccNum = protect(ccNum);
    }

    /**
     * Credit card expiration date getter
     *
     * @return the expiration date
     */
    public String getCcExpDate() {
        return protect(ccExpDate);
    }

    /**
     *  Credit card expiration date setter
     *
     * @param ccExpDate the new credit card expiration date
     */
    public void setCcExpDate(String ccExpDate) {
        this.ccExpDate = protect(ccExpDate);
    }

    /**
     * Getter for the credit card holder's name
     *
     * @return the name
     */
    public String getCcName() {
        return protect(ccName);
    }

    /***
     * Setter for credit card holder name
     *
     * @param ccName the new credit card holder name
     */
    public void setCcName(String ccName) {
        this.ccName = protect(ccName);
    }
}
