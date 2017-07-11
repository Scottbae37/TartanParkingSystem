package edu.cmu.tartan.hardware;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A connection to the Tartan Garage. This class handles the network connection to the
 * garage.
 *
 * Project: LG Exec Ed SDET Program
 * Copyright: 2017 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2016 - initial version
 */
public class TartanGarageConnection {
    private Boolean isConnected = false;

    /** connection settings */
    private String address = null;

    private final Integer PORT = 5050; // the default port for the house

    /** The connection is private so it can be controlled */
    private static TartanGarageConnection connection = null;
    private Socket houseSocket=null;
    private BufferedWriter out=null;
    private BufferedReader in = null;

    private TartanGarageConnection() { }

    /**
     * Get the house address.
     *
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Get an existing connection, or make a new one
     * @param addr the house address
     * @return the established connection or null
     */
    public static TartanGarageConnection getConnection(String addr) {
        if (addr!=null) {
            if (connection != null) {
                if (connection.isConnected()) {
                    if (connection.getAddress().equals(addr)) {
                        return connection; // already connected to this garage
                    } else {
                        connection.disconnect(); // connect to new garage
                        connection.connect(addr);
                    }
                } else {
                    connection.connect(addr); // connect to garage
                }
            } else {
                // first connection
                connection = new TartanGarageConnection();
                connection.connect(addr);
            }
        }
        // invalid address - return current connection
        if (connection.isConnected()) {
            return connection;
        }
        return null;
    }

    /**
     * Get connection currentState
     * @return true if connected, false otherwise
     */
    public Boolean isConnected() {
        return isConnected;
    }

    /**
     * Send a message to the garage and get a response
     * @param msg the message to send
     * @return the response
     */
    public String sendMessageToGarage(String msg) {
        try {

            out.write(msg, 0, msg.length());
            out.flush();

            return in.readLine();

        } catch (IOException ioe) {
            //ioe.printStackTrace();
        }
        return null;
    }

    /**
     * Disconnect from the house
     */
    public void disconnect() {
        if (houseSocket!=null) {
            if (houseSocket.isConnected()) {
                try {
                    houseSocket.close();
                } catch (IOException e) {

                }
            }
        }
        isConnected = false;
    }

    /**
     * Connect to a garage
     * @param addr the address of the house
     * @return true if connection successful, false otherwise
     */
    private Boolean connect(String addr) {
        address = addr;

        try {
            houseSocket = new Socket(address, PORT);

            out = new BufferedWriter(new OutputStreamWriter(houseSocket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader( houseSocket.getInputStream()));

        } catch (UnknownHostException uhe) {
            System.out.println("Unknown host: " + address);
            return false;
        } catch (IOException ioe){
            ioe.printStackTrace();
            return false;
        }
        System.out.println("Connected");
        isConnected = true;
        return true;
    }
}
