package com.example.camview1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
    SurfaceView surfaceView;
    Button button_for_size;
    int int_sizes=-1;
    Button add_sur_button;
    Long last_time;
    Boolean vis=false;
    Button button2;
    Long fps_10;
    Button button_to_start_mThread;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(!firstcreated) {
            view = inflater.inflate(R.layout.tab1_fragment, container, false);
            final Button button_for_interrupt = view.findViewById(R.id.button5);
            button_to_start_mThread = view.findViewById(R.id.button6);
            final Button cam_char_button = view.findViewById(R.id.button7);
            button2 = view.findViewById(R.id.button2);
            button_for_size = view.findViewById(R.id.button);
            button_for_size.setText("Resize");
            add_sur_button = view.findViewById(R.id.button3);
            add_sur_button.setText("Add Surface");
            button2.setText("300x");
            button_for_interrupt.setText("Kill Background");
            button_to_start_mThread.setText("Start mThread");

            button_for_interrupt.setEnabled(false);
            cam_char_button.setEnabled(false);

            button2.setOnClickListener(v -> {
                ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
                params.height=300;
                params.width=300;
                surfaceView.setLayoutParams(params);
            });
            add_sur_button.setOnClickListener(v -> {
                //((MainActivity)getActivity()).cameraService.set_rotation_capture_builder(0);

                ((MainActivity) getActivity()).cameraService.addsurface_capture_builder(surfaceView.getHolder().getSurface());
            });
            button_for_size.setOnClickListener(v -> {
                int_sizes++;
                try {
                    setSurfaceView_size();
                } catch (Exception e) {
                    ((Button) v).setText(e.toString());
                    int_sizes = -1;
                }

            });
            button_for_interrupt.setOnClickListener(v -> {

                try {
                    ((MainActivity) Objects.requireNonNull(getActivity())).interrupt_cameraService_mThread();
                    button_for_interrupt.setEnabled(false);
                    button_to_start_mThread.setEnabled(true);
                    cam_char_button.setEnabled(false);
                } catch (Exception e) {
                    ((MainActivity) Objects.requireNonNull(getActivity())).append_to_tab3_fragment(e.toString());
                }
            });
            button_to_start_mThread.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted
                        ((MainActivity) Objects.requireNonNull(getActivity())).append_to_tab3_fragment("Asking for permission");
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 0);
                    } else {
                        ((MainActivity) Objects.requireNonNull(getActivity())).reset_cameraService();
                        ((MainActivity) Objects.requireNonNull(getActivity())).startCameraService_mThread();
                        button_for_interrupt.setEnabled(true);
                        cam_char_button.setEnabled(true);
                        button_to_start_mThread.setEnabled(false);

                    }
                }
            });
            cam_char_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) Objects.requireNonNull(getActivity())).frag2_scan_cameraChar_keys();
                }
            });
            ((MainActivity) Objects.requireNonNull(getActivity())).append_to_tab3_fragment(Thread.currentThread().getName() + " :onCreateView Tab1_Fragment");
            surfaceView = view.findViewById(R.id.surfaceView);



            firstcreated = true;
        }
        return view;
    }
    void setSurfaceView_size(){
//        Size size = ((MainActivity)getActivity()).get_surfaceview_size();
//        ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
//        params.height=size.getWidth();
//        params.width=size.getHeight();
//        surfaceView.setLayoutParams(params);
//        button_for_size.setText(size.getWidth()+","+size.getHeight());
        Size[] sizes = ((MainActivity)getActivity()).get_surfaceview_size();
        ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
        params.height=sizes[int_sizes].getHeight();
        params.width=sizes[int_sizes].getWidth();
        surfaceView.setLayoutParams(params);
        button_for_size.setText(sizes[int_sizes].getWidth()+","+sizes[int_sizes].getHeight());
    }
    void setSurfaceView_size_300(){
        ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
        params.height=300;
        params.width=300;
        surfaceView.setLayoutParams(params);
        button_for_size.setText(300+","+300);
    }
    void fps(){
        if (last_time == null || fps_10==null) {
            last_time = System.currentTimeMillis();
            button_to_start_mThread.setText("first");
            fps_10 = System.currentTimeMillis();
        } else {
            int elapsed_time_in_milis = ((int)(System.currentTimeMillis()- last_time) );
            last_time=System.currentTimeMillis();
            try {
                int fps = 1000 / elapsed_time_in_milis;

                if(System.currentTimeMillis()- fps_10>100){
                    button_to_start_mThread.setText(Integer.toString(fps));
                    fps_10=System.currentTimeMillis();
                }
            }catch (ArithmeticException e){
                button_to_start_mThread.setText("Divide by zero");
            }
        }
    }
    void print_bitmap(Bitmap bitmap){
        synchronized (this) {
            try {

                Canvas canvas = surfaceView.getHolder().getSurface().lockCanvas(null);
                if (canvas != null) {
//                    ((MainActivity) getActivity()).append_to_tab3_fragment("YEY will draw surface");
                    canvas.drawBitmap(bitmap, 0, 0, new Paint());
//                    ((MainActivity) getActivity()).append_to_tab3_fragment(Thread.currentThread().getName() + " DRAWN");
                    surfaceView.getHolder().getSurface().unlockCanvasAndPost(canvas);
//                    ((MainActivity) getActivity()).append_to_tab3_fragment(Thread.currentThread().getName() + " POST");
                    ((MainActivity) getActivity()).cameraService.unlock_camera_service();
                    ((MainActivity) getActivity()).append_to_tab3_fragment("Drawn sending unlock msg");
                }

            } catch (Exception e) {
                ((MainActivity) getActivity()).append_to_tab3_fragment(e.toString());
            }
        }
    }
}

