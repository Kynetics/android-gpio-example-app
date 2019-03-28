/*
 *  Copyright (C)  2018-2019 Kynetics, LLC
 *  SPDX-License-Identifier: Apache-2.0
 */
package com.kynetics.gpiolibapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SelectMonitorFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = GpioActivity.class.getSimpleName();
    private TextView listGpioTextView;
    private EditText listGpioTextEdit;
    private Button monitorBtn;
    private Button selectBtn;
    private ArrayList<Integer> integerArrayListGpio;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        View view = inflater.inflate(R.layout.gpio_card_view, container, false);
        listGpioTextView = (TextView) view.findViewById(R.id.gpio_manage_list_view);
        listGpioTextEdit = (EditText) view.findViewById(R.id.gpio_manage_list_edit);
        monitorBtn = (Button) view.findViewById(R.id.monitor_btn);
        monitorBtn.setOnClickListener(this);
        selectBtn = (Button) view.findViewById(R.id.select_btn);
        selectBtn.setOnClickListener(this);
        listGpioTextEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        return view;
    }


    public void select() {
        if(checkCorrectGpioList(listGpioTextEdit.getText().toString())) {
            String direction = listGpioTextEdit.getText().toString();
            if (!direction.equals("")) {
                integerArrayListGpio = getArrayListGpio(direction);
                try {
                    Log.d(TAG, "Select GPIO" + direction);
                    final Intent selectedGpioActivity = new Intent(getActivity(), SelectedGpioActivity.class);
                    selectedGpioActivity.putIntegerArrayListExtra("gpioIntegerList", integerArrayListGpio);
                    startActivity(selectedGpioActivity);
                } catch (NumberFormatException e) {
                    Log.w(TAG, e.getMessage());
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Select a GPIO from the list", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private ArrayList<Integer> getArrayListGpio(String s) {
        ArrayList<Integer> retArrayList = new ArrayList<>();
        Log.d(TAG, "GetArrayListGpio string" + s);
        if(checkCorrectGpioList(listGpioTextEdit.getText().toString())) {
            String[] parts = s.split(",");
            for(int i = 0; i < parts.length; i++) {
                retArrayList.add(Integer.parseInt(parts[i]));
                Log.d(TAG, "GetArrayListGpio" + Integer.parseInt(parts[i]));
            }
            return retArrayList;}
        else{
            Toast.makeText(getActivity(), "Syntax error: please enter a comma separated list of GPIO numbers", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public void monitoring() {
        if(checkCorrectGpioList(listGpioTextEdit.getText().toString())) {
            try {
                Log.d(TAG, "Selected GPIO list" + listGpioTextEdit.getText().toString());
                integerArrayListGpio = getArrayListGpio(listGpioTextEdit.getText().toString());
                final Intent monitorGpioActivity = new Intent(getActivity(), MonitorGpioActivity.class);
                monitorGpioActivity.putIntegerArrayListExtra("gpioIntegerList", integerArrayListGpio);
                startActivity(monitorGpioActivity);
            } catch (NumberFormatException e) {
                Log.w(TAG, e.getMessage());
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkCorrectGpioList(String s) {
        if(s.matches("(\\d)+(,(\\d)+)*") || s.matches("\\d")){
            return true;
        }
        else{
            Toast.makeText(getActivity(), "Syntax error: please enter a comma separated list of GPIO numbers", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.monitor_btn:
                monitoring();
                break;
            case R.id.select_btn:
                select();
                break;
        }
    }
}
