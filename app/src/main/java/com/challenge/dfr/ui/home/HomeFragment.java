package com.challenge.dfr.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.challenge.dfr.R;
import com.challenge.dfr.logical.CaseManager;

public class HomeFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        root.findViewById(R.id.create_case_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CaseManager.setCase(null);
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_navigation_home_to_navigation_create);
            }
        });

        root.findViewById(R.id.scan_evidence_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_navigation_home_to_navigation_scan);
            }
        });

        root.findViewById(R.id.quick_reference_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_navigation_home_to_navigation_quick_reference);
            }
        });

        return root;
    }
}
