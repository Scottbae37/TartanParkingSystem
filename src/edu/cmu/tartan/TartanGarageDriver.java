package edu.cmu.tartan;

import edu.cmu.tartan.service.*;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * The main driver for the Tartan Application.
 *
 * Project: LG Exec Ed SDET Program
 * Copyright: 2017 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2016 - initial version
 */
public class TartanGarageDriver {

    /** List of services. */
    private ArrayList<TartanService> services = new ArrayList<TartanService>();

    /**
     * Default constructor.
     */
    public TartanGarageDriver() {    }

    /**
     * Shut down the services.
     */
    private void terminateGarage() {
        for (TartanService s : services) {
            s.terminate();
        }
    }

    /**
     * Start the garage by starting the services.
     *
     * @param settings The settings to use for this run.
     */
    public void startGarage(String[] settings) {

        KioskService kioskService = new KioskService();
        services.add(kioskService);

        PaymentService paymentService = new PaymentService();
        services.add(paymentService);

        ReservationService reservationService = new ReservationService(settings[0]);
        services.add(reservationService);

        ParkingService parkingService = new ParkingService();
        services.add(parkingService);

        // start the kiosk window
        TartanKioskWindow window = new TartanKioskWindow(kioskService);

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                terminateGarage();
            }
        });

        System.out.println("Connecting to " + settings[1]);

        if (parkingService.connectToGarage(settings[1])) {

            // start the services
            new Thread(reservationService).start();
            new Thread(kioskService).start();
            new Thread(parkingService).start();
            new Thread(paymentService).start();
        }
        else {
            JOptionPane.showMessageDialog(window,
                    "Cannot connect to garage!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
        }
    }

    public static void main(String[] args) {

        // allow objects to be serializable with activemq
        System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","*");

        TartanGarageDriver garage = new TartanGarageDriver();
        garage.startGarage(args);

    }
}
