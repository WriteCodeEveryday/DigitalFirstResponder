package com.challenge.dfr.ui.quick_reference;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class QuickReferenceViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public QuickReferenceViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Quick Reference Fragment Here");
    }

    public LiveData<String> getText() {
        return mText;
    }
}