/*
 *  Copyright (C)  2018-2019 Kynetics, LLC
 *  SPDX-License-Identifier: Apache-2.0
 */
package com.kynetics.gpiolibapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kynetics.gpiolib.GpioListener;
import com.kynetics.gpiolib.GpioManager;
import com.kynetics.gpiolib.GpioPin;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class MonitorGpioActivity extends AppCompatActivity {

    private static final String TAG = MonitorGpioActivity.class.getSimpleName();
    private LinearLayout newValueLinearLayout;
    private Map<Integer, TextView> integerTextViewMap;
    List<GpioPin> integerGpioPinList = new ArrayList<>();
    private boolean exporting;
    private Button exportButton;
    private Button unexportButton;
    private Button startStopMonitoringButton;

    @SuppressLint("UseSparseArrays")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.monitor_card_view);
        newValueLinearLayout = (LinearLayout) findViewById(R.id.newValueLinearLayout);
        exportButton = (Button) findViewById(R.id.export_btn);
        unexportButton = (Button) findViewById(R.id.unexport_btn);
        startStopMonitoringButton = (Button) findViewById(R.id.start_stop_monitoring_btn);
        integerTextViewMap = new HashMap<>();
        exporting = false;
        /* Get list of gpio from GpioActivity */
        Set<Integer> myIntegerArrayList = new HashSet<>(getIntent().getIntegerArrayListExtra("gpioIntegerList"));
        integerGpioPinList = getGpioPinList(myIntegerArrayList);

        for(int i : myIntegerArrayList){
            TextView tempTextView = new TextView(this);
            integerTextViewMap.put(i, tempTextView);
            tempTextView.setText(getString(R.string.monitor_single_gpio, i));
            tempTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            tempTextView.setGravity(Gravity.CENTER);
            ((LinearLayout) newValueLinearLayout).addView(tempTextView);
        }
    }

    private List<GpioPin> getGpioPinList(Set<Integer> gpioIntegerList) {
        ArrayList<GpioPin> gpioPinArrayList = new ArrayList<>(gpioIntegerList.size());
        for(int gpio : gpioIntegerList)
            gpioPinArrayList.add(GpioManager.getInstance().getGpio(gpio));
        return  gpioPinArrayList;
    }

    public void onStartStopMonitoring(View view){
        if (!GpioManager.getInstance().isMonitoring()){
            startMonitoring();
        }
        else {
            stopMonitoring();
        }
    }

    public void startMonitoring() {
        Log.d(TAG, "startMonitoring");
        try {
            GpioManager.getInstance().startMonitoring(integerGpioPinList);
            Log.d(TAG, "startMonitoring");
            startStopMonitoringButton.setText(R.string.stopMonitoring);
            for(final GpioPin gpio : integerGpioPinList){
                gpio.setListener(new GpioListener(gpio.gpioId, new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {
                        Log.d(TAG, "set text");
                        final int newValue = (Integer) arg;
                        newValueLinearLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                integerTextViewMap.get(gpio.gpioId).setText(getString(R.string.new_value_gpio, newValue, gpio.gpioId));
                            }
                        });
                    }
                }
                ));
            }
        } catch (GpioManager.NotAlreadyExportException e) {
            handleException(e);
        } catch (GpioManager.AlreadyMonitorException e) {
            handleException(e);
        }
    }

    public void stopMonitoring() {
        Log.d(TAG, "stopMonitoring");
        if(GpioManager.getInstance().isMonitoring()) {
            for (Map.Entry<Integer, TextView> textViewEntry : integerTextViewMap.entrySet()) {
                textViewEntry.getValue().setText(getString(R.string.monitor_single_gpio, textViewEntry.getKey()));
            }
            for (GpioPin gpio : integerGpioPinList)
                gpio.setListener(null);
            try {
                GpioManager.getInstance().stopMonitoring();
                startStopMonitoringButton.setText(R.string.startMonitoring);
            } catch (GpioManager.NotMonitorException e) {
                handleException(e);
            }
        }
    }

    public void onExport(View view) {
        Log.d(TAG, "exportListGpio");
        try {
            GpioManager.getInstance().export(integerGpioPinList);
        } catch (FileNotFoundException e) {
            handleException(e);
        }
    }

    public void onUnexport(View view) {
        Log.d(TAG, "unexportListGpio");
        if(GpioManager.getInstance().isMonitoring())
            stopMonitoring();
        try {
            GpioManager.getInstance().unexport(integerGpioPinList);
        } catch (FileNotFoundException e) {
            handleException(e);
        }
    }

    private void handleException(Throwable e) {
        Log.e(TAG, e.getMessage());
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        if(GpioManager.getInstance().isMonitoring())
            stopMonitoring();
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "onSupportNavigateUp");
        super.onBackPressed();

        return true;
    }
}
