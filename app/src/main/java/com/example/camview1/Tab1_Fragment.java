package com.example.camview1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
            final Button button_for_interrupt = view.findViewById(R.id.button5);
            final Button button_to_start_mThread = view.findViewById(R.id.button6);
            final Button button7 = view.findViewById(R.id.button7);

            button_for_interrupt.setText("Kill Background");
            button_to_start_mThread.setText("Start mThread");

            button_for_interrupt.setEnabled(false);
            button_for_interrupt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        ((MainActivity) Objects.requireNonNull(getActivity())).interrupt_cameraService_mThread();
                        button_for_interrupt.setEnabled(false);
                        button_to_start_mThread.setEnabled(true);
                    }catch (Exception e){
                        ((MainActivity) Objects.requireNonNull(getActivity())).append_to_tab3_fragment(e.toString());
                    }
                }
            });
            button_to_start_mThread.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted
                        ((MainActivity) Objects.requireNonNull(getActivity())).append_to_tab3_fragment("Asking for permission");
                        ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA},0 );
                    }else {
                        ((MainActivity) Objects.requireNonNull(getActivity())).reset_cameraService();
                        ((MainActivity) Objects.requireNonNull(getActivity())).startCameraService_mThread();
                        button_for_interrupt.setEnabled(true);
                        button_to_start_mThread.setEnabled(false);
                    }
                }
            });
            button7.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            ((MainActivity) Objects.requireNonNull(getActivity())).append_to_tab3_fragment(Thread.currentThread().getName()+" :onCreateView Tab1_Fragment");

            firstcreated=true;
        }
        return view;
    }
}
