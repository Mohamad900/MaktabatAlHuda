package com.maktabat.al.huda.model;

/**
 * Created by User on 7/14/2019.
 */

public class Settings {
    public String Id;
    public String PhoneNumber;
    public String Email;

    public Settings(String phoneNumber, String email) {
        PhoneNumber = phoneNumber;
        Email = email;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }
}
