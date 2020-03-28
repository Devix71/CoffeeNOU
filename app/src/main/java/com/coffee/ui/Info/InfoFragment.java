package com.coffee.ui.Info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.coffee.R;

public class InfoFragment extends Fragment {
private InfoFragmentView infoFragmentView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        infoFragmentView = ViewModelProviders.of(this).get(InfoFragmentView.class);
        View root = inflater.inflate(R.layout.fragment_info, container, false);

        return root;
    }
}
