package edu.cmu.tartan.hardware;

public class TartanGarageManagerSpy extends  TartanGarageManager {
    /**
     * Set up the connection manager with a connection.
     *
     * @param conn the (established) connection
     */
    public TartanGarageManagerSpy(TartanGarageConnection conn) {
        super(conn);
    }

    @Override
    public void startUpdateThread() {
        super.startUpdateThread();
        try {
            updateThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
