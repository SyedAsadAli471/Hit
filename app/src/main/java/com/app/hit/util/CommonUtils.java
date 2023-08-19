package com.app.hit.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;

import androidx.appcompat.app.AlertDialog;

import com.app.hit.R;
import com.wang.avi.AVLoadingIndicatorView;

public class CommonUtils {

    static AVLoadingIndicatorView loaderView;
    private static Dialog dialog;

    public static void showAlert(Context context, String title, String msg, boolean showTwoButton) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);

        if (showTwoButton) {
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        } else {
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }
        alertDialog.show();
    }

    public static void showProgressBar(Context context) {

        if (dialog == null) {
            dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_progress_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            loaderView = (AVLoadingIndicatorView) dialog.findViewById(R.id.progress_bar);
            loaderView.show();
            dialog.show();
        } else {
            if (!dialog.isShowing()) {
                dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_progress_dialog);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                loaderView = (AVLoadingIndicatorView) dialog.findViewById(R.id.progress_bar);
                loaderView.show();
                dialog.show();
            }
        }


    }

    public static void hideProgressBar() {


        if (loaderView != null && dialog != null) {
            loaderView.hide();
            dialog.dismiss();
        }
    }
}
