package com.act.snapjob_worker.Global;

public class Client {

    public String fullName, email, phoneNumber, image;

    public Client(){

    }

    public Client(String fullName, String email, String phoneNumber){
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public Client(String image){
        this.image = image;
    }

    public String getFullName() {
        return fullName;
    }

    public String setFullName(String fullName) {
        this.fullName = fullName;
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String setEmail(String email) {
        this.email = email;
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return phoneNumber;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
