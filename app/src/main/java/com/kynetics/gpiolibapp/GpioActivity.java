/*
 *  Copyright (C)  2018-2019 Kynetics, LLC
 *  SPDX-License-Identifier: Apache-2.0
 */
package com.kynetics.gpiolibapp;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kynetics.gpiolib.GpioManagerInterface;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GpioActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = GpioActivity.class.getSimpleName();
    public static final int REQUEST_CODE_CONFIGURATION_FILE = 1;
    private TextView listGpioTextView;
    private EditText listGpioTextEdit;
    private Button monitorBtn;
    private Button selectBtn;
    private ArrayList<Integer> integerArrayListGpio;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private GpioManagerInterface manager = GpioManagerInterface.getGpioManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);
        switchFragment(new SelectMonitorFragment());
        setTitle(R.string.title_gpio_activity);
        Log.d(TAG, "onCreate");
        listGpioTextView = (TextView) findViewById(R.id.gpio_manage_list_view);
        listGpioTextEdit = (EditText) findViewById(R.id.gpio_manage_list_edit);
        monitorBtn = (Button) findViewById(R.id.monitor_btn);
        selectBtn = (Button) findViewById(R.id.select_btn);
        drawerLayout = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == REQUEST_CODE_CONFIGURATION_FILE){
            Uri uriTree = data.getData();
            Log.e(TAG, uriTree.getPath());
            if(uriTree.getPath().substring(uriTree.getPath().lastIndexOf(".") + 1, uriTree.getPath().length()).equals("json")) {
                Log.d(TAG, "equals json "+MimeTypeMap.getFileExtensionFromUrl(uriTree.getPath()));
                String jsonString = loadJSONFromAsset(uriTree);
                try {
                    manager.initGpio(jsonString);
                    Toast.makeText(this, "Load of GPIO initialization file completed. Initialized GPIO "+getStringFromList(manager.getInitializedGpio()), Toast.LENGTH_LONG).show();
                } catch (GpioManagerInterface.EmptyJsonFileException|RuntimeException e) {
                    handleException(e);
                }
            }
            else {
                Toast.makeText(this, "The selected file is not of the correct format. Please select another one", Toast.LENGTH_SHORT).show();
                selectConfigurationFile();
            }
            navigationView.getMenu().getItem(0).setChecked(true);
        }
    }

    private String getStringFromList(List<Integer> initializedGpio) {
        String retString = new String();
        if(initializedGpio.size() > 1) {
            for (int gpio = 0; gpio < initializedGpio.size()-1; gpio++) {
                retString = retString + " " + String.valueOf(initializedGpio.get(gpio))+",";
            }
            retString = retString+ " " + String.valueOf(initializedGpio.get(initializedGpio.size()-1));
            return retString;
        }
        else{
            retString = String.valueOf(initializedGpio.get(0));
            return retString;
        }
    }

    public String loadJSONFromAsset(Uri uri) {
        String json = null;
        try {
            Log.e(TAG, uri.getPath());
            InputStream inputStream = getContentResolver().openInputStream(uri);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG,"onNavigationItemSelected");
        switch (id){
            case R.id.monitor_and_selection:
                Log.d(TAG,"monitor and selection");
                switchFragment(new SelectMonitorFragment());
                break;
            case R.id.loading_initialization_file:
                Log.d(TAG,"loading_initialization_file");
                selectConfigurationFile();
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    private void switchFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrame, fragment);
        fragmentTransaction.commit();
    }

    public void selectConfigurationFile() {
        Log.d(TAG, "onInitClick");
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("*/*");
        String[] mimetypes = {"application/octet-stream"};
        chooseFile.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, REQUEST_CODE_CONFIGURATION_FILE);
    }

    private void handleException(Throwable e) {
        Log.e(TAG, e.getMessage());
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
