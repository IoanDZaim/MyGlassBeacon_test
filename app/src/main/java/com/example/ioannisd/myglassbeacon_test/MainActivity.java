/**
 * ****************************************************************************
 * Copyright (C) 2015 Open Universiteit Nederland
 * <p/>
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Contributors: Ioannis D. Zaimidis
 * ****************************************************************************
 */


package com.example.ioannisd.myglassbeacon_test;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.util.List;

public class MainActivity extends Activity implements SensorEventListener{

    private CardScrollView mCardScroller;
    private BeaconManager beaconManager;
    private View mView;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private Handler handler = new Handler();
    private Region TeliSpace;
    Beacon[] Beacons;
    private enum Distance {UNKNOWN, IMMEDIATE, NEAR, FAR}
    Distance[] BeaconDist;
    private static final String TAG="Ranged Beacons";
    private static final String ESTIMOTE_PROXIMITY_UUID= "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private static final int Beacon1Major=29740;
    private static final int Beacon1Minor=13061;
    private static final int Beacon2Major=42439;
    private static final int Beacon2Minor=55045;
    private static final int Beacon3Major=8427;
    private static final int Beacon3Minor=15281;

    private static final double ImmediateThres=0.2;
    private static final double nearThres=0.9;
    private static final double farThres=3.1;



    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Beacons = new Beacon[3];
        BeaconDist = new Distance[3];
        sensorManager= (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this, stepSensor,SensorManager.SENSOR_DELAY_NORMAL);

        TeliSpace= new Region("regionID", ESTIMOTE_PROXIMITY_UUID, null, null);
        beaconManager =new BeaconManager(getApplicationContext());
        //((Beacon) arrayListBeacon[0]).getMajor();

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
           @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Beacon beacon:beacons ) {
                           if (beacon.getMajor()==Beacon1Major && beacon.getMinor()==Beacon1Minor) {
                               Beacons[0]=beacon;
                           }//1st
                           if (beacon.getMajor()==Beacon2Major && beacon.getMinor()==Beacon2Minor){
                                Beacons[1]=beacon;
                            }//2nd if
                            if (beacon.getMajor()==Beacon3Major && beacon.getMinor()==Beacon3Minor){
                                Beacons[2]=beacon;
                            }//3rd if
                        }//for
                        if (Beacons[0] !=null){
                           double Beacon1Distance= Utils.computeAccuracy(Beacons[0]);
                            Log.d(TAG, "Beacon1Distance: "+ Beacon1Distance+" "+BeaconDist[0]);
                            if (Beacon1Distance > farThres){
                                BeaconDist[0]=Distance.UNKNOWN;
                            }else if (Beacon1Distance < farThres && Beacon1Distance > nearThres){
                                BeaconDist[0]=Distance.FAR;
                            }else if (Beacon1Distance < nearThres && Beacon1Distance > ImmediateThres){
                                BeaconDist[0]=Distance.NEAR;
                            }else if (Beacon1Distance <ImmediateThres) {
                                BeaconDist[0]=Distance.IMMEDIATE;
                            }
                        }else{
                            Log.w("First Beacon","Is not detected");
                        }
                        if (Beacons[1] !=null){
                            double Beacon2Distance= Utils.computeAccuracy(Beacons[1]);
                            Log.d(TAG, "Beacon2Distance: "+ Beacon2Distance+" "+BeaconDist[1]);
                            if (Beacon2Distance > farThres){
                                BeaconDist[1]=Distance.UNKNOWN;
                            }else if (Beacon2Distance < farThres && Beacon2Distance > nearThres){
                                BeaconDist[1]=Distance.FAR;
                            }else if (Beacon2Distance < nearThres && Beacon2Distance > ImmediateThres){
                                BeaconDist[1]=Distance.NEAR;
                            }else if (Beacon2Distance <ImmediateThres) {
                                BeaconDist[1]=Distance.IMMEDIATE;
                            }
                        }else {//2nd beacon null check
                            Log.w("Second Beacon","Is not detected");}
                        if (Beacons[2] !=null){
                            double Beacon3Distance= Utils.computeAccuracy(Beacons[2]);
                            Log.d(TAG, "Beacon3Distance: "+ Beacon3Distance+" "+BeaconDist[2]);
                            if (Beacon3Distance > farThres){
                                BeaconDist[2]=Distance.UNKNOWN;
                            }else if (Beacon3Distance < farThres && Beacon3Distance > nearThres){
                                BeaconDist[2]=Distance.FAR;
                            }else if (Beacon3Distance < nearThres && Beacon3Distance > ImmediateThres){
                                BeaconDist[2]=Distance.NEAR;
                            }else if (Beacon3Distance <ImmediateThres) {
                                BeaconDist[2]=Distance.IMMEDIATE;
                            }
                        }else {//3rd beacon null check
                            Log.w("Third Beacon","Is not detected");}
                       setContentView(buildView());
                    }//run
                }/**thread*/);
            }//onBeaconDiscovered
        }/** listener */)/**listener*/;



        mView = buildView();
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardScrollAdapter() {
            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public Object getItem(int position) {
                return mView;
            }


            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return mView;
            }


            @Override
            public int getPosition(Object item) {
                if (mView.equals(item)) {
                    return 0;
                }
                return AdapterView.INVALID_POSITION;
            }
        });


        // TAP Listener

        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

               AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
               am.playSoundEffect(Sounds.DISALLOWED);

            }
        });

        setContentView(mCardScroller);

    }//onCreate




    @Override
    protected void onResume() {
        super.onResume();


        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(TeliSpace);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        try {
            beaconManager.stopRanging(TeliSpace);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.onPause();
    }



    private View buildView() {
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT_FIXED);
        card.setText("The distance of the beacons is as follows -Beacon1: " + BeaconDist[0] + " -Beacon2: " + BeaconDist[1] + " -Beacon3: " + BeaconDist[2]);
        return card.getView();
    }



    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
/**
    //@Override
    public IBinder onBind(Intent intent) {
        return null;
    }
*/
}
