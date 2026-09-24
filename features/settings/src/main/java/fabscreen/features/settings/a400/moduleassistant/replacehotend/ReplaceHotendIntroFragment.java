package fabscreen.features.settings.a400.moduleassistant.replacehotend;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fabscreen.features.settings.R;
import fabscreen.platform.base.view.BaseFragment;

public class ReplaceHotendIntroFragment extends BaseFragment {

    public static Fragment newInstance() {
        return new ReplaceHotendIntroFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_replace_hotend_intro;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle("Replace Hot End");
        Bundle bundle = new Bundle();
        bundle.putInt(CommonIntroFragment.KEY_OPERATION, CommonIntroFragment.INTRO);
        getChildFragmentManager().beginTransaction().replace(R.id.fcv_replace_intro, CommonIntroFragment.class, bundle).commit();
    }
}
