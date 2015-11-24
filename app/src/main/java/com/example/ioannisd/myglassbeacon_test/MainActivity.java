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
    private Beacon Beacon1;
    private Beacon Beacon2;
    private Beacon Beacon3;




    private enum Distance {UNKNOWN, IMMEDIATE, NEAR, FAR}
    private Distance Beacon1Dist;
    private Distance Beacon2Dist;
    private Distance Beacon3Dist;

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

        sensorManager= (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this, stepSensor,SensorManager.SENSOR_DELAY_NORMAL);

        Beacon1Dist=Distance.UNKNOWN;
        Beacon2Dist=Distance.UNKNOWN;
        Beacon3Dist=Distance.UNKNOWN;
        TeliSpace= new Region("regionID", ESTIMOTE_PROXIMITY_UUID, null, null);
        beaconManager =new BeaconManager(getApplicationContext());


        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
           @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Beacon beacon:beacons ) {
                           if (beacon.getMajor()==Beacon1Major && beacon.getMinor()==Beacon1Minor) {
                               Beacon1=beacon;
                           }//1st
                           if (beacon.getMajor()==Beacon2Major && beacon.getMinor()==Beacon2Minor){
                                Beacon2=beacon;
                            }//2nd if
                            if (beacon.getMajor()==Beacon3Major && beacon.getMinor()==Beacon3Minor){
                                Beacon3=beacon;
                            }//3rd if
                        }//for
                        if (Beacon1 !=null){
                           double Beacon1Distance= Utils.computeAccuracy(Beacon1);
                            Log.d(TAG, "Beacon1Distance: "+ Beacon1Distance+" "+Beacon1Dist);
                            if (Beacon1Distance > farThres){
                                Beacon1Dist=Distance.UNKNOWN;
                            }else if (Beacon1Distance < farThres && Beacon1Distance > nearThres){
                                Beacon1Dist=Distance.FAR;
                            }else if (Beacon1Distance < nearThres && Beacon1Distance > ImmediateThres){
                                Beacon1Dist=Distance.NEAR;
                            }else if (Beacon1Distance <ImmediateThres) {
                                Beacon1Dist=Distance.IMMEDIATE;
                            }
                        }else{
                            Log.w("First Beacon","Is not detected");
                        }
                        if (Beacon2 !=null){
                            double Beacon2Distance= Utils.computeAccuracy(Beacon2);
                            Log.d(TAG, "Beacon2Distance: "+ Beacon2Distance+" "+Beacon2Dist);
                            if (Beacon2Distance > farThres){
                                Beacon2Dist=Distance.UNKNOWN;
                            }else if (Beacon2Distance < farThres && Beacon2Distance > nearThres){
                                Beacon2Dist=Distance.FAR;
                            }else if (Beacon2Distance < nearThres && Beacon2Distance > ImmediateThres){
                                Beacon2Dist=Distance.NEAR;
                            }else if (Beacon2Distance <ImmediateThres) {
                                Beacon2Dist=Distance.IMMEDIATE;
                            }
                        }else {//2nd beacon null check
                            Log.w("Second Beacon","Is not detected");}
                        if (Beacon3 !=null){
                            double Beacon3Distance= Utils.computeAccuracy(Beacon3);
                            Log.d(TAG, "Beacon3Distance: "+ Beacon3Distance+" "+Beacon3Dist);
                            if (Beacon3Distance > farThres){
                                Beacon3Dist=Distance.UNKNOWN;
                            }else if (Beacon3Distance < farThres && Beacon3Distance > nearThres){
                                Beacon3Dist=Distance.FAR;
                            }else if (Beacon3Distance < nearThres && Beacon3Distance > ImmediateThres){
                                Beacon3Dist=Distance.NEAR;
                            }else if (Beacon3Distance <ImmediateThres) {
                                Beacon3Dist=Distance.IMMEDIATE;
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
        mCardScroller.setAdapter(new CardScrollAdapter (){
            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public Object getItem(int position) {
                return mView;
            }


            @Override
            public View getView ( int position, View convertView, ViewGroup parent){
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
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT);
        card.setText("The distance of the beacons is as follows -Beacon1: " + Beacon1Dist + " -Beacon2: " + Beacon2Dist + " -Beacon3: " + Beacon3Dist);
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
