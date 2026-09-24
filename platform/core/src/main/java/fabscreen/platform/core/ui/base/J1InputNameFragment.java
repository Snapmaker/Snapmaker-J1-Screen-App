package fabscreen.platform.core.ui.base;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;

public abstract class J1InputNameFragment extends BaseFragment {

    @BindView(R2.id.btn_next)
    Button mBtnNxt;
    @BindView(R2.id.tv_title)
    TextView mTvTitle;
    @BindView(R2.id.edit_j1_machine_name)
    public EditText editName;

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Override
    protected final int getLayoutResID() {
        return R.layout.fragment_j1_machine_name_input;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editName.setText(getName());
        mBtnNxt.setEnabled(!TextUtils.isEmpty(editName.getText()));
        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mBtnNxt.setEnabled(TextUtils.isEmpty(s.toString()) ? false : true);
            }
        });
    }

    @NonNull
    private String getName() {
        String savedName = getServiceContainer().getService(IPreferences.class).getHelper().getMachineName();
        if (TextUtils.isEmpty(savedName)) {
            return generateName();
        } else {
            return savedName;
        }
    }

    /**
     * Generate a random name like "J1A01"
     */
    @NonNull
    private String generateName() {
        return "J1"
                + LETTERS.charAt(new Random().nextInt(LETTERS.length()))
                + new Random().nextInt(10)
                + new Random().nextInt(10)
                + new Random().nextInt(10);
    }

    @Override
    protected final void setTitle(int resid) {
        mTvTitle.setText(resid);
    }

    @Override
    protected final void setTitle(String title) {
        mTvTitle.setText(title);
    }

    @OnClick(R2.id.btn_next)
    void onStartClicked() {
        playNormalClickSound();
        getServiceContainer().getService(IPreferences.class).getHelper().setMachineName(editName.getText().toString());
        onSaveClicked();
    }

    protected abstract void onSaveClicked();

    @OnClick(R2.id.top_bar_back)
    void onClickBack() {
        playNormalClickSound();
        back();
    }
}
