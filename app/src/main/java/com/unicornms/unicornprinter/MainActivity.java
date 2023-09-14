package com.unicornms.unicornprinter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper helper;
    EditText name;
    EditText phone;
    EditText amount;
    EditText PG;
    EditText PN;
    EditText TRXID;
    EditText rateEDT;
    EditText RMEDT;
    Spinner spinner;

    TextView bank;
    TextView others;
    boolean isBanking = true;
    JSONArray mobileBankinArray;
    JSONArray bankingArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Button scan = findViewById(R.id.scan);
        Button print = findViewById(R.id.print);

        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        amount = findViewById(R.id.amount);
        PG = findViewById(R.id.gateway);
        PN = findViewById(R.id.paymentPhone);
        TRXID = findViewById(R.id.trxid);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        helper = new DatabaseHelper(this);
        spinner = findViewById(R.id.spinner);
        rateEDT = findViewById(R.id.rateEDT);
        bank = findViewById(R.id.bank);
        others = findViewById(R.id.others);
        RMEDT = findViewById(R.id.converted);
        List<String> initialmobileBanking = Arrays.asList("Bkash", "Nagad", "FAX", "Others");
        List<String> initialbanks = Arrays.asList("Sonali Bank","Agrani Bank","Pubali Bank","Krishi Bank","IBBL","DBBL","Prime Bank","Others");


        List<String>finalmobileBanking = new ArrayList<>();
        List<String>finalBanking = new ArrayList<>();

        try {
             mobileBankinArray = new JSONArray(getSharedPreference("mb","{}"));
             bankingArray = new JSONArray(getSharedPreference("b","{}"));

            if(mobileBankinArray.length() == 0){
                for(String s:initialmobileBanking){
                    mobileBankinArray.put(s);
                }
                setSharedPreference("mb",mobileBankinArray.toString());
            }else {
                for(int i=0;i<mobileBankinArray.length();i++){
                    finalmobileBanking.add(mobileBankinArray.getString(i));
                }
            }
            if(bankingArray.length() == 0){
                for(String s: initialbanks){
                    bankingArray.put(s);
                }
                setSharedPreference("b",bankingArray.toString());
            }else {
                for(int i=0;i<bankingArray.length();i++){
                    finalBanking.add(bankingArray.getString(i));
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }


        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });



        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, finalBanking);
        spinner.setAdapter(mArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String s = finalBanking.get(position);
                PG.setText(s);
                PG.setEnabled(s.equals("Others"));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                PG.setText(null);
            }

        });


        bank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bank.setBackgroundColor(Color.parseColor("#58FF58"));
                others.setBackgroundColor(Color.parseColor("#ffffff"));


                // Create an adapter as shown below
                ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, finalBanking);
                spinner.setAdapter(mArrayAdapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        String s = finalBanking.get(position);
                        PG.setText(s);
                        PG.setEnabled(s.equals("Others"));

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        PG.setText(null);
                    }

                });
            }
        });
        others.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bank.setBackgroundColor(Color.parseColor("#ffffff"));
                others.setBackgroundColor(Color.parseColor("#58FF58"));
                // Create an adapter as shown below
                ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, finalmobileBanking);
                spinner.setAdapter(mArrayAdapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        String s = finalmobileBanking.get(position);
                        PG.setText(s);
                        PG.setEnabled(s.equals("Others"));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        PG.setText(null);
                    }

                });
            }
        });


        if (getSharedPreference("rate", "asdf").equals("asdf")) {
            rateEDT.setError(null);
        } else {
            rateEDT.setText(getSharedPreference("rate", "asdf"));
        }


        boolean b = Boolean.parseBoolean(getSharedPreference("isFirst", "false"));
        if (!b) {
            setSharedPreference("isFirst", "true");
            showInstruction();
        }







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

                    String rateStr = rateEDT.getText().toString();

                    if (TextUtils.isEmpty(namestr)) {
                        name.setError("Field can not be empty!");
                        name.requestFocus();
                    } else if (TextUtils.isEmpty(phonestr)) {
                        phone.setError("Field can not be empty!");
                        phone.requestFocus();
                    } else if (TextUtils.isEmpty(amountstr)) {
                        amount.setError("Field can not be empty!");
                        amount.requestFocus();
                    } else if (TextUtils.isEmpty(pg)) {
                        PG.setError("Field can not be empty!");
                        PG.requestFocus();
                    } else if (TextUtils.isEmpty(pn)) {
                        PN.setError("Field can not be empty!");
                        PN.requestFocus();
                    } else if (TextUtils.isEmpty(trx)) {
                        TRXID.setError("Field can not be empty!");
                        TRXID.requestFocus();
                    } else if (TextUtils.isEmpty(rateStr)) {
                        rateEDT.setError("Field Can Not Be Empty!");
                        rateEDT.requestFocus();
                    } else {

                        setSharedPreference("rate", rateStr);

                        if(isBanking){
                            if(!finalBanking.contains(pg)){
                                bankingArray.put(pg);
                                setSharedPreference("b",bankingArray.toString());
                            }
                        }else {
                            if(!finalmobileBanking.contains(pg)){
                                mobileBankinArray.put(pg);
                                setSharedPreference("mb",mobileBankinArray.toString());
                            }
                        }

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


                            String p = "[C]================================\n" +
                                    "[L]Name: [R]" + namestr + "\n" +
                                    "[L]Phone: [R]" + phonestr + "\n" +
                                    "[L]Customer ID: [R]" + model.getUSERID() + "\n" +
                                    "[L]Amount: [R]" + model.getPAYAMOUNT() + "\n" +
                                    "[L]Payment GW: [R]" + model.getPAYMENTGATEWAY() + "\n" +
                                    "[L]Payment NO: [R]" + model.getPAYMENTPHONENUMBER() + "\n" +
                                    "[L]Date: [R]" + model.getDATE() + "\n" +
                                    "[C]================================\n" +
                                    "\n" +
                                    "[C]<qrcode size='30'>" + jsonObject + "</qrcode>";
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
                            } catch (Exception e) {
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

    AlertDialog alertDialog;

    private void showInstruction() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Instruction");
        builder.setMessage("Before using this app or print anything enable bluetooth and connect to your bluetooth printer.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    public static String createRandom() {
        final int min = 9999999;
        final int max = 99999999;
        final int random = new Random().nextInt((max - min) + 1) + min;
        return String.valueOf(random);
    }

    static SharedPreferences preferences;

    public static void setSharedPreference(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
        editor.apply();// commit is important here.
    }

    public static String getSharedPreference(String key, String defaultvalue) {
        return preferences.getString(key, defaultvalue);
    }


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

    AlertDialog dialog;

    void showScanResultDialog(JSONObject jsonObject) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View vi = getLayoutInflater().inflate(R.layout.result, null, false);
        TextView names = vi.findViewById(R.id.name);
        TextView ids = vi.findViewById(R.id.id);
        TextView amounts = vi.findViewById(R.id.amount);
        TextView dates = vi.findViewById(R.id.date);
        TextView paymentGateways = vi.findViewById(R.id.paymentgateway);
        TextView paymentNumbers = vi.findViewById(R.id.paymentNumber);
        TextView phoneNumbers = vi.findViewById(R.id.phone);
        TextView trxids = vi.findViewById(R.id.transactionID);
        Button newScan = vi.findViewById(R.id.newS);
        newScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.e("clicked", "new Scan");
                    name.setText(names.getText().toString());
                    phone.setText(phoneNumbers.getText().toString());
                    // amount.setText(amounts.getText().toString());
                    PG.setText(paymentGateways.getText().toString());
                    PN.setText(phoneNumbers.getText().toString());
                    // TRXID.setText(trxids.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                dialog.dismiss();
            }
        });

        try {
            String userID = jsonObject.getString("I");
            String trxID = jsonObject.getString("T");
            Cursor c = helper.getUserDetails(userID, trxID);
            Log.e("userID", userID);
            Log.e("trxid", trxID);

            if (c == null) {
                Toast.makeText(this, "Data Not Found", Toast.LENGTH_SHORT).show();
                return;
            }
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                String datestr = c.getString(c.getColumnIndexOrThrow(helper.DATE));
                String namestr = c.getString(c.getColumnIndexOrThrow(helper.USERNAME));
                String idstr = c.getString(c.getColumnIndexOrThrow(helper.USERID));
                String phonestr = c.getString(c.getColumnIndexOrThrow(helper.USERPHONE));
                String pgstr = c.getString(c.getColumnIndexOrThrow(helper.PAYMENTGATEWAY));
                String amountstr = c.getString(c.getColumnIndexOrThrow(helper.PAYAMOUNT));
                String payphnstr = c.getString(c.getColumnIndexOrThrow(helper.PAYMENTPHONENUMBER));
                String trxid = c.getString(c.getColumnIndexOrThrow(helper.TRANSACTIONID));

                Log.e("Name", namestr);
                Log.e("id", idstr);
                Log.e("phone", phonestr);
                Log.e("pg", pgstr);
                Log.e("pn", payphnstr);
                Log.e("amount", amountstr);
                Log.e("trxid", trxid);
                Log.e("date", datestr);

                names.setText(namestr);
                ids.setText(idstr);
                amounts.setText(amountstr);
                dates.setText(datestr);
                paymentGateways.setText(pgstr);
                paymentNumbers.setText(payphnstr);
                phoneNumbers.setText(phonestr);
                trxids.setText(trxid);
            }

            c.close();


        } catch (Exception e) {
            Log.e("error", e.toString());
        }


        builder.setView(vi);
        dialog = builder.create();
        dialog.show();
    }

    private void showInvoice(String text) {
        try {
            JSONObject jsonObject = new JSONObject(text);
            showScanResultDialog(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid Data", Toast.LENGTH_SHORT).show();
        }
        Log.e("text", text);

    }

}