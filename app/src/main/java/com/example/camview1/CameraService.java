package com.example.camview1;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Script;
import android.renderscript.ScriptC;
import android.renderscript.Type;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.TensorFlowLite;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class CameraService {
    //NET stuff
    private Handler client_handler;
    private Thread client_thread;
    private Socket socket ;
    DataInputStream in_stream;
    DataOutputStream outputStream;
    byte [] camera_data;
    byte [] server_msg = new byte[4000];
    //Cam stuff
    CameraManager cameraManager;
    CaptureRequest.Builder capture_builder;
    private int pos;
    private CaptureRequest capture_request;
    private List<Surface> surface_list;
    private Surface surface;
    private CameraCaptureSession captureSession;
    int onImageAvaible_executed=0;
    private CaptureRequest imageReader_capture_request;
    private Object lock2;
    RenderScript rs;
    private static  final String MODEL_PATH = "detect.tflite";



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
    private ImageReader imageReader;
    private  Surface imageSurface;
    private Activity activity;
    MappedByteBuffer mappedByteBuffer;
    Interpreter detector ;
    CameraService(Handler hnd, Context cnt, Activity activity) {
        ui_handler = hnd;
        mContext = cnt;
        this.activity=activity;
        mThread = new Thread(mThread_runnable);
        sendMsg("New Camera service object: "+this.toString());
        lock2=new Object();
        rs= RenderScript.create(mContext);
        try {
            mappedByteBuffer = loadModelFile(this.activity);
        } catch (IOException e) {
            sendMsg(e.toString());
        }
        detector=new Interpreter(mappedByteBuffer);

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

        ui_handler.sendMessage(msg);

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
                    //Set ui size 300x300
                    Message msg = Message.obtain();
                    msg.what = MainActivity.MainHandler.SET_300_SURFAC;
                    ui_handler.sendMessage(msg);
                    //autoset_surface_capture_builder
                    autoset_surface_capture_builder();

                } catch (CameraAccessException e) {
                    sendMsg(e.toString());
                }
            });
    }

    public void addsurface_capture_builder(Surface surface) {
        back_handler.post(()->{
            this.surface = surface;
            Canvas canvas = this.surface.lockCanvas(null);
            int width =canvas.getWidth();
            int height = canvas.getHeight();
            sendMsg("Adding surface with size wh:"+width+"x"+height);
            this.surface.unlockCanvasAndPost(canvas);
//            capture_builder.addTarget(surface);
//            sendMsg("Surface added to builder"+surface);

            imageReader = ImageReader.newInstance(width,height, ImageFormat.YUV_420_888,1);
            imageSurface = imageReader.getSurface();
            imageReader.setOnImageAvailableListener(onImageAvailableListener,back_handler);
            capture_builder.addTarget(imageSurface);
            surface_list = new ArrayList<>();
            surface_list.add(imageSurface);
//            surface_list.add(this.surface);
//            set_rotation_capture_builder(0);
            imageReader_capture_request = capture_builder.build();

        });
    }
    public void autoset_surface_capture_builder(){
        imageReader = ImageReader.newInstance(1088,1088, ImageFormat.YUV_420_888,1);
        imageSurface = imageReader.getSurface();
        imageReader.setOnImageAvailableListener(onImageAvailableListener,back_handler);
        capture_builder.addTarget(imageSurface);
        surface_list = new ArrayList<>();
        surface_list.add(imageSurface);
//            surface_list.add(this.surface);
//            set_rotation_capture_builder(0);
        imageReader_capture_request = capture_builder.build();
        buildRequest_CreateCapSes_thenStartRepeatingReq();
    }
    public void set_rotation_capture_builder(int r){
        back_handler.post(()->{
//            capture_builder.set(CaptureRequest.JPEG_ORIENTATION,r);
            sendMsg("set rotation for builder to:"+r);

        });

    }
    public void buildRequest_CreateCapSes_thenStartRepeatingReq(){
        if(captureSession==null) {
            back_handler.post(() -> {
                capture_request = capture_builder.build();
                sendMsg("Building capture session");
                try {
                    cameraDevice.createCaptureSession(surface_list, ses_StateCallback, back_handler);
                } catch (CameraAccessException e) {
                    sendMsg(e.toString());
                }
            });
        }else{
            try {
                captureSession.capture(imageReader_capture_request,ses_CaptureCallback,back_handler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
    //will be in back thread
    private CameraCaptureSession.StateCallback ses_StateCallback  = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            captureSession = session;
            sendMsg("captureSession state callback succesfull");
            sendMsg("Automaticly starting repating capture on configuration");
            try {
//                captureSession.setRepeatingRequest(capture_request,ses_CaptureCallback,null);
//                captureSession.capture(imageReader_capture_request,null,back_handler);
               captureSession.capture(imageReader_capture_request,ses_CaptureCallback,back_handler);
            } catch (CameraAccessException e) {
                sendMsg(e.toString());
            }

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            sendMsg("captureSession state callback failed");
            cameraDevice.close();
        }
    };
    private CameraCaptureSession.CaptureCallback ses_CaptureCallback = new CameraCaptureSession.CaptureCallback(){
        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Message msg = Message.obtain();
            msg.what=MainActivity.MainHandler.CAPTURE_CALLBACK_MSG;
            msg.obj=System.currentTimeMillis();
            msg.setTarget(ui_handler);
            sendMsg(msg);
            sendMsg(request+" :capture complete");
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);

        }

        @Override
        public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {
            sendMsg(request.toString() +" :this requests buffer is lost");
        }
    };
    private ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            long start_time= System.currentTimeMillis();
            sendMsg("Time start");
            Image im = reader.acquireLatestImage();
//            ByteBuffer buffer = im.getPlanes()[0].getBuffer();
//            byte[] bytes = new byte[buffer.capacity()];
//            buffer.get(bytes);
//            BitmapFactory.Options opt = new BitmapFactory.Options();
//            opt.inMutable = true;
//            Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);
//            sendMsg("JPEG TO Bitmap took :"+(System.currentTimeMillis()-start_time));

            //300x300 yap bmap
            Log.d(Thread.currentThread().getName(),"Image widthi ve heighti :"+im.getWidth()+" "+im.getHeight());
            Bitmap bitmapImage = YUV_420_888_toRGB(im,im.getWidth(),im.getHeight());
            bitmapImage = getResizedBitmap(bitmapImage,300,300);
            //Rotate  bmap
//            Matrix matrix = new Matrix();
//            matrix.postRotate(90);
//            bitmapImage =  Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, true);
              bitmapImage = rotate(bitmapImage);
            //Convert to byte array
//
            int size = bitmapImage.getRowBytes() * bitmapImage.getHeight();
            ByteBuffer byteBuffer = ByteBuffer.allocate(size);
            bitmapImage.copyPixelsToBuffer(byteBuffer);
            ByteBuffer byteBuffer2 = ByteBuffer.allocate(size*3/4);
            int bytebuffer2size = byteBuffer2.array().length;
            int j= 0;
            for(int i=0 ; i < (size*3/4) ; i++){
                if(i%3 == 0 & i!=0){
                    j++;
                    byteBuffer2.put(byteBuffer.array()[j]);

                }else{
                    byteBuffer2.put(byteBuffer.array()[j]);
                }
                j++;
            }
            /////////////////////////////////////////////////////////////////////////////////
            ByteBuffer imgData = ByteBuffer.allocate(270000);
            int[] intValues  = new int[90000];
            bitmapImage.getPixels(intValues, 0, bitmapImage.getWidth(), 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight());

            imgData.rewind();
            for (int i = 0; i < 300; ++i) {
                for ( j = 0; j < 300; ++j) {
                    int pixelValue = intValues[i * 300 + j];

                        imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                        imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                        imgData.put((byte) (pixelValue & 0xFF));

                }
            }
            ///////////////////////////////////////////////////////////////////////////////////////
//            ByteBuffer output_buffer = ByteBuffer.allocate(160);
//            float[][][] output_buffer  = new float[1][10][4];

            float[][][] t0 = new float[1][10][4]; //160 bytes
            float[][] t1 = new float[1][10];  // 40 bytes
            float[][] t2 = new float[1][10]; // 40 bytes
            float[] t3 = new float[1]; // 4 bytes
            Map<Integer, Object> hmap = new HashMap<>();
            hmap.clear();
            hmap.put(0,t0);
            hmap.put(1,t1);
            hmap.put(2,t2);
            hmap.put(3,t3);
            camera_data = byteBuffer2.array();

            Object[] bb = {imgData};

            Tensor tin = detector.getInputTensor(0);


            int[] shap  =tin.shape();

            sendMsg("BEFORE DETECTION numbytes: and shape "+ Integer.toString(tin.numBytes()));
            for(int i :shap){
                sendMsg(Integer.toString(i));
            }
            detector.runForMultipleInputsOutputs(bb, hmap);
            Log.d("LOOKHERE",Integer.toString(detector.getOutputTensor(0).numBytes() ));

            FloatBuffer outb = FloatBuffer.allocate(40);

             tin = detector.getInputTensor(0);
             shap  =tin.shape();
            sendMsg("After detct numbytes: and shape "+ Integer.toString(tin.numBytes()));
            for(int i :shap){
                sendMsg(Integer.toString(i));
            }
//            float location0=t0[0][0][0];
            float location0=t0[0][0][0];
            float location1=t0[0][0][1];
            float location2=t0[0][0][2];
            float location3=t0[0][0][3];
            float klass=t1[0][0];
            float prob=t2[0][0];
            float tnd=t3[0];


            sendMsg("DETECTED: "+Float.toString(klass)+".. prob: "+Float.toString(prob)+" ..in locations .. ["+
                    Float.toString(location0)+","+Float.toString(location1)+","+Float.toString(location2)+","
                    +Float.toString(location3)+"] "+"..TotalObjects found: "+Float.toString(tnd)+"..");

//            int in =detector.getOutputIndex("Locations");
//
//            byte[] outputarray = output_buffer.array();
//            float[] outputarray_asfloat = new float[40];
//            int j= 0 ;
//            for(int i=0 ; i <160 ;i=i+4){
//                int asInt = (outputarray[i+3] & 0xFF)
//                        | ((outputarray[i+2] & 0xFF) << 8)
//                        | ((outputarray[i+1] & 0xFF) << 16)
//                        | ((outputarray[i] & 0xFF) << 24);
//                outputarray_asfloat[j] = Float.intBitsToFloat(asInt);
//                sendMsg("output float array index:"+j+" )"+Float.toString(outputarray_asfloat[j]));
//                j++;
//            }

            Message msg = Message.obtain();
            msg.what = MainActivity.MainHandler.BITMAP_IMAGE;
            if (bitmapImage == null) {
                sendMsg("WHOOPS sending null bitmap");
            }
            msg.obj = bitmapImage;
            msg.setTarget(ui_handler);
            sendMsg("Locking back thread");
            synchronized (lock2) {
                try {
                    msg.sendToTarget();
                    sendMsg("send msg in lock waiting");
                    lock2.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                onImageAvaible_executed++;
                if(client_handler!=null){

                    sayhello_to_Sever();
                }

                sendMsg("unlocked ,Executed image reader times: " + onImageAvaible_executed);
                im.close();

            }
            sendMsg("WHOLE THING took :"+(System.currentTimeMillis()-start_time));
        }
    };
    public void unlock_camera_service(){
        synchronized (lock2){
            lock2.notify();
        }

    }
    public void close_captureSesssion(){
        back_handler.post(()->{
            sendMsg("Closing capture session ");
            try {
                captureSession.abortCaptures();
            } catch (CameraAccessException e) {
                sendMsg(e.toString());
            }
            captureSession.close();
        });

    }
    private Bitmap YUV_420_888_toRGB(Image image, int width, int height){
        // Get the three image planes
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        byte[] y = new byte[buffer.remaining()];
        buffer.get(y);

        buffer = planes[1].getBuffer();
        byte[] u = new byte[buffer.remaining()];
        buffer.get(u);

        buffer = planes[2].getBuffer();
        byte[] v = new byte[buffer.remaining()];
        buffer.get(v);

        // get the relevant RowStrides and PixelStrides
        // (we know from documentation that PixelStride is 1 for y)
        int yRowStride= planes[0].getRowStride();
        int uvRowStride= planes[1].getRowStride();  // we know from   documentation that RowStride is the same for u and v.
        int uvPixelStride= planes[1].getPixelStride();  // we know from   documentation that PixelStride is the same for u and v.


        // rs creation just for demo. Create rs just once in onCreate and use it again.

        //RenderScript rs = MainActivity.rs;
        ScriptC_yuv420888 mYuv420=new ScriptC_yuv420888 (rs);

        // Y,U,V are defined as global allocations, the out-Allocation is the Bitmap.
        // Note also that uAlloc and vAlloc are 1-dimensional while yAlloc is 2-dimensional.
        Type.Builder typeUcharY = new Type.Builder(rs, Element.U8(rs));
        typeUcharY.setX(yRowStride).setY(height);
        Allocation yAlloc = Allocation.createTyped(rs, typeUcharY.create());
        yAlloc.copyFrom(y);
        mYuv420.set_ypsIn(yAlloc);

        Type.Builder typeUcharUV = new Type.Builder(rs, Element.U8(rs));
        // note that the size of the u's and v's are as follows:
        //      (  (width/2)*PixelStride + padding  ) * (height/2)
        // =    (RowStride                          ) * (height/2)
        // but I noted that on the S7 it is 1 less...
        typeUcharUV.setX(u.length);
        Allocation uAlloc = Allocation.createTyped(rs, typeUcharUV.create());
        uAlloc.copyFrom(u);
        mYuv420.set_uIn(uAlloc);

        Allocation vAlloc = Allocation.createTyped(rs, typeUcharUV.create());
        vAlloc.copyFrom(v);
        mYuv420.set_vIn(vAlloc);

        // handover parameters
        mYuv420.set_picWidth(width);
        mYuv420.set_uvRowStride (uvRowStride);
        mYuv420.set_uvPixelStride (uvPixelStride);

        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Allocation outAlloc = Allocation.createFromBitmap(rs, outBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

        Script.LaunchOptions lo = new Script.LaunchOptions();
        lo.setX(0, width);  // by this we ignore the y’s padding zone, i.e. the right side of x between width and yRowStride
        lo.setY(0, height);

        mYuv420.forEach_doConvert(outAlloc,lo);
        outAlloc.copyTo(outBitmap);

        return outBitmap;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
    public MappedByteBuffer loadModelFile(Activity act) throws IOException{
        AssetFileDescriptor fileDescriptor = act.getAssets().openFd(MODEL_PATH);
        FileInputStream fileInputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset= fileDescriptor.getStartOffset();
        long declaredLength= fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
    }
    public Bitmap rotate(Bitmap bitmap) {

        ScriptC_rotator script = new ScriptC_rotator(rs);
        script.set_inWidth(bitmap.getWidth());
        script.set_inHeight(bitmap.getHeight());
        Allocation sourceAllocation = Allocation.createFromBitmap(rs, bitmap,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
        bitmap.recycle();
        script.set_inImage(sourceAllocation);

        int targetHeight = bitmap.getWidth();
        int targetWidth = bitmap.getHeight();
        Bitmap.Config config = bitmap.getConfig();
        Bitmap target = Bitmap.createBitmap(targetWidth, targetHeight, config);
        final Allocation targetAllocation = Allocation.createFromBitmap(rs, target,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
        script.forEach_rotate_270_clockwise(targetAllocation, targetAllocation);
        targetAllocation.copyTo(target);
        rs.destroy();
        return target;
    }
    private Runnable client_runnable = new Runnable() {
        @Override
        public void run() {

            Thread.currentThread().setName("ClientThread");
            sendMsg("Creating looper");
            Looper.prepare();
            client_handler = new Handler();
            sendMsg("Looper prepared and client_handler initialized, client_thread is runing now");
            Looper.loop();
            sendMsg("Ended");
        }

    };
    void connect_to_Server(String addr , int port ){
        if(client_thread == null){
            back_handler.post(()->{

                client_thread = new Thread(client_runnable);
                client_thread.start();

            });
        }
        if(client_handler==null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(socket==null||socket.isConnected()==false ) {
            client_handler.post(() -> {
                try {
                    socket = new Socket(addr, port);
                    sendMsg("Connected to " + socket.getRemoteSocketAddress());
                    outputStream = new DataOutputStream(socket.getOutputStream());
                    in_stream = new DataInputStream(socket.getInputStream());

                    byte[] Hello = "hello".getBytes();
                    outputStream.write(Hello);
                    sendMsg("Sent hello to server ");
                    outputStream.flush();
                    wait_response();
                } catch (IOException e) {
                    sendMsg(e.toString());
                }

            });
        }
    }
    void sayhello_to_Sever(){

        client_handler.post(()->{
            try {

                outputStream.write(camera_data);
                sendMsg("Sent data to server ");
                outputStream.flush();
                wait_response();
            }catch (IOException e){
                sendMsg(e.toString());
            }
        });
    }
    void wait_response() {
        client_handler.post(() -> {
            try {
                Thread.sleep(75);
                int avb = in_stream.available();
                in_stream.read(server_msg);
                sendMsg(new String(server_msg));
            } catch (Exception e) {
                sendMsg(e.toString());
            }

        });
    }
}