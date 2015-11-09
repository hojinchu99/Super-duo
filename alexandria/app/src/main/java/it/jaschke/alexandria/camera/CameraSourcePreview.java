package it.jaschke.alexandria.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.util.Log;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;
import java.lang.reflect.Field;


/**
 * Created by hojin on 15. 10. 27.
 */
public class CameraSourcePreview extends ViewGroup {
    private static final String TAG="CameraSourcePreview";

    private Context mContext;
    private SurfaceView mSurfaceView;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;

    private GraphicOverlay mOverlay;

    public CameraSourcePreview(Context context, AttributeSet attributeSet){
        super(context, attributeSet);

        mContext=context;
        mStartRequested=false;
        mSurfaceAvailable=false;

        mSurfaceView=new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(mSurfaceView);

    }

    public void start(CameraSource cameraSource) throws IOException{
        if(cameraSource ==null){
            stop();
        }

        mCameraSource=cameraSource;

        if(mCameraSource !=null){
            mStartRequested=true;
            startIfReady();
        }
    }

    ///여기서부터~~~
    public void start(CameraSource cameraSource, GraphicOverlay graphicOverlay) throws IOException{
        mOverlay=graphicOverlay;
        start(cameraSource);
    }


    private boolean stoopped;

    public void stop(){
        if(mCameraSource !=null){
            mCameraSource.stop();
        }
    }

    public void release(){
        if(mCameraSource !=null){
            mCameraSource.release();
            mCameraSource=null;
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback{

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceAvailable=true;
            try{
                startIfReady();
            }catch (IOException e){
                Log.e(TAG, "Could not start camera source.",e);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mSurfaceAvailable=false;
        }
    }

    private void startIfReady() throws IOException{
        if(mStartRequested && mSurfaceAvailable){
            mCameraSource.start(mSurfaceView.getHolder());
            cameraFocus(mCameraSource, Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            if(mOverlay !=null){
                Size size=mCameraSource.getPreviewSize();
                int min=Math.min(size.getWidth(), size.getHeight());
                int max=Math.max(size.getWidth(), size.getHeight());

                if(isPortraitMode()){
                    mOverlay.setCameraInfo(min,max,mCameraSource.getCameraFacing());
                }else{
                    mOverlay.setCameraInfo(max,min,mCameraSource.getCameraFacing());
                }
                mOverlay.clear();
            }
            mStartRequested=false;
        }
    }

    public static boolean cameraFocus( @NonNull CameraSource cameraSource, @NonNull String focusMode){

        Field[] declaredFields= CameraSource.class.getDeclaredFields();

        for(Field field:declaredFields){
            if(field.getType() == Camera.class){
                field.setAccessible(true);
                try {
                    Camera camera=(Camera) field.get(cameraSource);
                    if(camera !=null){
                        Camera.Parameters parameters=camera.getParameters();

                        if(!parameters.getSupportedFocusModes().contains(focusMode)){
                            return false;
                        }
                        parameters.setFocusMode(focusMode);
                        camera.setParameters(parameters);
                        return true;
                    }

                    return false;
                }catch (IllegalAccessException e){
                    e.printStackTrace();
                }
                break;
            }
        }
        return false;
    }

    private boolean isPortraitMode(){
        int orientation= mContext.getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            return false;
        }

        if(orientation == Configuration.ORIENTATION_PORTRAIT){
            return true;
        }

        Log.d(TAG, "isPortraitMode returning false by default");
        return false;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width=320;
        int height=240;
        if(mCameraSource !=null){
            Size size=mCameraSource.getPreviewSize();
            if(size !=null){
                width=size.getWidth();
                height=size.getHeight();
            }
        }

        if(isPortraitMode()){
            int temp=width;
            width=height;
            height=temp;
        }

        final int layoutWidth=right-left;
        final int layoutHeight=bottom-top;

        int childWidth=layoutWidth;
        int childHeight=(int)(((float)layoutWidth/(float)width)*height);

        if(childHeight>layoutHeight){
            childHeight=layoutHeight;
            childWidth=(int)((((float)layoutHeight/(float)height))*width);
        }

        for(int i=0; i<getChildCount();i++){
            getChildAt(i).layout(0,0,childWidth,childHeight);
        }

        try {
            startIfReady();
        }catch (IOException e){
            Log.e(TAG, "Could not start camera source.",e);
        }
    }


}



































