package com.app.hit.util.images;

import android.graphics.Bitmap;

public class PostRXImage {
    //1 face
    // 2 back
    private int cameraMode;
    //private int orientation;
    //private PictureResult pictureResult;
    private Bitmap bitmap;

    public int getCameraMode() {
        return cameraMode;
    }

    public void setCameraMode(int cameraMode) {
        this.cameraMode = cameraMode;
    }

   /* public PictureResult getPictureResult() {
        return pictureResult;
    }

    public void setPictureResult(PictureResult pictureResult) {
        this.pictureResult = pictureResult;
    }*/

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    /*public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }*/
}
