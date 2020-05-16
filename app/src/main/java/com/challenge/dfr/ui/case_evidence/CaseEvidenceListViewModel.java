package com.challenge.dfr.ui.case_evidence;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.challenge.dfr.database.ForensicCase;
import com.challenge.dfr.database.ForensicEvidence;
import com.challenge.dfr.logical.CaseManager;
import com.challenge.dfr.logical.ForensicsDatabaseManager;

import java.util.List;

public class CaseEvidenceListViewModel extends ViewModel {

    private MutableLiveData<List<ForensicEvidence>> saved_evidence;

    public CaseEvidenceListViewModel() {
        saved_evidence = new MutableLiveData<List<ForensicEvidence>>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ForensicCase current = CaseManager.getCase();
                List<ForensicEvidence> cases = ForensicsDatabaseManager
                        .instance()
                        .getForensicEvidenceDao()
                        .getEvidence(current.cid);


                saved_evidence.postValue(cases);
            }
        }).start(); // get all cases.
    }

    public LiveData<List<ForensicEvidence>> getEvidence() {
        return saved_evidence;
    }
}
