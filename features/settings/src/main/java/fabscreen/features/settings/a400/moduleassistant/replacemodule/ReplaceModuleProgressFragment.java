package fabscreen.features.settings.a400.moduleassistant.replacemodule;

import android.app.AlertDialog;
import android.view.View;

import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.core.ui.base.BaseProgressFragment;

public abstract class ReplaceModuleProgressFragment extends BaseProgressFragment {
    @OnClick(R2.id.iv_close)
    @Override
    public void onClick(View view) {
        super.onClick(view);
        playNormalClickSound();
        if (view.getId() == R.id.iv_close) {
            new AlertDialog.Builder(requireContext())
                    .setMessage("Sure to quit?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        back();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                    })
                    .create()
                    .show();
        }
    }
}
