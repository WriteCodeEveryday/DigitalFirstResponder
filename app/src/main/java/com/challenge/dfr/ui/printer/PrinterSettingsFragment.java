package com.challenge.dfr.ui.printer;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.challenge.dfr.R;
import com.challenge.dfr.hardware.PrinterManager;

public class PrinterSettingsFragment extends Fragment {
    View parent;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_printer_settings, container, false);
        parent = root;

        updateStatus();
        parent.findViewById(R.id.radio_option_label).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new Thread() {
                    @Override
                    public void run() {
                        PrinterManager.setWorkingDirectory(getContext());
                        PrinterManager.loadLabel();
                        savePrinterPreferences();
                        updateStatus();
                        finish();
                    }
                }.start();
            }
        });

        parent.findViewById(R.id.radio_option_roll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new Thread() {
                    @Override
                    public void run() {
                        PrinterManager.setWorkingDirectory(getContext());
                        PrinterManager.loadRoll();
                        savePrinterPreferences();
                        updateStatus();
                        finish();
                    }
                }.start();
            }
        });
        setUpPrinterOptions();

        return root;
    }

    protected void finish() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NavController navController = Navigation
                        .findNavController(getActivity(), R.id.nav_host_fragment);
                navController.navigate(
                        R.id.action_navigation_printer_settings_to_navigation_case_evidence_preview);
            }
        });
    }

    protected void savePrinterPreferences() {
        SharedPreferences prefs = getContext()
                .getSharedPreferences("printer_settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String printer = PrinterManager.getModel();
        PrinterManager.CONNECTION connection = PrinterManager.getConnection();
        String mode = PrinterManager.getMode();

        editor.putString("printer", printer);
        editor.putString("connection", String.valueOf(connection));
        editor.putString("mode", mode);


        editor.commit();
    }

    private void setUpPrinterOptions() {
        String currentModel = PrinterManager.getModel();
        PrinterManager.CONNECTION currentConnection = PrinterManager.getConnection();

        final String[] supportedModels = PrinterManager.getSupportedModels();
        final PrinterManager.CONNECTION[] supportedConnections = PrinterManager.getSupportedConnections();

        RadioGroup connectors = parent.findViewById(R.id.connection_selection_group);
        RadioGroup printers = parent.findViewById(R.id.printer_selection_group);


        connectors.removeAllViews();
        for (int i = 0; i < supportedConnections.length; i++) {
            RadioButton button = new RadioButton(getContext());
            if (currentConnection != null) {
                button.setChecked(supportedConnections[i].compareTo(currentConnection) == 0);
            }
            button.setText(supportedConnections[i].toString());
            button.setId(i);
            final int j = i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!v.isSelected()) {
                        PrinterManager.setConnection(supportedConnections[j]);
                        savePrinterPreferences();
                        resetStatus();
                    }
                }
            });
            connectors.addView(button);
        }

        printers.removeAllViews();
        for (int i = 0; i < supportedModels.length; i++) {
            RadioButton button = new RadioButton(getContext());
            if (currentModel != null) {
                button.setChecked(supportedModels[i].equals(currentModel));
            }
            button.setText(supportedModels[i]);
            button.setId(i);
            final int j = i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!v.isSelected()) {
                        PrinterManager.setModel(supportedModels[j]);
                        PrinterManager.setConnection(null);
                        savePrinterPreferences();
                        setUpPrinterOptions();
                        resetStatus();
                    }
                }
            });
            printers.addView(button);
        }
    }

    private void resetStatus() {
        TextView status = parent.findViewById(R.id.printer_status_text);
        status.setText(R.string.printer_status_text);

        RadioButton label = parent.findViewById(R.id.radio_option_label);
        RadioButton roll = parent.findViewById(R.id.radio_option_roll);
        label.setVisibility(View.GONE);
        roll.setVisibility(View.GONE);

        if (PrinterManager.getModel() != null && PrinterManager.getConnection() != null) {
            new Thread() {
                @Override
                public void run() {
                    String currentModel = PrinterManager.getModel();
                    PrinterManager.CONNECTION currentConnection = PrinterManager.getConnection();

                    if (currentConnection == null || currentModel == null) {
                        return;
                    }

                    if (PrinterManager.getConnection().equals(PrinterManager.CONNECTION.BLUETOOTH)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                        }

                        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                                .getDefaultAdapter();
                        if (bluetoothAdapter != null) {
                            if (!bluetoothAdapter.isEnabled()) {
                                Intent enableBtIntent = new Intent(
                                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(enableBtIntent);
                            }
                        }
                    }

                    PrinterManager.findPrinter(PrinterManager.getModel(), PrinterManager.getConnection());
                    updateStatus();
                }
            }.start();
        }
    }

    private void updateStatus() {
        if (PrinterManager.getPrinter() != null) {
            final RadioButton label = parent.findViewById(R.id.radio_option_label);
            final RadioButton roll = parent.findViewById(R.id.radio_option_roll);

            final String[] options = PrinterManager.getLabelRoll();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setUpPrinterOptions();
                    if (options.length == 2){
                        label.setText(options[0]);
                        roll.setText(options[1]);

                        label.setVisibility(View.VISIBLE);
                        roll.setVisibility(View.VISIBLE);
                        label.setEnabled(true);
                        roll.setEnabled(true);
                        label.setChecked(false);
                        roll.setChecked(false);
                    }
                }
            });

            PrinterManager.CONNECTION conn = PrinterManager.getConnection();
            String model = PrinterManager.getModel();
            String mode = PrinterManager.getMode();
            String output = "";

            if (conn == null || model == null) {
                // Do nothing.
            }
            else if (mode == null) {
                output = getResources().getString(R.string.no_roll_text);
            } else {
                output = getResources().getString(R.string.printer_cancel_text);
            }

            final TextView status = parent.findViewById(R.id.printer_status_text);
            final String finalOutput = output;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    status.setText(finalOutput);
                }
            });
        }
    }
}
