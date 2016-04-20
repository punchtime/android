package io.punchtime.punchtime.data;

/**
 * Created by elias on 12/04/16.
 */

public class Pulse {
    double latitude;
    double longitude;
    String note;
    long checkin;
    long checkout;
    String type;
    String employee;
    String employer;

    public Pulse() {}

    public Pulse(double latitude, double longitude, String note, long checkin, String employee, String employer) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.note = note;
        this.checkin = checkin;
        this.employee = employee;
        this.employer = employer;
    }



    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }

    public String getNote() {
        return note;
    }

    public long getCheckin() {
        return checkin;
    }
    public long getCheckout() {
        return checkout;
    }
    public void setCheckout(long checkout) {
        this.checkout = checkout;
    }

    public String getEmployee() {
        return employee;
    }
    public String getEmployer() {
        return employer;
    }
}
