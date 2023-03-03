package com.act.snapjob_worker.Global;

public class User {

    public String fullName, age, job, email,phoneNum, picture;

    public User(String name, String job, String number, String email){

    }

    public User(String fullName, String age, String job, String phoneNum, String email/*,String pPic*/){
        this.fullName = fullName;
        this.age = age;
        this.job = job;
        this.email = email;
        this.phoneNum = phoneNum;
        //this.picture = pPic;
    }
}
