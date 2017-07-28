package edu.cmu.tartan.service;

import edu.cmu.tartan.edu.cmu.tartan.reservation.Payment;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import edu.cmu.tartan.hardware.TartanGarageConnection;
import edu.cmu.tartan.hardware.TartanGarageManager;
import edu.cmu.tartan.hardware.TartanSensors;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The ParkingService is the Tartan service that manages parking spaces.
 *
 * Project: LG Exec Ed SDET Program
 * Copyright: 2017 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2016 - initial version
 */
public class ParkingService extends TartanService implements Observer {

    /**
     * The name of this service
     */
    public final static String PARKING_SERVICE = "ParkingService";

    /**
     * connection to the garage
     */
    private TartanGarageManager garageManager = null;

    /**
     * The list of reservations for vehicles in the garage
     */
    private Vector<Reservation> occupancy = new Vector<Reservation>();

    /**
     * The parking service manages parking spots in the garage
     */
    public ParkingService() {
        super.init(PARKING_SERVICE);
    }

    /**
     * Receive notifications (via an Observer) about vehicle entry/departure.
     *
     * @param obs  The element being observed.
     * @param obj, The message from the observed element.
     */
    @Override
    public void update(Observable obs, Object obj) {

        String cmd = (String) obj;
        if (cmd.equals(TartanParams.MSG_VEHICLE_AT_ENTRY)) {
            signalVehicleArrived();
        } else if (cmd.equals(TartanParams.MSG_VEHICLE_AT_EXIT)) {
            signalVehicleReadyToLeave();
        }
    }

    /**
     * Handle the situation where a vehicle is ready to exit the garage
     */
    private void signalVehicleReadyToLeave() {

        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put(TartanParams.COMMAND, TartanParams.MSG_VEHICLE_AT_EXIT);
        body.put(TartanParams.ACTUAL_SPOT, garageManager.getSpotOccupiedState());
        sendMessage(KioskService.KIOSK_SERVICE, body);
    }

    /**
     * Indicate that a vehicle has arrived.
     */
    private void signalVehicleArrived() {

        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put(TartanParams.COMMAND, TartanParams.MSG_VEHICLE_AT_ENTRY);
        body.put(TartanParams.ACTUAL_SPOT, garageManager.getSpotOccupiedState());
        sendMessage(KioskService.KIOSK_SERVICE, body);
    }

    /**
     * Connect to a house
     *
     * @param houseAddress The network address of the house. Once connected, this method starts a new thread to
     *                     update house state.
     * @return True if connected, false otherwise.
     */
    public Boolean connectToGarage(String houseAddress) {
        try {
            garageManager = new TartanGarageManager(TartanGarageConnection.getConnection(houseAddress));
            garageManager.addObserver(this); // for notifications
            initialize();
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
        }

        return false;
    }

    /**
     * Initialize the parking system by closing gates and setting lights to red.
     */
    private void initialize() {
        garageManager.setEntryLight(TartanSensors.RED);
        garageManager.setExitLight(TartanSensors.RED);
        garageManager.closeEntryGate();
        garageManager.closeExitGate();

        ArrayList<String> lightState = new ArrayList<String>();
        for (int i = 0; i < garageManager.getCapacity(); i++) {
            lightState.add(TartanSensors.OFF);
        }
        garageManager.setParkingSpotLights(lightState);

        garageManager.startUpdateThread();
    }


    /**
     * Disconnect from the garage.
     */
    public void disconnectFromGarage() {
        if (garageManager.isConnected()) {
            garageManager.disconnectFromGarage();
        }
    }

    /**
     * Terminate the service.
     */
    protected void finalize() {
        disconnectFromGarage();
        stop();
    }

    /**
     * Allow a vehicle to enter by setting the entry light to green and opening the gate. If the assigned spot is taken
     * then handle updating the reservation.
     *
     * @param rsvp The reservation associated with this entry.
     */
    private void handleGarageEntry(Reservation rsvp) {

        if (garageManager.isConnected()) {

            Integer spot = rsvp.getSpotId();
            Integer[] occupiedStateCheck = garageManager.getSpotOccupiedState();
            if (occupiedStateCheck[spot] == 1) {
                // The spot is already occupied. Try to update a new RSVP

                HashMap<String, Object> body = new HashMap<String, Object>();
                body.put(TartanParams.COMMAND, TartanParams.MSG_UPDATE_RSVP);

                rsvp.setSpotId(TartanParams.INVALID_SPOT);

                body.put(TartanParams.PAYLOAD, rsvp);
                sendMessage(ReservationService.RESERVATION_SERVICE, body);
                return;
            }

            garageManager.setEntryLight(TartanSensors.GREEN);
            Integer[] preParkState = garageManager.getSpotOccupiedState();
            garageManager.openEntryGate();

            ArrayList<String> lightState = new ArrayList<String>();

            for (int i = 0; i < garageManager.getCapacity(); i++) {
                if (i == spot) lightState.add(i, TartanSensors.ON);
                else {
                    lightState.add(i, TartanSensors.OFF);
                }
            }
            garageManager.setParkingSpotLights(lightState);

            // you get 10 seconds to enter
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            garageManager.setEntryLight(TartanSensors.RED);
            garageManager.closeEntryGate();

            // signal the entry is complete
            HashMap<String, Object> body = new HashMap<String, Object>();
            body.put(TartanParams.COMMAND, TartanParams.MSG_ENTRY_COMPLETE);
            body.put(TartanParams.PAYLOAD, rsvp);
            sendMessage(KioskService.KIOSK_SERVICE, body);


            // Now guide car to spot
            int timeout = 0;
            Boolean parkedOK = false;
            Integer[] occupiedState = null;
            while (timeout < 6) {
                occupiedState = garageManager.getSpotOccupiedState();
                if (occupiedState[spot] == 1) {
                    parkedOK = true;
                    break;
                } else {
                    timeout++;
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                    }
                }
            }

            //  The driver parked in the wrong spot
            if (!parkedOK) {
                Integer wrongSpot = TartanParams.INVALID_SPOT;
                for (int i = 0; i < preParkState.length; i++) {
                    if (!preParkState[i].equals(occupiedState[i])) {
                        wrongSpot = i;
                        break;
                    }
                }


                HashMap<String, Object> resp = new HashMap<String, Object>();
                resp.put(TartanParams.COMMAND, TartanParams.MSG_WRONG_SPOT);
                HashMap<String, Object> msg = new HashMap<String, Object>();

                msg.put(TartanParams.RSVP, rsvp);
                msg.put(TartanParams.ACTUAL_SPOT, wrongSpot);
                resp.put(TartanParams.PAYLOAD, msg);

                sendMessage(KioskService.KIOSK_SERVICE, resp);
            }

            // parking spot occupied, turn off the lights
            ArrayList<String> offLightState = new ArrayList<String>();
            for (int i = 0; i < garageManager.getCapacity(); i++) {
                offLightState.add(TartanSensors.OFF);
            }
            garageManager.setParkingSpotLights(offLightState);

            // Log that the car is now in the garage
            synchronized (occupancy) {
                occupancy.add(rsvp);
            }
        }
    }

    /**
     * Handle garage exit by setting the exit light to green and opening the gate.
     */
    private void allowGarageExit() {

        if (garageManager.isConnected()) {
            garageManager.setExitLight(TartanSensors.GREEN);
            garageManager.openExitGate();

            // you get 10 seconds to exit
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            garageManager.setExitLight(TartanSensors.RED);
            garageManager.closeExitGate();
        }
    }

    /**
     * Run the parking service thread.
     */
    @Override
    public void run() {

        System.out.println("ParkingService.run");
    }

    /**
     * Handle Tartan service messages. The following messages are handled:
     * <p>
     * <ul>
     * <li>MSG_GET_PARKING_SPOTS: Fetch the number of supported parking spots and their states.</li>
     * <li>MSG_ENTER_GARAGE: Handle garage entry.</li>
     * <li>MSG_EXIT_GARAGE: Handle garage exit.</li>
     * <li>MSG_UPDATE_RSVP: This message acknowledges that a reservation has been updated.</li>
     * </ul>
     *
     * @param message
     */
    @Override
    public void handleMessage(HashMap<String, Object> message) {

        String cmd = (String) message.get(TartanParams.COMMAND);

        if (cmd.equals(TartanParams.MSG_GET_PARKING_SPOTS)) {
            handleGetParkingSpots(message);
        }
        if (cmd.equals(TartanParams.MSG_ENTER_GARAGE)) {
            Reservation rsvp = (Reservation) message.get(TartanParams.PAYLOAD);
            handleGarageEntry(rsvp);
        }
        if (cmd.equals(TartanParams.MSG_EXIT_GARAGE)) {
            handleGarageExitRequest(message);
        }
        if (cmd.equals(TartanParams.MSG_PAYMENT_COMPLETE)) {
            handleExecuteGarageExit(message);
        }
        if (cmd.equals(TartanParams.MSG_UPDATE_RSVP)) {
            Reservation rsvp = (Reservation) message.get(TartanParams.PAYLOAD);
            handleGarageEntry(rsvp);
        }

    }

    /**
     * Actually allow the exit and generate a payment receipt
     *
     * @param message the payment complete message.
     */
    private void handleExecuteGarageExit(HashMap<String, Object> message) {

        Reservation rsvp = (Reservation) message.get(TartanParams.PAYLOAD);
        String vid = rsvp.getVehicleID();

        Payment payment = rsvp.getPayment();
        if (payment.getFee() != null) {
            allowGarageExit();

            synchronized (occupancy) {
                ListIterator<Reservation> iter = occupancy.listIterator();

                while (iter.hasNext()) {
                    if (iter.next().getVehicleID().equals(vid)) {
                        iter.remove();
                        break;
                    }
                }
            }

            HashMap<String, Object> receipt = new HashMap<String, Object>();
            receipt.put(TartanParams.COMMAND, TartanParams.MSG_EXIT_COMPLETE);
            receipt.put(TartanParams.PAYLOAD, rsvp);
            sendMessage(KioskService.KIOSK_SERVICE, receipt);
        }
    }

    /***
     * Handle garage exit.
     *
     * @param message the exit message.
     */
    private void handleGarageExitRequest(HashMap<String, Object> message) {

        String vid = (String) message.get(TartanParams.PAYLOAD);

        Boolean leavingVehicle = false;
        synchronized (occupancy) {
            for (Reservation r : occupancy) {
                if (r.getVehicleID().equals(vid)) {
                    leavingVehicle = true;
                    break;
                }
            }
        }

        // if the vehicle leaving was properly identified, then apply the payment. Otherwise ask the
        // driver to enter the vehicle ID again.

        if (leavingVehicle) {
            applyPayment(vid);
        }
    }

    private void applyPayment(String vid) {

        Reservation rsvp = null;
        ListIterator<Reservation> iter = occupancy.listIterator();
        while (iter.hasNext()) {
            Reservation tmp = iter.next();
            if (tmp.getVehicleID().equals(vid)) {
                rsvp = tmp;
                break;
            }
        }

        HashMap<String, Object> message = new HashMap<String, Object>();
        message.put(TartanParams.COMMAND, TartanParams.MSG_MAKE_PAYMENT);
        message.put(TartanParams.PAYLOAD, rsvp);

        // send the payment
        sendMessage(PaymentService.PAYMENT_SERVICE, message);
    }

    /**
     * Handle the request for fetching parking spots.
     *
     * @param request The parking spot request.
     */
    private void handleGetParkingSpots(HashMap<String, ?> request) {

        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put(TartanParams.COMMAND, TartanParams.MSG_GET_PARKING_SPOTS);
        body.put(TartanParams.PAYLOAD, garageManager.getParkingSpots());

        sendMessage(ReservationService.RESERVATION_SERVICE, body);
    }

    /**
     * Terminate the service.
     */
    @Override
    public void terminate() {
        garageManager.disconnectFromGarage();
        stop();

    }
}
