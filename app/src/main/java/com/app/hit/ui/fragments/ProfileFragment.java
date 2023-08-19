package com.app.hit.ui.fragments;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.hit.R;
import com.app.hit.model.response.AddUserResponse;
import com.app.hit.model.response.User;
import com.app.hit.network.APIClient;
import com.app.hit.network.APIInterface;
import com.app.hit.ui.CameraActivity;
import com.app.hit.ui.ScanDevicesActivity;
import com.app.hit.ui.customviews.TextThumbSeekBar;
import com.app.hit.util.CommonUtils;
import com.app.hit.util.Prefs;
import com.app.hit.util.images.FileUtils;
import com.app.hit.util.images.ImageModel;
import com.app.hit.util.images.ImageUtilsCallback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.disposables.CompositeDisposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    MultipartBody.Part filePartCamera = null;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    // TODO: Rename and change types of parameters
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private String mParam1;
    private String mParam2;
    Button enterAgeBtn, enterWeightBtn, enterHeightBtn, entersportBtn, enterPositionBtn;
    TextView titleTxt, ageTxt, weightTxt, heightTxt, sportTxt, positionTxt, termsTxt, primaryTxt;
    String selectedValue;
    int selectedUnit;
    String[] WEIGHT_UNITS = {"lb", "kg"};
    String[] HEIGHT_UNITS = {"inches", "cms"};
    String[] SPORTS = {"Rugby(Union)", "Football", "American Football", "Rugby League", "Other"};
    String[] POSITIONS = {"Tighthead", "Prop", "Hooker", "Second Row", "Lock", "Halfback", "Five-Eighth", "Centre", "Wing", "Fullback"};
    private APIInterface apiInterface;
    ImageView uploadPhoto;
    TextView deviceIdTxt;
    Button saveProfileBtn;
    EditText nameEdit, emailEdit;
    CheckBox termsCheck, primaryCheck;
    ImageView back;
    TextThumbSeekBar thresholdSeekbar;

    public static final int IMAGE_CAPTURE = 1111;


    //for photo
    Uri picUri;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    private final static int ALL_PERMISSIONS_RESULT = 107;
    private final static int IMAGE_RESULT = 1;
    private String selectedImagePath;
    private String threshold = "";

    User user;
    boolean isEdit = false;


    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlayerListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.user = (User) getArguments().getSerializable("USER");
            isEdit = getArguments().getBoolean("IS_EDIT");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        enterAgeBtn = view.findViewById(R.id.enter_age_btn);
        enterWeightBtn = view.findViewById(R.id.enter_weight_btn);
        enterHeightBtn = view.findViewById(R.id.enter_height_btn);
        entersportBtn = view.findViewById(R.id.enter_sport_btn);
        enterPositionBtn = view.findViewById(R.id.enter_position_btn);
        ageTxt = view.findViewById(R.id.age_txt);
        weightTxt = view.findViewById(R.id.weight_txt);
        heightTxt = view.findViewById(R.id.height_txt);
        sportTxt = view.findViewById(R.id.sport_txt);
        positionTxt = view.findViewById(R.id.position_txt);
        uploadPhoto = view.findViewById(R.id.user_image_upload);
        deviceIdTxt = view.findViewById(R.id.device_id_txt);
        saveProfileBtn = view.findViewById(R.id.save_btn);
        nameEdit = view.findViewById(R.id.name_edit);
        emailEdit = view.findViewById(R.id.email_edit);
        termsCheck = view.findViewById(R.id.terms_check);
        back = view.findViewById(R.id.back);
        thresholdSeekbar = view.findViewById(R.id.max_threshold);
        primaryCheck = view.findViewById(R.id.primary_user_check);
        termsTxt = view.findViewById(R.id.position_txt);
        primaryTxt = view.findViewById(R.id.position_txt);
        titleTxt = view.findViewById(R.id.title);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Prefs.getInstance(getContext()).getString("IS_FIRST") == null) {
            primaryCheck.setChecked(true);
            primaryCheck.setEnabled(false);
        } else {
            if (Prefs.getInstance(getContext()).getString("IS_FIRST").equalsIgnoreCase("")) {
                primaryCheck.setChecked(true);
                primaryCheck.setEnabled(false);
            }
        }

        threshold = "" + thresholdSeekbar.getProgress();

        if (user != null) {
            init();
        }
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        enterAgeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show("1", "Select Age", false, 1, 100, null, 0, 0, null);
            }
        });
        enterWeightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show("1", "Select Weight", true, 1, 1000, null, 0, 1, WEIGHT_UNITS);
            }
        });
        enterHeightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show("1", "Select Height", true, 1, 1000, null, 0, 1, HEIGHT_UNITS);
            }
        });
        entersportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show(SPORTS[0], "Select Sport", false, 0, 4, SPORTS, 0, 0, null);
            }
        });
        enterPositionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show(POSITIONS[0], "Select Position", false, 0, 9, POSITIONS, 0, 0, null);
            }
        });

        uploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivityForResult(getPickImageChooserIntent(), IMAGE_RESULT);
                selectImage(getContext());
            }
        });

        deviceIdTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ScanDevicesActivity.class);
                startActivityForResult(intent, 3);
            }
        });

        thresholdSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold = "" + seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        saveProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameEdit.getText().toString().equalsIgnoreCase("")) {
                    CommonUtils.showAlert(getContext(), "Alert", "Please enter name", false);
                } else if (ageTxt.getText().toString().equalsIgnoreCase("")) {
                    CommonUtils.showAlert(getContext(), "Alert", "Please enter age", false);
                } else if (weightTxt.getText().toString().equalsIgnoreCase("")) {
                    CommonUtils.showAlert(getContext(), "Alert", "Please enter weight", false);
                } else if (heightTxt.getText().toString().equalsIgnoreCase("")) {
                    CommonUtils.showAlert(getContext(), "Alert", "Please enter height", false);
                } else if (sportTxt.getText().toString().equalsIgnoreCase("")) {
                    CommonUtils.showAlert(getContext(), "Alert", "Please enter sport", false);
                } else if (positionTxt.getText().toString().equalsIgnoreCase("")) {
                    CommonUtils.showAlert(getContext(), "Alert", "Please enter position", false);
                } else if (emailEdit.getText().toString().equalsIgnoreCase("")) {
                    CommonUtils.showAlert(getContext(), "Alert", "Please enter email", false);
                } else if (!emailEdit.getText().toString().trim().matches(emailPattern)) {
                    CommonUtils.showAlert(getContext(), "Alert", "Please enter correct email", false);
                } else if (termsCheck.isChecked() == false) {
                    CommonUtils.showAlert(getContext(), "Alert", "Please agree to terms and conditions", false);
                } else if (selectedImagePath == null || selectedImagePath.equalsIgnoreCase("")) {
                    CommonUtils.showAlert(getContext(), "Alert", "Please select profile picture", false);
                } else {
                    saveProfile(nameEdit.getText().toString(), ageTxt.getText().toString(), weightTxt.getText().toString(),
                            heightTxt.getText().toString(), sportTxt.getText().toString(), positionTxt.getText().toString(),
                            emailEdit.getText().toString().trim(), deviceIdTxt.getTag().toString(), "yes", selectedImagePath);
                }
            }
        });

        permissions.add(CAMERA);
        permissions.add(WRITE_EXTERNAL_STORAGE);
        permissions.add(READ_EXTERNAL_STORAGE);
        permissionsToRequest = findUnAskedPermissions(permissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
    }

    public void init() {
        String imageUrl = "https://app.hitrecognition.co.uk/storage/app/users/";
        if (user.getImage() != null) {
            if (!user.getImage().equalsIgnoreCase("")) {
                Picasso.get().load(imageUrl + user.getImage()).into(uploadPhoto);
            }
        }
        nameEdit.setText(user.getName());
        ageTxt.setText(user.getAge());
        weightTxt.setText(user.getWeight());
        heightTxt.setText(user.getHeight());
        sportTxt.setText(user.getSports());
        positionTxt.setText(user.getPosition());
        emailEdit.setText(user.getEmail());
        deviceIdTxt.setText(user.getDeviceName());
        deviceIdTxt.setTag(user.getDeviceId());
        double num = Double.parseDouble(user.getDevicedetail().equalsIgnoreCase("") ? "0" : user.getDevicedetail());
        thresholdSeekbar.setProgress((int) num);
        termsCheck.setChecked(true);
        primaryCheck.setChecked(true);
        if (!isEdit) {
            titleTxt.setText("View Profile");
            uploadPhoto.setEnabled(false);
            nameEdit.setEnabled(false);
            enterAgeBtn.setEnabled(false);
            enterWeightBtn.setEnabled(false);
            enterHeightBtn.setEnabled(false);
            entersportBtn.setEnabled(false);
            enterPositionBtn.setEnabled(false);
            emailEdit.setEnabled(false);
            deviceIdTxt.setEnabled(false);
            thresholdSeekbar.setEnabled(false);
            termsCheck.setEnabled(false);
            primaryCheck.setEnabled(false);
            saveProfileBtn.setVisibility(View.GONE);
        } else {
            titleTxt.setText("Edit Profile");
        }
    }

    public Intent getPickImageChooserIntent() {

        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getContext().getPackageManager();

        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = getContext().getPackageManager().queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getContext().getExternalFilesDir("");
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
        }
        return outputFileUri;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_CAPTURE) {

                try {
                    String result = data.getStringExtra("image");
                    Log.e("result", result + "");
                    Uri imageUri = Uri.parse(result);
                    uploadPhoto.setImageURI(imageUri);
                    selectedImagePath = result;
//                    String filePath = getImageFilePath(data);
//                    if (filePath != null) {
//                        Bitmap selectedImage = BitmapFactory.decodeFile(filePath);
//                        try {
//                            selectedImage = getRotateImage(filePath, selectedImage);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        selectedImagePath = filePath;
//                    }
                    compositeDisposable.add(
                            FileUtils.getInstant().convertUriToFile(getContext(), imageUri, getString(R.string.app_name), new ImageUtilsCallback() {
                                @Override
                                public void onSuccess(ImageModel imageModel) {
                                    filePartCamera = MultipartBody.Part.createFormData("image", imageModel.getFile().getName(), RequestBody.create(MediaType.parse("image/*"), imageModel.getFile()));
                                }

                                @Override
                                public void onFailure() {
                                    //DialogHelper.hideProgressLoader();
                                    //Toast.makeText(context, "Unable to get file", Toast.LENGTH_SHORT).show();
                                }
                            })
                    );


                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (requestCode == IMAGE_RESULT) {
                picUri = getPickImageResultUri(data);
                String filePath = getImageFilePath(data);
                if (filePath != null) {
                    Bitmap selectedImage = BitmapFactory.decodeFile(filePath);
                    try {
                        selectedImage = getRotateImage(filePath, selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    selectedImagePath = filePath;
                    uploadPhoto.setImageBitmap(selectedImage);
                }
            }

        } else if (resultCode == 3) {
            deviceIdTxt.setText(data.getStringExtra("SELECTED_DEVICE_NAME"));
            deviceIdTxt.setTag(data.getStringExtra("SELECTED_DEVICE_ID"));
        }

    }


    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }

        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }


    private String getImageFromFilePath(Intent data) {
        boolean isCamera = data == null || data.getData() == null;

        if (isCamera) return getCaptureImageOutputUri().getPath();
        else return getPathFromURI(data.getData());

    }

    public String getImageFilePath(Intent data) {
        return getImageFromFilePath(data);
    }

    private String getPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    public static Bitmap getRotateImage(String photoPath, Bitmap bitmap) throws IOException {
        ExifInterface ei = new ExifInterface(photoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }

        return rotatedBitmap;

    }


    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

//    private static Bitmap rotateImage(Bitmap img, int degree) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate(degree);
//        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
//        img.recycle();
//        return rotatedImg;
//    }


    private void selectImage(Context context) {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    Intent i = new Intent(getContext(), CameraActivity.class);
                    i.putExtra("facing", "back");
                    i.putExtra("sendRx", "false");
                    startActivityForResult(i, IMAGE_CAPTURE);
//                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                    startActivityForResult(takePicture, 0);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void show(String defaultValue, String titleStr, boolean isTwoValues, int minValueOne, int maxValueOne, String[] valuesOne, int minValueTwo, int maxValueTwo, String[] valuesTwo) {

        selectedValue = defaultValue;
        selectedUnit = 0;
        final Dialog d = new Dialog(getContext());
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.picker_dialog);

        TextView title = (TextView) d.findViewById(R.id.title);
        title.setText(titleStr);
        Button done = (Button) d.findViewById(R.id.done_button);
        Button cancel = (Button) d.findViewById(R.id.cancel_button);

        final NumberPicker pickerOne = (NumberPicker) d.findViewById(R.id.picker_one);
        final NumberPicker pickerTwo = (NumberPicker) d.findViewById(R.id.picker_two);
        pickerOne.setTag(titleStr);
        pickerOne.setMinValue(minValueOne);
        pickerOne.setMaxValue(maxValueOne);
        if (valuesOne != null)
            pickerOne.setDisplayedValues(valuesOne);

        if (isTwoValues) {
            pickerTwo.setVisibility(View.VISIBLE);
            pickerTwo.setMinValue(minValueTwo);
            pickerTwo.setMaxValue(maxValueTwo);
            if (valuesTwo != null)
                pickerTwo.setDisplayedValues(valuesTwo);
        } else {
            pickerTwo.setVisibility(View.GONE);
        }

        pickerOne.setWrapSelectorWheel(false);
        pickerOne.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                selectedValue = newVal + "";
            }
        });
        pickerTwo.setWrapSelectorWheel(false);
        pickerTwo.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                selectedUnit = newVal;
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pickerOne.getTag().toString().contains("Age")) {
                    ageTxt.setText(selectedValue + " Years");
                } else if (pickerOne.getTag().toString().contains("Weight")) {
                    weightTxt.setText(selectedValue + " " + WEIGHT_UNITS[selectedUnit]);
                } else if (pickerOne.getTag().toString().contains("Height")) {
                    heightTxt.setText(selectedValue + " " + HEIGHT_UNITS[selectedUnit]);
                } else if (pickerOne.getTag().toString().contains("Sport")) {
                    sportTxt.setText(selectedValue);
                } else if (pickerOne.getTag().toString().contains("Position")) {
                    positionTxt.setText(selectedValue);
                }
                d.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }


    public void saveProfile(String name, String age, String weight, String height, String sport, String position, String email,
                            String deviceId, String terms, String imagePath) {


        try {
            RequestBody requestName =
                    RequestBody.create(
                            MediaType.parse("text/plain"), name);
            RequestBody requestAge =
                    RequestBody.create(
                            MediaType.parse("text/plain"), age);
            RequestBody requestWeight =
                    RequestBody.create(
                            MediaType.parse("text/plain"), weight);
            RequestBody requestHeight =
                    RequestBody.create(
                            MediaType.parse("text/plain"), height);
            RequestBody requestSport =
                    RequestBody.create(
                            MediaType.parse("text/plain"), sport);
            RequestBody requestPosition =
                    RequestBody.create(
                            MediaType.parse("text/plain"), position);
            RequestBody requestEmail =
                    RequestBody.create(
                            MediaType.parse("text/plain"), email);
            RequestBody requestDeviceId =
                    RequestBody.create(
                            MediaType.parse("text/plain"), deviceId);
            String uniqueID = android.provider.Settings.Secure.getString(getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
//                String uniqueID = UUID.randomUUID().toString();

            RequestBody requestDeviceType =
                    RequestBody.create(
                            MediaType.parse("text/plain"), uniqueID);
            RequestBody requestTerms =
                    RequestBody.create(
                            MediaType.parse("text/plain"), terms);

            RequestBody requestDeviceDetail =
                    RequestBody.create(
                            MediaType.parse("text/plain"), threshold);

            RequestBody requestDeviceName =
                    RequestBody.create(
                            MediaType.parse("text/plain"), deviceIdTxt.getText().toString());

            MultipartBody.Part image = null;
            if (imagePath != null) {
                File file = new File(imagePath);
                // create RequestBody instance from file
                RequestBody requestFile =
                        RequestBody.create(MediaType.parse("image/*"), file);
                // MultipartBody.Part is used to send also the actual file name
                if (filePartCamera != null) {
                    image = filePartCamera;
                    filePartCamera = null;
                } else {
                    image = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
                }
                CommonUtils.showProgressBar(getContext());
                apiInterface = APIClient.getPostClient().create(APIInterface.class);

                if (!isEdit) {
                    Call<AddUserResponse> call = apiInterface.addUser(image, requestName, requestAge, requestWeight, requestHeight,
                            requestSport, requestPosition, requestEmail, requestDeviceId, requestDeviceType, requestDeviceName,
                            requestDeviceDetail, requestTerms);
                    call.enqueue(new Callback<AddUserResponse>() {
                        @Override
                        public void onResponse(Call<AddUserResponse> call, Response<AddUserResponse> response) {
                            CommonUtils.hideProgressBar();
                            Log.i("response", response.body() + "");
                            User user = response.body().getData().getData();
                            if (primaryCheck.isChecked())
                                Prefs.getInstance(getContext()).setString("IS_PRIMARY", user.getId());
                            Toast.makeText(getContext(), "User created successfully", Toast.LENGTH_LONG).show();
                            getFragmentManager().popBackStack();
                            Prefs.getInstance(getContext()).setString("IS_FIRST", "0");
                        }

                        @Override
                        public void onFailure(Call<AddUserResponse> call, Throwable t) {
                            CommonUtils.hideProgressBar();
                            call.cancel();
                        }
                    });
                } else {
                    Call<AddUserResponse> call = apiInterface.editUser(image, requestName, requestAge, requestWeight, requestHeight,
                            requestSport, requestPosition, requestEmail, requestDeviceId, requestDeviceType, requestDeviceName,
                            requestDeviceDetail, requestTerms);
                    call.enqueue(new Callback<AddUserResponse>() {
                        @Override
                        public void onResponse(Call<AddUserResponse> call, Response<AddUserResponse> response) {
                            CommonUtils.hideProgressBar();
                            Log.i("response", response.body() + "");
                            if (primaryCheck.isChecked())
                                Prefs.getInstance(getContext()).setString("IS_PRIMARY", user.getId());
                            Toast.makeText(getContext(), "User updated successfully", Toast.LENGTH_LONG).show();
                            getFragmentManager().popBackStack();
                        }

                        @Override
                        public void onFailure(Call<AddUserResponse> call, Throwable t) {
                            CommonUtils.hideProgressBar();
                            call.cancel();
                        }
                    });
                }
            } else {
                CommonUtils.showAlert(getContext(), "Alert", "Please select profile picture", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}