package com.unicornms.unicornprinter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "printer.db";

    private static final int DATABASE_VERSION = 1;
    private  String TABLE_NAME = "USERTABLE";
    private  final String ID = "id";
    public final String USERID = "userID";
    public  final String USERNAME = "ReceiverName";
    public  final String USERPHONE = "SenderPhone";
    public  final String PAYMENTGATEWAY = "PaymentGateway";
    public  final String RECEIVERS_PHONE = "ReceiversPhone";
    public  final String ADDRESS = "ReceiversAddress";
    public  final String ISPERSONAL = "AgentorPersonal";

    public  final String PAYMENTPHONENUMBER = "AC_OR_MOB";
    public  final String PAYAMOUNT = "PaymentAmountBDT";
    public  final String PAYAMOUNTRM = "PaymentAmountRM";
    public  final String RMRATE = "RMRate";
    public  final String TRANSACTIONID = "TransactionID";
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
                + RECEIVERS_PHONE + " TEXT, "
                + ADDRESS + " TEXT, "
                + ISPERSONAL + " TEXT, "
                + PAYAMOUNTRM + " TEXT, "
                + RMRATE + " TEXT, "
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
            values.put(USERNAME,model.getRECEIVERNAME());
            values.put(USERPHONE, model.getUSERPHONE());
            values.put(PAYMENTGATEWAY, model.getPAYMENTGATEWAY());
            values.put(PAYMENTPHONENUMBER, model.getPAYMENTPHONENUMBER());
            values.put(PAYAMOUNT, model.getPAYMENNT_BDT());
            values.put(TRANSACTIONID, model.getTRANSACTIONID());
            values.put(DATE,model.getDATE());
            values.put(RECEIVERS_PHONE,model.getRECEIVERS_PHONE());
            values.put(RMRATE,model.getRM_RATE());
            values.put(PAYAMOUNTRM,model.getPAYMENT_RM());
            values.put(ISPERSONAL,model.getISPERSONAL());
            values.put(ADDRESS,model.getADDRESS());
            this.getWritableDatabase().insertOrThrow(TABLE_NAME, null, values);
    }


    public Cursor getUserDetails(String userID,String TRANSACTIONID) {
        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery("select * from "+TABLE_NAME+" where userID = ? and TransactionID = ?",new String[]{userID,TRANSACTIONID});
    }


    public Cursor getTodayHistory(){
        SQLiteDatabase db = this.getReadableDatabase();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());

        return db.rawQuery("select * from "+TABLE_NAME+" where date = ?",new String[]{currentDateandTime});
    }












}
