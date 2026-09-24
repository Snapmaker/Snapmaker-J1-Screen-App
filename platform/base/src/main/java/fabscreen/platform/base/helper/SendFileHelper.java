package fabscreen.platform.base.helper;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;

import fabscreen.platform.base.service.machine.structure.DataStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.remote.RemoteFileController;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class SendFileHelper {
    private static final int SUBCONTRACT_RULES = 60 * 1024;
    RemoteFileController mRemoteFileController;

    String mFileName;
    long mFileSize;
    int mPackageNum;
    int mPackageIndex;
    String mMd5;
    ArrayList<byte[]> mDatas;

    private CompositeDisposable mDisposable = new CompositeDisposable();

    public SendFileHelper(RemoteFileController remoteController) {
        mRemoteFileController = remoteController;
        mDisposable.add(mRemoteFileController.getSendDataObservable()
                .subscribe(this::sendData));
    }

    public Observable<ResponseStructure> sendFileDesc(File file) {
        //FIXME:
        if (!file.isFile()) return null;
        mFileName = file.getName();
        mFileSize = file.length();
        mPackageNum = (int) (mFileSize % SUBCONTRACT_RULES != 0 ? file.length() / SUBCONTRACT_RULES + 1 : mFileSize / SUBCONTRACT_RULES);
        mDatas = new ArrayList<>();
        intData(file);
        return mRemoteFileController.sendFileDesc(mFileName, mFileSize, mPackageNum, mMd5);
//                .flatMap(responseStructure -> responseStructure.isSuccess() ? mSendFileResultSubject.hide() : Observable.just(responseStructure.resultProp.getValue()));
    }

    private void sendData(int mPackageIndex) {
        DataStructure dataStructure = new DataStructure();
        dataStructure.setMd5(mMd5);
        dataStructure.setIndex(mPackageIndex);
        dataStructure.setData(mDatas.get(mPackageIndex));
        mRemoteFileController.sendData(dataStructure);
    }

    private void intData(File file) {
        MessageDigest digest = null;
        FileInputStream in;
        byte[] buffer = new byte[SUBCONTRACT_RULES];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, SUBCONTRACT_RULES)) != -1) {
                digest.update(buffer, 0, len);
                byte[] out = new byte[len];
                System.arraycopy(buffer, 0, out, 0, len);
                mDatas.add(out);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMd5 = bytesToHexString(digest.digest());
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
