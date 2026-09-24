package fabscreen.platform.base.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.orhanobut.logger.Logger;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.AutoDisposeConverter;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.R;
import fabscreen.platform.base.R2;
import fabscreen.platform.base.helper.SoundUtil;
import fabscreen.platform.base.instantiation.IServiceContainer;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import io.reactivex.disposables.CompositeDisposable;


public abstract class BaseFragment extends Fragment {
    protected final CompositeDisposable disposables = new CompositeDisposable();
    protected IRouter mRouter;
    @Nullable
    @BindView(R2.id.top_bar_title)
    protected TextView mTvTopBarTitle;
    private View mRootView;
    protected IAppService mApp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("Route: Create " + getClass().getSimpleName());
        mApp = getServiceContainer().getService(IAppService.class);
        mRouter = getServiceContainer().getService(IRouter.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(getLayoutResID(), container, false);
            modifyViewBeforeBind(mRootView);
            ButterKnife.bind(this, mRootView);
        }
        return mRootView;
    }

    /**
     * Let child do some view inflating dynamically
     *
     * @param rootView
     */
    protected void modifyViewBeforeBind(View rootView) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    /**
     * Abstract fragment's layout resource ID, where subclasses doesn't need to inflate the view themselves.
     */
    protected abstract int getLayoutResID();

    @NonNull
    protected ViewModelProvider getViewModelProvider() {
        if (getActivity() != null) {
            return new ViewModelProvider(requireActivity());
        } else {
            throw new IllegalStateException("call getViewModelProvider() outside of lifecycle");
        }
    }

    protected ViewModelProvider getViewFragmentScopeViewModelProvider() {
        return new ViewModelProvider(this);
    }

    protected BaseViewModel getViewModel() {
        return null;
    }

    protected <T extends BaseViewModel> T getActivityScopeViewModel(Class<T> viewModelClass) {
        return new ViewModelProvider(requireActivity()).get(viewModelClass);
    }

    protected <T extends BaseViewModel> T getFragmentScopeViewModel(Class<T> viewModelClass) {
        return new ViewModelProvider(this).get(viewModelClass);
    }

    /**
     * Auto-disposing on
     */
    protected <T> AutoDisposeConverter<T> bindToLifecycle() {
        return AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY));
    }

    @Optional
    @OnClick(R2.id.top_bar_back)
    protected void back() {
        playNormalClickSound();
        if (getActivity() != null) {
            Logger.d("Route: Back from " + getClass().getSimpleName());
            getActivity().onBackPressed();
        }
    }

    @Optional
    @OnClick(R2.id.top_bar_info)
    protected void info() {
        playNormalClickSound();
        // Info button is hidden by default.
        // Implement button behavior based on the page.
    }

    protected void setTitle(CharSequence title) {
        if (mTvTopBarTitle != null) {
            mTvTopBarTitle.setText(title);
        }
    }

    protected void setTitle(String title) {
        if (mTvTopBarTitle != null) {
            mTvTopBarTitle.setText(title);
        }
    }

    protected void setTitle(int resid) {
        if (mTvTopBarTitle != null) {
            mTvTopBarTitle.setText(resid);
        }
    }

    @NonNull
    protected IServiceContainer getServiceContainer() {
        return ServiceContainer.getInstance();
    }

    protected FirebaseAnalytics getFirebaseAnalytics() {
        if (getActivity() != null) {
            return ((BaseActivity) getActivity()).getFirebaseAnalytics();
        } else {
            return null;
        }
    }

    /**
     * Navigate home and clear activities above home Activity.
     *
     * @param clazz the target home Activity class.
     *              Different apps may(not must) have different home Activities.
     */
    protected <T extends Activity> void backToHome(Class<T> clazz) {
        Logger.d("Route: Back to home.");
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, clazz);
            intent.putExtra(Constants.KEY_IS_FORCE_BACK_HOME, true);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
        }
    }

    protected void showDialog(Dialog dialog) {
        if (dialog == null || requireActivity().isFinishing()) return;
        dialog.show();
    }

    protected void dismissDialog(Dialog dialog) {
        if (dialog == null) return;
        dialog.dismiss();
    }

    protected void finishActivityWithResultOk() {
        requireActivity().setResult(Activity.RESULT_OK);
        requireActivity().finish();
    }

    protected void finishActivityWithResultOk(Intent extraData) {
        requireActivity().setResult(Activity.RESULT_OK, extraData);
        requireActivity().finish();
    }

    public void playNormalClickSound() {
        if (getServiceContainer().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().seriesId == IMachine.MachineSeries.A
                &&
                getServiceContainer().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().modelId == IMachine.MachineModel.A400
        ) {

            SoundUtil.playSound(mApp.getSoundPool(), mApp.getSoundIdByResourceId(R.raw.sound_click));
        }
    }

    public void playSwitchSound() {
        if (getServiceContainer().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().seriesId == IMachine.MachineSeries.A
                &&
                getServiceContainer().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().modelId == IMachine.MachineModel.A400
        ) {
            SoundUtil.playSound(mApp.getSoundPool(), mApp.getSoundIdByResourceId(R.raw.sound_switch));
        }
    }

    public void playProcedureCompleteSound() {
        if (getServiceContainer().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().seriesId == IMachine.MachineSeries.A
                &&
                getServiceContainer().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().modelId == IMachine.MachineModel.A400
        )
            SoundUtil.playSound(mApp.getSoundPool(), mApp.getSoundIdByResourceId(R.raw.sound_procedure_complete));
    }

    public void onClick(View view) {
        playNormalClickSound();
    }
}
