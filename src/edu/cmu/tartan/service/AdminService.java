package edu.cmu.tartan.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;

/**
 * Created by kyungman.yu on 2017-07-19.
 */
public class AdminService extends TartanService {
    @Override
    public void handleMessage(HashMap<String, Object> message) {

    }

    @Override
    public void terminate() {

    }

    @Override
    public void run() {


    }

    private void handleCreateNewReservation(HashMap<String, Object> request) {



    }

    public boolean authenticate(String id, String pwd) {
        String inputPwdEncoded = hashPassword(pwd);
        String adminAuth[] = getAdminAuth();
        return id.equals(adminAuth[0]) && inputPwdEncoded.equals(adminAuth[1]);
    }

    public String[] getAdminAuth() {
        String adminAuth[] = { "admin", "BZoAGSWS1URLwMqtcgP5i1BjMuLPers11oTqm/fBjwg=" };
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
}
