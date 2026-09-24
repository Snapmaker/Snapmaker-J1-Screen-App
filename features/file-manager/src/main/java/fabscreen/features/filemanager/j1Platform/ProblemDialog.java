package fabscreen.features.filemanager.j1Platform;

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
import fabscreen.features.filemanager.R;
import fabscreen.features.filemanager.R2;
import fabscreen.platform.base.helper.DimensUtils;

public class ProblemDialog {

    @BindView(R2.id.rv)
    RecyclerView rv;
    @BindView(R2.id.close)
    ImageView close;
    private List<Integer> mList;
    private ProblemRvAdapter mAdapter;

    private AlertDialog mDialog;

    ProblemDialog(AlertDialog dialog, View view) {
        mDialog = dialog;
        ButterKnife.bind(this, view);

        mList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            mList.add(i);
        }
        mAdapter = new ProblemRvAdapter(mList, dialog.getContext());
        rv.setLayoutManager(new LinearLayoutManager(mDialog.getContext()));
        rv.setAdapter(mAdapter);
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
    public static ProblemDialog create(Context context) {
        // create dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, fabscreen.platform.core.R.style.DialogTheme);
        final Activity activity = (Activity) context;

        // Create new dialog and config
        AlertDialog dialog = builder.create();

        final LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_problem, null);
        dialog.setView(view);

        return new ProblemDialog(dialog, view);
    }


    public void show() {
        mDialog.show();
        Window window = mDialog.getWindow();
        window.setLayout(
                DimensUtils.dp2px(560, mDialog.getContext()),
                DimensUtils.dp2px(280, mDialog.getContext()));
    }

}

