package edu.cmu.tartan;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Handle new reservations.
 * <p>
 * Project: LG Exec Ed SDET Program
 * Copyright: 2017 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2016 - initial version
 */
public class NewReservationDialog extends JDialog {

    final SimpleDateFormat DATE_PARSER = new SimpleDateFormat("EEE, MMM dd hh a yyyy", Locale.ENGLISH);
    Predicate<String> IS_EMPTY = (s) -> Objects.isNull(s) || s.isEmpty();
    /**
     * Various GUI elements for the dialog
     */
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameTextField;
    private JTextField licensePlateTextField;
    private JComboBox startTimeComboBox;
    private JComboBox endTimeComboBox;
    private JComboBox startDayComboBox;
    private JComboBox endDayComboBox;
    /**
     * the new reservation.
     */
    private Reservation rsvp = null;

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Create the new reservation dialog.
     *
     * @param window The main window.
     */
    public NewReservationDialog(TartanKioskWindow window) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        setLocationRelativeTo(window);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // Fill out the day selectors
        populateDate(startDayComboBox);
        populateTime(startTimeComboBox);
        populateDate(endDayComboBox);
        populateTime(endTimeComboBox);

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /**
     * Set up the date combo box.
     *
     * @param dayCb The date combo box
     */
    private void populateDate(JComboBox dayCb) {
        try {
            Calendar c = Calendar.getInstance();
            for (int i = 0; i < 7; i++) {
                Date date = c.getTime();
                SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMM dd", Locale.ENGLISH);
                dayCb.addItem(formatter.format(date));
                System.out.println(formatter.format(date));
                c.add(Calendar.DAY_OF_YEAR, 1);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
        }
    }

    /**
     * Set up the time combo box.
     *
     * @param timeCb The time combo box
     */
    private void populateTime(JComboBox timeCb) {

        for (int i = 0; i < 24; i++) {

            String t = String.valueOf((i % 12) == 0 ? 12 : (i % 12));
            t += (i > 11) ? " PM" : " AM";
            timeCb.addItem(t);
        }
    }

    /**
     * Handle successfully completing the new reservation.
     */
    private void onOK() {
        try {
            if (verifyInput() == false) {
                return;
            }

            String customerName = nameTextField.getText();
            String licensePlate = licensePlateTextField.getText();
            String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

            String start = String.valueOf(startDayComboBox.getSelectedItem()) + " " + String.valueOf(startTimeComboBox.getSelectedItem() + " " + year);
            String end = String.valueOf(endDayComboBox.getSelectedItem()) + " " + String.valueOf(endTimeComboBox.getSelectedItem() + " " + year);

            Date startDate = DATE_PARSER.parse(start);
            Date endDate = DATE_PARSER.parse(end);

            rsvp = new Reservation();
            rsvp.setCustomerName(customerName);
            rsvp.setVehicleID(licensePlate);
            rsvp.setStartTime(startDate);
            rsvp.setEndTime(endDate);
        } catch (Exception e) {
            e.printStackTrace();
            rsvp = null;
        }
        dispose();
    }

    private boolean verifyInput() throws ParseException {
        String msg = null;
        boolean isValid = true;
        String customerName = nameTextField.getText();
        String licensePlate = licensePlateTextField.getText();

        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        String strStart = String.valueOf(startDayComboBox.getSelectedItem()) + " " + String.valueOf(startTimeComboBox.getSelectedItem() + " " + year);
        String strEnd = String.valueOf(endDayComboBox.getSelectedItem()) + " " + String.valueOf(endTimeComboBox.getSelectedItem() + " " + year);

        Date start = DATE_PARSER.parse(strStart);
        Date end = DATE_PARSER.parse(strEnd);

        // check other parameters
        if (IS_EMPTY.test(customerName)) {
            msg = "Invalid customer name\nCheck it out!";
            isValid = false;
        } else if (IS_EMPTY.test(licensePlate)) {
            msg = "Invalid license plate\nCheck it out!";
            isValid = false;
        }
        // start time must come before end time
        else if (!(start.before(end))) {
            msg = "start time must come before end time\nCheck it out!";
            isValid = false;
        }
        // check bounds for reservation

        // max reservation is 24 hours
        else if (((end.getTime() - start.getTime()) / (1000 * 60 * 60)) > 24) {
            msg = "max reservation is 24 hours\nCheck it out!";
            isValid = false;
        }

        // No reservations in the past
        else if (Calendar.getInstance().getTime().after(start)) {
            msg = "No reservations in the past\nCheck it out!";
            isValid = false;
        }

        // prevent reservations more than a week out
        else if ((start.getTime() - System.currentTimeMillis()) >= 604800000) {
            msg = "prevent reservations more than a week out\nCheck it out!";
            isValid = false;
        } else if ((end.getTime() - System.currentTimeMillis()) >= 604800000) {
            msg = "prevent reservations more than a week out\nCheck it out!";
            isValid = false;
        }

        if (isValid == false) {
            JOptionPane.showMessageDialog(this,
                    msg,
                    "Invalid input",
                    JOptionPane.ERROR_MESSAGE);
        }
        return isValid;
    }


    /**
     * Fetch the new reservation.
     *
     * @return The reservation.
     */
    public Reservation getNewRsvp() {
        return rsvp;
    }

    /**
     * Cancel the new reservation.
     */
    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText(" Name:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("License Plate:");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Date:");
        panel3.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameTextField = new JTextField();
        panel3.add(nameTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        licensePlateTextField = new JTextField();
        panel3.add(licensePlateTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), null));
        final JLabel label4 = new JLabel();
        label4.setText("Day");
        panel4.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startTimeComboBox = new JComboBox();
        panel4.add(startTimeComboBox, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        endTimeComboBox = new JComboBox();
        panel4.add(endTimeComboBox, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("End Time");
        panel4.add(label5, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Start Time");
        panel4.add(label6, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startDayComboBox = new JComboBox();
        panel4.add(startDayComboBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
