package fabscreen.features.filemanager;

public class DetailDesc {
    String mDetailDataName;
    String mDetailDataValue;

    public DetailDesc(String detailDataName, String detailDataValue) {
        mDetailDataName = detailDataName;
        mDetailDataValue = detailDataValue;
    }

    public String getDetailDataName() {
        return mDetailDataName;
    }

    public void setDetailDataName(String detailDataName) {
        this.mDetailDataName = detailDataName;
    }

    public String getDetailDataValue() {
        return mDetailDataValue;
    }

    public void setDetailDataValue(String detailDataValue) {
        this.mDetailDataValue = mDetailDataValue;
    }
}
