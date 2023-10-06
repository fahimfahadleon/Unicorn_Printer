package com.unicornms.unicornprinter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.anggrayudi.storage.SimpleStorage;
import com.anggrayudi.storage.SimpleStorageHelper;
import com.anggrayudi.storage.file.DocumentFileCompat;
import com.anggrayudi.storage.file.FileFullPath;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.google.zxing.Result;
import com.liyu.sqlitetoexcel.SQLiteToExcel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper helper;
    EditText name;
    EditText phone;
    EditText amount;
    EditText PG;
    EditText PN;

    EditText rateEDT;
    EditText RMEDT;
    Spinner spinner;
    EditText address;
    EditText receiverphone;
    LinearLayout addressLayout;

    TextView bank;
    TextView agentText;
    TextView personalText;
    TextView others;
    TextView fax;
    TextView addresstext;
    private static final int BANKING_TYPE_BANK = 0;
    private static final int BANKING_TYPE_MOBILE_BANK = 1;
    private static final int BANKING_TYPE_FAX = 2;
    int isBanking = 0;
    JSONArray mobileBankinArray;
    JSONArray bankingArray;
    TextWatcher amountTextWatcher;
    TextWatcher RMTextWatcher;

    ArrayAdapter<String> bankingAdapter;
    ArrayAdapter<String> mobileBankingAdapter;

    LinearLayout chooser;
    boolean isPersonal = false;

    Button checkHistory;
    Button export;


    List<String> finalmobileBanking;
    List<String> finalBanking;

    SimpleStorageHelper storageHelper = new SimpleStorageHelper(this);

    void export(String query,String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri));
        } else {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (!dir.exists()) {
                boolean b = dir.mkdirs();
                Log.e("valueofB", String.valueOf(b));
            }


            new SQLiteToExcel
                    .Builder(MainActivity.this)
                    .setDataBase(getDatabasePath(DatabaseHelper.DATABASE_NAME).getAbsolutePath())
//                            .setSQL("select * from USERTABLE")
                    .setSQL(query)
                    .setOutputPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath())
                    //Optional, if null, default output path is app ExternalFilesDir.
                    .setOutputFileName(name)
                    .start(new SQLiteToExcel.ExportListener() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onCompleted(String filePath) {
                            Toast.makeText(MainActivity.this, "Exported to Documents (Name: "+name+")!", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(MainActivity.this, "Something went wrong while exporting!", Toast.LENGTH_SHORT).show();

                        }
                    });
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            storageHelper.onSaveInstanceState(savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Button scan = findViewById(R.id.scan);
        Button print = findViewById(R.id.print);

        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        amount = findViewById(R.id.converted);
        PG = findViewById(R.id.gateway);
        PN = findViewById(R.id.paymentPhone);
        checkHistory = findViewById(R.id.checkHistory);
        fax = findViewById(R.id.fax);
        addresstext = findViewById(R.id.addresstext);


        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        helper = new DatabaseHelper(this);
        spinner = findViewById(R.id.spinner);
        rateEDT = findViewById(R.id.rateEDT);
        bank = findViewById(R.id.bank);
        others = findViewById(R.id.others);
        RMEDT = findViewById(R.id.amount);
        addressLayout = findViewById(R.id.addressLayout);
        address = findViewById(R.id.addrss);
        chooser = findViewById(R.id.chooser);
        agentText = findViewById(R.id.agentText);
        personalText = findViewById(R.id.personalText);
        receiverphone = findViewById(R.id.receiverphone);
        export = findViewById(R.id.export);




        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog1;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View vi = getLayoutInflater().inflate(R.layout.date_chooser, null, false);
//                EditText startDate = vi.findViewById(R.id.startDate);
//                EditText endDate = vi.findViewById(R.id.endDate);
                Button export = vi.findViewById(R.id.export);
                Button exportall = vi.findViewById(R.id.exportall);
                export.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        String s = startDate.getText().toString();
//                        String e = endDate.getText().toString();
//                        if (TextUtils.isEmpty(s)) {
//                            startDate.setError("Field Can Not Be Empty!");
//                            startDate.requestFocus();
//                        } else if (TextUtils.isEmpty(e)) {
//                            endDate.setError("Field Can Not Be Empty!");
//                            endDate.requestFocus();
//                        } else {
//
//
//                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String currentDateandTime = sdf.format(new Date());

                        String query = "select * from USERTABLE where date = '" + currentDateandTime + "'";
//                            String query = "SELECT * FROM USERTABLE WHERE date BETWEEN '" + startDate + "' AND '" + endDate + "'";
//                            String query = "SELECT * FROM USERTABLE WHERE date BETWEEN '" + startDate + "' AND '" + endDate + "'";
                        export(query,currentDateandTime+".xls");

                    }
                });
                exportall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String query = "select * from USERTABLE";
                        export(query,"Exported.xls");
                    }
                });


                builder.setView(vi);
                dialog1 = builder.create();
                dialog1.show();

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
//                    Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
//                    startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri));
//                } else {
//                    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
//                    if (!dir.exists()) {
//                        boolean b = dir.mkdirs();
//                        Log.e("valueofB", String.valueOf(b));
//                    }
//
//
//
//
//
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//                    String currentDateandTime = sdf.format(new Date());
//
//                    String query = "select * from USERTABLE where date = '"+currentDateandTime+"'";
//
//
//
//
//                    new SQLiteToExcel
//                            .Builder(MainActivity.this)
//                            .setDataBase(getDatabasePath(DatabaseHelper.DATABASE_NAME).getAbsolutePath())
////                            .setSQL("select * from USERTABLE")
//                            .setSQL(query)
//                            .setOutputPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath())
//                            //Optional, if null, default output path is app ExternalFilesDir.
//                            .setOutputFileName(currentDateandTime+".xls")
//                            .start(new SQLiteToExcel.ExportListener() {
//                                @Override
//                                public void onStart() {
//
//                                }
//
//                                @Override
//                                public void onCompleted(String filePath) {
//                                    Toast.makeText(MainActivity.this, "Exported to Documents (Name: "+currentDateandTime+".xls)!", Toast.LENGTH_LONG).show();
//                                }
//
//                                @Override
//                                public void onError(Exception e) {
//                                    Log.e("eror",e.toString());
//                                    Toast.makeText(MainActivity.this, "Something went wrong while exporting!", Toast.LENGTH_SHORT).show();
//
//                                }
//                            });
//                }
            }
        });

        checkHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHistory();
            }
        });

        agentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agentText.setBackgroundColor(Color.parseColor("#58FF58"));
                personalText.setBackgroundColor(Color.parseColor("#ffffff"));
                isPersonal = false;
            }
        });
        personalText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agentText.setBackgroundColor(Color.parseColor("#ffffff"));
                personalText.setBackgroundColor(Color.parseColor("#58FF58"));
                isPersonal = true;
            }
        });


        List<String> initialmobileBanking = Arrays.asList("Bkash", "Nagad", "Others");
        List<String> initialbanks = Arrays.asList("Sonali Bank", "Agrani Bank", "Pubali Bank", "Krishi Bank", "IBBL", "DBBL", "Prime Bank", "Others");


        finalmobileBanking = new ArrayList<>();
        finalBanking = new ArrayList<>();

        try {
            mobileBankinArray = new JSONArray(getSharedPreference("mb", "[]"));
            bankingArray = new JSONArray(getSharedPreference("b", "[]"));

            if (mobileBankinArray.length() == 0) {
                for (String s : initialmobileBanking) {
                    mobileBankinArray.put(s);
                    finalmobileBanking.add(s);
                }
                setSharedPreference("mb", mobileBankinArray.toString());

            } else {
                for (int i = 0; i < mobileBankinArray.length(); i++) {
                    finalmobileBanking.add(mobileBankinArray.getString(i));
                }
            }
            if (bankingArray.length() == 0) {
                for (String s : initialbanks) {
                    bankingArray.put(s);
                    finalBanking.add(s);
                }
                setSharedPreference("b", bankingArray.toString());
            } else {
                for (int i = 0; i < bankingArray.length(); i++) {
                    finalBanking.add(bankingArray.getString(i));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        RMTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String s = rateEDT.getText().toString();
                if (!TextUtils.isEmpty(s)) {
                    double d = Double.parseDouble(s);
                    if (charSequence.toString().length() != 0) {
                        Log.e("called", "rm");
                        try {
                            String msg = String.valueOf(d * Double.parseDouble(charSequence.toString()));
                            amount.setText(msg);
                        } catch (Exception e) {
                            amount.setText(String.valueOf(1));
                        }
                    } else {
                        amount.setText(String.valueOf(1));
                    }
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        amountTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String s = rateEDT.getText().toString();
                if (!TextUtils.isEmpty(s)) {
                    double d = Double.parseDouble(s);
                    if (charSequence.toString().length() != 0) {
                        Log.e("called", "amount");
                        try {
                            String msg = String.valueOf(Double.parseDouble(charSequence.toString()) / d);
                            RMEDT.setText(msg);
                        } catch (Exception e) {
                            RMEDT.setText(String.valueOf(1));
                        }
                    } else {
                        RMEDT.setText(String.valueOf(1));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        RMEDT.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                amount.removeTextChangedListener(amountTextWatcher);
                RMEDT.addTextChangedListener(RMTextWatcher);
            }
        });

        amount.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                RMEDT.removeTextChangedListener(RMTextWatcher);
                amount.addTextChangedListener(amountTextWatcher);
            }
        });


        bankingAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, finalBanking);
        spinner.setAdapter(bankingAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String s = finalBanking.get(position);
                PG.setText(s);

                if (s.equals("Others")) {
                    PG.setEnabled(true);
                    PG.setText(null);
                } else {
                    PG.setEnabled(false);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                PG.setText(null);
            }

        });


        bank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionManager.beginDelayedTransition(findViewById(R.id.animationid), new AutoTransition());
                isBanking = BANKING_TYPE_BANK;
                bank.setBackgroundColor(Color.parseColor("#58FF58"));
                others.setBackgroundColor(Color.parseColor("#ffffff"));
                fax.setBackgroundColor(Color.parseColor("#ffffff"));
                addressLayout.setVisibility(View.VISIBLE);
                chooser.setVisibility(View.GONE);
                spinner.setVisibility(View.VISIBLE);


                addresstext.setText("Branch");


                // Create an adapter as shown below
                bankingAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, finalBanking);
                spinner.setAdapter(bankingAdapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        String s = finalBanking.get(position);
                        PG.setText(s);

                        if (s.equals("Others")) {
                            PG.setEnabled(true);
                            PG.setText(null);
                        } else {
                            PG.setEnabled(false);
                        }

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
                TransitionManager.beginDelayedTransition(findViewById(R.id.animationid), new AutoTransition());
                spinner.setVisibility(View.VISIBLE);
                bank.setBackgroundColor(Color.parseColor("#ffffff"));
                fax.setBackgroundColor(Color.parseColor("#ffffff"));
                others.setBackgroundColor(Color.parseColor("#58FF58"));
                isBanking = BANKING_TYPE_MOBILE_BANK;
                addressLayout.setVisibility(View.GONE);
                chooser.setVisibility(View.VISIBLE);
                // Create an adapter as shown below
                mobileBankingAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, finalmobileBanking);
                spinner.setAdapter(mobileBankingAdapter);

                addresstext.setText("Address");


                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        String s = finalmobileBanking.get(position);
                        PG.setText(s);

                        if (s.equals("Others")) {
                            PG.setEnabled(true);
                            PG.setText(null);
                        } else {
                            PG.setEnabled(false);
                        }

                        if (s.equals("FAX")) {
                            addressLayout.setVisibility(View.VISIBLE);
                        } else {
                            addressLayout.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        PG.setText(null);
                    }

                });
            }
        });
        fax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionManager.beginDelayedTransition(findViewById(R.id.animationid), new AutoTransition());
                bank.setBackgroundColor(Color.parseColor("#ffffff"));
                others.setBackgroundColor(Color.parseColor("#ffffff"));
                fax.setBackgroundColor(Color.parseColor("#58FF58"));
                isBanking = BANKING_TYPE_FAX;
                addressLayout.setVisibility(View.VISIBLE);
                chooser.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
                PG.setText("FAX");


                // Create an adapter as shown below
            }
        });


        if (getSharedPreference("rate", "1").equals("1")) {
            rateEDT.setText(String.valueOf(1));
        } else {
            rateEDT.setText(getSharedPreference("rate", "1"));
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
                    String receiverName = name.getText().toString();
                    String phonestr = phone.getText().toString();
                    String amountstr = amount.getText().toString();
                    String rmAmount = RMEDT.getText().toString();
                    String pg = PG.getText().toString();
                    String pn = PN.getText().toString();
                    String trx = createRandomTRX();
                    String chooserText = isPersonal ? "Personal" : "Agent";
                    String addressstr = address.getText().toString();
                    String receiverphonestr = receiverphone.getText().toString();
                    String rateStr = rateEDT.getText().toString();


                    if (TextUtils.isEmpty(amountstr)) {
                        amount.setError("Field can not be empty!");
                        amount.requestFocus();
                    } else if (TextUtils.isEmpty(pn)) {
                        PN.setError("Field can not be empty!");
                        PN.requestFocus();
                    } else if (TextUtils.isEmpty(rateStr)) {
                        rateEDT.setError("Field can not be empty!");
                        rateEDT.requestFocus();
                    } else if (TextUtils.isEmpty(rmAmount)) {
                        RMEDT.setError("Field Can Not Be Empty!");
                        RMEDT.requestFocus();
                    } else if (TextUtils.isEmpty(pg)) {
                        PG.setError("Field Can Not Be Empty!");
                        PG.requestFocus();
                    } else {
                        boolean isOk = true;
                        if (isBanking == BANKING_TYPE_BANK) {
                            if (TextUtils.isEmpty(receiverName)) {
                                name.setError("Field Can Not Be Empty!");
                                name.requestFocus();
                                isOk = false;
                            }
                        }
                        if (isBanking == BANKING_TYPE_FAX) {

                            if (TextUtils.isEmpty(receiverName)) {
                                name.setError("Field Can Not Be Empty!");
                                name.requestFocus();
                                isOk = false;
                            }

                            if (TextUtils.isEmpty(receiverphonestr)) {
                                receiverphone.setError("Field Can Not Be Empty!");
                                receiverphone.requestFocus();
                                isOk = false;
                            }
                        }


                        if (isBanking == BANKING_TYPE_FAX || isBanking == BANKING_TYPE_BANK) {
                            if (TextUtils.isEmpty(addressstr)) {
                                address.setError("Field Can Not Empty!");
                                address.requestFocus();
                                isOk = false;
                            }
                        }
                        if (isOk) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            String currentDateandTime = sdf.format(new Date());

                            PaymentModel model = new PaymentModel();
                            model.setRECEIVERNAME(TextUtils.isEmpty(receiverName) ? "" : receiverName);
                            model.setRECEIVERS_PHONE(TextUtils.isEmpty(receiverphonestr) ? "" : receiverphonestr);
                            model.setUSERPHONE(TextUtils.isEmpty(phonestr) ? "" : phonestr);
                            model.setUSERID(createRandom());
                            model.setADDRESS(TextUtils.isEmpty(addressstr) ? "" : addressstr);

                            Log.e("isBanking", String.valueOf(isBanking));
                            Log.e("chooserText", chooserText);


                            String personalValue;
                            if (isBanking == BANKING_TYPE_BANK) {
                                personalValue = "Bank";
                            } else if (isBanking == BANKING_TYPE_MOBILE_BANK) {
                                personalValue = isPersonal ? "Personal" : "Agent";
                            } else {
                                personalValue = "FAX";
                            }

                            model.setISPERSONAL(personalValue);
                            model.setTRANSACTIONID(trx);
                            model.setPAYMENT_RM(TextUtils.isEmpty(rmAmount) ? "" : rmAmount);
                            model.setRM_RATE(TextUtils.isEmpty(rateStr) ? "" : rateStr);
                            model.setPAYMENTPHONENUMBER(TextUtils.isEmpty(pn) ? "" : pn);
                            model.setPAYMENNT_BDT(TextUtils.isEmpty(amountstr) ? "" : amountstr);
                            model.setDATE(currentDateandTime);
                            model.setPAYMENTGATEWAY(TextUtils.isEmpty(pg) ? "" : pg);

                            try {
                                printData(model, trx, currentDateandTime, phonestr);
                            } catch (EscPosConnectionException e) {
                                Log.e("Error", e.toString());
                            } catch (EscPosEncodingException e) {
                                Log.e("Error", e.toString());
                            } catch (EscPosBarcodeException e) {
                                Log.e("Error", e.toString());
                            } catch (EscPosParserException e) {
                                Log.e("Error", e.toString());
                            } catch (JSONException e) {
                                Log.e("Error", e.toString());
                            }

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

//            storageHelper.requestStorageAccess();


            // do stuff


            requestPermissionMethod();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void printData(PaymentModel model, String trx, String currentDateandTime, String phonestr) throws EscPosConnectionException, EscPosEncodingException, EscPosBarcodeException, EscPosParserException, JSONException {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("I", model.getUSERID());
        jsonObject.put("T", trx);
        jsonObject.put("D", currentDateandTime);


        String p = "[C]================================\n" +
                "[L]Transaction ID: [R]" + model.getTRANSACTIONID() + "\n" +
                "[L]Sender's Phone: [R]" + phonestr + "\n" +
                "[C]================================\n" +
                "[L]Rec Name: [R]" + model.getRECEIVERNAME() + "\n" +
                "[L]Rec Phone: [R]" + model.getRECEIVERS_PHONE() + "\n" +
                "[L]Address: [R]" + model.getADDRESS() + "\n" +
                "[L]RM Rate: [R]" + model.getRM_RATE() + "\n" +
                "[L]RM Amount: [R]" + model.getPAYMENT_RM() + "\n" +
                "[C]================================\n" +
                "[L]BDT Amount: [R]" + model.getPAYMENNT_BDT() + "\n" +
                "[L]Payment GW: [R]" + model.getPAYMENTGATEWAY() + "\n" +
                "[L]AC/MOB NO: [R]" + model.getPAYMENTPHONENUMBER() + "\n" +
                "[L]Receiver Type: [R]" + model.getISPERSONAL() + "\n" +
                "[L]Date: [R]" + model.getDATE() + "\n" +
                "[C]================================\n" +
                "\n" +
                "[C]<qrcode size='30'>" + jsonObject + "</qrcode>";


        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "There is a problem with the bluetooth device", Toast.LENGTH_SHORT).show();
            return;
        } else if (mBluetoothAdapter.isEnabled()) {


            EscPosPrinter printer = new EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 48f, 32);
            printer.printFormattedText(p, 100f).disconnectPrinter();
            name.setText(null);
            phone.setText(null);
            amount.setText(null);
//                                    PG.setText(null);
            PN.setText(null);
            RMEDT.setText(null);
            receiverphone.setText(null);
            address.setText(null);
            setSharedPreference("rate", model.getRM_RATE());
            helper.saveMessage(model);

        } else {
            Toast.makeText(MainActivity.this, "Please turn on your bluetooth.", Toast.LENGTH_SHORT).show();
        }


    }

    private void showHistory() {
        AlertDialog dialog1;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View vi = getLayoutInflater().inflate(R.layout.show_history, null, false);
        RecyclerView showHistory = vi.findViewById(R.id.historyContainer);

        Spinner spinner1 = vi.findViewById(R.id.spinner);
        String[] list = new String[]{"All", "Bank", "Mobile Banking", "FAX"};
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item,
                list);
        spinner1.setAdapter(adapter);
        Cursor c = helper.getTodayHistory();
        ArrayList<PaymentModel> models = new ArrayList<>();
        ArrayList<PaymentModel> banks = new ArrayList<>();
        ArrayList<PaymentModel> nonbanks = new ArrayList<>();
        ArrayList<PaymentModel> faxlist = new ArrayList<>();


        if (c == null) {
            Toast.makeText(this, "Data Not Found", Toast.LENGTH_SHORT).show();
            return;
        }
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            PaymentModel model = new PaymentModel();
            String datestr = c.getString(c.getColumnIndexOrThrow(helper.DATE));
            String namestr = c.getString(c.getColumnIndexOrThrow(helper.USERNAME));
            String idstr = c.getString(c.getColumnIndexOrThrow(helper.USERID));
            String phonestr = c.getString(c.getColumnIndexOrThrow(helper.USERPHONE));
            String pgstr = c.getString(c.getColumnIndexOrThrow(helper.PAYMENTGATEWAY));
            String bdtamountstr = c.getString(c.getColumnIndexOrThrow(helper.PAYAMOUNT));
            String payphnstr = c.getString(c.getColumnIndexOrThrow(helper.PAYMENTPHONENUMBER));
            String trxid = c.getString(c.getColumnIndexOrThrow(helper.TRANSACTIONID));
            String addressstr = c.getString(c.getColumnIndexOrThrow(helper.ADDRESS));
            String rmAmountstr = c.getString(c.getColumnIndexOrThrow(helper.PAYAMOUNTRM));
            String rmratesstr = c.getString(c.getColumnIndexOrThrow(helper.RMRATE));
            String paymenttypestr = c.getString(c.getColumnIndexOrThrow(helper.ISPERSONAL));
            String receiverPhoneStr = c.getString(c.getColumnIndexOrThrow(helper.RECEIVERS_PHONE));
            model.setDATE(datestr);
            model.setRECEIVERNAME(namestr);
            model.setUSERID(idstr);
            model.setUSERPHONE(phonestr);
            model.setPAYMENTGATEWAY(pgstr);
            model.setPAYMENNT_BDT(bdtamountstr);
            model.setPAYMENTPHONENUMBER(payphnstr);
            model.setTRANSACTIONID(trxid);
            model.setADDRESS(addressstr);
            model.setPAYMENT_RM(rmAmountstr);
            model.setRM_RATE(rmratesstr);
            model.setISPERSONAL(paymenttypestr);
            model.setRECEIVERS_PHONE(receiverPhoneStr);
            models.add(model);
            if (!pgstr.equals("Others")) {
                if (finalmobileBanking.contains(pgstr)) {
                    nonbanks.add(model);
                } else if (finalBanking.contains(pgstr)) {
                    banks.add(model);
                } else {
                    faxlist.add(model);
                }
            }
        }

        Collections.reverse(models);
        Collections.reverse(nonbanks);
        Collections.reverse(banks);


        RVAdapter adapter2 = new RVAdapter(this, models);
        showHistory.setAdapter(adapter2);


        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Log.e("position", String.valueOf(position));
                switch (position) {
                    case 0: {
                        RVAdapter adapter = new RVAdapter(MainActivity.this, models);
                        showHistory.setAdapter(adapter);
                        break;
                    }
                    case 1: {
                        RVAdapter adapter = new RVAdapter(MainActivity.this, banks);
                        showHistory.setAdapter(adapter);
                        break;
                    }
                    case 2: {
                        RVAdapter adapter = new RVAdapter(MainActivity.this, nonbanks);
                        showHistory.setAdapter(adapter);
                        break;
                    }
                    case 3: {
                        RVAdapter adapter = new RVAdapter(MainActivity.this, faxlist);
                        showHistory.setAdapter(adapter);
                        break;
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                RVAdapter adapter = new RVAdapter(MainActivity.this, models);
                showHistory.setAdapter(adapter);
            }

        });


        builder.setView(vi);
        dialog1 = builder.create();
        dialog1.show();

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

    private static final String ALLOWED_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyz";

    private static String createRandomTRX() {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(15);
        for (int i = 0; i < 15; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
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
        int DocumentsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_DOCUMENTS);
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        int readExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int writeExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (readExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (writeExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (DocumentsPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.MANAGE_DOCUMENTS);
        }
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        }
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
        Toast.makeText(this, "showIng Toast: " + jsonObject.toString(), Toast.LENGTH_LONG).show();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View vi = getLayoutInflater().inflate(R.layout.result, null, false);
        TextView names = vi.findViewById(R.id.name);
        TextView ids = vi.findViewById(R.id.id);
        TextView amounts = vi.findViewById(R.id.rmamount);
        TextView dates = vi.findViewById(R.id.date);
        TextView paymentGateways = vi.findViewById(R.id.paymentgateway);
        TextView paymentNumbers = vi.findViewById(R.id.paymentNumber);
        TextView senderphoneNumbers = vi.findViewById(R.id.phone);
        TextView trxids = vi.findViewById(R.id.transactionID);
        TextView addressres = vi.findViewById(R.id.address);
        TextView bdtamount = vi.findViewById(R.id.bdtamount);
        TextView rmrateres = vi.findViewById(R.id.rmRate);
        TextView paymenttyperes = vi.findViewById(R.id.paymenttype);
        TextView receiverphonesss = vi.findViewById(R.id.receiverphone);

        Button newScan = vi.findViewById(R.id.newS);
        newScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.e("clicked", "new Scan");
                    name.setText(names.getText().toString());
                    phone.setText(senderphoneNumbers.getText().toString());
                    // amount.setText(amounts.getText().toString());

                    if (finalmobileBanking.contains(paymentGateways.getText().toString())) {
                        others.performClick();
                    } else if (finalBanking.contains(paymentGateways.getText().toString())) {
                        bank.performClick();
                    } else {
                        fax.performClick();
                    }


                    PG.setText(paymentGateways.getText().toString());
                    PN.setText(paymentNumbers.getText().toString());
                    // TRXID.setText(trxids.getText().toString());
                    address.setText(addressres.getText().toString());
                    receiverphone.setText(receiverphonesss.getText().toString());
                    if (paymenttyperes.getText().toString().equals("Personal")) {
                        personalText.performClick();
                    } else if (paymenttyperes.getText().toString().equals("Agent")) {
                        agentText.performClick();
                    }


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
                String bdtamountstr = c.getString(c.getColumnIndexOrThrow(helper.PAYAMOUNT));
                String payphnstr = c.getString(c.getColumnIndexOrThrow(helper.PAYMENTPHONENUMBER));
                String trxid = c.getString(c.getColumnIndexOrThrow(helper.TRANSACTIONID));

                String addressstr = c.getString(c.getColumnIndexOrThrow(helper.ADDRESS));
                String rmAmountstr = c.getString(c.getColumnIndexOrThrow(helper.PAYAMOUNTRM));
                String rmratesstr = c.getString(c.getColumnIndexOrThrow(helper.RMRATE));
                String paymenttypestr = c.getString(c.getColumnIndexOrThrow(helper.ISPERSONAL));
                String receiverPhoneStr = c.getString(c.getColumnIndexOrThrow(helper.RECEIVERS_PHONE));

                addressres.setText(addressstr);
                bdtamount.setText(bdtamountstr);
                rmrateres.setText(rmratesstr);
                paymenttyperes.setText(paymenttypestr);

                names.setText(namestr);
                ids.setText(idstr);
                amounts.setText(rmAmountstr);
                dates.setText(datestr);
                paymentGateways.setText(pgstr);
                paymentNumbers.setText(payphnstr);
                senderphoneNumbers.setText(phonestr);
                trxids.setText(trxid);
                receiverphonesss.setText(receiverPhoneStr);


            }

            c.close();


        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
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