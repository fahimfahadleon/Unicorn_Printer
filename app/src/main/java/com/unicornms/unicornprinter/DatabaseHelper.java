package com.unicornms.unicornprinter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "printer";

    private static final int DATABASE_VERSION = 1;
    private  String TABLE_NAME = "USERTABLE";
    private  final String ID = "id";
    public final String USERID = "userID";
    public  final String USERNAME = "userName";
    public  final String USERPHONE = "userPhone";
    public  final String PAYMENTGATEWAY = "PG";

    public  final String PAYMENTPHONENUMBER = "PN";
    public  final String PAYAMOUNT = "PA";
    public  final String TRANSACTIONID = "TID";
    public  final String DATE = "date";





    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        CreateTable();
    }


    public void CreateTable(){
        String query2 = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USERID + " TEXT, "
                + USERNAME + " TEXT, "
                + USERPHONE + " TEXT, "
                + PAYMENTGATEWAY + " TEXT, "
                + PAYMENTPHONENUMBER + " TEXT, "
                + PAYAMOUNT + " TEXT, "
                + TRANSACTIONID + " TEXT UNIQUE, "
                + DATE + " TEXT)";
        this.getWritableDatabase().execSQL(query2);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


    public void saveMessage(PaymentModel model){

            ContentValues values = new ContentValues();
            values.put(USERID, model.getUSERID());
            values.put(USERNAME,model.getUSERNAME());
            values.put(USERPHONE, model.getUSERPHONE());
            values.put(PAYMENTGATEWAY, model.getPAYMENTGATEWAY());
            values.put(PAYAMOUNT, model.getPAYAMOUNT());
            values.put(TRANSACTIONID, model.getTRANSACTIONID());
            values.put(DATE,model.getDATE());
            this.getWritableDatabase().insertOrThrow(TABLE_NAME, null, values);


    }


    public Cursor getUserDetails(String userID,String TRANSACTIONID) {
        return this.getWritableDatabase().rawQuery( "select * from "+TABLE_NAME+" where userID = "+userID+" and TID = "+TRANSACTIONID+"", null );
    }














}
