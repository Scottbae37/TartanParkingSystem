package edu.cmu.tartan.integrationtest;

import edu.cmu.tartan.TartanGarageDriver;
import edu.cmu.tartan.TartanKioskWindow;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Payment;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import edu.cmu.tartan.hardware.TartanGarageConnection;
import edu.cmu.tartan.hardware.TartanGarageManager;
import edu.cmu.tartan.hardware.TartanSensors;
import edu.cmu.tartan.service.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static org.mockito.Matchers.anyString;


@RunWith(PowerMockRunner.class)
@PrepareForTest({TartanServiceMessageBus.class, TartanGarageManager.class, TartanGarageConnection.class, TartanGarageDriver.class, TartanKioskWindow.class, JOptionPane.class})
public class GarageEntryTest {
    private TartanServiceMessageBus msgBus;
    private MessageConsumer consumer;
    private MessageProducer producer;

    private ArrayList<TartanService> services = new ArrayList<>();
    private static TartanGarageConnection connection = null;
    private String bufferReadLine;
    private TartanGarageDriver garage;
    private TartanKioskWindow tartanKioskWindow;

    private KioskService kioskService;
    private ReservationService reservationService;

    @Before
    public void setUp() throws Exception {

        services = new ArrayList<>();
        garage = new TartanGarageDriver();
        tartanKioskWindow = PowerMockito.mock(TartanKioskWindow.class);
        PowerMockito.mockStatic(TartanKioskWindow.class);
        PowerMockito.whenNew(TartanKioskWindow.class).withArguments(KioskService.class).thenReturn(tartanKioskWindow);
        msgBus = PowerMockito.mock(TartanServiceMessageBus.class);
        consumer = PowerMockito.mock(MessageConsumer.class);
        producer = PowerMockito.mock(MessageProducer.class);
        PowerMockito.when(msgBus.getConsumer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(consumer);
        PowerMockito.when(msgBus.getProducer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(producer);
        PowerMockito.mockStatic(TartanServiceMessageBus.class);
        PowerMockito.when(TartanServiceMessageBus.connect()).thenReturn(msgBus);
        PowerMockito.mockStatic(JOptionPane.class);
        PowerMockito.doNothing().when(JOptionPane.class, "showMessageDialog", Mockito.any(Component.class),
                Mockito.any(Object.class), Mockito.anyString(), Mockito.anyInt());

        connection = PowerMockito.mock(TartanGarageConnection.class);
        PowerMockito.mockStatic(TartanGarageConnection.class);

        String settings[] = {"./", "address"};
        Mockito.when(connection.connect(settings[1])).thenReturn(true);
        Mockito.when(connection.isConnected()).thenReturn(true);
        PowerMockito.when(TartanGarageConnection.getConnection(settings[1])).thenReturn(connection);

        PowerMockito.doAnswer(new Answer<ObjectMessage>() {
            @Override
            public ObjectMessage answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String target = (String) args[1];
                final HashMap<String, Object> m = (HashMap<String, Object>) args[0];
                for (TartanService service : services) {
                    if ("RsvpService".equals(target)) {
                        target = "ReservationService";
                    }

                    if (service.getClass().getName().contains(target)) {
                        System.out.println("Mock bus : " + target + ", " + m);
                        service.handleMessage(m);
                    }
                }
                return null;
            }
        }).when(msgBus, "generateMessage", Mockito.any(HashMap.class), Mockito.anyString());

        PowerMockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments(); // arguments
                String cmd = (String) args[0];
                //String result = "";
                System.out.println("sendToMessage cmd=" + cmd);
                switch (cmd) {
                    case TartanSensors.ENTRY_LIGHT + TartanSensors.PARAM_EQ + TartanSensors.GREEN + TartanSensors.MSG_END:
                    case TartanSensors.ENTRY_LIGHT + TartanSensors.PARAM_EQ + TartanSensors.RED + TartanSensors.MSG_END:
                    case TartanSensors.ENTRY_GATE + TartanSensors.PARAM_EQ + TartanSensors.OPEN + TartanSensors.MSG_END:
                    case TartanSensors.ENTRY_GATE + TartanSensors.PARAM_EQ + TartanSensors.CLOSE + TartanSensors.MSG_END:
                    case TartanSensors.EXIT_GATE + TartanSensors.PARAM_EQ + TartanSensors.OPEN + TartanSensors.MSG_END:
                    case TartanSensors.EXIT_GATE + TartanSensors.PARAM_EQ + TartanSensors.CLOSE + TartanSensors.MSG_END:
                        bufferReadLine = TartanSensors.OK;
                        break;
                    case TartanSensors.GET_STATE + TartanSensors.MSG_END:
                        break;
                    //case TartanSensors.ENTRY_IR +
                    default:
                        break;

                }

                return bufferReadLine;
            }
        }).when(connection, "sendMessageToGarage", anyString());

        garage.startGarage(settings);
        Field fields[] = garage.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            services = (ArrayList<TartanService>) field.get(garage);
        }

        for (TartanService service : services) {
            if (service instanceof KioskService) {
                kioskService = (KioskService) service;
                kioskService.setKiosk(tartanKioskWindow);
            } else if (service instanceof ReservationService) {
                reservationService = (ReservationService) service;
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        for (TartanService s : services) {
            s.terminate();
        }
    }


    @Test
    public void testEntryProcedure_LeaveWithoutRedeem() throws Exception {
        try {
            Thread.sleep(500);
            //Car was arrived at entry.
            bufferReadLine = "SU:NG=0;XG=0;NL=R;NIR=1;XIR=0;XL=R;PO=[1=0,2=0,3=0,4=0];PL=[1=0,2=0,3=0,4=0]";
            Thread.sleep(5000);
            Mockito.verify(tartanKioskWindow, Mockito.atMost(2)).setStatus(Mockito.any(HashMap.class));
            Mockito.verify(tartanKioskWindow, Mockito.atMost(2)).enableRsvpRedemption();
            System.out.println("Redeem is enabled.");

            //Car was gone without redeem.
            bufferReadLine = "SU:NG=0;XG=0;NL=R;NIR=0;XIR=0;XL=R;PO=[1=0,2=0,3=0,4=0];PL=[1=0,2=0,3=0,4=0]";
            Thread.sleep(5000);
            Mockito.verify(tartanKioskWindow, Mockito.atMost(3)).setStatus(Mockito.any(HashMap.class));
            Mockito.verify(tartanKioskWindow, Mockito.atMost(3)).disableRsvpRedemption();
        } catch (InterruptedException ie) {
        }
    }

    @Test
    public void testEntryProcudyre_ParkingWrongSpot() throws Exception {
        try {
            Thread.sleep(500);
            makeNewReservation();

            //Car was arrived at entry.
            bufferReadLine = "SU:NG=0;XG=0;NL=R;NIR=1;XIR=0;XL=R;PO=[1=0,2=0,3=0,4=0];PL=[1=0,2=0,3=0,4=0]";
            Thread.sleep(5000);
            Mockito.verify(tartanKioskWindow, Mockito.atMost(2)).setStatus(Mockito.any(HashMap.class));
            Mockito.verify(tartanKioskWindow, Mockito.atMost(2)).enableRsvpRedemption();
            System.out.println("Redeem is enabled.");
            kioskService.getReservation("IntegrationTest", "IntegrationTest");

            //Park wrong spot
            bufferReadLine = "SU:NG=0;XG=0;NL=R;NIR=1;XIR=0;XL=R;PO=[1=0,2=0,3=1,4=0];PL=[1=0,2=0,3=0,4=0]";
            Thread.sleep(5000);

            ArgumentCaptor<String> command = ArgumentCaptor.forClass(String.class);
            Mockito.verify(tartanKioskWindow).showError(command.capture());
            Assert.assertTrue(command.getValue().contains("wrong spot"));
        } catch (InterruptedException ie) {
        }
    }

    private void makeNewReservation() throws Exception {
        Reservation reservation = new Reservation();
        Payment payment = new Payment();
        payment.setCcNum("IntegrationTest");
        payment.setCcName("IntegrationTest");
        payment.setCcExpDate("December\\2020");
        Mockito.when(tartanKioskWindow.acceptPayment(reservation)).thenReturn(payment);
        reservation.setCustomerName("IntegrationTest");
        reservation.setVehicleID("IntegrationTest");
        Date startDate = Calendar.getInstance().getTime();
        Date endDate = new Date();
        startDate.setTime(startDate.getTime() + 1000);
        endDate.setTime(startDate.getTime() + 1000 * 60 * 60);
        reservation.setStartTime(startDate);
        reservation.setEndTime(endDate);

        kioskService.makeNewReservation(reservation);
    }
}
