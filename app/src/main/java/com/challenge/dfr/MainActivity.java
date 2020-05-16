package com.challenge.dfr;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.challenge.dfr.broken.BadIdeas;
import com.challenge.dfr.logical.ForensicsDatabaseManager;
import com.challenge.dfr.hardware.PrinterManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


public class MainActivity extends AppCompatActivity {
    protected void loadPrinterPreferences() {
        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences("printer_settings", Context.MODE_PRIVATE);
        String printer = prefs.getString("printer", null);
        String raw_connection = prefs.getString("connection", null);
        PrinterManager.CONNECTION connection = null;
        if (raw_connection != null && !raw_connection.equals("null")) {
            connection = PrinterManager.CONNECTION.valueOf(raw_connection);
        }
        String mode = prefs.getString("mode", null);

        if(printer != null) {
            PrinterManager.setModel(printer);
        }

        if (connection != null) {
            PrinterManager.setConnection(connection);
        }

        if (printer != null && connection != null) {
            PrinterManager.findPrinter(printer, connection);
        }

        if (mode != null) {
            PrinterManager.setWorkingDirectory(getApplicationContext());
            switch(mode) {
                case "label":
                    PrinterManager.loadLabel();
                    break;
                case "roll":
                    PrinterManager.loadRoll();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Fix the SQL.
        BadIdeas.fix(getApplicationContext());

        //Setup UI
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_quick_reference, R.id.navigation_saved)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        ForensicsDatabaseManager.instance(getApplicationContext());

        //Load the printer
        loadPrinterPreferences();
    }
}
