package fabscreen.features.settings.a400.update;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

@Route(path = RoutePath.A400_SETTINGS_UPDATE_MODULES)
public class UpdateModulesActivity extends BaseActivity {
    public static final String EM_PATH = "emPath";
    private UpdateModulesViewModel mViewModel;

    @BindView(R2.id.progress)
    CircularProgressIndicator mCpiProgress;
    @BindView(R2.id.tv_percent)
    TextView mTvPercent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel(UpdateModulesViewModel.class);
        setContentView(R.layout.fragment_a400_update_in_progress);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        String emPath = getIntent().getStringExtra("emPath");
        mViewModel.update(emPath);

        mViewModel.getProgressObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(progress -> {
                    mTvPercent.setText(progress);
                    mCpiProgress.setProgress(progress, true);
                });

        mViewModel.getUpdateResultObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::handleUpdateResult, LogHelper::log);

    }

    private void handleUpdateResult(Boolean success) {
        if (success) {
            mRouter.routeToUpdateSuccess(1, null).start(this);
        } else {
            DecisionDialog.create(this)
                    .setDialogStatus(1, true, false, false, false)
                    .setPic(R.drawable.ic_pic_a400_error_68x68)
                    .setContent("Module update fail, machine won't behave normal, please contact support.")
                    .setFirstTv("Ok", R.color.select_dialog_yellow_txt, (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }
}
