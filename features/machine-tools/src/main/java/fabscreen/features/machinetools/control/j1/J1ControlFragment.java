package fabscreen.features.machinetools.control.j1;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.view.BaseFragment;

public class J1ControlFragment extends BaseFragment {
    public static Fragment newInstance() {
        return new J1ControlFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.all_control);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_control;
    }

    public void showControlDetail(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fcv_control_detail, fragment).commit();
    }
}
