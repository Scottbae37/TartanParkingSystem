package edu.cmu.tartan.service;


import edu.cmu.tartan.MapUtil;
import edu.cmu.tartan.edu.cmu.tartan.reservation.Reservation;
import edu.cmu.tartan.edu.cmu.tartan.reservation.ReservationStore;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Created by kyungman.yu on 2017-07-19.
 */
public class AdminService extends TartanService {

    public final static String ADMIN_SERVICE = "AdminService";
    private Preferences prefs;
    private Vector<Reservation> reservations = new Vector<>();
    private ReservationStore rsvpStore;
    private String configPath = null;

    public AdminService() {
        super.init(ADMIN_SERVICE);
        prefs = Preferences.userNodeForPackage(this.getClass());
        prefs.put("id", "admin");
        prefs.put("pwd", "BZoAGSWS1URLwMqtcgP5i1BjMuLPers11oTqm/fBjwg=");
        rsvpStore = new ReservationStore(configPath);
    }

    @Override
    public void handleMessage(HashMap<String, Object> message) {
        final String command = (String) message.get(TartanParams.COMMAND);
        switch (command) {
            case TartanParams.MSG_AUTHENTICATE_ADMIN:
                handleAuthenticate(message);
                break;
            case TartanParams.MSG_GET_STATISTICAL_DATA:
                handleGetStatisticalData(message);
                break;
            default:
                break;
        }
    }

    public void handleAuthenticate(HashMap<String, Object> message) {
        HashMap<String, Object> resultMessage = new HashMap<>();
        ArrayList authlist = (ArrayList) message.get(TartanParams.PAYLOAD);
        boolean isValid = authenticate((String) authlist.get(0), (String) authlist.get(1));
        resultMessage.put(TartanParams.COMMAND, TartanParams.MSG_AUTHENTICATION_RESULT);
        resultMessage.put(TartanParams.PAYLOAD, isValid);
        sendMessage(KioskService.KIOSK_SERVICE, resultMessage);
    }

    public void handleGetStatisticalData(HashMap<String, Object> message) {
        try {
            rsvpStore.loadCumulativeReservations();;
        } catch (Exception e) {
            e.printStackTrace();
        }
        reservations = rsvpStore.getReservations();

        HashMap<String, Object> resultMessage = new HashMap<>();
        Long revenue = getRevenue(reservations);
        HashMap<String, Integer> averageOccupancy = getAverageOccupancy(reservations);
        ArrayList<Integer> peakUsageHours = getPeakUsageHours(reservations);
        resultMessage.put(TartanParams.COMMAND, TartanParams.MSG_STATISTICAL_DATA_RESULT);
        resultMessage.put(TartanParams.REVENUE, revenue);
        resultMessage.put(TartanParams.AVERAGE_OCCUPANCY, averageOccupancy);
        resultMessage.put(TartanParams.PEAK_USAGE_HOURS, peakUsageHours);
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


    public Long getRevenue(Vector<Reservation> reservations) {
        Long revenue = 0L;
        for (Reservation r : reservations) {
            revenue += r.getPayment().getFee();
        }
        return revenue;
    }

    class DateUtil {
        Calendar calendar;
        DateUtil() {
            calendar = GregorianCalendar.getInstance();
        }

        void setDate(Date date) {
            calendar.setTime(date);
        }

        int getHourFromDate() {
            return calendar.get(Calendar.HOUR_OF_DAY);
        }

        String getDate() {
            int year = calendar.get(Calendar.YEAR);;
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String date = String.valueOf(year) + ":";
            if (month < 10) {
                date += "0";
            }
            date +=  String.valueOf(month) + ":" + String.valueOf(day);
            return date;
        }
    }

    public HashMap<String, Integer> getAverageOccupancy(Vector<Reservation> reservations) {
        HashMap<String, Integer> occupancyMap = new HashMap<>();
        int startHour, endHour, dailyCount;
        DateUtil dateUtil = new DateUtil();
        for (Reservation reservation : reservations) {
            dateUtil.setDate(reservation.getStartTime());
            startHour = dateUtil.getHourFromDate();
            dateUtil.setDate(reservation.getEndTime());
            endHour = dateUtil.getHourFromDate();
            dailyCount = endHour - startHour + 1;
            String hashKey = dateUtil.getDate();
            System.out.println("hashKey=" + hashKey);
            if (occupancyMap.get(hashKey) != null) {
                dailyCount += occupancyMap.get(hashKey);
            }
            occupancyMap.put(hashKey, dailyCount);
        }

        for (String key : occupancyMap.keySet()) {
            int count = occupancyMap.get(key);
            occupancyMap.put(key, count * 100 / (24 * 4));
        }
        return occupancyMap;
    }

    public ArrayList<Integer> getPeakUsageHours(Vector<Reservation> reservations) {
        Map<Integer, Integer> usageHours = new HashMap<>();
        int startHour, endHour;
        DateUtil dateUtil = new DateUtil();
        for (Reservation reservation : reservations) {
            dateUtil.setDate(reservation.getStartTime());
            startHour = dateUtil.getHourFromDate();
            dateUtil.setDate(reservation.getEndTime());
            endHour = dateUtil.getHourFromDate();
            for (int i = startHour; i <= endHour; i++) {
                Integer value = usageHours.get(i);
                Integer updatedValue = 1;
                if (value != null) {
                    updatedValue += value;
                }
                usageHours.put(i, updatedValue);
            }
        }
        usageHours = MapUtil.sortByValue(usageHours);
        System.out.println("usageHours="+usageHours);
        Integer peakHour = 0;
        ArrayList<Integer> usageHoursList = new ArrayList<>();
        for (Integer hour : usageHours.keySet()) {
            if (usageHours.get(hour) >= peakHour) {
                peakHour = usageHours.get(hour);
                usageHoursList.add(hour);
            }
        }

        System.out.println("usageHoursList="+usageHoursList);
        return usageHoursList;
    }

    public Vector<Reservation> getReservations() {
        return reservations;
    }

}
