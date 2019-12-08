package com.example.camview1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;

public class Tab1_Fragment extends Fragment {
    private Boolean firstcreated = false ;
    private View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(!firstcreated){
            view = inflater.inflate(R.layout.tab1_fragment,container,false);
            Button button_for_Fragment2 = view.findViewById(R.id.button5);
            button_for_Fragment2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) Objects.requireNonNull(getActivity())).setTab2Text();
                }
            });
            firstcreated=true;
        }
        return view;
    }
}
