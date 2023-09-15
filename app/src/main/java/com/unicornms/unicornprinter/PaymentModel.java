package com.unicornms.unicornprinter;

public class PaymentModel {

    private   String USERID = "";
    private   String RECEIVERNAME = "";
    private   String USERPHONE = "";
    private   String PAYMENTGATEWAY = "";
    private   String RECEIVERS_PHONE = "";
    private   String ADDRESS = "";
    private   String ISPERSONAL = "";
    private   String PAYMENTPHONENUMBER = "";

    private   String TRANSACTIONID = "";
    private   String PAYMENT_RM = "";
    private   String PAYMENNT_BDT = "";
    private   String RM_RATE = "";
    private   String DATE = "";

    public String getADDRESS() {
        return ADDRESS;
    }

    public void setADDRESS(String ADDRESS) {
        this.ADDRESS = ADDRESS;
    }

    public String getISPERSONAL() {
        return ISPERSONAL;
    }

    public String getRECEIVERNAME() {
        return RECEIVERNAME;
    }

    public void setRECEIVERNAME(String RECEIVERNAME) {
        this.RECEIVERNAME = RECEIVERNAME;
    }

    public void setISPERSONAL(String ISPERSONAL) {
        this.ISPERSONAL = ISPERSONAL;
    }
    public String getRECEIVERS_PHONE() {
        return RECEIVERS_PHONE;
    }

    public void setRECEIVERS_PHONE(String RECEIVERS_PHONE) {
        this.RECEIVERS_PHONE = RECEIVERS_PHONE;
    }

    public String getPAYMENT_RM() {
        return PAYMENT_RM;
    }

    public void setPAYMENT_RM(String PAYMENT_RM) {
        this.PAYMENT_RM = PAYMENT_RM;
    }

    public String getPAYMENNT_BDT() {
        return PAYMENNT_BDT;
    }

    public void setPAYMENNT_BDT(String PAYMENNT_BDT) {
        this.PAYMENNT_BDT = PAYMENNT_BDT;
    }

    public String getRM_RATE() {
        return RM_RATE;
    }

    public void setRM_RATE(String RM_RATE) {
        this.RM_RATE = RM_RATE;
    }

    public String getUSERID() {
        return USERID;
    }

    public void setUSERID(String USERID) {
        this.USERID = USERID;
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
