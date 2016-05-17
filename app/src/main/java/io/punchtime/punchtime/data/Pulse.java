package io.punchtime.punchtime.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by elias on 12/04/16.
 */

public class Pulse implements Parcelable {
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

    public void setNote(String note) {
        this.note = note;
    }

    // Parcelling part
    public Pulse(Parcel in){
        addressStreet = in.readString();
        addressCityCountry= in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        note = in.readString();
        checkin = in.readLong();
        checkout = in.readLong();
        employee = in.readString();
        employer = in.readString();
        confirmed = false;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(addressStreet);
        out.writeString(addressCityCountry);
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeString(note);
        out.writeLong(checkin);
        out.writeLong(checkout);
        out.writeString(employee);
        out.writeString(employer);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Pulse createFromParcel(Parcel in) {
            return new Pulse(in);
        }

        public Pulse[] newArray(int size) {
            return new Pulse[size];
        }
    };
}
