package fabscreen.features.machinetools.cncassist.bit;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.presenter.CoordinateSystemPresenter;

public class CNCBitAssistantIntroFragment extends BaseFragment {
    private CoordinateSystemPresenter mCoordinateSystemPresenter;

    public static CNCBitAssistantIntroFragment newInstance() {
        return new CNCBitAssistantIntroFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.cnc_bit_assistant);

        mCoordinateSystemPresenter = new CoordinateSystemPresenter(disposables);
        mCoordinateSystemPresenter.ensureCoordinate(1);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_cnc_bit_assistant_intro;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_cnc_bit_assistant_intro_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((CNCBitAssistantActivity) getActivity()).gotoCNCBitAssistantSafetyGoggles();
        }
    }
}
