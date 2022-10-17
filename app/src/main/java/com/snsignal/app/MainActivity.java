package com.snsignal.app;


import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;

import static java.security.AccessController.getContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    private TextView textView, typeNetwork, tvLatitude, tvLongitude;
    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();

        textView = findViewById(R.id.rssi);
        typeNetwork = findViewById(R.id.networkType);
        tvLatitude = findViewById(R.id.latitude);
        tvLongitude = findViewById(R.id.longitude);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        getSignalStrength(this);
        GetLatLon();
        /*MultiThreadingTest object =  new MultiThreadingTest(this);
        object.start();*/


    }


    /*class MultiThreadingTest extends Thread{

        Context context;

        MultiThreadingTest(Context context){
            this.context = context;
        }

        @Override
        public void run() {
            try {
                while(true){
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    Log.d("myThread", "Testing Multithreading every 3 secs");
                    textView.setText(""+getSignalStrength(context));


                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }*/


    public void getSignalStrength(Context context) throws SecurityException {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //String strength = null;
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();   //This will give info of all sims present inside your mobile
        if(cellInfos != null) {
            for (int i = 0 ; i < cellInfos.size() ; i++) {
                if (cellInfos.get(i).isRegistered()) {
                    if (cellInfos.get(i) instanceof CellInfoWcdma) {
                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfos.get(i);
                        CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                        //strength = String.valueOf(cellSignalStrengthWcdma.getDbm()) + " dBm";
                        textView.setText(cellSignalStrengthWcdma.getDbm() + " dBm");
                        typeNetwork.setText("3G (WCDMA)");
                        break;

                    } else if (cellInfos.get(i) instanceof CellInfoGsm) {
                        CellInfoGsm cellInfogsm = (CellInfoGsm) cellInfos.get(i);
                        CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                        //strength = String.valueOf(cellSignalStrengthGsm.getDbm()) + " dBm";
                        textView.setText(cellSignalStrengthGsm.getDbm() + " dBm");
                        typeNetwork.setText("2G (GSM)");
                        break;

                    } else if (cellInfos.get(i) instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = (CellInfoLte) cellInfos.get(i);
                        CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                        //strength = String.valueOf(cellSignalStrengthLte.getDbm()) + " dBm";
                        textView.setText(cellSignalStrengthLte.getDbm() + " dBm");
                        typeNetwork.setText("4G (LTE)");
                        break;

                    } else if (cellInfos.get(i) instanceof CellInfoCdma) {
                        CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfos.get(i);
                        CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
                        //strength = String.valueOf(cellSignalStrengthCdma.getDbm()) + " dBm";
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

                tvLatitude.setText("" + location.getLatitude());
                tvLongitude.setText("" + location.getLongitude());
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, READ_PHONE_NUMBERS, READ_PHONE_STATE}, 100);
        }
    }
}