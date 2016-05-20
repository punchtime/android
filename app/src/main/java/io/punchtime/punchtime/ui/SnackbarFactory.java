package io.punchtime.punchtime.ui;

import android.content.Context;
//import android.os.Build;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;

import io.punchtime.punchtime.R;

/**
 * Created by elias on 24/04/16.
 * for project: Punchtime
 */
public class SnackbarFactory {

    //private static final boolean GET_THEME_COLOR_SUPPORT = Build.VERSION.SDK_INT >= 23;

    public static Snackbar createSnackbar(Context context, View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.setActionTextColor(context.getResources().getColor(R.color.colorAccent));
        ViewGroup group = (ViewGroup) snackbar.getView();
        group.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));

        return snackbar;
    }
}
