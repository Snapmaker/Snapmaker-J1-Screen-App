package fabscreen.features.print.print;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.helper.SoundUtil;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseFragment;

public class PrintCompleteLandFragment extends BaseFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SoundUtil.playSound(mApp.getSoundPool(),mApp.getSoundIdByResourceId(R.raw.sound_work_complete));
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_print_complete;
    }

    @OnClick(R2.id.btn_print_complete)
    void OnClickComplete() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeToHome().start(requireContext());
    }

    @OnClick(R2.id.btn_print_continue)
    void onClickContinue() {
        playNormalClickSound();
        requireActivity().finish();

    }
}

