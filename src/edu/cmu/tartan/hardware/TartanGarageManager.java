package edu.cmu.tartan.hardware;

import edu.cmu.tartan.service.TartanParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.StringTokenizer;

/**
 * Manages connection and data flow to the Tartan Garage.
 *
 * Project: LG Exec Ed SDET Program
 * Copyright: 2017 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2016 - initial version
 */
public class TartanGarageManager extends Observable {

    /** Connection to the garage */
    private TartanGarageConnection connection = null;

    /** Thread to manage currentState updates */
    private Thread updateThread = null;

    /** the currentState of the garage, updated every 5 seconds */
    HashMap<String, Object> currentState = new HashMap<String, Object>();

    /** The list of parking spots */
    ArrayList<Integer> parkingSpots = new ArrayList<Integer>();

    /** Garage capacity */
    private final Integer capacity = 4;

    /**
     * Set up the connection manager with a connection.
     *
     * @param conn the (established) connection
     */
    public TartanGarageManager(TartanGarageConnection conn) {

        connection = conn;

        for (int i=0; i<capacity; ++i) {
            parkingSpots.add(i);
        }
    }

    /**
     * Fetch the garage capacity.
     *
     * @return The capacity.
     */
    public Integer getCapacity() { return capacity; }

    /**
     * Fetch the list of parking spots.
     *
     * @return The list of parking spots.
     */
    public ArrayList<Integer> getParkingSpots() { return parkingSpots; }

    /**
     * Disconnect from the house
     */
    public void disconnectFromGarage() {
        connection.disconnect();
    }

    /**
     * Send command to close the exit gate.
     */
    public void closeExitGate() { toggleExitGate(false); }

    /**
     * Send command to open the exit gate.
     */
    public void openExitGate() { toggleExitGate(true); }

    /**
     * Send command to close the entry gate.
     */
    public void closeEntryGate() { toggleEntryGate(false); }

    /**
     * Send command to open the entry gate.
     */
    public void openEntryGate() { toggleEntryGate(true); }

    /**
     * Send command to open or close the entry gate.
     *
     * @param state True means open, false means close.
     *
     * @return True if command successfully executed.
     */
    private Boolean toggleEntryGate(Boolean state) {

        StringBuffer msg = new StringBuffer();
        msg.append(TartanSensors.ENTRY_GATE + TartanSensors.PARAM_EQ);
        if (state) {
            msg.append(TartanSensors.OPEN);
        }
        else {
            msg.append(TartanSensors.CLOSE);
        }
        msg.append(TartanSensors.MSG_END);

        String response = connection.sendMessageToGarage(msg.toString());

        if (response == null) {
            return false;
        }

        return response.equals(TartanSensors.OK);
    }

    /**
     * Send command to open or close the exit gate.
     *
     * @param state True means open, false means close.
     *
     * @return True if command successfully executed, false otherwise.
     */
    public Boolean toggleExitGate(Boolean state) {

        StringBuffer msg = new StringBuffer();
        msg.append(TartanSensors.EXIT_GATE + TartanSensors.PARAM_EQ);
        if (state) {
            msg.append(TartanSensors.OPEN);
        }
        else {
            msg.append(TartanSensors.CLOSE);
        }
        msg.append(TartanSensors.MSG_END);

        String response = connection.sendMessageToGarage(msg.toString());

        if (response == null) {
            return false;
        }

        return response.equals(TartanSensors.OK);
    }

    /**
     * Send command to turn parking stall lights on/off.
     *
     * @param state the list of parking stall lights.
     *
     * @return True if command successfully executed, false otherwise.
     */
    public Boolean setParkingSpotLights(ArrayList<String> state) {

        StringBuffer msg = new StringBuffer();

        msg.append(TartanSensors.PARKING_SPOT_LIGHT + TartanSensors.PARAM_EQ + TartanSensors.LIST_START);
        for (int i=0; i < state.size(); i++) {
            String spot = String.valueOf(i + 1);
            msg.append(spot + TartanSensors.PARAM_EQ + state.get(i));
            if (i + 1 < state.size()) {
                msg.append(TartanSensors.LIST_DELIM);
            }
        }

        msg.append(TartanSensors.LIST_END + TartanSensors.MSG_END);

        String response = connection.sendMessageToGarage(msg.toString());
        if (response == null) {
            return false;
        }
        return response.equals(TartanSensors.OK);
    }

    /**
     * Get the connected state.
     *
     * @return true if connected, false otherwise
     */
    public Boolean isConnected() {
        if (connection!=null) {
            return connection.isConnected();
        }
        return false;
    }

    /**
     * Request and process garage state.
     */
    public synchronized void updateGarageState() {

        System.out.println("Requesting currentState");

        synchronized (connection) {
            String update = connection.sendMessageToGarage(TartanSensors.GET_STATE + TartanSensors.MSG_END);
            if (update == null) {
                return;
            }

            handleStateUpdate(update);
        }
    }

    /**
     * Get state for each parking stall.
     *
     * @return A list of parking stall states.
     */
    public synchronized Integer[] getSpotOccupiedState() {
        Integer[] occupiedState = (Integer[]) currentState.get(TartanSensors.PARKING_SPOT_OCCUPIED);
        return occupiedState;
    }

    /**
     * Process the new currentState reported by the garage.
     *
     * @param stateUpdateMsg the new currentState message.
     */
    private synchronized void handleStateUpdate(String stateUpdateMsg) {

        if (stateUpdateMsg == null) {
            return;
        }
        if (stateUpdateMsg.length() == 0) {
            return;
        }

        String[] req = stateUpdateMsg.split(TartanSensors.MSG_DELIM);

        // invalid currentState update
        if (req.length != 2) {
            return;
        }
        // is this a currentState update
        String cmd = req[0];
        String body = req[1];

        if (!cmd.equals(TartanSensors.STATE_UPDATE)) { // only message that comes from garage
            return;
        }

        if (String.valueOf(body.charAt(body.length() - 1)).equals(TartanSensors.MSG_END)) {
            body = body.substring(0, body.length() - 1);
        }
        if (body == null) {
            return;
        }

        synchronized (currentState) {

            // clear the currentState for an update
            currentState.clear();

            StringTokenizer pt = new StringTokenizer(body, TartanSensors.PARAM_DELIM);

            // process the new currentState
            while (pt.hasMoreTokens()) {
                String param = pt.nextToken();
                int paramOffset = param.indexOf(TartanSensors.PARAM_EQ);
                String pcmd = param.substring(0, paramOffset);
                String pval = param.substring(paramOffset + 1, param.length());


                if (pcmd.equals(TartanSensors.ENTRY_GATE)) {
                    if (pval.equals("1")) {
                        currentState.put(TartanSensors.ENTRY_GATE, true);
                    } else {
                        currentState.put(TartanSensors.ENTRY_GATE, false);
                    }
                } else if (pcmd.equals(TartanSensors.EXIT_GATE)) {
                    if (pval.equals("1")) {
                        currentState.put(TartanSensors.EXIT_GATE, true);
                    } else {
                        currentState.put(TartanSensors.EXIT_GATE, false);
                    }
                } else if (pcmd.equals(TartanSensors.ENTRY_LIGHT)) {
                    if (pval.equals("R")) {
                        currentState.put(TartanSensors.ENTRY_LIGHT, TartanSensors.RED);
                    }
                    else if (pval.equals("G")) {
                        currentState.put(TartanSensors.ENTRY_LIGHT, TartanSensors.GREEN);
                    }
                    else if (pval.equals("0")) {
                        currentState.put(TartanSensors.ENTRY_LIGHT, TartanSensors.OFF);
                    }
                } else if (pcmd.equals(TartanSensors.EXIT_LIGHT)) {
                    if (pval.equals("R")) {
                        currentState.put(TartanSensors.EXIT_LIGHT, TartanSensors.RED);
                    }
                    else if (pval.equals("G")) {
                        currentState.put(TartanSensors.EXIT_LIGHT, TartanSensors.GREEN);
                    }
                    else if (pval.equals("0")) {
                        currentState.put(TartanSensors.EXIT_LIGHT, TartanSensors.OFF);
                    }
                } else if (pcmd.equals(TartanSensors.ENTRY_IR)) {
                    if (pval.equals("1")) {
                        currentState.put(TartanSensors.ENTRY_IR, true);
                    } else {
                        currentState.put(TartanSensors.ENTRY_IR, false);
                    }
                } else if (pcmd.equals(TartanSensors.EXIT_IR)) {
                    if (pval.equals("1")) {
                        currentState.put(TartanSensors.EXIT_IR, true);
                    } else {
                        currentState.put(TartanSensors.EXIT_IR, false);
                    }
                } else if (pcmd.equals(TartanSensors.PARKING_SPOT_LIGHT)) {

                    Integer[] lightState = new Integer[10];

                    String spots = pval.substring(1, pval.length() - 1);
                    StringTokenizer spotTok = new StringTokenizer(spots, TartanSensors.LIST_DELIM);
                    while (spotTok.hasMoreTokens()) {
                        String t = spotTok.nextToken();
                        String spotInfo[] = t.split(TartanSensors.PARAM_EQ);
                        lightState[Integer.parseInt(spotInfo[0])] = Integer.parseInt(spotInfo[1]);
                    }
                    currentState.put(TartanSensors.PARKING_SPOT_LIGHT, lightState);
                } else if (pcmd.equals(TartanSensors.PARKING_SPOT_OCCUPIED)) {

                    Integer[] occupiedState = new Integer[10];

                    String spots = pval.substring(1, pval.length() - 1);
                    StringTokenizer spotTok = new StringTokenizer(spots, TartanSensors.LIST_DELIM);
                    while (spotTok.hasMoreTokens()) {
                        String t = spotTok.nextToken();
                        String spotInfo[] = t.split(TartanSensors.PARAM_EQ);
                        occupiedState[Integer.parseInt(spotInfo[0]) - 1] = Integer.parseInt(spotInfo[1]);
                    }
                    currentState.put(TartanSensors.PARKING_SPOT_OCCUPIED, occupiedState);
                }
            }
        }
    }

    /**
     *  Process new parking stall states.
     *
     * @param stateString The new states.
     *
     * @return a list of parking stall state information.
     */
    private ArrayList<Integer> processParkingSpots(String stateString) {

        ArrayList<Integer> psState = new ArrayList<Integer>();
        char[] c = stateString.toCharArray();
        for (int i=0; i < c.length; i++) {
            psState.add(Integer.parseInt(String.valueOf(c[i])));
        }
        return psState;
    }

    /**
     * Issue command to set exit light.
     *
     * @param mode The mode for the exit light.
     *
     * @return True on success, false otherwise.
     */
    public Boolean setExitLight(String mode) {

        String response = connection.sendMessageToGarage(TartanSensors.EXIT_LIGHT + TartanSensors.PARAM_EQ + mode + TartanSensors.MSG_END);

        if (response == null) {
            return false;
        }

        return response.equals(TartanSensors.OK);
    }

    /**
     * Issue command to set entry light.
     *
     * @param mode The mode for the entry light.
     *
     * @return True on success, false otherwise.
     */
    public Boolean setEntryLight(String mode) {
        String response = connection.sendMessageToGarage(TartanSensors.ENTRY_LIGHT + TartanSensors.PARAM_EQ + mode + TartanSensors.MSG_END);

        if (response == null) {
            return false;
        }

        return response.equals(TartanSensors.OK);
    }

    /**
     * This thread monitors the entry/exit gates.
     */
    public void startUpdateThread() {

        updateThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    synchronized (connection) {
                        if (connection.isConnected() == false) {
                            return;
                        }
                        synchronized (currentState) {
                            updateGarageState();
                        }
                    }
                    if (vehicleDetectedAtEntry(currentState)) {
                        alertVehicleAtEntry();
                    }
                    if (vehicleDetectedAtExit(currentState) ) {
                        alertVehicleAtExit();
                    }
                    // currently a 5sec delay
                    try { Thread.sleep(5000); } catch (InterruptedException ie) { }
                }
            }
        });
        updateThread.start();
    }

    /**
     * Notify vehicle at entry gate. Note that this method notifies all parties who care
     * about vehicle entry.
     */
    private void alertVehicleAtEntry() {

        // The only observer should be the parking service
        setChanged();


        notifyObservers(TartanParams.MSG_VEHICLE_AT_ENTRY);
    }


    /**
     * Notify vehicle at exit gate. Note that this method notifies all parties who care
     * about vehicle departure.
     */
    private void alertVehicleAtExit() {
        setChanged();
        notifyObservers(TartanParams.MSG_VEHICLE_AT_EXIT);
    }

    /**
     * Signal vehicle at entry gate.
     *
     * @param state The state of the Tartan Garage.
     *
     * @return True if vehicle detected, false otherwise.
     */
    private Boolean vehicleDetectedAtEntry(HashMap<String, Object> state) {

        if (state.containsKey(TartanSensors.ENTRY_IR)) {
            Boolean gateState = (Boolean) state.get(TartanSensors.ENTRY_IR);
            if (gateState) {
                return true;
            }
        }
        return false;
    }

    /**
     * Signal vehicle at exit gate.
     *
     * @param state The state of the Tartan Garage.
     *
     * @return True if vehicle detected, false otherwise.
     */
    private Boolean vehicleDetectedAtExit(HashMap<String, Object> state) {
        if (state.containsKey(TartanSensors.EXIT_IR)) {
            Boolean gateState = (Boolean) state.get(TartanSensors.EXIT_IR);
            if (gateState) {
                return true;
            }
        }
        return false;
    }
}

