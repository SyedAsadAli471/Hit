package com.app.hit.util.images;

import static java.io.File.separator;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FileUtils {

    public static FileUtils mInstant;
    private Disposable disposable;


    public static FileUtils getInstant(){
        if(mInstant==null){
            mInstant = new FileUtils();
        }
        return mInstant;
    }

    public Disposable getDisposable() {
        return disposable;
    }

    public void setDisposable(Disposable disposable) {
        this.disposable = disposable;
    }

    public File saveImage(Bitmap bitmap, Context context, String folderName) {
        if (Build.VERSION.SDK_INT >= 29) {

            String fileName = System.currentTimeMillis() + ".jpg";
            File file = null;
            try {
                Uri uri = getBitmapUri(context,bitmap,fileName);
                Log.e("ZIA_ZIA_ZIA","getBitmapUri: "+uri);
                file = getFile(context,uri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return file;
        } else {
            File directory = new File(Environment.getExternalStorageDirectory().toString() + separator + folderName);
            // getExternalStorageDirectory is deprecated in API 29

            if (!directory.exists()) {
                directory.mkdirs();
            }
            String fileName = System.currentTimeMillis() + ".jpg";
            File file = new File(directory, fileName);
            try {
                saveImageToStream(bitmap, new FileOutputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            ///if (file.getAbsolutePath() != null) {
                //ContentValues values = contentValues();
                //values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                // .DATA is deprecated in API 29
                //context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            //}

            return file;
        }


    }



    private ContentValues contentValues(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        }
        return values;
    }

    private void saveImageToStream(Bitmap bitmap, OutputStream outputStream) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private Uri getBitmapUri(@NonNull final Context context, @NonNull final Bitmap bitmap,
                                    @NonNull final String displayName) throws IOException
    {
        final String relativeLocation = Environment.DIRECTORY_DCIM;

        final ContentValues  contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation);

        final ContentResolver resolver = context.getContentResolver();

        OutputStream stream = null;
        Uri uri = null;

        try
        {
            final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            uri = resolver.insert(contentUri, contentValues);

            if (uri == null)
            {
                throw new IOException("Failed to create new MediaStore record.");
            }

            stream = resolver.openOutputStream(uri);

            if (stream == null)
            {
                throw new IOException("Failed to get output stream.");
            }

            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
            {
                throw new IOException("Failed to save bitmap.");
            }
        }
        catch (IOException e)
        {
            if (uri != null)
            {
                // Don't leave an orphan entry in the MediaStore
                resolver.delete(uri, null, null);
            }

            throw e;
        }
        finally
        {
            if (stream != null)
            {
                stream.close();
            }
        }

        return uri;
    }


    public File getFile(Context context, Uri uri) {
        File destinationFilename = new File(context.getFilesDir().getPath() + File.separatorChar + queryName(context, uri));
        try (InputStream ins = context.getContentResolver().openInputStream(uri)) {
            createFileFromStream(ins, destinationFilename);
        } catch (Exception ex) {
            //Log.e("Save File", ex.getMessage());
            ex.printStackTrace();
        }
        return destinationFilename;
    }

    public void createFileFromStream(InputStream ins, File destination) {
        try (OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        } catch (Exception ex) {
            //Log.e("Save File", ex.printStackTrace());
            ex.printStackTrace();
        }
    }

    public String queryName(Context context, Uri uri) {
        Cursor returnCursor =
                context.getContentResolver().query(uri, null, null, null, null);
        //assert returnCursor != null;
        int nameIndex = 0;
        if (returnCursor != null) {
            nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String name = returnCursor.getString(nameIndex);
            returnCursor.close();
            return name;
        }
        else{
            //Toast.makeText(context,"Cursor null",Toast.LENGTH_SHORT).show();
        }

        return null;


    }



    private Bitmap compress(String imagePath){
        BitmapFactory.Options Options = new BitmapFactory.Options();
        //Options.inSampleSize = 4;
        Options.inSampleSize = 1;
        Options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, Options);
    }




    public  Bitmap getCompressedUri(String imagePath) {
        float maxHeight = 1920.0f;
        float maxWidth = 1080.0f;
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        float imgRatio = (float) actualWidth / (float) actualHeight;
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        //options.inPurgeable = true;
        //options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(imagePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);

        byte[] byteArray = out.toByteArray();

        Bitmap updatedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        return updatedBitmap;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }
        return inSampleSize;
    }



    public Disposable convertPdfUriToFile(Context context, Uri uri, String fileName, ImageUtilsCallback callback){
        final Disposable[] disposable = new Disposable[1];
        Observable.just(uri)
                .map(bitmap -> {
                    try {
                        ImageModel imageModel1 = new ImageModel();
                        File file  = FileUtils.getInstant().getFile(context,uri);
                        long imageSizeKb = file.length() / 1024;
                        Log.e("HIT_HIT_HIT","imageSizeKb: " + imageSizeKb);

                        imageModel1.setBitmap(null);
                        imageModel1.setUri(uri);
                        imageModel1.setFile(file);
                        return imageModel1;




                    }
                    catch (Exception e){
                        callback.onFailure();
                        return null;
                    }

                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())

                .subscribe(new Observer<ImageModel>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable[0] = d;
                    }

                    @Override
                    public void onNext(@NonNull ImageModel imageModel) {
                        callback.onSuccess(imageModel);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                        callback.onFailure();
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        //DialogHelper.hideProgressLoader();
                    }
                });


        return disposable[0];
    }



    public Disposable convertUriToFile(Context context, Uri uri, String fileName, ImageUtilsCallback callback){
        final Disposable[] disposable = new Disposable[1];
        Observable.just(uri)
                .map(bitmap -> {
                    try {
                        ImageModel imageModel1 = new ImageModel();
                        File file  = FileUtils.getInstant().getFile(context,uri);
                        long imageSizeKb = file.length() / 1024;
                        Log.e("ZIA_ZIA_ZIA","Before image Size in Kb: " + imageSizeKb);
                        if(imageSizeKb < 1024){

                            //Bitmap bmp = BitmapFactory.decodeFile(String.valueOf(uri));
                            Bitmap bmp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                            imageModel1.setBitmap(bmp);
                            imageModel1.setUri(uri);
                            imageModel1.setFile(FileUtils.getInstant().saveImage(bmp,context,fileName));
                            return imageModel1;
                        }


                        Bitmap bitmapCompressed = getCompressedUri(file.getAbsolutePath());
                        //Bitmap bitmapCompressed = compress(file.getAbsolutePath());
                        imageModel1.setBitmap(bitmapCompressed);
                        File compressedFile = FileUtils.getInstant().saveImage(bitmapCompressed,context,fileName);
                        long imageSizeKb2 = compressedFile.length() / 1024;
                        Log.e("ZIA_ZIA_ZIA","after image Size in Kb: " + imageSizeKb2);
                        imageModel1.setUri(Uri.parse(compressedFile.toString()));
                        imageModel1.setFile(compressedFile);
                        return imageModel1;


                    }
                    catch (Exception e){
                        Log.e("ZIA_ZIA_ZIA","Image Utils Exception: "+e.getMessage());
                        callback.onFailure();
                        return null;
                    }

                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())

                .subscribe(new Observer<ImageModel>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable[0] = d;
                    }

                    @Override
                    public void onNext(@NonNull ImageModel imageModel) {
                        callback.onSuccess(imageModel);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e("ZIA_ZIA_ZIA","onError: "+e.getMessage());
                        callback.onFailure();
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        //DialogHelper.hideProgressLoader();
                    }
                });


        return disposable[0];
    }



    public Disposable convertBitmapToFile(Context context, Bitmap bitmap, String fileName, ImageUtilsCallback callback){
        final Disposable[] disposable = new Disposable[1];
        Observable.just(bitmap)
                .map(uris -> {
                    try {
                        ImageModel imageModel1 = new ImageModel();
                        File file  = saveImage(bitmap,context,fileName);
                        long imageSizeKb = file.length() / 1024;
                        Log.e("ZIA_ZIA_ZIA","Before image Size in Kb: " + imageSizeKb);
                        if(imageSizeKb < 1024){
                            imageModel1.setBitmap(bitmap);
                            imageModel1.setFile(file);
                            imageModel1.setUri(Uri.parse(file.getAbsolutePath()));
                            return imageModel1;
                        }
                        Bitmap bitmapCompressed = getCompressedUri(file.getAbsolutePath());
                        //Bitmap bitmapCompressed = compress(file.getAbsolutePath());
                        imageModel1.setBitmap(bitmapCompressed);
                        File fileCompressed = FileUtils.getInstant().saveImage(bitmapCompressed,context,fileName);
                        long imageSizeKb2 = fileCompressed.length() / 1024;
                        Log.e("ZIA_ZIA_ZIA","after image Size in Kb: " + imageSizeKb2);
                        imageModel1.setFile(fileCompressed);
                        imageModel1.setUri(Uri.parse(fileCompressed.getAbsolutePath()));
                        return imageModel1;


                    }
                    catch (Exception e){
                        callback.onFailure();
                        return null;
                    }

                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())

                .subscribe(new Observer<ImageModel>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable[0] = d;
                    }

                    @Override
                    public void onNext(@NonNull ImageModel imageModel) {
                        callback.onSuccess(imageModel);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                        callback.onFailure();
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        //DialogHelper.hideProgressLoader();
                    }
                });

        return disposable[0];
    }



    public ImageModel createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        ImageModel imageModel = new ImageModel();
        imageModel.setBitmap(null);
        imageModel.setFile(image);
        imageModel.setUri(Uri.parse(image.getAbsolutePath()));

        return imageModel;
    }



}
