package fabscreen.platform.base.view.debugtool;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;
import fabscreen.platform.base.R;
import fabscreen.platform.base.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.model.ILaserCameraController;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.SuperToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class BTDebugFragment extends BaseFragment implements BTAdapter.OnItemClickListener {
    private static final String TAG = "BTDebugFragment";
    private ILaserCameraController mBt;
    private ImageView mIvRcv;

    public static Fragment newInstance() {
        return new BTDebugFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_debug_bt;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        mBt = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController();

        List<String> nameList = new ArrayList<>();
        nameList.add("Capture and show");
        nameList.add("Set WB");
        nameList.add("Set Lighting on");
        nameList.add("Set Lighting off");
        nameList.add("Set Photo Quality 10");
        nameList.add("Set Photo Quality 30");
        nameList.add("Set Photo Resolution");
        nameList.add("Set expose time 2");
        nameList.add("Set expose time 0");

        RecyclerView rv = view.findViewById(R.id.rv_bt_interface_list);
        mIvRcv = view.findViewById(R.id.iv_rcv);
        BTAdapter adapter = new BTAdapter(nameList);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        adapter.setOnItemClickListener(this);

        mBt.getBluetoothConnectedObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(connected -> {
                    new SuperToastHelper.Builder().setMessage(connected ? "Connected" : "Disconnected").build().showToast(requireContext());
                });
    }

    @Override
    public void onItemClick(TextView itemView, int position) {
        switch (position) {
            case 0:
                mBt.requestCapturePhoto()
                        .doOnSubscribe(disposable -> itemView.setText("Capturing..."))
                        .doOnNext(success -> itemView.setText("Receiving..."))
                        .flatMap(success -> mBt.watchPhotoReceive())
                        .doOnNext(bitmap -> itemView.setText("Capture Again"))
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(bindToLifecycle())
                        .subscribe(bitmap -> mIvRcv.setImageBitmap(bitmap));
                break;

            case 1:
                mBt.checkCameraAutoWhiteBalanceActivated()
                        .doOnNext(on -> Logger.d("WB is %s", on ? "on" : "off"))
                        .flatMap(on -> mBt.setCameraAutoWhiteBalance(!on))
                        .as(bindToLifecycle())
                        .subscribe(success -> Logger.d("WB set."));
                break;
            case 2:
                mBt.setCameraLighting(true)
                        .as(bindToLifecycle())
                        .subscribe(success -> Logger.d("Light On"));
                break;
            case 3:
                mBt.setCameraLighting(false)
                        .as(bindToLifecycle())
                        .subscribe(success -> Logger.d(success ? "Light Off" : "Fail"));
                break;
            case 4:
                mBt.setPhotoQuality(10)
                        .as(bindToLifecycle())
                        .subscribe(success -> Logger.d(success ? "Q 10" : "Fail"));
                break;
            case 5:
                mBt.setPhotoQuality(30)
                        .as(bindToLifecycle())
                        .subscribe(success -> Logger.d(success ? "Q 30" : "Fail"));
                break;
            case 6:
                mBt.setPhotoResolution(300)
                        .as(bindToLifecycle())
                        .subscribe(success -> Logger.d(success ? "R 300" : "Fail"));
                break;
            case 7:
                mBt.setExposeTime(2)
                        .as(bindToLifecycle())
                        .subscribe(success -> Logger.d(success ? "E 20" : "Fail"));
                break;
            case 8:
                mBt.setExposeTime(0)
                        .as(bindToLifecycle())
                        .subscribe(success -> Logger.d(success ? "E 0" : "Fail"));
                break;

        }
    }

    @OnClick(R2.id.btn_quit)
    void quit() {
        back();
    }
}
