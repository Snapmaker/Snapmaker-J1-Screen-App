package fabscreen.features.settings.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import fabscreen.features.settings.R;
import fabscreen.platform.base.helper.DimensUtils;

public class ExperienceProgramDialogFragment extends DialogFragment {

    private ImageView mIvClose;
    private TextView mTVContent;
    private TextView mTvTitle;

    private int mTitleResID = -1;
    private int mContentResID = -1;
    private boolean mIsVisibility;

    public static ExperienceProgramDialogFragment newInstance(@StringRes int title, @StringRes int content, boolean isVisibility) {
        ExperienceProgramDialogFragment experienceFragment = new ExperienceProgramDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("title", title);
        bundle.putInt("content", content);
        bundle.putBoolean("isVisibility", isVisibility);
        experienceFragment.setArguments(bundle);
        return experienceFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_j1_experience_program, container, false);
        mIvClose = view.findViewById(R.id.iv_close);
        mTVContent = view.findViewById(R.id.tv_content);
        mTvTitle = view.findViewById(R.id.tv_title);
        view.findViewById(R.id.iv_close).setOnClickListener(v -> dismiss());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTitleResID = (int) getArguments().get("title");
        mContentResID = (int) getArguments().get("content");
        mIsVisibility = (Boolean) getArguments().get("isVisibility");

        mIvClose.setVisibility(mIsVisibility ? View.VISIBLE : View.GONE);
        mTvTitle.setText(mTitleResID);
        mTVContent.setText(mContentResID);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(fabscreen.platform.core.R.drawable.dialog_black_round_bg);
            getDialog().getWindow().setLayout((int) DimensUtils.dp2px(560), (int) DimensUtils.dp2px(280));
        }
    }
}
