package com.snsignal.app;


import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private TextView textView, typeNetwork, tvLatitude, tvLongitude, tvOperator;
    private LocationManager locationManager;
    private Boolean writeCsv = false;

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 5000;
    private String csv = "/storage/emulated/0/Android/data/com.snsignal.app/data/data.csv";
    String[] data = new String[]{"","","","",""};
    CSVWriter csvWriter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        //getSupportActionBar().setDisplayShowTitleEnabled(false);

        textView = findViewById(R.id.rssi);
        typeNetwork = findViewById(R.id.networkType);
        tvLatitude = findViewById(R.id.latitude);
        tvLongitude = findViewById(R.id.longitude);
        tvOperator = findViewById(R.id.operator);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeCsv = !writeCsv;
                if(writeCsv){
                    Toast.makeText(MainActivity.this, "Grabando datos...", Toast.LENGTH_SHORT).show();
                    button.setText("◼ Detener");
                    button.setBackgroundColor(getResources().getColor(R.color.buttonOnRecording));
                }
                else{
                    Toast.makeText(MainActivity.this, "Datos Guardados en\nAndroid/data/com.snsignal.app/data/data.csv", Toast.LENGTH_LONG).show();
                    button.setText("Guardar Datos");
                    button.setBackgroundColor(getResources().getColor(R.color.primaryDarkColor));

                }
            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //getExternalFilesDir(null);
        createFiles();



        getSignalStrength();
        GetLatLon();
        timedRefresh();
    }

    private void createFiles() {
        File outFile = new File(getExternalFilesDir(null).getParent(), "data");

        try {
            if (!outFile.exists()){
                outFile.mkdirs();

                csvWriter = new CSVWriter(new FileWriter(csv));
                String[] headerRow = new String[]{"Operador", "Red", "RSSI", "Latitud", "Longitud"};
                csvWriter.writeNext(headerRow);
                csvWriter.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    protected void timedRefresh() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                getSignalStrength();

                data[0] = tvOperator.getText().toString();
                data[1] = typeNetwork.getText().toString();
                data[2] = textView.getText().toString();
                data[3] = tvLatitude.getText().toString();
                data[4] = tvLongitude.getText().toString();

                if(writeCsv){
                    try {
                        File file = new File(csv);
                        FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(String.valueOf(data[0]+","+data[1]+","+data[2]+","+data[3]+","+data[4])+"\n");
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, delay);
    }


    public void getSignalStrength() throws SecurityException {
        TelephonyManager telephonyManager = (TelephonyManager) MainActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();   //This will give info of all sims present inside your mobile

        TelephonyManager tManager = (TelephonyManager) getBaseContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        //String sim1 = tManager.getNetworkOperatorName();
        tvOperator.setText(String.valueOf(tManager.getNetworkOperatorName()));


        if(cellInfos != null) {
            for (int i = 0 ; i < cellInfos.size() ; i++) {
                if (cellInfos.get(i).isRegistered()) {
                    if (cellInfos.get(i) instanceof CellInfoWcdma) {
                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfos.get(i);
                        CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                        textView.setText(cellSignalStrengthWcdma.getDbm() + " dBm");
                        typeNetwork.setText("3G (WCDMA)");
                        break;

                    } else if (cellInfos.get(i) instanceof CellInfoGsm) {
                        CellInfoGsm cellInfogsm = (CellInfoGsm) cellInfos.get(i);
                        CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                        textView.setText(cellSignalStrengthGsm.getDbm() + " dBm");
                        typeNetwork.setText("2G (GSM)");
                        break;

                    } else if (cellInfos.get(i) instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = (CellInfoLte) cellInfos.get(i);
                        CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                        textView.setText(cellSignalStrengthLte.getDbm() + " dBm");
                        typeNetwork.setText("4G (LTE)");
                        break;

                    } else if (cellInfos.get(i) instanceof CellInfoCdma) {
                        CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfos.get(i);
                        CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
                        textView.setText(cellSignalStrengthCdma.getDbm() + " dBm");
                        typeNetwork.setText("3G (CDMA)");
                        break;

                    }
                }
            }
        }
    }

    public void GetLatLon() {
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                tvLatitude.setText(String.valueOf(location.getLatitude()));
                tvLongitude.setText(String.valueOf(location.getLongitude()));
                //getSignalStrength();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }

        };
        // Register the listener with the location manager to receive location updates
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    private void requestPermission() {
        requestPermissions(new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, READ_PHONE_NUMBERS, READ_PHONE_STATE, WRITE_EXTERNAL_STORAGE}, 100);
    }


}