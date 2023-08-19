package com.app.hit.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.hit.R;
import com.app.hit.util.images.FileUtils;
import com.app.hit.util.images.ImageModel;
import com.app.hit.util.images.ImageUtilsCallback;
import com.app.hit.util.images.PostRXImage;
import com.app.hit.util.images.RxBus;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Mode;

import org.jetbrains.annotations.NotNull;

public class CameraActivity extends AppCompatActivity {

    private CameraView camera;
    //private ImageView image;
    private ImageView imageView;
    private FloatingActionButton ivSwitchCamera;
    private FloatingActionButton photoButton;
    private FloatingActionButton flash;
    private boolean sendRx = true;
    private ProgressBar progressBar;
    FrameLayout cameraLayout, previewLayout;
    Button saveBtn,retakeBtn,cancelBtn;

    PostRXImage postRXImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

//        if (prefHelper.getLanguage().equals("ar")) {
//            LocaleHelper.setLocale(this, "ar");
//        } else {
//            LocaleHelper.setLocale(this, "en");
//        }

        cameraLayout = findViewById(R.id.camera_layout);
        previewLayout = findViewById(R.id.preview_layout);

        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progress);
        ivSwitchCamera = findViewById(R.id.ivSwitchCamera);
        saveBtn = findViewById(R.id.btnSave);
        retakeBtn = findViewById(R.id.btnRetake);
        cancelBtn = findViewById(R.id.btnCancel);
        flash = findViewById(R.id.flash);
        camera = findViewById(R.id.camera1);
        camera.setLifecycleOwner(this);
        camera.setMode(Mode.PICTURE);
        camera.setUseDeviceOrientation(true);

        Intent intent = getIntent();
        if(intent.hasExtra("sendRx")){
            sendRx = false;
        }

        if(intent.hasExtra("facing")){
            if(intent.getStringExtra("facing").equals("front")){
                camera.setFacing(Facing.FRONT);
            }
            else{
                camera.setFacing(Facing.BACK);
            }
        }
        else{
            camera.setFacing(Facing.BACK);
        }

        camera.setFlash(Flash.OFF);
        flash.setImageResource(R.drawable.flash_of);

        //image = findViewById(R.id.imageView);

        photoButton = findViewById(R.id.photoButton);
        photoButton.setOnClickListener(photoOnClickListener);




        ivSwitchCamera.setOnClickListener(v -> {
            if(camera.getFacing() == Facing.FRONT){
                camera.setFacing(Facing.BACK);
            }
            else{
                camera.setFacing(Facing.FRONT);
            }
        });

        flash.setOnClickListener(v -> {
            if(camera.getFlash() == Flash.ON){
                camera.setFlash(Flash.OFF);
                flash.setImageResource(R.drawable.flash_of);
            }
            else{
                camera.setFlash(Flash.ON);
                flash.setImageResource(R.drawable.flash_on);
            }
        });

        saveBtn.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            Bitmap bm=((BitmapDrawable)imageView.getDrawable()).getBitmap();
            FileUtils.getInstant().convertBitmapToFile(this, bm, getString(R.string.app_name), new ImageUtilsCallback() {
                @Override
                public void onSuccess(ImageModel imageModel) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("image", ""+ Uri.fromFile(imageModel.getFile()));
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }

                @Override
                public void onFailure() {
                    camera.setVisibility(View.VISIBLE);
                    cameraLayout.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CameraActivity.this, "Unable to capture image.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        retakeBtn.setOnClickListener(v -> {
            camera.setVisibility(View.VISIBLE);
            cameraLayout.setVisibility(View.VISIBLE);
            previewLayout.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        });

        cancelBtn.setOnClickListener(v -> {
            finish();
        });


        camera.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NotNull PictureResult result) {
                postRXImage = new PostRXImage();

                Bitmap bitmap = BitmapFactory.decodeByteArray(result.getData() , 0, result.getData().length);
                /*ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
                Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));*/


                int orientation = result.getRotation();

                Matrix matrix = new Matrix();

                if (orientation == 90) {
                    matrix.postRotate(90);
                }
                else if (orientation == 180) {
                    matrix.postRotate(180);
                }
                else if (orientation == 270) {
                    matrix.postRotate(270);
                }

                if(camera.getFacing() == Facing.FRONT){
                    matrix.postScale(-1, 1);
                }

                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap

                int nh = (int) ( bitmap.getHeight() * (1024.0 / bitmap.getWidth()) );
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 1024, nh, true);

                postRXImage.setBitmap(scaled);
                finishAct(postRXImage);
            }
        });
    }


    private void finishAct(PostRXImage postRXImage){
        if(sendRx){
            RxBus.getInstance().send(postRXImage);
            finish();
        }
        else{
            camera.setVisibility(View.GONE);
//            progressBar.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
//            Picasso.get().load(postRXImage.getBitmap()).into(imageView);
            imageView.setImageBitmap(postRXImage.getBitmap());
            cameraLayout.setVisibility(View.GONE);
            previewLayout.setVisibility(View.VISIBLE);
//            FileUtils.getInstant().convertBitmapToFile(this, postRXImage.getBitmap(), getString(R.string.app_name), new ImageUtilsCallback() {
//                @Override
//                public void onSuccess(ImageModel imageModel) {
//                    Intent returnIntent = new Intent();
//                    returnIntent.putExtra("image", ""+ Uri.fromFile(imageModel.getFile()));
//                    setResult(Activity.RESULT_OK,returnIntent);
//                    finish();
//                }
//
//                @Override
//                public void onFailure() {
//                    camera.setVisibility(View.VISIBLE);
//                    imageView.setVisibility(View.GONE);
//                    progressBar.setVisibility(View.GONE);
//                    Toast.makeText(CameraActivity.this, "Unable to capture image.", Toast.LENGTH_SHORT).show();
//                }
//            });
        }


    }



    private final View.OnClickListener photoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            camera.takePicture();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        camera.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.destroy();
    }



}