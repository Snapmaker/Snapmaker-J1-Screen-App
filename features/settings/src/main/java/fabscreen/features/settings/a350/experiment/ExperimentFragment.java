package fabscreen.features.settings.a350.experiment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import fabscreen.features.settings.R;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class ExperimentFragment extends BaseFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.all_experiment);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_experiment;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }
}
