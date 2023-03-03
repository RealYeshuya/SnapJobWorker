package com.act.snapjob_worker.Global;

public class Transactions {

    public String userId, userName, address, transactionStatus, transId, workerName, transactionDescription, userPhoneNumber, workerId, transactionDate;

    public Transactions(){

    }

    public Transactions(String userId, String userName, String address, String transactionStatus, String transId, String workerName, String transactionDescription, String userPhoneNumber, String workerId, String transactionDate) {
        this.userName = userName;
        this.address = address;
        this.userId = userId;
        this.transactionStatus = transactionStatus;
        this.transId = transId;
        this.workerName = workerName;
        this.transactionDescription = transactionDescription;
        this.userPhoneNumber = userPhoneNumber;
        this.workerId = workerId;
        this.transactionDate = transactionDate;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getUserName() {
        return userName;
    }

    public String getAddress() {
        return address;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }
}


