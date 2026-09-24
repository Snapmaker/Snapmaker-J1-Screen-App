package fabscreen.features.welcome.s20;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.welcome.R;
import fabscreen.features.welcome.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.view.VideoPlayerIJK;
import fabscreen.platform.core.ui.view.VideoPlayerListener;
import fabscreen.platform.lib.LogHelper;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class WelcomeHelloFragment extends BaseFragment {
    @BindView(R2.id.iv_welcome_hello)
    VideoPlayerIJK mVpVideo;

    public static WelcomeHelloFragment newInstance() {
        return new WelcomeHelloFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVideo();
    }

    private void initVideo() {
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
    protected int getLayoutResID() {
        return R.layout.fragment_welcome_hello;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_welcome_hello_start)
    void onClickStart() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((WelcomeActivity) getActivity()).startTermsFragment();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mVpVideo.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mVpVideo.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        IjkMediaPlayer.native_profileEnd();
    }
}
