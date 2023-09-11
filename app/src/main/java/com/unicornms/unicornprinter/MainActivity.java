package com.unicornms.unicornprinter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button scan = findViewById(R.id.scan);
        Button print = findViewById(R.id.print);

        EditText name = findViewById(R.id.name);
        EditText phone = findViewById(R.id.phone);
        EditText amount = findViewById(R.id.amount);
        EditText PG = findViewById(R.id.gateway);
        EditText PN = findViewById(R.id.paymentPhone);
        EditText TRXID = findViewById(R.id.trxid);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
       helper = new DatabaseHelper(this);



        try {


//
//            Bitmap icon = BitmapFactory.decodeResource(getResources(),
//                    R.drawable.qrcode);
//            new ESCPOSApi("MTP-II_F432").printImage(icon);
            print.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String namestr = name.getText().toString();
                    String phonestr = phone.getText().toString();
                    String amountstr = amount.getText().toString();
                    String pg = PG.getText().toString();
                    String pn = PN.getText().toString();
                    String trx = TRXID.getText().toString();
                    if (TextUtils.isEmpty(namestr)) {
                        name.setError("Field can not be empty!");
                        name.requestFocus();
                    } else if (TextUtils.isEmpty(phonestr)) {
                        phone.setError("Field can not be empty!");
                        phone.requestFocus();
                    } else if (TextUtils.isEmpty(amountstr)) {
                        amount.setError("Field can not be empty!");
                        amount.requestFocus();
                    }else if (TextUtils.isEmpty(pg)) {
                        PG.setError("Field can not be empty!");
                        PG.requestFocus();
                    }else if (TextUtils.isEmpty(pn)) {
                        PN.setError("Field can not be empty!");
                        PN.requestFocus();
                    }else if (TextUtils.isEmpty(trx)) {
                        TRXID.setError("Field can not be empty!");
                        TRXID.requestFocus();
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        String currentDateandTime = sdf.format(new Date());

                        PaymentModel model = new PaymentModel();
                        model.setDATE(currentDateandTime);
                        model.setUSERNAME(namestr);
                        model.setPAYAMOUNT(amountstr);
                        model.setPAYMENTGATEWAY(pg);
                        model.setPAYMENTPHONENUMBER(pn);
                        model.setTRANSACTIONID(trx);
                        model.setUSERPHONE(phonestr);
                        model.setUSERID(createRandom());







                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("I", model.getUSERID());
                            jsonObject.put("T", trx);
                            jsonObject.put("A", amountstr);
                            jsonObject.put("D", currentDateandTime);
//                            String s = "name:"+namestr;
//                             s =s+ " id:"+idstr;
//                             s =s+ " amount:"+amountstr;
//                             s =s+ " time:"+currentDateandTime;

                            String p ="[C]================================\n"+
                                        "[L]Name: [R]"+namestr+"\n"+
                                        "[L]Phone: [R]"+phonestr+"\n"+
                                        "[L]Customer ID: [R]"+model.getUSERID()+"\n"+
                                        "[L]Date: [R]"+model.getDATE()+"\n"+
                                        "[C]================================\n"+
                                        "[C]<qrcode size='30'>"+jsonObject+"</qrcode>";
                            try {
                                EscPosPrinter printer = new EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 48f, 32);
                                printer.printFormattedText(p);

                                name.setText(null);
                                phone.setText(null);
                                amount.setText(null);
                                PG.setText(null);
                                PN.setText(null);
                                TRXID.setText(null);
                                helper.saveMessage(model);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });


            scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openScanner();
                }
            });




            requestPermissionMethod();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String createRandom() {
        final int min = 9999999;
        final int max = 99999999;
        final int random = new Random().nextInt((max - min) + 1) + min;
        return String.valueOf(random);
    }

    static SharedPreferences preferences;
//    public static void setSharedPreference(String key, String value) {
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putString(key, value);
//        editor.commit();
//        editor.apply();// commit is important here.
//    }
//
//    public static String getSharedPreference(String key, String defaultvalue) {
//        return preferences.getString(key, defaultvalue);
//    }


    private void requestPermissionMethod() {
        int permissionContacts = ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH);
        int permissionScan = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        int locationPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT);
        int cameraPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.BLUETOOTH_CONNECT);
        }
        if (permissionContacts != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.BLUETOOTH);
        }
        if (permissionScan != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
        }
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 0);
        }
    }
    // this will find a bluetooth printer device

    private CodeScanner mCodeScanner;
    AlertDialog qrcodedialog;

    private void openScanner() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View vi = getLayoutInflater().inflate(R.layout.qr_code_scanner, null, false);
        CodeScannerView scannerView = vi.findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        qrcodedialog.dismiss();
                        showInvoice(result.getText());
                    }
                });
            }


        });


        builder.setView(vi);
        qrcodedialog = builder.create();
        qrcodedialog.show();


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        width = (width * 9) / 10;
        qrcodedialog.getWindow().setLayout(width, width); //Controlling width and height.


        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mCodeScanner.startPreview();
            }
        }, 500);


    }

    void showScanResultDialog(JSONObject jsonObject){
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View vi = getLayoutInflater().inflate(R.layout.result,null,false);
        TextView name = vi.findViewById(R.id.name);
        TextView id = vi.findViewById(R.id.id);
        TextView amount = vi.findViewById(R.id.amount);
        TextView date = vi.findViewById(R.id.date);

        try {
            String userID = jsonObject.getString("I");
            String trxID = jsonObject.getString("T");

            Cursor c = helper.getUserDetails(userID,trxID);

            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                String datestr = c.getString(c.getColumnIndexOrThrow(helper.DATE));
                String namestr = c.getString(c.getColumnIndexOrThrow(helper.USERNAME));
                String idstr = c.getString(c.getColumnIndexOrThrow(helper.USERID));
                String phonestr = c.getString(c.getColumnIndexOrThrow(helper.USERPHONE));
                String pgstr = c.getString(c.getColumnIndexOrThrow(helper.PAYMENTGATEWAY));
                String amountstr = c.getString(c.getColumnIndexOrThrow(helper.PAYAMOUNT));
                String payphnstr = c.getString(c.getColumnIndexOrThrow(helper.PAYMENTPHONENUMBER));
                String trxid = c.getString(c.getColumnIndexOrThrow(helper.TRANSACTIONID));
                Log.e("username",namestr);
                Log.e("userid",idstr);
                Log.e("userphone",phonestr);
                Log.e("pg",pgstr);
                Log.e("pn",payphnstr);
                Log.e("trxID",trxid);
                Log.e("date",datestr);
                Log.e("amount",amountstr);
            }


            //get data from db


//            name.setText("Name: "+jsonObject.getString("name"));
//            id.setText("Phone: "+jsonObject.getString("id"));
//            amount.setText("Amount: "+jsonObject.getString("amount"));
//            date.setText("Date: "+jsonObject.getString("date"));
        }catch (Exception e){
            e.printStackTrace();
        }
        builder.setView(vi);
        dialog = builder.create();
        dialog.show();
    }
    private void showInvoice(String text) {
        try {
            JSONObject jsonObject = new JSONObject(text);
            showScanResultDialog(jsonObject);
        }catch (Exception e){
            Toast.makeText(this, "Invalid Data", Toast.LENGTH_SHORT).show();
        }
        Log.e("text",text);

    }

}