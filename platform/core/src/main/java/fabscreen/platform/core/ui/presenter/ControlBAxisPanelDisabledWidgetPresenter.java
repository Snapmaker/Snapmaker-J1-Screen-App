package fabscreen.platform.core.ui.presenter;

import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.view.ActionButton;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

// FIXME: Temporary Presenter for disabling B Axis.
public class ControlBAxisPanelDisabledWidgetPresenter extends ControlBAxisPanelWidgetPresenter {
    @BindView(R2.id.sbg_control_b_axis_steps)
    SegmentedButtonGroup mSbgBAxisSteps;
    @BindView(R2.id.btn_widget_b_axis_clockwise)
    ActionButton mBtnBAxisClockwise;
    @BindView(R2.id.btn_widget_b_axis_counterclockwise)
    ActionButton mBtnBAxisCounterClockwise;
    private CompositeDisposable mCompositeDisposable;
    private BehaviorSubject<Boolean> mMovingEventSubject = BehaviorSubject.createDefault(false);

    public ControlBAxisPanelDisabledWidgetPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
        mCompositeDisposable = compositeDisposable;
    }

    public Observable<Boolean> getMovingEventObservable() {
        return mMovingEventSubject.hide();
    }

    @Override
    public void bind(View view) {
        ButterKnife.bind(this, view);

        Disposable sub = mMovingEventSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(movingEvent -> {/**/});
        mCompositeDisposable.add(sub);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mSbgBAxisSteps.setEnabled(false);
        mBtnBAxisClockwise.setEnabled(false);
        mBtnBAxisCounterClockwise.setEnabled(false);
    }
}
