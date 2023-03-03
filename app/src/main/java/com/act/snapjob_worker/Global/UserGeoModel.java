package com.act.snapjob_worker.Global;

import com.firebase.geofire.GeoLocation;

public class UserGeoModel {
    private String key;
    private GeoLocation geoLocation;
    private UserInfoModel userInfoModel;

    public UserGeoModel() {
    }

    public UserGeoModel(String key, GeoLocation geoLocation) {
        this.key = key;
        this.geoLocation = geoLocation;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public UserInfoModel getUserInfoModel() {
        return userInfoModel;
    }

    public void setUserInfoModel(UserInfoModel userInfoModel) {
        this.userInfoModel = userInfoModel;
    }
}
