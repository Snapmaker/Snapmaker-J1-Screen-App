package fabscreen.platform.base.view.debugtool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import fabscreen.platform.base.R;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRemote;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.PerpetualPopuBean;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;


public class FloatWindow implements DefaultLifecycleObserver {
    CompositeDisposable mDisposable = new CompositeDisposable();
    private View floatView;
    private DebugButton debugButton;
    private boolean mIsDeveloper;

    private long mTime = 0;
    private int mTouchCount = 0;

    private LinearLayout mLlToast;
    private ImageView mIvPic;
    private TextView mTvTitle;
    private TextView mTvContent;
    private Button mBtnClose;

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        IRemote remote = ServiceContainer.getInstance().getService(IRemote.class);
        if (owner instanceof Activity) {
            Context context = (Context) owner;
            ViewGroup rootView = (ViewGroup) ((Activity) owner).findViewById(android.R.id.content).getRootView();
            floatView = LayoutInflater.from((Context) owner).inflate(R.layout.view_float_debug, rootView, false);
            rootView.addView(floatView);
            debugButton = floatView.findViewById(R.id.debug_button);
            // Show different bg color depend on whether luban is connected.
            if (remote != null) {
                mDisposable.add(remote.getRemoteConnectedObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(state -> debugButton.setBackgroundResource(state == 2 ? R.drawable.all_button_round_orange : R.drawable.all_button_round_blue)));
            }

            debugButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, MockMachineActivity.class);
                context.startActivity(intent);
            });

            //Toast
            mLlToast = floatView.findViewById(R.id.ll_all_toast);
            mIvPic = floatView.findViewById(R.id.iv_dialog_logo);
            mTvTitle = floatView.findViewById(R.id.tv_dialog_title);
            mTvContent = floatView.findViewById(R.id.iv_dialog_content);
            mBtnClose = floatView.findViewById(R.id.btn_dialog_close);
            mBtnClose.setVisibility(View.VISIBLE);
            mLlToast.setVisibility(View.GONE);
            mBtnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLlToast.setVisibility(View.GONE);
                }
            });

            //TODO:Remove this code after filming.
            Button goToGreenScreen = floatView.findViewById(R.id.btn_green_screen);
            goToGreenScreen.setVisibility(ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().seriesId == IMachine.MachineSeries.A ?
                    View.VISIBLE : View.GONE);
            goToGreenScreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long currentTime = SystemClock.elapsedRealtime();
                    if (currentTime - mTime < 500) {
                        mTouchCount += 1;
                    } else {
                        mTouchCount = 1;
                    }
                    mTime = currentTime;
                    if (mTouchCount >= 3) {
                        ServiceContainer.getInstance().getService(IRouter.class).routeToGreenScreen().start(context);
                    }
                }
            });

        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        // dispose
        mDisposable.clear();
        mDisposable = null;
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        mIsDeveloper = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineDeveloper();
        hideView(mIsDeveloper);
    }

    public void hideView(boolean isDeveloper) {
        debugButton.setVisibility(isDeveloper ? View.VISIBLE : View.GONE);
    }

    public void showToast(PerpetualPopuBean bean) {
        mIvPic.setBackgroundResource(bean.getImgRes());
        mTvTitle.setText(bean.getTitle());
        mTvContent.setText(bean.getContent());
        mLlToast.setVisibility(View.VISIBLE);
    }

}
