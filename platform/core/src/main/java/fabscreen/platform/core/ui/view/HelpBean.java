package fabscreen.platform.core.ui.view;

public class HelpBean {

    private int mPicResource;
    private int mContent;

    public HelpBean(int picResource, int content) {
        mPicResource = picResource;
        mContent = content;
    }

    public int getPicResource() {
        return mPicResource;
    }

    public void setPicResource(int mPicResource) {
        this.mPicResource = mPicResource;
    }

    public int getContent() {
        return mContent;
    }

    public void setContent(int mContent) {
        this.mContent = mContent;
    }

}
