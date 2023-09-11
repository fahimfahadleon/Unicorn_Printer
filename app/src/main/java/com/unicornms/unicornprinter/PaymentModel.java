package com.unicornms.unicornprinter;

public class PaymentModel {

    private   String USERID = "userID";
    private   String USERNAME = "userName";
    private   String USERPHONE = "userPhone";
    private   String PAYMENTGATEWAY = "PG";

    private   String PAYMENTPHONENUMBER = "PN";
    private   String PAYAMOUNT = "PA";
    private   String TRANSACTIONID = "TID";
    private   String DATE = "date";

    public String getUSERID() {
        return USERID;
    }

    public void setUSERID(String USERID) {
        this.USERID = USERID;
    }

    public String getUSERNAME() {
        return USERNAME;
    }

    public void setUSERNAME(String USERNAME) {
        this.USERNAME = USERNAME;
    }

    public String getUSERPHONE() {
        return USERPHONE;
    }

    public void setUSERPHONE(String USERPHONE) {
        this.USERPHONE = USERPHONE;
    }

    public String getPAYMENTGATEWAY() {
        return PAYMENTGATEWAY;
    }

    public void setPAYMENTGATEWAY(String PAYMENTGATEWAY) {
        this.PAYMENTGATEWAY = PAYMENTGATEWAY;
    }

    public String getPAYMENTPHONENUMBER() {
        return PAYMENTPHONENUMBER;
    }

    public void setPAYMENTPHONENUMBER(String PAYMENTPHONENUMBER) {
        this.PAYMENTPHONENUMBER = PAYMENTPHONENUMBER;
    }

    public String getPAYAMOUNT() {
        return PAYAMOUNT;
    }

    public void setPAYAMOUNT(String PAYAMOUNT) {
        this.PAYAMOUNT = PAYAMOUNT;
    }

    public String getTRANSACTIONID() {
        return TRANSACTIONID;
    }

    public void setTRANSACTIONID(String TRANSACTIONID) {
        this.TRANSACTIONID = TRANSACTIONID;
    }

    public String getDATE() {
        return DATE;
    }

    public void setDATE(String DATE) {
        this.DATE = DATE;
    }
}
