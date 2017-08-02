package edu.cmu.tartan;

import edu.cmu.tartan.service.TartanParams;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AdminConsoleDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JRadioButton occupiedRadioButton;
    private JRadioButton occupiedRadioButton1;
    private JRadioButton occupiedRadioButton2;
    private JRadioButton occupiedRadioButton3;

    private JLabel peak_usage;
    private JLabel revenue_value;
    private JList list1;
    private JLabel occupancy_value;
    Long revenue;
    HashMap<String, Integer> averageOccupancy;
    ArrayList<Integer> peakUsageHours;


    public AdminConsoleDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);


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

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public AdminConsoleDialog(HashMap<String, Object> message) {
        this();

        revenue = (Long) message.get(TartanParams.REVENUE);
        revenue_value.setText("total amount : " + 12);
        peakUsageHours = (ArrayList<Integer>) message.get(TartanParams.PEAK_USAGE_HOURS);
        if (peakUsageHours != null && peakUsageHours.size() > 1) {
            peak_usage.setText("time is from " + peakUsageHours.get(0) + " to " + String.valueOf(peakUsageHours.get(1) + 1));
        }
        averageOccupancy = (HashMap<String, Integer>) message.get(TartanParams.AVERAGE_OCCUPANCY);
        averageOccupancy = new HashMap<>();
        //for test
        averageOccupancy.put("2017.08.18", 45);
        averageOccupancy.put("2017.08.19", 90);
        averageOccupancy.put("2017.08.20", 95);

        Iterator ir = (Iterator) averageOccupancy.keySet().iterator();
        String day;
        StringBuilder sbr = new StringBuilder();
        sbr.append("<html>");
        while (ir.hasNext()) {
            day = (String) ir.next();

            sbr.append(" " + day + "  -->  " + String.valueOf(averageOccupancy.get(day)) + "% " + " <br>");


        }
        sbr.append("</html>");
        occupancy_value.setText(sbr.toString());

        Integer[] states = (Integer[]) message.get(TartanParams.ACTUAL_SPOT);

         occupiedRadioButton.setEnabled(false);
        occupiedRadioButton1.setEnabled(false);
        occupiedRadioButton2.setEnabled(false);
        occupiedRadioButton3.setEnabled(false);

        if (states != null && states.length > 0) {

            if (states[0] == 1) {
                occupiedRadioButton.setSelected(true);
                occupiedRadioButton.setText("occupied");
            } else {
                occupiedRadioButton.setSelected(false);
                occupiedRadioButton.setText("vacancy");
            }
            if (states[1] == 1) {
                occupiedRadioButton1.setSelected(true);
                occupiedRadioButton1.setText("occupied");
            } else {
                occupiedRadioButton1.setSelected(false);
                occupiedRadioButton1.setText("vacancy");
            }
            if (states[2] == 1) {
                occupiedRadioButton2.setSelected(true);
                occupiedRadioButton2.setText("occupied");
            } else {
                occupiedRadioButton2.setSelected(false);
                occupiedRadioButton2.setText("vacancy");
            }
            if (states[3] == 1) {
                occupiedRadioButton3.setSelected(true);
                occupiedRadioButton3.setText("occupied");
            } else {
                occupiedRadioButton3.setSelected(false);
                occupiedRadioButton3.setText("vacancy");
            }

        }


    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        AdminConsoleDialog dialog = new AdminConsoleDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
