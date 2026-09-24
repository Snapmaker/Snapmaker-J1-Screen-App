package fabscreen.features.machinetools.setup.laser.tenw;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_SETUP_COMMON_INTRO)
public class SetupIntroActivity extends BaseActivity {
    @BindView(R2.id.tv_title)
    TextView mTvTitle;
    @BindView(R2.id.progress)
    LinearProgressIndicator mProgress;
    @BindView(R2.id.btn_close)
    Button mBtnClose;

    private Bundle mPageData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mProgress.setVisibility(View.GONE);
        mBtnClose.setVisibility(View.GONE);
        mPageData = getIntent().getBundleExtra("page_data");
        mTvTitle.setText(mPageData.getString("title"));
        addFragment(R.id.fcv_setup_content, SetupIntroFragment.newInstance(mPageData));
    }

    public void goToDestinationForResult() {
        mRouter.routeWithClassPath(mPageData.getString("router_destination")).startForResult(this, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode);
        finish();
    }
}
