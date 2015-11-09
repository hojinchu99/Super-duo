package it.jaschke.alexandria;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

import it.jaschke.alexandria.camera.GraphicOverlay;

/**
 * Created by hojin on 15. 10. 29.
 */
public class GraphicTracker<T> extends Tracker<T> {
    private GraphicOverlay mOverlay;
    private TrackedGraphic<T> mGraphic;
    private Callback mCallback;

    public GraphicTracker(GraphicOverlay mOverlay, TrackedGraphic<T> mGraphic, Callback mCallback) {
        this.mOverlay = mOverlay;
        this.mGraphic = mGraphic;
        this.mCallback = mCallback;
    }

    public interface Callback{
        void onFound(String barcodeValue);
    }

    @Override
    public void onUpdate(Detector.Detections<T> detections, T item) {

        mCallback.onFound(((Barcode) item).rawValue);
        mOverlay.add(mGraphic);;
        mGraphic.updateItem(item);;
    }

    @Override
    public void onMissing(Detector.Detections<T> detections) {
        mOverlay.remove(mGraphic);
    }

    @Override
    public void onDone() {
        mOverlay.remove(mGraphic);
    }

    @Override
    public void onNewItem(int id, T item) {
        mGraphic.setId(id);
    }
}

































