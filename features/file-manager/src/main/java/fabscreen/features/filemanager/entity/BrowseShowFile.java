package fabscreen.features.filemanager.entity;

import fabscreen.platform.base.lib.file.IFile;

public class BrowseShowFile {
    private final FileType mFileType;
    private String mShowPic;
    private int mDefaultDisplay;
    private IFile mIFile;
    private boolean mIsHavePic = false;
    private boolean mIsSetView = false;
    private boolean mIsSelect = false;


    public BrowseShowFile(FileType fileType, IFile iFile, int defaultDisplay) {
        mFileType = fileType;
        mDefaultDisplay = defaultDisplay;
        mIFile = iFile;
    }

    public FileType getFileType() {
        return mFileType;
    }

    public IFile getIFile() {
        return mIFile;
    }

    public String getShowPic() {
        return mShowPic;
    }

    public void setShowPic(String extract) {
        mShowPic = extract;
        mIsHavePic = true;
    }

    public boolean isIsHavePic() {
        return mIsHavePic;
    }

    public boolean isIsSetView() {
        return mIsSetView;
    }

    public void setIsSetView(boolean isSetView) {
        mIsSetView = isSetView;
    }

    public int getDefaultDisplay() {
        return mDefaultDisplay;
    }

    public boolean isSelect() {
        return mIsSelect;
    }

    public void setSelect(boolean isSelect) {
        mIsSelect = isSelect;
    }

    @Override
    public String toString() {
        return "BrowseShowFile{" +
                "mFileType=" + mFileType +
                ", mShowPic='" + mShowPic + '\'' +
                ", mDefaultDisplay=" + mDefaultDisplay +
                ", mIFile=" + mIFile +
                ", mIsHavePic=" + mIsHavePic +
                ", mIsSetView=" + mIsSetView +
                ", mIsSelect=" + mIsSelect +
                '}';
    }
}
