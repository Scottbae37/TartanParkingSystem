package edu.cmu.tartan.service;

/**
 * Constants used by the Tartan Application.
 *
 * Project: LG Exec Ed SDET Program
 * Copyright: 2017 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2016 - initial version
 */
public interface TartanParams {

    // Generic parameters
    public static final String SERVICE_ID = "SERVICE_ID";     // The name of the service
    public static final String SOURCE_ID = "SOURCE_ID"; // The name of the service
    public static final String COMMAND = "COMMAND";           // the command to execute
    public static final String PAYLOAD = "PAYLOAD";           // command specific data
    public static final String ERROR = "ERROR";           // command specific data
    public static final String CURRENT_OCCUPIED_STATE = "CurrentOccupiedState";

    // System commands
    public final static String MSG_NEW_RSVP = "NewRsvp";
    public final static String MSG_VEHICLE_AT_ENTRY = "VehicleArrived";
    public final static String MSG_VEHICLE_OUT_ENTRY = "VehicleLeaved";
    public final static String MSG_VEHICLE_AT_EXIT = "VehicleDeparted";
    public final static String MSG_VEHICLE_RETURN = "VehicleReturned";
    public final static String MSG_ENTRY_COMPLETE = "EntryComplete";
    public final static String MSG_EXIT_COMPLETE = "ExitComplete";
    public final static String MSG_CONFIRM_RSVP = "ConfirmRsvp";
    public final static String MSG_REDEEM_RSVP = "RedeemRsvp";
    public final static String MSG_UPDATE_RSVP = "UpdateRsvp";
    public final static String MSG_GET_ALL_RSVP = "GetAll";
    public final static String MSG_COMPLETE_RSVP = "Delete";
    public final static String MSG_VALIDATE_PAYMENT = "ValidatePayment";
    public final static String MSG_MAKE_PAYMENT = "MakePayment";
    public final static String MSG_PAYMENT_COMPLETE = "PaymentComplete";
    public final static String MSG_PAYMENT_VALID = "PaymentValid";
    public final static String MSG_PAYMENT_INVALID = "PaymentInvalid";
    public final static String MSG_GET_PARKING_SPOTS = "GetParkingSpots";
    public final static String MSG_ENTER_GARAGE = "EnterGarage";
    public final static String MSG_EXIT_GARAGE = "ExitGarage";
    public final static String MSG_WRONG_SPOT = "ParkingError";
    public final static String MSG_AUTHENTICATE_ADMIN = "AuthenticateAdmin";
    public final static String MSG_AUTHENTICATION_RESULT = "AuthenticateResult";
    public final static String MSG_GET_STATISTICAL_DATA = "GetStatisticalData";
    public final static String MSG_STATISTICAL_DATA_RESULT = "StatisticalDataResult";
    public final static String MSG_EXIT_STATE = "ExitGarageState";

    // Command specific parameters
    public static final String RSVP = "Rsvp";
    public static final String ACTUAL_SPOT = "ActualSpot";
    public static final String CUSTOMER = "Customer";
    public static final String VEHICLE = "Vehicle";
    public static final String REVENUE = "Revenue";
    public static final String AVERAGE_OCCUPANCY = "AverageOccupancy";
    public static final String PEAK_USAGE_HOURS = "PeakUsageHours";
    public static final String EXIT_STATE = "ExitState";

    // Symbolic constants for understandability
    public static final Integer INVALID_SPOT = -1;
    public static final Integer SPOT_UNAVAILABLE = -2;

}

