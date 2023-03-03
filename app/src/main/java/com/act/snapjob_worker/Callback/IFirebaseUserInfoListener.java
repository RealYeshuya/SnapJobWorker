package com.act.snapjob_worker.Callback;

import com.act.snapjob_worker.Global.UserGeoModel;

public interface IFirebaseUserInfoListener {
    void onUserInfoLoadSuccess(UserGeoModel userGeoModel);
}
