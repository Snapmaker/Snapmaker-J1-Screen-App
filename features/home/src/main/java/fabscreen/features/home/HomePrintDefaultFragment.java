package fabscreen.features.home;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;

@Route(path = RoutePath.HOME_LAND)
public class HomePrintDefaultFragment extends BaseFragment {

    @BindView(R2.id.tv_home_slogan)
    TextView mTvHomeSlogan;

    public static HomePrintDefaultFragment newInstance() {
        return new HomePrintDefaultFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    public void onResume() {
        super.onResume();

        ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineInfo -> {
                    // TODO: Chang to initView
                    Logger.d("machineType " + machineInfo.seriesId);
                    if (machineInfo.seriesId == IMachine.MachineSeries.J) {
                        mTvHomeSlogan.setText("Jadeone");
                    } else {
                        mTvHomeSlogan.setText("A400");
                    }
                });

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_home_print_default;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {

    }

    @OnClick(R2.id.btn_home_start)
    void onClickStart() {
        playNormalClickSound();
        IMachine.WorkType workType = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;
        int fileType = 0;
        switch (workType) {
            case CNC:
                fileType = 3;
                break;
            case FDM:
                fileType = 1;
                break;
            case LASER:
                fileType = 2;
                break;
            default:
                fileType = 0;
        }
        ServiceContainer.getInstance().getService(IRouter.class).routeToFilesPage(fileType).start(getContext());
    }
}
