package edu.cmu.tartan.hardware;

/**
 * Constants used by Tartan Garage commands and sensors.
 *
 * Project: LG Exec Ed SDET Program
 * Copyright: 2017 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2016 - initial version
 */
public interface TartanSensors {

    // Protocol control values
    public static final String PARAM_DELIM = ";";
    public static final String MSG_DELIM = ":";
    public static final String LIST_START = "[";
    public static final String LIST_END = "]";
    public static final String LIST_DELIM = ",";
    public static final String PARAM_EQ = "=";
    public static final String MSG_END = ".";

    // Successful response
    public static final String OK = "OK";

    // Gate parameters
    public static final String ENTRY_GATE = "NG";
    public static final String EXIT_GATE = "XG";

    // Entry/exit beam parameters
    public static final String ENTRY_IR = "NIR";
    public static final String EXIT_IR = "XIR";

    // Entry/exit light parameters
    public static final String ENTRY_LIGHT = "NL";
    public static final String EXIT_LIGHT = "XL";

    // Parking stall light parameter
    public static final String PARKING_SPOT_LIGHT = "PL";

    // Parking stall state. This is read only from the system
    public static final String PARKING_SPOT_OCCUPIED = "PO";

    // Indicates something is open or closed
    public static final String OPEN = "1";
    public static final String CLOSE = "0";

    // Indicates something is on or off
    public static final String ON = "1";
    public static final String OFF = "0";

    // Indicates something is red or green (entry/exit lights)
    public static final String RED = "R";
    public static final String GREEN = "G";

    // System state parameters
    public static final String GET_STATE = "GS";
    public static final String STATE_UPDATE = "SU";
}
