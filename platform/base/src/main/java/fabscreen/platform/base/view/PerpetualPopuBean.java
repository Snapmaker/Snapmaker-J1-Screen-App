package fabscreen.platform.base.view;

public class PerpetualPopuBean {

    private int imgRes;
    private int title;
    private int content;

    public int getImgRes() {
        return imgRes;
    }

    public void setImgRes(int imgRes) {
        this.imgRes = imgRes;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getContent() {
        return content;
    }

    public void setContent(int content) {
        this.content = content;
    }

    public PerpetualPopuBean(int imgRes, int title, int content) {
        this.imgRes = imgRes;
        this.title = title;
        this.content = content;
    }


}
