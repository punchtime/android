package io.punchtime.punchtime.data;

/**
 * Created by elias on 12/04/16.
 */

public class Pulse {
    private String addressStreet;
    private String addressCityCountry;
    private double latitude;
    private double longitude;
    private String note;
    private long checkin;
    private long checkout;
    private String employee;
    private String employer;
    private boolean confirmed;

    public Pulse() {}

    public Pulse(double latitude, double longitude, String note, long checkin, long checkout, String employee, String employer, boolean confirmed, String addressStreet, String addressCityCountry) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.note = note;
        this.checkin = checkin;
        this.checkout = checkout;
        this.employee = employee;
        this.employer = employer;
        this.confirmed = confirmed;
        this.addressStreet = addressStreet;
        this.addressCityCountry = addressCityCountry;
    }

    public Pulse(double latitude, double longitude, String note, long checkin, String employee, String employer, boolean confirmed) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.note = note;
        this.checkin = checkin;
        this.checkout = 0;
        this.employee = employee;
        this.employer = employer;
        this.confirmed = confirmed;
    }

    public Pulse(double latitude, double longitude, String note, long checkin, String employee, String employer, boolean confirmed, String addressStreet, String addressCityCountry) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.note = note;
        this.checkin = checkin;
        this.checkout = 0;
        this.employee = employee;
        this.employer = employer;
        this.confirmed = confirmed;
        this.addressStreet = addressStreet;
        this.addressCityCountry = addressCityCountry;
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
    public String getEmployee() {
        return employee;
    }
    public String getEmployer() {
        return employer;
    }
    public long getCheckin() {
        return checkin;
    }
    public long getCheckout() {
        return checkout;
    }
    public boolean isConfirmed() {
        return confirmed;
    }
    public String getAddressStreet() {
        return addressStreet;
    }
    public String getAddressCityCountry() {
        return addressCityCountry;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public void setAddressCityCountry(String addressCityCountry) {
        this.addressCityCountry = addressCityCountry;
    }
}
