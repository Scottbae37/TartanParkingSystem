package edu.cmu.tartan.service;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.prefs.Preferences;

/**
 * Created by kyungman.yu on 2017-07-19.
 */
public class AdminService extends TartanService {

    public final static String ADMIN_SERVICE = "AdminService";
    private Preferences prefs;

    public AdminService() {
        super.init(ADMIN_SERVICE);
        prefs = Preferences.userNodeForPackage(this.getClass());
        prefs.put("id", "admin");
        prefs.put("pwd", "BZoAGSWS1URLwMqtcgP5i1BjMuLPers11oTqm/fBjwg=");
    }

    @Override
    public void handleMessage(HashMap<String, Object> message) {
        final String command = (String) message.get(TartanParams.COMMAND);
        HashMap<String, Object> resultMessage = new HashMap<>();
        switch (command) {
            case TartanParams.MSG_AUTHENTICATE_ADMIN:
                ArrayList authlist = (ArrayList) message.get(TartanParams.PAYLOAD);
                boolean isValid = authenticate((String) authlist.get(0), (String) authlist.get(1));
                resultMessage.put(TartanParams.COMMAND, TartanParams.MSG_AUTHENTICATION_RESULT);
                resultMessage.put(TartanParams.PAYLOAD, isValid);
                break;
            case TartanParams.MSG_GET_STATISTICAL_DATA:
                String revenue = getRevenue();
                String averageOccupancy = getAverageOccupancy();
                String peakUsageHours = getPeakUsageHours();
                resultMessage.put(TartanParams.COMMAND, TartanParams.MSG_STATISTICAL_DATA_RESULT);
        }
        sendMessage(KioskService.KIOSK_SERVICE, resultMessage);
    }

    @Override
    public void terminate() {

    }

    @Override
    public void run() {


    }


    public boolean authenticate(String id, String pwd) {
        String inputPwdEncoded = hashPassword(pwd);
        String adminAuth[] = getAdminAuth();
        return id.equals(adminAuth[0]) && inputPwdEncoded.equals(adminAuth[1]);
    }

    public String[] getAdminAuth() {
        String id = prefs.get("id", null);
        String pwd = prefs.get("pwd", null);
        if (id == null || pwd == null) {
            return null;
        }
        String adminAuth[] = {id, pwd};
        return adminAuth;
    }

    public String hashPassword(String pwd) {
        String encoded = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pwd.getBytes(StandardCharsets.UTF_8));
            encoded = Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return encoded;
    }


    public String getRevenue() {
        return "1000";
    }

    public String getAverageOccupancy() {
        return "45";
    }

    public String getPeakUsageHours() {
        return "14:00";
    }
}
