package com.challenge.dfr.ui.saved;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

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

import java.util.List;

public class SavedFragment extends Fragment {

    private SavedViewModel savedViewModel;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_saved_cases, container, false);
        savedViewModel =
                ViewModelProviders.of(this).get(SavedViewModel.class);
        savedViewModel.getCases().observe(getViewLifecycleOwner(), new Observer<List<ForensicCase>>() {
            @Override
            public void onChanged(@Nullable List<ForensicCase> cases) {
                LinearLayout parent = getView().findViewById(R.id.saved_cases_input_layout);

                if (cases.size() == 0) {
                    int marginSizeInDP = 8;
                    int marginInDp = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, marginSizeInDP, getResources()
                                    .getDisplayMetrics());

                    Button btn = new Button(getContext());
                    btn.setText(cases.size() + " " + getResources().getString(R.string.nav_saved));
                    btn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    btn.setEnabled(false);

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(marginInDp, marginInDp, marginInDp, marginInDp);
                    parent.addView(btn, layoutParams);
                }

                for (int i = 0; i < cases.size(); i++) {
                    final ForensicCase item = cases.get(i);

                    int marginSizeInDP = 8;
                    int marginInDp = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, marginSizeInDP, getResources()
                                    .getDisplayMetrics());

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(marginInDp, marginInDp, marginInDp, marginInDp);

                    int buttonSizeInDp = 24;
                    int buttonInDp = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, buttonSizeInDp, getResources()
                                    .getDisplayMetrics());

                    Button btn = new Button(getContext());
                    btn.setHeight(buttonInDp);
                    btn.setText(item.caseNumber);
                    btn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    btn.setCompoundDrawablesWithIntrinsicBounds(
                            getResources().getDrawable(R.drawable.ic_save_black_24dp),
                            null, null, null);

                    int paddingSizeInDP = 20;
                    int paddingInDp = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, paddingSizeInDP, getResources()
                                    .getDisplayMetrics());

                    btn.setPadding(paddingInDp, 0, 0, 0);
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CaseManager.setCase(item);
                            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                            navController.navigate(R.id.action_navigation_saved_to_navigation_case_evidence);
                        }
                    });

                    parent.addView(btn, layoutParams);
                }
            }
        });

        return root;
    }
}
