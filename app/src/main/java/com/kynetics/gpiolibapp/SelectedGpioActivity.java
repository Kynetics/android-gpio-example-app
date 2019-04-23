/*
 *  Copyright (C)  2018-2019 Kynetics, LLC
 *  SPDX-License-Identifier: Apache-2.0
 */
package com.kynetics.gpiolibapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.kynetics.gpiolib.GpioManagerInterface;
import com.kynetics.gpiolib.GpioPinInterface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SelectedGpioActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = SelectedGpioActivity.class.getSimpleName();
    private GpioPinInterface gpioPin;
    private int gpioId;
    List<GpioPinInterface> integerGpioPinList = new ArrayList<>();
    private Spinner selectedGpioSpinner;
    private RadioGroup setDirectionRadioGroup;
    private RadioGroup setValueRadioGroup;
    private Button exportButton;
    private Button unexportButton;
    private Button isExportedButton;
    private Button getDirectionBtn;
    private Button getValueBtn;
    private GpioManagerInterface manager = GpioManagerInterface.getGpioManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_card_view);
        exportButton = (Button) findViewById(R.id.exportBtn);
        unexportButton = (Button) findViewById(R.id.unexportBtn);
        isExportedButton = (Button) findViewById(R.id.isExportedBtn);
        selectedGpioSpinner = (Spinner) findViewById(R.id.selectGpioSpinner);
        setDirectionRadioGroup = (RadioGroup) findViewById(R.id.setDirectionRadio);
        setValueRadioGroup = (RadioGroup) findViewById(R.id.setValueRadio);
        getDirectionBtn = (Button) findViewById(R.id.getDirectionBtn);
        getValueBtn = (Button) findViewById(R.id.getValueBtn);


        setDirectionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.in_direction:
                        setDirection("in");
                        break;
                    case R.id.out_direction:
                        setDirection("out");
                        break;
                }
            }
        });

        setValueRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.one_value:
                        setValue(1);
                        break;
                    case R.id.zero_value:
                        setValue(0);
                        break;
                }
            }
        });

        /* Get list of gpio from GpioActivity */
        ArrayList<Integer> myIntegerArrayList = getIntent().getIntegerArrayListExtra("gpioIntegerList");
        integerGpioPinList = getGpioPinList(myIntegerArrayList);
        if(integerGpioPinList.size()==1){
            selectedGpioSpinner.setVisibility(View.GONE);
            gpioPin = integerGpioPinList.get(0);
            gpioId = gpioPin.getGpioId();
        }
        else{
            selectedGpioSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item, getArrayFrom((ArrayList<GpioPinInterface>) integerGpioPinList)));
            gpioPin = integerGpioPinList.get(0);
            gpioId = gpioPin.getGpioId();
            selectedGpioSpinner.setVisibility(View.VISIBLE);
        }

        selectedGpioSpinner.setOnItemSelectedListener(this);
    }

    public void onExportGpio(View view) {
        Log.d(TAG, "Export");
        try {
            manager.export(gpioPin.getGpioId());
            setDirectionRadioGroup.clearCheck();
            setValueRadioGroup.clearCheck();
        } catch (FileNotFoundException e) {
            handleException(e);
        }
    }

    public void onUnexportGpio(View view) {
        Log.d(TAG, "Unexport ");
        try {
            setDirectionRadioGroup.clearCheck();
            setValueRadioGroup.clearCheck();
            manager.unexport(gpioPin.getGpioId());
        } catch (FileNotFoundException e) {
            handleException(e);
        }
    }

    public void isExported(View view) {
        Toast.makeText(this, Boolean.toString(gpioPin.isExported()).toUpperCase(), Toast.LENGTH_SHORT).show();
    }

    public void getDirection(View view) {
        try {
            Toast.makeText(this, gpioPin.getDirection().toUpperCase(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            handleException(e);
        }
    }

    public void setDirection(String direction) {
        Log.d(TAG, "Set direction " + direction);
        try {
            gpioPin.setDirection(direction);
        } catch (FileNotFoundException e) {
            handleException(e);
        }
    }

    public void getValue(View view) {
        try {
            Toast.makeText(this, String.format(Locale.getDefault(),"%d", gpioPin.getValue()), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            handleException(e);
        }
    }

    public void setValue(int value) {
        try {
            Log.d(TAG, "Set value " + value);
            gpioPin.setValue(value);
        } catch (FileNotFoundException | NumberFormatException e) {
            handleException(e);
        }
    }

    private void handleException(Throwable e) {
        Log.e(TAG, e.getMessage());
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private List<GpioPinInterface> getGpioPinList(ArrayList<Integer> gpioIntegerList) {
        ArrayList<GpioPinInterface> gpioPinArrayList = new ArrayList<>(gpioIntegerList.size());
        for(int gpio : gpioIntegerList)
            gpioPinArrayList.add(manager.getGpio(gpio));
        return  gpioPinArrayList;
    }

    private String[] getArrayFrom(ArrayList<GpioPinInterface> myIntegerArrayList) {
        String[] retArray = new String[myIntegerArrayList.size()];
        for(int i = 0 ; i < myIntegerArrayList.size(); i++)
            retArray[i] = String.valueOf(myIntegerArrayList.get(i).getGpioId());
        return retArray;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        setDirectionRadioGroup.clearCheck();
        setValueRadioGroup.clearCheck();
        gpioId = Integer.parseInt((String) selectedGpioSpinner.getSelectedItem());
        gpioPin = manager.getGpio(gpioId);
        isExportedButton.setClickable(true);
        setDirectionRadioGroup.clearCheck();
        setValueRadioGroup.clearCheck();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "onSupportNavigateUp");
        super.onBackPressed();

        return true;
    }
}
