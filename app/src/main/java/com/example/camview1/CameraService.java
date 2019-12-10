package com.example.camview1;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;

import androidx.annotation.NonNull;


class CameraService {

    CameraManager cameraManager;
    CaptureRequest.Builder capture_builder;
    private int pos;

    public static abstract class StateCallback{
        static void on_mThread_Ready(Long milis_took) {
        }
    }
    private Handler ui_handler;
    private Context mContext;
    private Thread mThread;
    public CameraDevice cameraDevice;
    private Handler back_handler;
    private Object lock = new Object();
    CameraService(Handler hnd, Context cnt) {
        ui_handler = hnd;
        mContext = cnt;
        mThread = new Thread(mThread_runnable);
        sendMsg("New Camera service object: "+this.toString());
    }

    private Runnable mThread_runnable = new Runnable() {
        @Override
        public void run() {
            synchronized (lock) {
                sendMsg("Created and in lock");
                Looper.prepare();
                back_handler = new Handler();
                sendMsg("Leaving the lock");
            }
            sendMsg("Looper prepared and back_handler initialized, mThread is runing now");
            Looper.loop();
            sendMsg("Ended");
        }

    };

    private void sendMsg(String message) {
        Message msg = Message.obtain(); // Creates an new Message instance
        if (msg.what==MainActivity.MainHandler.STRING){
            msg.obj = Thread.currentThread().getName() + ": " + message; }

        msg.setTarget(ui_handler); // Set the Handler
        msg.sendToTarget(); //Send the message
    }
    private void sendMsg(Message msg){
        if(msg.what== MainActivity.MainHandler.CameraCharacteristics){
            ui_handler.sendMessage(msg);
        }
    }

    synchronized void start_mThread() {
        mThread.start();
    }

    void interrupt_mThread() {
        cameraDevice.close();
        try{
            back_handler.getLooper().quitSafely();
            if(!mThread.isAlive()){sendMsg(mThread.getName() + " is dead");}
        }catch (Error e){
            sendMsg(e.toString());
        }




    }

    private CameraDevice.StateCallback cameradevice_stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            sendMsg(cameraDevice.getId() + " Opened and initialized as camera");
            Message msg = Message.obtain();
            msg.what=MainActivity.MainHandler.CameraCharacteristics;
            try {
                msg.obj=cameraManager.getCameraCharacteristics(Integer.toString(pos));
            } catch (CameraAccessException e) {
                sendMsg(e.toString());
            }
            sendMsg(msg);
            create_captureReq_Builder();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            sendMsg("Disconnected :"+camera.getId() );
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            sendMsg("ERROR Code:"+(error)+", on camera id: "+camera.getId() );
            cameraDevice.close();
        }
    };

    /**
     *
     * @param pos camera no to open usually 0 or 1
     */
    void open_Camera(final int pos) {
        this.pos = pos;
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendMsg("Temporory syncronizer thread started simply to block open_Camera before mThread looper prepared");
                synchronized (lock){
                    sendMsg("Acquired lock sending runnable now");
                    back_handler.post(new Runnable() {
                        //Inside mThread
                        @Override
                        public void run() {
                            cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                            String[] ar_str = new String[0];
                            try {
                                ar_str = cameraManager.getCameraIdList();
                            } catch (CameraAccessException e) {
                                sendMsg(e.toString());
                            }
                            for (String str : ar_str) {
                                sendMsg("Camera id : " + str);
                                try{
                                    if(ar_str[pos]==str)
                                    cameraManager.openCamera(str, cameradevice_stateCallback, back_handler);}
                                catch (SecurityException | CameraAccessException e){
                                    sendMsg(e.toString());
                                }
                            }
                            sendMsg("(open Camera posting a msg to back handler, which hopefully exists by now ; then terminating");
                        }//Inside mThread end
                    });
                }
                sendMsg("Done with temporoy thread , killing now");
            }
        }).start();

    }
    void create_captureReq_Builder(){

            back_handler.post(()->{
                try {
                    capture_builder =cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    sendMsg("Capture buidler is initialized: "+capture_builder.toString());
                } catch (CameraAccessException e) {
                    sendMsg(e.toString());
                }
            });
    }

    public void addsurface_capture_builder(Surface surface) {
        back_handler.post(()->{
            capture_builder.addTarget(surface);
            sendMsg("Surface added to builder"+surface);
        });
    }
}