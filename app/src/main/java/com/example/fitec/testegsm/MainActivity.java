package com.example.fitec.testegsm;

import android.Manifest;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.view.View;

import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;


public class MainActivity extends Activity {

    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private TelephonyManager Tel;
    private MyPhoneStateListener MyListener;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private TextView label,textClock;
    private EditText timeText;
    private int delay;
    private FileHelper helper;
    private Button btStart,btStop;
    private Spinner spinner;
    private boolean isStop;

    private String LTESignal,GSMSignal, tipo,StgTextClock;
    private  int tempoMinutos,tempoSegundos;

    private Thread thread;
    DatabaseReference database;

    DatabaseReference myRef,MyRef2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
       // Log.d("ocCreate","entrou no onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
// Write a message to the database
        database = FirebaseDatabase.getInstance().getReference();
        myRef = database.child("GSMDatas");
        MyRef2 =database.child("Result");


        // Check if we have write permission
            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        this,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSION_ACCESS_COARSE_LOCATION);

       btStart = (Button) findViewById(R.id.btStart);
       btStop = (Button) findViewById(R.id.btStop);
       label  = (TextView) findViewById(R.id.texto);
       spinner = (Spinner) findViewById(R.id.spinner);
       timeText = (EditText) findViewById(R.id.timeText);
       textClock = (TextView) findViewById(R.id.textClock);
       tempoMinutos = 0;
       tempoSegundos = 0;


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.numbers,android.R.layout.simple_spinner_item );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
       spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               tipo = parent.getItemAtPosition(position).toString();
               Toast.makeText(parent.getContext(),tipo,Toast.LENGTH_SHORT).show();
           }

           @Override
           public void onNothingSelected(AdapterView<?> parent) {

           }
       });


       helper = new FileHelper(getApplicationContext());
       btStart.setOnClickListener(listener);
       btStop.setOnClickListener(listener);

       delay = 1000;
       thread = new Thread(new ScanThread());
       isStop = false;

       MyListener = new MyPhoneStateListener();
       Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
       Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);


       // thread.start();


    }

    @Override
    protected void onStart() {
        super.onStart();
        MyRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String texto = dataSnapshot.getValue().toString();
                //Toast.makeText(MainActivity.this,"Result:"+texto,Toast.LENGTH_SHORT).show();
               // myRef.




               label.setText(texto);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
        }
    };
    @Override
    protected void onPause()
    {
        super.onPause();
    }

    private OnClickListener listener = new OnClickListener()
    {
        @Override
        public void onClick(View btn)
        {
            if (btn.getId() == R.id.btStart)
            {
                try {
                    tempoMinutos = Integer.parseInt(timeText.getText().toString());

                    if(tempoMinutos != 0 ) {
                        tempoSegundos = 0;

                        String relogio = "";

                        if (tempoMinutos < 10) {
                            relogio += "0"+tempoMinutos;
                            if(tempoSegundos < 10)
                            {
                                relogio += ":0" + tempoSegundos;
                            }else
                            {
                                relogio += ":"+tempoSegundos;
                            }
                        }else
                        {
                            relogio +="" +tempoMinutos;
                            if(tempoSegundos < 10)
                            {
                                relogio += ":0" + tempoSegundos;
                            }else
                            {
                                relogio += ":"+tempoSegundos;
                            }
                        }
                    }
                }catch (Exception e) {
                    tempoSegundos =0;
                    tempoSegundos =0;
                    Toast.makeText(MainActivity.this,e.getMessage()+" Coleta sem cronometro...",Toast.LENGTH_SHORT).show();
                }


                thread.start();
                isStop =false;
            }
            else if (btn.getId() == R.id.btStop)
            {

                isStop = true;
                //thread.s
                label.setText("Parado!");

            }
        }
    };


    class ScanThread implements Runnable {

        @Override
        public void run() {
            while (!isStop){
                try {
                    if(!(tempoSegundos == 0 && tempoMinutos == 0)) {

                        if (tempoMinutos >= 1) {
                            if (tempoSegundos == 0) {
                                tempoMinutos --;
                                tempoSegundos =60;
                            }else
                             {
                                tempoSegundos--;
                             }
                        } else if (tempoMinutos <= 0) {
                            if (tempoSegundos <= 0) {
                                tempoMinutos = 0;
                                tempoSegundos = 0;
                                 isStop = true;
                                 //label.setText("Parado!");
                            }else
                            {
                                tempoSegundos--;
                            }
                        }
                    }else
                    {
                        isStop = true;
                    }

                    Thread.sleep(delay);
                    Message msg = new Message();
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    Toast.makeText(MainActivity.this,e.getMessage()+" Coleta sem cronometro...",Toast.LENGTH_SHORT).show();

                }
            }
        }
    }
    private int t = 0;
    String teste = "Null";
    int count = 0;
    int countGSM = 0;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1: {
                     if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    String relogio = "";

                    if (tempoMinutos < 10) {
                        relogio += "0"+tempoMinutos;
                        if(tempoSegundos < 10)
                        {
                            relogio += ":0" + tempoSegundos;
                        }else
                        {
                            relogio += ":"+tempoSegundos;
                        }
                    }else
                    {
                        relogio +="" +tempoMinutos;
                        if(tempoSegundos < 10)
                        {
                            relogio += ":0" + tempoSegundos;
                        }else
                        {
                            relogio += ":"+tempoSegundos;
                        }
                    }
                    textClock.setText(relogio);
                    if(tipo.equalsIgnoreCase("Indoor")) {
                        LTESignal += "1#";
                        GSMSignal += "1#";
                    }else if(tipo.equalsIgnoreCase("Outdoor"))
                    {
                        LTESignal += "2#";
                        GSMSignal += "2#";
                    }
                    for (final CellInfo infos : Tel.getAllCellInfo()) {

                        if (infos instanceof CellInfoLte) {

                           count++;
                           //LTESignal += "\t" + ((CellInfoLte) infos).getCellSignalStrength().getDbm();
                            LTESignal += "," + ((CellInfoLte) infos).getCellSignalStrength().getDbm();
                        }
                        if (infos instanceof CellInfoGsm) {

                            countGSM++;
                           // GSMSignal += "\t" + ((CellInfoGsm) infos).getCellSignalStrength().getDbm();
                            GSMSignal += "," + ((CellInfoGsm) infos).getCellSignalStrength().getDbm();
                        }
                    }

                    if(countGSM >0) {
                       // GSMSignal += "\n";
                        if(!isStop) {
                            label.setText("QTD: " + count + "\n SINAL GSM: " + GSMSignal);
                           // helper.writeSDFile(LTESignal, "gsm3.txt");
                            myRef.push().setValue(GSMSignal);
                        }else
                        {
                            label.setText("Parado!");
                        }
                    }else
                    {
                        // LTESignal += "\n";

                         if(!isStop) {
                             label.setText("QTD: " + count + "\n SINAL LTE: " + LTESignal);
                            // helper.writeSDFile(LTESignal, "gsm3.txt");
                                myRef.push().setValue(LTESignal);
                         }else
                         {
                             label.setText("Parado!");
                         }
                    }

                  //  Toast.makeText(MainActivity.this,
                    //        ""+LTESignal+" --- "+count,
                      //      Toast.LENGTH_SHORT)
                        //    .show();

                    LTESignal = "";
                    GSMSignal = "";
                    count = 0;
                    countGSM = 0;
                    t++;
                    break;
                }
                default:
                    break;
            }
        }
    };
}

