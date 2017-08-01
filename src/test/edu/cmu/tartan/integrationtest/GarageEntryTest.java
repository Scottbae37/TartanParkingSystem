package edu.cmu.tartan.integrationtest;

import edu.cmu.tartan.hardware.TartanGarageConnection;
import edu.cmu.tartan.hardware.TartanGarageManager;
import edu.cmu.tartan.hardware.TartanSensors;
import edu.cmu.tartan.service.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import java.util.ArrayList;

import static org.mockito.Matchers.anyString;


@RunWith(PowerMockRunner.class)
@PrepareForTest({TartanServiceMessageBus.class, TartanGarageManager.class, TartanGarageConnection.class})
public class GarageEntryTest {
    private TartanServiceMessageBus msgBus;
    private MessageConsumer consumer;
    private MessageProducer producer;

    private ArrayList<TartanService> services = new ArrayList<>();
    private static TartanGarageConnection connection = null;
    private String bufferReadLine;

    @Before
    public void setUp() throws Exception {

        msgBus = PowerMockito.mock(TartanServiceMessageBus.class);
        consumer = PowerMockito.mock(MessageConsumer.class);
        producer = PowerMockito.mock(MessageProducer.class);
        PowerMockito.when(msgBus.getConsumer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(consumer);
        PowerMockito.when(msgBus.getProducer(TartanServiceMessageBus.TARTAN_TOPIC)).thenReturn(producer);
        PowerMockito.mockStatic(TartanServiceMessageBus.class);
        PowerMockito.when(TartanServiceMessageBus.connect()).thenReturn(msgBus);

        connection = PowerMockito.mock(TartanGarageConnection.class);
        PowerMockito.mockStatic(TartanGarageConnection.class);

        String settings[] = {"exec", "address"};
        Mockito.when(connection.connect(settings[1])).thenReturn(true);
        Mockito.when(connection.isConnected()).thenReturn(true);
        PowerMockito.when(TartanGarageConnection.getConnection(settings[1])).thenReturn(connection);

        bufferReadLine = "SU:NG=0;XG=0;NL=R;NIR=0;XIR=0;XL=R;PO=[1=0,2=0,3=0,4=0];PL=[1=0,2=0,3=0,4=0]";
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

        startGarage(settings);
    }

    public void startGarage(String[] settings) {
        KioskService kioskService = new KioskService();
        services.add(kioskService);

        PaymentService paymentService = new PaymentService();
        services.add(paymentService);

        ParkingService parkingService = new ParkingService();
        services.add(parkingService);

        System.out.println("Connecting to " + settings[1]);
        boolean result = parkingService.connectToGarage(settings[1]);
        System.out.println("connection result=" + result);
        if (parkingService.connectToGarage(settings[1])) {
            new Thread(kioskService).start();
            new Thread(paymentService).start();
            new Thread(parkingService).start();
        }
    }

    @After
    public void tearDown() throws Exception {
        for (TartanService s : services) {
            s.terminate();
        }
    }


    @Test
    public void testEntryProcedure_DisplayPayment_IfNotPaid() throws Exception {
        //connection.sendMessageToGarage();
        bufferReadLine = "SU:NG=0;XG=0;NL=R;NIR=1;XIR=0;XL=R;PO=[1=0,2=0,3=0,4=0];PL=[1=0,2=0,3=0,4=0]";
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
        }

    }

}
