package fabscreen.platform.base.service.machine.controller;

import androidx.annotation.NonNull;

import com.orhanobut.logger.Logger;

import java.io.File;

import fabscreen.platform.base.lib.update.UpdateFileParser;
import fabscreen.platform.base.lib.update.Updater;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.SubscribeStructure;
import fabscreen.platform.base.service.machine.structure.prop.BytesProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt32Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.base.service.machine.structure.update.RequestPackageParam;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class UpdateController {
    private final MachineConnectionController mConnectionController;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private UpdateFileParser.UpdateFile mUpdateFile;
    private final PublishSubject<Updater.Progress> mProgressSubject = PublishSubject.create();
    private BehaviorSubject<Integer> mResultSubject = BehaviorSubject.create();


    public UpdateController(MachineConnectionController connectionController) {
        mConnectionController = connectionController;
        watchUpdateStatus();
    }

    private void watchUpdateStatus() {
        BaseStructure updateStatusStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("module_count", new UInt8Prop());
                addProp("current_module", new UInt8Prop());
            }
        };

        ResponseStructure<IStructure> responseStructure = new ResponseStructure<>();
        responseStructure.dataProp = updateStatusStructure;

        mDisposable.add(mConnectionController.watch(0xad, 0xa0, responseStructure)
                .subscribe(response -> {
//                    Logger.d("update status: %s", response);
                }, LogHelper::log));
    }

    /**
     * Request machine to start an updating progress.
     */
    public Observable<Integer> startUpdate(UpdateFileParser.UpdateFile file) {
        mResultSubject = createNewResultSubject();
        //start
        mUpdateFile = file;
        BytesProp bytesProp = new BytesProp();
        byte[] headerBytes = UpdateFileParser.getInstance().getMachineFileHeader(file.path);
        if (headerBytes == null) {
            mResultSubject.onError(new IllegalArgumentException("Header invalid!"));
        } else {
            bytesProp.setValue(headerBytes);
            mDisposable.add(mConnectionController.request(0xad, 0x01, bytesProp, new ResponseStructure<>())
                    .subscribeOn(Schedulers.io())
                    .subscribe(response -> {
                        if (!response.isSuccess()) {
                            mResultSubject.onError(new IllegalStateException("Start update fail," + response.resultProp.getValue()));
                        }
                    }));
        }

        return mResultSubject.hide();
    }

    @NonNull
    private BehaviorSubject<Integer> createNewResultSubject() {
        if (mResultSubject != null) {
            mResultSubject.onComplete();
            mResultSubject = null;
        }
        return BehaviorSubject.create();
    }

    public void subscribeUpdateStatus() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0xad, 0xa0, 1000);
        mDisposable.add(mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure<>())
                .subscribe(response -> {
                }, LogHelper::log));
    }

    public void unsubscribeUpdateStatus() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0xad, 0xa0, 0);
        mDisposable.add(mConnectionController.request(0x01, 0x01, subscribeStructure, new ResponseStructure<>())
                .subscribe(response -> {
                }, LogHelper::log));
    }

    @SuppressWarnings("unchecked")
    public void onRequestUpdatePackage(int commandSet, int commandId, int sequence, RequestPackageParam param) {
        int index = (int) param.getIndex();
        int maxSpace = param.getMaxSpace();

        Logger.d("Update: on request package, index=%1$d, maxSpace=%2$d", index, maxSpace);

        if (mUpdateFile == null) {
            // no update file exist
            mConnectionController.sendResponse(commandSet, commandId, sequence, new ResponseStructure<>(1));
        } else {
            byte[] chunk = UpdateFileParser.getChunk(mUpdateFile.path, index, maxSpace);

            if (chunk == null) {
                Logger.d("update ctrl, request index %d, no buffer.", index);
                mConnectionController.sendResponse(commandSet, commandId, sequence, new UInt8Prop(1));
                return;
            }

            BaseStructure updatePackage = new BaseStructure() {
                @Override
                protected void init() {
                    addProp("startPos", new UInt32Prop());
//                    addProp("packageSize", new UInt16Prop());
                    addProp("packagePayload", new BytesProp());
                }
            };

            updatePackage.getProp("startPos").setValue((long) index);
//            updatePackage.getProp("packageSize").setValue(chunk.length);
            updatePackage.getProp("packagePayload").setValue(chunk);

            ResponseStructure<IStructure> responseStructure = new ResponseStructure<>();
            responseStructure.dataProp = updatePackage;

            mConnectionController.sendResponse(commandSet, commandId, sequence, responseStructure);
            int progress = (int) ((float) index / new File(mUpdateFile.path).length() * 100);
            Logger.d("controller progress: %d", progress);
            mProgressSubject.onNext(new Updater.Progress(mUpdateFile.type, progress));
        }
    }

    public void onNotifyUpdateResult(int commandSet, int commandId, int sequence, UInt8Prop resultProp) {
        Logger.d("Update: got result, %s", resultProp);
        int result = resultProp.getValue();
//        String msg;
//        if (result == 0) {
        mResultSubject.onNext(result);
        mResultSubject.onComplete();
        mResultSubject = null;
//        } else {
//            if (result == 1) {
//                msg = "Machine report checksum fail!";
//            } else {
//                msg = "Machine report update fail for no reason.";
//            }
//            mProgressSubject.onError(new IllegalStateException(msg));
//        }
        mConnectionController.sendResponse(commandSet, commandId, sequence, new UInt8Prop(0));
    }

    public Observable<Updater.Progress> getProgressObservable() {
        return mProgressSubject.hide();
    }
}
