package it.jaschke.alexandria.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.vision.CameraSource;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by hojin on 15. 10. 28.
 */
public class GraphicOverlay extends View {
    private final Object mLockObject=new Object();
    private int mPreviewWidth;
    private int mPreviewHeight;
    private float mWidthScaleFactor=1.0f;
    private float mHeightScaleFactor=1.0f;
    private int mFacing=CameraSource.CAMERA_FACING_BACK;
    private Set<Graphic> mGraphics=new HashSet<>();

    public GraphicOverlay(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
    }

    public static abstract class Graphic{
        private GraphicOverlay mGraphicOverlay;
        public Graphic(GraphicOverlay overlay){
            mGraphicOverlay=overlay;
        }

        public abstract void draw(Canvas canvas);

        public float scaleX(float horizontal){
            return horizontal*mGraphicOverlay.mWidthScaleFactor;
        }

        public float scaleY(float vertical){
            return vertical*mGraphicOverlay.mHeightScaleFactor;
        }

        public float translateX(float x){
            if(mGraphicOverlay.mFacing==CameraSource.CAMERA_FACING_FRONT){
                return mGraphicOverlay.getWidth()-scaleX(x);
            }else{
                return scaleX(x);
            }
        }

        public float translateY(float y){
            return scaleY(y);
        }

        public void postInvalidate(){
            mGraphicOverlay.postInvalidate();
        }
    }

    public void clear(){
        synchronized (mLockObject){
            mGraphics.clear();
        }
        postInvalidate();
    }

    public void add(Graphic graphic){
        synchronized (mLockObject){
            mGraphics.add(graphic);
        }
        postInvalidate();
    }

    public void remove(Graphic graphic){
        synchronized (mLockObject){
            mGraphics.remove(graphic);
        }
        postInvalidate();
    }

    public void setCameraInfo(int previewWidth, int previewHeight, int facing){
        synchronized (mLockObject){
            mPreviewWidth=previewWidth;
            mPreviewHeight=previewHeight;
            mFacing=facing;
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (mLockObject){
            if((mPreviewWidth !=0) && (mPreviewHeight !=0)){
                mWidthScaleFactor=(float)canvas.getWidth()/(float)mPreviewWidth;
                mHeightScaleFactor=(float)canvas.getHeight()/(float)mPreviewHeight;
            }

            for(Graphic graphic : mGraphics){
                graphic.draw(canvas);
            }
        }
    }
}




























