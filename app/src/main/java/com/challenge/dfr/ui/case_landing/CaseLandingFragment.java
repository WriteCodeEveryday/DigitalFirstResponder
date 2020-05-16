package com.challenge.dfr.ui.case_landing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.challenge.dfr.R;
import com.challenge.dfr.database.ForensicCase;
import com.challenge.dfr.logical.CaseManager;

import java.util.Date;

public class CaseLandingFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_case_landing, container, false);

        final ForensicCase current = CaseManager.getCase();
        if (current != null) {
            TextView case_number = root.findViewById(R.id.case_number);
            case_number.setText(getResources().getString(R.string.case_number_text) +
                    ": " + current.caseNumber);

            TextView examiner = root.findViewById(R.id.examiner);
            examiner.setText(getResources().getString(R.string.examiner_text) +
                    ": " + current.examiner);

            TextView location = root.findViewById(R.id.location);
            location.setText(getResources().getString(R.string.location_text) +
                    ": " + current.location);

            Date creation = new Date();
            creation.setTime(current.created);

            TextView created = root.findViewById(R.id.created);
            created.setText(getResources().getString(R.string.timestamp_text) +
                    " " + creation.toString());

            TextView notes = root.findViewById(R.id.notes);
            notes.setText(getResources().getString(R.string.notes_text) +
                    ": " + current.notes);

            root.findViewById(R.id.edit_case_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.action_navigation_case_landing_to_navigation_create);
                }
            });

            root.findViewById(R.id.view_evidence_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.action_navigation_case_landing_to_navigation_case_evidence);
                }
            });


            // Evidence Options.
            root.findViewById(R.id.add_initial_screen_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CaseManager.setEvidenceType(R.string.nav_initial_screen);
                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.action_navigation_case_landing_to_navigation_case_evidence_options);
                }
            });

            root.findViewById(R.id.add_tool_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CaseManager.setEvidenceType(R.string.nav_tool);
                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.action_navigation_case_landing_to_navigation_case_evidence_options);
                }
            });

            root.findViewById(R.id.add_hardware_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CaseManager.setEvidenceType(R.string.nav_add_hardware);
                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.action_navigation_case_landing_to_navigation_case_evidence_options);
                }
            });

            root.findViewById(R.id.add_manual_extraction_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CaseManager.setEvidenceType(R.string.nav_add_manual_extraction);
                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.action_navigation_case_landing_to_navigation_case_evidence_options);
                }
            });

            root.findViewById(R.id.add_id_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CaseManager.setEvidenceType(R.string.nav_scan_id);
                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.action_navigation_case_landing_to_navigation_case_evidence_options);
                }
            });

            root.findViewById(R.id.add_text_files_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CaseManager.setEvidenceType(R.string.nav_scan_text);
                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.action_navigation_case_landing_to_navigation_case_evidence_options);
                }
            });
        }

        return root;
    }
}
