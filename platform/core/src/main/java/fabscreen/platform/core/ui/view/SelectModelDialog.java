package fabscreen.platform.core.ui.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fabscreen.platform.core.R;

public class SelectModelDialog {

    private static SelectModelDialog sInstance;
    private AlertDialog mDialog;
    public RecyclerView mRv;
    public ImageView mIvClose;
    private TextView mTvTitle;
    public SelectModelAdapter mSelectModelAdapter;
    public List<SelectModelBean> mList;
    public OnItemClickListener mListener;

    public SelectModelDialog setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
        return this;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static SelectModelDialog create(Context context) {
        if (sInstance != null) {
            sInstance.mDialog.cancel();
            sInstance.mDialog.dismiss();
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog_Fullscreen);
        final Activity activity = (Activity) context;

        // Create new dialog and config
        AlertDialog dialog = builder.create();

        final LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_select_model, null);
        dialog.setView(view);

        sInstance = new SelectModelDialog();
        sInstance.mDialog = dialog;

        sInstance.mList = new ArrayList<>();
        sInstance.mSelectModelAdapter = new SelectModelAdapter(sInstance.mList, sInstance.mDialog.getContext());
        sInstance.mRv = view.findViewById(R.id.rv_dialog_select_model);
        sInstance.mRv.setAdapter(sInstance.mSelectModelAdapter);
        sInstance.mRv.setLayoutManager(new LinearLayoutManager(sInstance.mDialog.getContext()));

        sInstance.mIvClose = view.findViewById(R.id.tv_dialog_select_model_close);
        sInstance.mTvTitle = view.findViewById(R.id.tv_dialog_select_model_title);

        sInstance.mIvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        sInstance.mSelectModelAdapter.setOnItemClickListener(new SelectModelAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                sInstance.mSelectModelAdapter.selectPosition(position);
                if (sInstance.mListener != null) {
                    sInstance.mListener.onItemClick(position);
                }
            }
        });

        return sInstance;
    }

    public SelectModelDialog setData(List<SelectModelBean> list) {
        if (mList.size() > 0) {
            mList.clear();
        }
        mList.addAll(list);
        mSelectModelAdapter.notifyDataSetChanged();
        return this;
    }

    public SelectModelDialog setPosition(int position) {
        mSelectModelAdapter.selectPosition(position);
        return this;
    }

    public SelectModelDialog setTitle(String title) {
        mTvTitle.setText(title);
        return this;
    }

    public SelectModelDialog setTitle(@StringRes int titleId) {
        mTvTitle.setText(titleId);
        return this;
    }

    public void show() {
        mDialog.show();
    }

    public void dismiss() {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public boolean isShowing() {
        return mDialog.isShowing();
    }

}
