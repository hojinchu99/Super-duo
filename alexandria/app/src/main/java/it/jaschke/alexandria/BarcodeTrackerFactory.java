package it.jaschke.alexandria;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

import it.jaschke.alexandria.camera.GraphicOverlay;

/**
 * Created by hojin on 15. 10. 29.
 */
class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {
    private GraphicOverlay mGraphicOverlay;
    private GraphicTracker.Callback mCallback;

    public BarcodeTrackerFactory(GraphicOverlay mGraphicOverlay, GraphicTracker.Callback mCallback) {
        this.mGraphicOverlay = mGraphicOverlay;
        this.mCallback = mCallback;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        BarcodeGraphic graphic= new BarcodeGraphic(mGraphicOverlay);
        return new GraphicTracker<>(mGraphicOverlay,graphic,mCallback);
    }
}

class BarcodeGraphic extends TrackedGraphic<Barcode>{
    private static final int COLOR_CHOICES[]={
            Color.BLUE,
            Color.CYAN,
            Color.GREEN
    };

    private static int mCurrentColorIndex=0;

    private Paint mRectPaint;
    private Paint mTextPaint;
    BarcodeGraphic(GraphicOverlay overlay){
        super(overlay);
        mCurrentColorIndex= (mCurrentColorIndex + 1)%COLOR_CHOICES.length;
        final int selectedColor=COLOR_CHOICES[mCurrentColorIndex];

        //////////여기서부터
        mRectPaint=new Paint();
        mRectPaint.setColor(selectedColor);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(4.0f);

        mTextPaint=new Paint();
        mTextPaint.setColor(selectedColor);
        mTextPaint.setTextSize(36.0f);



    }

    @Override
    public void draw(Canvas canvas) {
        Barcode barcode=mBarcode;
        if(barcode==null){
            return;
        }

        RectF rectF=new RectF(barcode.getBoundingBox());
        rectF.left=translateX(rectF.left);
        rectF.top=translateY(rectF.top);
        rectF.right=translateX(rectF.right);
        rectF.bottom=translateY(rectF.bottom);
        canvas.drawRect(rectF,mRectPaint);

        canvas.drawText(barcode.rawValue,rectF.left,rectF.bottom,mTextPaint);
    }

    private volatile Barcode mBarcode;
    @Override
    void updateItem(Barcode barcode) {
        mBarcode=barcode;
        postInvalidate();
    }

}



























