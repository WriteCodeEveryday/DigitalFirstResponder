package com.challenge.dfr.ui.case_evidence;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.challenge.dfr.R;
import com.challenge.dfr.database.ForensicCase;
import com.challenge.dfr.database.ForensicEvidence;
import com.challenge.dfr.logical.CaseManager;

import java.util.List;

public class CaseEvidenceListFragment extends Fragment {

    private CaseEvidenceListViewModel evidenceViewModel;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_case_evidence, container, false);

        final ForensicCase current = CaseManager.getCase();
        if (current != null) {
            TextView case_number = root.findViewById(R.id.case_number);
            case_number.setText(getResources().getString(R.string.case_number_text) +
                    ": " + current.caseNumber);

            TextView examiner = root.findViewById(R.id.examiner);
            examiner.setText(getResources().getString(R.string.examiner_text) +
                    ": " + current.examiner);

            evidenceViewModel =
                    ViewModelProviders.of(this).get(CaseEvidenceListViewModel.class);
            evidenceViewModel.getEvidence().observe(getViewLifecycleOwner(), new Observer<List<ForensicEvidence>>() {
                @Override
                public void onChanged(@Nullable List<ForensicEvidence> evidence_list) {
                    LinearLayout evidence_items = root.findViewById(R.id.evidence_items_layout);
                    evidence_items.removeAllViews();

                    System.out.println("Evidence: " + evidence_list.size());
                    for (int i = 0; i < evidence_list.size(); i++) {
                        final ForensicEvidence evidence = evidence_list.get(i);

                        int marginSizeInDP = 4;
                        int marginInDp = (int) TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP, marginSizeInDP, getResources()
                                        .getDisplayMetrics());

                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(0, marginInDp, 0, 0);

                        System.out.println("Creating button: " + evidence.evidenceType);
                        Button btn = new Button(getContext());
                        btn.setText(evidence.evidenceType);
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
                                CaseManager.setEvidence(evidence);
                                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                                navController.navigate(R.id.action_navigation_case_evidence_to_navigation_case_evidence_preview);
                            }
                        });

                        evidence_items.addView(btn, layoutParams);
                    }
                }
            });

            root.findViewById(R.id.add_evidence_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.action_navigation_case_evidence_to_navigation_case_landing);
                }
            });

            root.findViewById(R.id.export_case_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO Remove this toast.
                    Toast.makeText(getActivity(), "Coming Soon",
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        return root;
    }
}
