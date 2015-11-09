package it.jaschke.alexandria;

import it.jaschke.alexandria.camera.GraphicOverlay;

/**
 * Created by hojin on 15. 10. 29.
 */
abstract class TrackedGraphic<T> extends GraphicOverlay.Graphic {
    private int mId;

    TrackedGraphic(GraphicOverlay overlay) {
        super(overlay);
    }

    void setId(int id) {
        mId=id;
    }

    protected int getId(){
        return mId;
    }

    abstract void updateItem(T item);
}
