package fabscreen.platform.base.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import fabscreen.platform.base.service.machine.controller.SACPLaserCameraController;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.prop.BytesProp;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class CameraHelper {
    SACPLaserCameraController mLaserCameraController;
    long mPhotoSize;
    int mPackageNum;
    int fileId;

    PublishSubject<Integer> mRequestPackage = PublishSubject.create();
    PublishSubject<Bitmap> mBitmapPackage = PublishSubject.create();

    private ArrayList<byte[]> mDataList;
    int mCheckPhotoSize;
    Disposable subscribe;
    CompositeDisposable disposable = new CompositeDisposable();

    public CameraHelper(SACPLaserCameraController laserCameraController) {
        mLaserCameraController = laserCameraController;

        disposable.add(mRequestPackage.subscribe(integer -> {
            if (integer < mPackageNum) {
                requestNext(integer);
            } else {
                byte[] pic = mergeData();
                if (pic == null) {
                    Logger.d("Not for a picture");
                    return;
                }
                Bitmap bitmap = BitmapFactory.decodeByteArray(pic, 0, pic.length);
                if (bitmap != null) {
                    mBitmapPackage.onNext(bitmap);
                    Logger.d("Finished receiving pictures");
                }
            }
        }));


    }

    private byte[] mergeData() {
        byte[] pic = new byte[mCheckPhotoSize];
        int offset = 0;
        if (mDataList == null) return null;

        for (byte[] d : mDataList) {
            System.arraycopy(d, 0, pic, offset, d.length);
            offset += d.length;
        }
        return pic;
    }

    private void requestNext(Integer integer) {
        if (subscribe != null && !subscribe.isDisposed()) subscribe.dispose();
        final int temp = integer;
        subscribe = mLaserCameraController.getPhoto(fileId, integer)
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        byte[] value = ((BytesProp) responseStructure.dataProp).getValue();
                        mCheckPhotoSize += value.length;
                        mDataList.add(value);
                        mRequestPackage.onNext(temp + 1);
                    }
                });
    }

    /**
     * Slower speed, more stability
     */
    public Observable<Bitmap> takePhoto() {
        mDataList = new ArrayList<>();
        mCheckPhotoSize = 0;
        Logger.d("start TakePhoto");
        return mLaserCameraController.takePhoto(0, 0)
                .flatMap(responseStructure -> {
                    BaseStructure baseStructure = (BaseStructure) responseStructure.dataProp;
                    mPhotoSize = (long) baseStructure.getProp("size").getValue();
                    mPackageNum = (int) baseStructure.getProp("packageNum").getValue();
                    fileId = (int) baseStructure.getProp("fileId").getValue();
                    mRequestPackage.onNext(0);
                    Logger.d("---FDT--- End TakePhoto");
                    return mBitmapPackage.hide();
                });
    }

    /**
     * Fast speed, the image may be garbled
     * TODO:To repair
     */
    public Observable<Bitmap> autoTakePhoto() {
        mDataList = new ArrayList<>();
        mCheckPhotoSize = 0;
        Logger.d("start TakePhoto");
        return mLaserCameraController.takePhoto(0, 0, true)
                .flatMap(responseStructure -> {
                    BaseStructure baseStructure = (BaseStructure) responseStructure.dataProp;
                    mPhotoSize = (long) baseStructure.getProp("size").getValue();
                    mPackageNum = (int) baseStructure.getProp("packageNum").getValue();
                    fileId = (int) baseStructure.getProp("fileId").getValue();
                    Logger.d("---FDT--- End TakePhoto");
                    return mLaserCameraController.getBitmapObservable();
                });
    }

}
