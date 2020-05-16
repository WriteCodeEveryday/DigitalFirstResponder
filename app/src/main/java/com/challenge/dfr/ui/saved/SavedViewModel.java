package com.challenge.dfr.ui.saved;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.challenge.dfr.database.ForensicCase;
import com.challenge.dfr.logical.ForensicsDatabaseManager;

import java.util.List;

public class SavedViewModel extends ViewModel {

    private MutableLiveData<List<ForensicCase>> saved_cases;

    public SavedViewModel() {
        saved_cases = new MutableLiveData<List<ForensicCase>>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<ForensicCase> cases = ForensicsDatabaseManager
                        .instance()
                        .getForensicCaseDao()
                        .getCases();


                saved_cases.postValue(cases);
            }
        }).start(); // get all cases.
    }

    public LiveData<List<ForensicCase>> getCases() {
        return saved_cases;
    }
}
