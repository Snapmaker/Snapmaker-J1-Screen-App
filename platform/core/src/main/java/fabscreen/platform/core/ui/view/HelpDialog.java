package fabscreen.platform.core.ui.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;

public class HelpDialog {

    @BindView(R2.id.rv)
    RecyclerView rv;
    @BindView(R2.id.close)
    ImageView close;
    private AlertDialog mDialog;
    private HelpAdapter mAdapter;

    HelpDialog(AlertDialog dialog, View view, List<HelpBean> helpList, Context context) {
        mDialog = dialog;
        ButterKnife.bind(this, view);
        mAdapter = new HelpAdapter(helpList, context);
        rv.setAdapter(mAdapter);
        rv.setLayoutManager(new LinearLayoutManager(context));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }

    /**
     * Display alert.
     */
    public static HelpDialog create(Context context, List<HelpBean> helpList) {
        // create dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, fabscreen.platform.core.R.style.DialogTheme);
        final Activity activity = (Activity) context;

        // Create new dialog and config
        AlertDialog dialog = builder.create();

        final LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_help, null);
        dialog.setView(view);

        return new HelpDialog(dialog, view, helpList, context);
    }

    public void show() {
        mDialog.show();
        Window window = mDialog.getWindow();
        window.setLayout(
                DimensUtils.dp2px(560, mDialog.getContext()),
                DimensUtils.dp2px(280, mDialog.getContext()));
    }

}
