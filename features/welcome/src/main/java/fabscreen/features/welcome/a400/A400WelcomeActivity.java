package fabscreen.features.welcome.a400;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentContainerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import fabscreen.features.welcome.R;
import fabscreen.features.welcome.R2;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.core.ui.common.WelcomeWifiPasswordFragment;
import fabscreen.platform.core.ui.view.VideoPlayerIJK;
import fabscreen.platform.core.ui.view.VideoPlayerListener;
import fabscreen.platform.lib.LogHelper;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

@Route(path = RoutePath.WELCOME_A400)
public class A400WelcomeActivity extends BaseActivity {
    @BindView(R2.id.fcv_welcome)
    FragmentContainerView mFragmentContainer;
    @BindView(R2.id.iv_welcome_hello)
    VideoPlayerIJK mVpVideo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a400_welcome);
        ButterKnife.bind(this);
        initView();
        startLanguageFragment();
    }

    private void initView() {
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        } catch (Exception e) {
            LogHelper.log(e);
        }
        mVpVideo.setListener(new VideoPlayerListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer mp, int percent) {

            }

            @Override
            public void onCompletion(IMediaPlayer mp) {
            }

            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                Logger.e("IMediaPlayer error %d\t%d", what, extra);
                return false;
            }

            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                return false;
            }

            @Override
            public void onPrepared(IMediaPlayer mp) {
            }

            @Override
            public void onSeekComplete(IMediaPlayer mp) {
            }

            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {

            }
        });
        mVpVideo.setVideoPath(ServiceContainer.getInstance().getService(IAppService.class).getVideDir() + "/hello.webm");
        mVpVideo.setLooping(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVpVideo.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVpVideo.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        IjkMediaPlayer.native_profileEnd();
    }


    /**
     * Language Page 1, Language
     */
    public void startLanguageFragment() {
        addFragment(R.id.fcv_welcome, A400WelcomeLanguageFragment.newInstance());
    }

    /**
     * Welcome page 2, hello
     */
    public void startHelloFragment() {
        addFragment(R.id.fcv_welcome, A400HelloFragment.newInstance());
    }


    /**
     * Welcome page 3, name
     */
    public void startNameFragment() {
        addFragment(R.id.fcv_welcome, A400WelcomeNameFragment.newInstance());
    }

    /**
     * Welcome page 4, Wi-Fi
     */
    public void startWiFiFragment() {
        addFragment(R.id.fcv_welcome, A400WelcomeWiFiListFragment.newInstance());
    }

    /**
     * Welcome page 5, Wi-Fi password
     */
    public void startPasswordFragment() {
        addFragment(R.id.fcv_welcome, WelcomeWifiPasswordFragment.newInstance());
    }

    /**
     * Welcome page , terms
     * <p>
     * User should agree terms and conditions to use this app.
     */
    public void startTermsFragment() {
        addFragment(R.id.fcv_welcome, A400WelcomeTermsFragment.newInstance());
    }


    public void goToGuide() {
        finish();
        mRouter.routeToGuideMilestone().start(this);
    }
}
