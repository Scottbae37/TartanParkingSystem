package edu.cmu.tartan;

import javax.swing.*;
import java.awt.event.*;

public class AdminConsoleDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JRadioButton occupiedRadioButton;
    private JRadioButton occupiedRadioButton1;
    private JRadioButton occupiedRadioButton2;
    private JRadioButton occupiedRadioButton3;


    public AdminConsoleDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        occupiedRadioButton.setSelected(false);
        occupiedRadioButton1.setSelected(false);
        occupiedRadioButton2.setSelected(false);
        occupiedRadioButton3.setSelected(true);

        occupiedRadioButton3.setText("occupied by kyungman");

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
