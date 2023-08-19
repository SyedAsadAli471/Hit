package com.app.hit.util.images;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;

public class ImageModel {

    private Bitmap bitmap;
    private File file;
    private Uri uri;

    public ImageModel() {
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
