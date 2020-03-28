package com.coffee.ui.Info;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InfoFragmentView extends ViewModel {
    private MutableLiveData<String> mText;

    public InfoFragmentView() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the info fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
