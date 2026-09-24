package fabscreen.features.settings.a350.factory;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import fabscreen.features.settings.R;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class FactoryColorDifferenceFragment extends BaseFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle("Color Difference");
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_factory_color_difference;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

}
