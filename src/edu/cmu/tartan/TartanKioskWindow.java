package edu.cmu.tartan;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Payment;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import edu.cmu.tartan.service.KioskService;
import edu.cmu.tartan.service.TartanService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * UI to handle customer interaction.
 * <p/>
 * Project: LG Exec Ed SDET Program
 * Copyright: 2017 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2016 - initial version
 */
public class TartanKioskWindow extends JFrame {

    /**
     * GUI elements
     */
    private JButton makeNewReservationButton;
    private JTextField nameTextField;
    private JTextField licensePlateTextField;
    private JPanel mainPanel;
    private JLabel nameLabel;
    private JLabel licensePlateLabel;
    private JLabel mainLabel;
    private JPanel getRsvpPanel;
    private JButton adminButton;
    private JButton redeemReservationButton;

    /**
     * The kiosk service associated with this window
     */
    private KioskService kioskService = null;

    /**
     * Create a new kiosk window.
     *
     * @param kioskService The kiosk service associated with this window.
     */
    public TartanKioskWindow(final KioskService kioskService) {
        super("Tartan Parking Kiosk");

        setContentPane(mainPanel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.kioskService = kioskService;

        redeemReservationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameTextField.getText();
                String licensePlate = licensePlateTextField.getText();
                kioskService.getReservation(name, licensePlate);
            }
        });

        disableRsvpRedemption();

        makeNewReservationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                NewReservationDialog newRsvpDialog = new NewReservationDialog(TartanKioskWindow.this);
                newRsvpDialog.pack();
                newRsvpDialog.setVisible(true);
                newRsvpDialog.setModal(true);

                Reservation rsvp = newRsvpDialog.getNewRsvp();
                if (rsvp != null) {
                    kioskService.makeNewReservation(rsvp);
                }
            }
        });

        kioskService.setKiosk(this);

        setVisible(true);
    }

    /**
     * Show the user an error.
     *
     * @param errMsg The error message.
     */
    public void showError(String errMsg) {
        JOptionPane.showMessageDialog(this,
                errMsg,
                "Error Creating Reservation",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Open the payment dialog to accept a payment.
     *
     * @return The accepted payment.
     */
    public Payment acceptPayment() {

        PaymentDialog paymentDialog = new PaymentDialog();
        paymentDialog.pack();
        paymentDialog.setModal(true);
        paymentDialog.setVisible(true);

        return paymentDialog.getPayment();
    }

    /**
     * Inform the customer that their reservation is confirmed.
     *
     * @param rsvp The confirmed reservation.
     */
    public void confirmReservation(Reservation rsvp) {
        JOptionPane.showMessageDialog(this,
                rsvp.toString(),
                "New reservation confirmed",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Notify the customer that the reservation has been updated.
     *
     * @param rsvp The updated reservation.
     */
    public void notifyUpdatedReservation(Reservation rsvp) {

        nameTextField.setText("");
        licensePlateTextField.setText("");

        JOptionPane.showMessageDialog(this,
                "Note that your reservation has been updated:\n" + rsvp.toString(),
                "Updated reservation",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Inform the customer that they have successfully redeemed their reservation.
     *
     * @param rsvp The redeemed reservation.
     */
    public void redeemReservation(Reservation rsvp) {

        nameTextField.setText("");
        licensePlateTextField.setText("");

        JOptionPane.showMessageDialog(this,
                rsvp.toString(),
                "Redeemed reservation",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void showReceipt(Reservation rsvp) {

        Payment p = rsvp.getPayment();
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
        String receipt = n.format(p.getFee());

        JOptionPane.showMessageDialog(this,
                "A payment of " + receipt + " was completed successfully.",
                "Payment Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Allow the user to redeem a reservation.
     */
    public void enableRsvpRedemption() {
        redeemReservationButton.setEnabled(true);
        nameTextField.setEnabled(true);
        licensePlateTextField.setEnabled(true);
    }

    /**
     * Disable reservation redemption.
     */
    public void disableRsvpRedemption() {
        redeemReservationButton.setEnabled(false);
        nameTextField.setEnabled(false);
        licensePlateTextField.setEnabled(false);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        makeNewReservationButton = new JButton();
        makeNewReservationButton.setText("Make New Reservation");
        mainPanel.add(makeNewReservationButton, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        getRsvpPanel = new JPanel();
        getRsvpPanel.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(getRsvpPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        getRsvpPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), null));
        nameTextField = new JTextField();
        getRsvpPanel.add(nameTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        nameLabel = new JLabel();
        nameLabel.setText("Name:");
        getRsvpPanel.add(nameLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        licensePlateLabel = new JLabel();
        licensePlateLabel.setText("License Plate:");
        getRsvpPanel.add(licensePlateLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        licensePlateTextField = new JTextField();
        getRsvpPanel.add(licensePlateTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer2 = new Spacer();
        getRsvpPanel.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        redeemReservationButton = new JButton();
        redeemReservationButton.setText("Redeem Reservation");
        getRsvpPanel.add(redeemReservationButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mainLabel = new JLabel();
        mainLabel.setBackground(new Color(-16777216));
        mainLabel.setFont(new Font(mainLabel.getFont().getName(), Font.BOLD, 24));
        mainLabel.setForeground(new Color(-3924422));
        mainLabel.setHorizontalAlignment(0);
        mainLabel.setText("Tartan Parking Kiosk");
        mainPanel.add(mainLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        adminButton = new JButton();
        adminButton.setText("Launch Administrator Console");
        mainPanel.add(adminButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }


}
