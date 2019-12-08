package com.example.camview1;

import android.graphics.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.Message;

class CameraService  {
    private Handler handler;
    private  Thread mThread;
    CameraService(Handler hnd){
        handler=hnd;
    }

    private Runnable mThread_runnable = new Runnable() {
        @Override
        public void run() {
            sendMsg("Created");

            sendMsg("Ended");
        }
    };
    private void sendMsg(String message){
        Message msg = Message.obtain(); // Creates an new Message instance
        msg.obj = Thread.currentThread().getName()+": "+message; // Put the string into Message, into "obj" field.
        msg.setTarget(handler); // Set the Handler
        msg.sendToTarget(); //Send the message
    }


    synchronized void start_mThread() {
        new Thread(mThread_runnable).start();
    }
    void interrupt_mThread(){
        mThread.interrupt();
    }
}
