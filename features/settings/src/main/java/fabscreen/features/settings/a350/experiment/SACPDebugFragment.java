package fabscreen.features.settings.a350.experiment;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.BaseFragment;

public class SACPDebugFragment extends BaseFragment {
    @BindView(R2.id.tv_received_hex)
    TextView mTvReceivedHex;

    public static SACPDebugFragment newInstance() {
        return new SACPDebugFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvReceivedHex.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_sacp_debug;
    }

    @OnClick(R2.id.btn_subscribe_heartbeat)
    void onSubscribeHeartbeatClicked() {
        playNormalClickSound();
        // TODO:Not compatible with 3.0 protocol
//        MachineController MachineController = ServiceContainer.getInstance().getService(IMachine.class).getMachineController();
//        MachineController.requestSubscribeHeartbeat(0x02)
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(packet -> mTvReceivedHex.setText(packet.toHexString() + "\n"));
//        MachineController.watchHeartbeat()
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(packet -> {
//                    mTvReceivedHex.append(packet.toHexString() + "\n");
//                });
    }
}
