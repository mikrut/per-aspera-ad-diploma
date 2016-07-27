package ru.mail.park.chat.activities.views;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Created by Михаил on 27.06.2016.
 */
public class EditTextDialogBuilder extends AlertDialog.Builder {
    private final EditText input;

    public EditTextDialogBuilder(Context context) {
        super(context);

        input = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        setView(input);
    }

    public EditText getInput() {
        return input;
    }
}
