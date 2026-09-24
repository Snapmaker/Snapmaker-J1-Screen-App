package fabscreen.platform.base.service.remote;

import android.os.SystemClock;
import android.util.SparseArray;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

import fabscreen.platform.base.R;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IFileManagerService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.DataStructure;
import fabscreen.platform.base.service.machine.structure.RemoteFileStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.BytesProp;
import fabscreen.platform.base.service.machine.structure.prop.StringProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.base.view.FileLoadingDialog;
import fabscreen.platform.base.view.SuperToastHelper;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class RemoteFileController {
    private final IAppService mAppService;
    private final IPreferences mPreferences;
    private final RemoteConnectionController mConnectionController;
    private final CompositeDisposable mDisposables = new CompositeDisposable();

    PublishSubject<Integer> mRequestPackageSubject = PublishSubject.create();
    PublishSubject<Integer> mSendPackageSubject = PublishSubject.create();
    PublishSubject<IFile> mFilePackageSubject = PublishSubject.create();
    public BehaviorSubject<BaseStructure> mSendFileResultSubject;
    private final int mRequestedThreadNum = 10;

    private ArrayList<byte[]> mDataList;
    int mReceivedFileLength;
    int mReceivingNum;
    Disposable subscribe;
    private int mSendDataSequence;

    private String mFileName;
    private long mFileLength;
    private long mPackageCount;
    private String mMd5;
    PublishSubject<RemoteFileStructure> mRemoteFileSubject = PublishSubject.create();
    private SparseArray<Disposable> disposableSparseArray;
    long start;
    long end;
    float speed;
    PublishSubject<Integer> mFabConfirmSubject = PublishSubject.create();
    private FileLoadingDialog mFabConfirm;

    public RemoteFileController(IMachine iMachine, RemoteConnectionController connectionController, IAppService appService, IPreferences preferences) {
        mConnectionController = connectionController;
        mAppService = appService;
        mPreferences = preferences;
        if (subscribe != null && !subscribe.isDisposed()) {
            subscribe.dispose();
        }
        subscribe = mRequestPackageSubject.subscribe(index -> {
            mReceivingNum++;
//            Logger.d("---FDT--- mReceivingNum:%d,\t%d", mReceivingNum, (int) ((float) mReceivingNum / mPackageCount * 100));
            mFabConfirmSubject.onNext((int) ((float) mReceivingNum / mPackageCount * 100));

            // All the packages has been received, ready to write into file.
            if (mReceivingNum == mPackageCount) {
                // Received data length were not match with the request params.
                if (mReceivedFileLength != mFileLength) {
                    returnTransmissionResult(1);
                    return;
                }
                // Write data into file.
                File file = null;
                try {
                    file = new File(mAppService.getFilesDir(), mFileName);
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    for (int i = 0; i < mDataList.size(); i++) {
                        byte[] d = mDataList.get(i);
                        if (d == null) {
                            continue;
                        }
                        fileOutputStream.write(d);
                    }
                    fileOutputStream.close();
                } catch (Exception e) {
                    Logger.e("Error occurs when writing file.");
                    LogHelper.log(e);
                }

                // Check file exists and not empty?
                if (file != null) {
                    end = SystemClock.elapsedRealtime();
                    speed = (float) mReceivedFileLength / (end - start);
                    Logger.d("file received, using %.1f s.", (int) (end - start) / 1000f);
                    // TODO: Check the Md5?
                    // return result(success), file received.
                    returnTransmissionResult(0);
                }
            } else if (mRequestedThreadNum < mPackageCount) {
                // Continue to receive other packages.
                // TODO: Needs to check this condition carefully.
                Disposable disposable = disposableSparseArray.get(index);
                if (disposable != null && !disposable.isDisposed()) {
                    disposable.dispose();
                }
                // Why using `mReceivingNum`?
                requestFileData(mRequestedThreadNum - 1 + mReceivingNum);
            }
        }, LogHelper::log);

        Disposable subscribe3 = mFabConfirmSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (mFabConfirm == null || !mFabConfirm.isShowing()) {
                        String unit = "KB";
                        long showFileLength = 0;
                        if (mFileLength != 0) {
                            showFileLength = mFileLength;
                            if (showFileLength > 1024) {
                                showFileLength /= 1024;
                                unit = "KB";
                                if (showFileLength > 1024) {
                                    showFileLength /= 1024;
                                    unit = "MB";
                                }
                            }
                        }
                        String showFileName = mFileName.length() > 15 ? mFileName.substring(0, 15) + "..." : mFileName;
                        mFabConfirm = FileLoadingDialog.create(mAppService.getNowViewContext(), false);
                        mFabConfirm.mProgressbar.setMax(100);
                        mFabConfirm.setProgress(integer);
                        String content = mAppService.getNowViewContext().getString(R.string.j1_dialog_remote_file_receiving_content, showFileName, (showFileLength + unit));
                        mFabConfirm.setContent(content);
                        mFabConfirm.show();
                    }
                    if (integer == -1) {
                        if (mFabConfirm != null && mFabConfirm.isShowing()) {
                            mFabConfirm.dismiss();
                            mFabConfirm = null;
                        }
                    } else {
                        mFabConfirm.setProgress(integer);
                    }
                }, LogHelper::log);
    }

    private void requestFileData(int packageIndex) {
        if (packageIndex >= mPackageCount) return;
        BaseStructure requestStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("md5", new StringProp(mMd5));
                addProp("packageIndex", new UInt16Prop(packageIndex));
            }
        };
        BaseStructure responseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("md5", new StringProp(mMd5));
                addProp("packageIndex", new UInt16Prop());
                addProp("data", new BytesProp());
            }
        };
//        Logger.d("---FDT--- RequestFileData:%d", packageIndex);

        // Send package(index) request and handle response
        Disposable tempSubscribe = mConnectionController.request(0xb0, 0x01, requestStructure, new ResponseStructure<>(responseStructure))
                .subscribe(response -> {
                    if (response.isSuccess()) {
                        BaseStructure dataProp = response.dataProp;
                        String md5 = ((StringProp) dataProp.getProp("md5")).getValue();
                        int index = ((UInt16Prop) dataProp.getProp("packageIndex")).getValue();
                        byte[] data = ((BytesProp) dataProp.getProp("data")).getValue();
                        if (!md5.equals(mMd5)) {
                            // TODO:
                            Logger.d("MD5 Error");
                            return;
                        }
                        if (mDataList.get(index) == null) {
                            mDataList.set(index, data);
                            mReceivedFileLength += data.length;
                            mRequestPackageSubject.onNext(index);
                        } else {
                            mDataList.set(index, data);
                            mReceivedFileLength += mDataList.get(index).length - data.length;
                        }
                    }
                }, LogHelper::log);
        disposableSparseArray.put(packageIndex, tempSubscribe);
    }

    public void requestStartSendFile(RemoteFileStructure fileStructure) {
        start = SystemClock.elapsedRealtime();
        Logger.d("Requesting send file... ");
        mFileName = fileStructure.getFileName();
        mFileLength = fileStructure.getFileLength();
        mPackageCount = fileStructure.getPackageNum();
        mMd5 = fileStructure.getMd5();
        mReceivedFileLength = 0;
        mReceivingNum = 0;

        IFileManagerService fileManagerService = ServiceContainer.getInstance().getService(IFileManagerService.class);
        IPartition device = fileManagerService.getDevice(true);
        try {
            if (mFileLength >= device.getTotalSpace()) {
                device.removeFile(device.getRootFile());
            } else if (mFileLength >= device.getFreeSpace() - 30 * 1024 * 1024) {
                while (mFileLength >= device.getFreeSpace() - 30 * 1024 * 1024) {
                    ArrayList<IFile> iFiles = device.getRootFile().listFiles();
                    iFiles.sort(Comparator.comparingLong(IFile::lastModified));
                    iFiles.get(0).removeFile();
                }
                Logger.d("Free up space done, available " + device.getFreeSpace());
            }
        } catch (IOException e) {
            Logger.d("Clear local file error!");
            LogHelper.log(e);
        }

        disposableSparseArray = new SparseArray<Disposable>((int) mPackageCount);
        mDataList = new ArrayList<>((int) mPackageCount);
        for (int i = 0; i < mPackageCount; i++) {
            mDataList.add(null);
        }

        if (mRequestedThreadNum > mPackageCount) {
            for (int i = 0; i < mPackageCount; i++) {
                requestFileData(i);
            }
        } else {
            for (int i = 0; i < mRequestedThreadNum; i++) {
                requestFileData(i);
            }
        }
//        mFabConfirmSubject.onNext((int) ((float) mReceivingNum / mPackageCount * 100));
    }

    private void returnTransmissionResult(int result) {
        if (result == 0) {
            new SuperToastHelper.Builder()
                    .setDrawable(R.drawable.ic_pic_a400_success_68x68)
                    .setTitle(mAppService.getNowViewContext().getString(R.string.a400_all_file_received))
                    .setMessage(mFileName)
                    .build()
                    .showToast(mAppService.getNowViewContext());
        }
        clearCache();
        mConnectionController.request(0xb0, 0x02, new UInt8Prop(result), new ResponseStructure<>()).subscribe();
    }

    private void clearCache() {
        mFileName = null;
        mFileLength = 0;
        mPackageCount = 0;
        mMd5 = null;
        mReceivedFileLength = 0;
        mReceivingNum = 0;
        disposableSparseArray.clear();
        disposableSparseArray = null;
        mDataList.clear();
        mDataList = null;
        mFabConfirmSubject.onNext(-1);
        mDisposables.clear();
    }

    public Observable<ResponseStructure> sendFileDesc(String fileName, long fileSize, int packageNum, String md5) {
        if (mSendFileResultSubject != null) {
            mSendFileResultSubject.onComplete();
            mSendFileResultSubject = null;
        }
        mSendFileResultSubject = BehaviorSubject.create();
        return mConnectionController.request(0xb0, 0x00, new RemoteFileStructure(fileName, fileSize, packageNum, md5), new ResponseStructure());
    }

    public void requestPackage(int sequence, Integer value) {
        // FIXME:Transfer the Sequence?
        mSendDataSequence = sequence;
        mSendPackageSubject.onNext(value);
    }

    public void sendData(DataStructure dataPackage) {
        mConnectionController.sendResponse(0xb0, 0x01, mSendDataSequence, new ResponseStructure<>(dataPackage));
    }

    public Observable<Integer> getSendDataObservable() {
        return mSendPackageSubject.hide();
    }

    public void onSendFileFinish(int commandSet, int commandId, int sequence, BaseStructure finishStruct) {
        mConnectionController.sendResponse(commandSet, commandId, sequence, new ResponseStructure<>());

        BaseStructure structure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("name", new StringProp());
                addProp("md5", new StringProp());
            }
        };
        structure.getProp("name").setValue(finishStruct.getProp("name").getValue());
        structure.getProp("md5").setValue(finishStruct.getProp("md5").getValue());
        mSendFileResultSubject.onNext(structure);
    }

    public Observable<BaseStructure> getSendFileResultObservable() {
        return mSendFileResultSubject.hide();
    }
}
