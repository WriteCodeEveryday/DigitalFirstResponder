package com.challenge.dfr.ui.create;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.challenge.dfr.R;
import com.challenge.dfr.database.ForensicCase;
import com.challenge.dfr.database.ForensicEvidence;
import com.challenge.dfr.logical.CaseManager;
import com.challenge.dfr.logical.ChecksumManager;
import com.challenge.dfr.logical.ForensicsDatabaseManager;

import java.util.Date;
import java.util.List;

public class CreateFragment extends Fragment {
    private Date creationDate;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_create_case, container, false);

        // Allow editing.
        final ForensicCase current = CaseManager.getCase();
        if (current != null) {
            creationDate = new Date();
            creationDate.setTime(current.created);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final List<ForensicEvidence> evidence =
                            ForensicsDatabaseManager
                                    .instance()
                                    .getForensicEvidenceDao()
                                    .getEvidence(current.cid);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Button create = root.findViewById(R.id.create_case_button);
                            create.setText(R.string.nav_update_case);

                            EditText case_number_input = root.findViewById(R.id.case_number_input);
                            case_number_input.setText(current.caseNumber);

                            EditText examiner_input = root.findViewById(R.id.examiner_input);
                            examiner_input.setText(current.examiner);

                            EditText location_input = root.findViewById(R.id.location_input);
                            location_input.setText(current.location);

                            EditText notes_input = root.findViewById(R.id.notes_input);
                            notes_input.setText(current.notes);

                            if (evidence.size() > 0) {
                                examiner_input.setEnabled(false);
                                location_input.setEnabled(false);
                            }
                        }
                    });
                }
            }).start();
        } else {
            creationDate = new Date();
        }

        TextView textView = root.findViewById(R.id.timestamp_text);
        textView.setText(getResources().getString(R.string.timestamp_text) +
                " " + creationDate.toString());

        root.findViewById(R.id.create_case_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int warning = getResources().getColor(R.color.colorAccent);
                boolean canSubmit = true;

                EditText case_number_input = root.findViewById(R.id.case_number_input);
                String number = case_number_input.getText().toString();
                if (number.trim().equals("")) {
                    canSubmit = false;
                    case_number_input.setBackgroundColor(warning);
                } else {
                    case_number_input.setBackgroundColor(Color.TRANSPARENT);
                }

                EditText examiner_input = root.findViewById(R.id.examiner_input);
                String examiner = examiner_input.getText().toString();
                if (examiner.trim().equals("")) {
                    canSubmit = false;
                    examiner_input.setBackgroundColor(warning);
                } else {
                    examiner_input.setBackgroundColor(Color.TRANSPARENT);
                }

                EditText location_input = root.findViewById(R.id.location_input);
                String location = location_input.getText().toString();
                if (location.trim().equals("")) {
                    canSubmit = false;
                    location_input.setBackgroundColor(warning);
                } else {
                    location_input.setBackgroundColor(Color.TRANSPARENT);
                }

                EditText notes_input = root.findViewById(R.id.notes_input);
                String notes = notes_input.getText().toString();
                if (notes.trim().equals("")) {
                    canSubmit = false;
                    notes_input.setBackgroundColor(warning);
                } else {
                    notes_input.setBackgroundColor(Color.TRANSPARENT);
                }

                if (canSubmit) {
                    // Create the brand new case.
                    final ForensicCase created_case;
                    if (current == null) {
                        created_case = new ForensicCase();
                        created_case.created = creationDate.getTime();
                    } else {
                        created_case = current;
                    }
                    created_case.caseNumber = number;
                    created_case.examiner = examiner;
                    created_case.location = location;
                    created_case.notes = notes;
                    created_case.checksum = ChecksumManager
                            .getUpdatedForensicCaseChecksum(created_case);

                    // Push the case into the CaseManager and the database.
                    CaseManager.setCase(created_case);
                    if (current == null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ForensicsDatabaseManager
                                        .instance()
                                        .getForensicCaseDao()
                                        .insert(created_case);
                            }
                        }).start(); //Needs to be a background thread.
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ForensicsDatabaseManager
                                        .instance()
                                        .getForensicCaseDao()
                                        .update(created_case);
                            }
                        }).start(); //Needs to be a background thread.
                    }

                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.action_navigation_create_to_navigation_case_landing);
                }
            }
        });

        return root;
    }
}
