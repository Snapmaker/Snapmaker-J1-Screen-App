package fabscreen.platform.core.ui.view.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.orhanobut.logger.Logger;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.helper.SoundUtil;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class RemoveGlassPlateDialogFragment extends DialogFragment {
    @BindView(R2.id.tv_content)
    TextView mTvContent;
    @BindView(R2.id.iv_heated_bed)
    ImageView mIvHeatedBed;
    @BindView(R2.id.tv_temperature)
    TextView mTvTemperature;
    private IAppService mAppService;

    private OnclickListener mListener;
    private RemoveGlassPlateViewModel mViewModel;

    public static RemoveGlassPlateDialogFragment newInstance(int initTemperature) {
        RemoveGlassPlateDialogFragment fragment = new RemoveGlassPlateDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("initTemp", initTemperature);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAppService = ServiceContainer.getInstance().getService(IAppService.class);
        View view = inflater.inflate(R.layout.dialog_remove_glass_plate, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    /**
     * https://stackoverflow.com/questions/12478520/how-to-set-dialogfragments-width-and-height
     */
    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_black_round_bg);
            getDialog().getWindow().setLayout((int) DimensUtils.dp2px(400), (int) DimensUtils.dp2px(213));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(RemoveGlassPlateViewModel.class);
        int initTemp = requireArguments().getInt("initTemp");
        if (initTemp >= 40) {
            observeBedTemp();
            mIvHeatedBed.setVisibility(View.VISIBLE);
            mTvTemperature.setVisibility(View.VISIBLE);
            mTvContent.setText("After the heated bed cools down under 40℃, please remove the PEI glass plate.");
        } else {
            mIvHeatedBed.setVisibility(View.INVISIBLE);
            mTvTemperature.setVisibility(View.INVISIBLE);
            mTvContent.setText("Later, the machine will start to heat the bed and nozzles.");
        }
    }

    private void observeBedTemp() {
        mViewModel.getBedTempObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(this::refreshBedTemperature, LogHelper::log);
    }

    private void refreshBedTemperature(int temp) {
        mTvTemperature.setText(temp + "℃");
    }

    @OnClick(R2.id.tv_confirm)
    void onConfirmClicked() {
        SoundUtil.playSound(mAppService.getSoundPool(), mAppService.getSoundIdByResourceId(fabscreen.platform.base.R.raw.sound_click));
        if (mListener != null) {
            mListener.onClickConfirm();
        }
        dismiss();
    }

    public RemoveGlassPlateDialogFragment setOnClickListener(OnclickListener listener) {
        mListener = listener;
        return this;
    }

    public interface OnclickListener {
        void onClickConfirm();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("dialog fragment on destroy");
    }
}
