package com.example.clint_test0;
// A Java program for a Client
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.net.*;
import java.io.*;

public class Client {
    // initialize socket and input output streams
    private Socket socket = null;
    private DataInputStream input = null;
    private DataOutputStream out = null;
    private DataInputStream input_from_server ;
    private byte[] buffered_from_server= new byte[20];
    byte[] bufferArrayForOutput = new byte[20];
    Handler uihandler;
    // constructor to put ip address port and input stream
    public Client(String address, int port,ByteArrayInputStream inpstr,Handler hnd) {
        uihandler= hnd ;

        // establish a connection
        new Thread(()-> {
            try {
                socket = new Socket(address, port);
                System.out.println("Connected");
                // takes input from inpstr
                input = new DataInputStream(inpstr);
                // gets sockets output stream
                out = new DataOutputStream(socket.getOutputStream());
                input_from_server = new DataInputStream(socket.getInputStream());
            } catch (UnknownHostException u) {
                System.out.println(u);
            } catch (IOException i) {
                System.out.println(i);
            }
            // Read from input stream , write to sockets output stream
            //TODO
            //Listen the program when a new input stream request is sent do read/write again
            try {
                input.read(bufferArrayForOutput);
                out.write(bufferArrayForOutput);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //try to read the response

            try {
                Thread.sleep(100);
                Log.d(Thread.currentThread().getName(),"Avaible msg "+input_from_server.available());
                input_from_server.read(buffered_from_server);
                sendRunRead();
                bufferArrayForOutput[0] = 48 ;
                out.write(bufferArrayForOutput);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //CLOSE CONNECTION
            System.out.println(buffered_from_server[0]);
            try {
                input.close(); // Close incoming input stream
                out.close(); // close socket output stream
                socket.close(); // close connection
            } catch (IOException i) {
                System.out.println(i);
            }
        }).start(); /// Stard the new thread
    }
    public byte[] getByteArrayFromSERVER(){
        return buffered_from_server;
    };
    public void sendRunRead(){
        Message msg = Message.obtain();
        msg.what = MainActivity.MSG_READ0;
        msg.arg1 = buffered_from_server[0];
        uihandler.sendMessage(msg);
    }
}
