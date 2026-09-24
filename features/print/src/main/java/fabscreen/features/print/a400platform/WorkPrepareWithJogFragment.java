package fabscreen.features.print.a400platform;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.jogger.XYZJogFragment;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class WorkPrepareWithJogFragment extends BaseFragment {

    @BindView(R2.id.iv_main_pic)
    ImageView mIvMainPic;
    @BindView(R2.id.tv_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_desc)
    TextView mTvDesc;

    private BehaviorSubject<Boolean> mShouldEnableButtonsSubject = BehaviorSubject.create();

    public static Fragment newInstance(Bundle bundle) {
        Fragment fragment = new WorkPrepareWithJogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public Observable<Boolean> getButtonsEnableObservable() {
        return mShouldEnableButtonsSubject.hide();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_laser_z_jog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) return;
        int pic = bundle.getInt("pic");
        String title = bundle.getString("title");
        String desc = bundle.getString("desc");
        mTvTitle.setText(title);
        mTvDesc.setText(desc);

        Fragment fragment = XYZJogFragment.newInstance();
        watchForChildClick(fragment);
        getChildFragmentManager().beginTransaction().replace(R.id.fcv_jog, fragment).commit();
    }

    private void watchForChildClick(Fragment fragment) {
        if (fragment instanceof XYZJogFragment) {
            ((XYZJogFragment) fragment).getButtonsEnableObservable()
                    .as(bindToLifecycle())
                    .subscribe(shouldEnableButtons -> mShouldEnableButtonsSubject.onNext(shouldEnableButtons), LogHelper::log);
        }
    }
}
