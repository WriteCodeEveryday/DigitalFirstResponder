package com.challenge.dfr.ui.quick_reference;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.challenge.dfr.R;

public class QuickReferenceFragment extends Fragment {

    private QuickReferenceViewModel quickReferenceViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //TODO Create the quick reference page.
        quickReferenceViewModel =
                ViewModelProviders.of(this).get(QuickReferenceViewModel.class);
        View root = inflater.inflate(R.layout.fragment_quick_reference, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);
        quickReferenceViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        //TODO Remove this toast.
        Toast.makeText(getActivity(), "Coming Soon",
                Toast.LENGTH_LONG).show();
        return root;
    }
}
