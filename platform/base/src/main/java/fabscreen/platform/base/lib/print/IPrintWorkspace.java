package fabscreen.platform.base.lib.print;

import java.io.File;

import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.model.ModelBoundary;
import io.reactivex.Observable;

public interface IPrintWorkspace {
    int PRINT_MODE_NORMAL = 0;
    int PRINT_MODE_DUAL_EXTRUDER_BACK_UP = 1;
    int PRINT_MODE_CLONE = 2;
    int PRINT_MODE_MIRROR = 3;

    void initLastPrintFile();

    IFile getPrintFile();

    void setPrintFile(IFile file);

    String getFileMD5Value();

    void setFileMD5Value(String value);

    int getPrintMode();

    void setPrintMode(int printMode);

    float getEstimatedTime();

    void setEstimatedTime(float estimatedTime);

    int getFileTotalLineCount();

    void setFileTotalLineCount(int totalCount);

    int getPrintSource();

    void setPrintSource(int source);

    String getFileName();

    Observable<Boolean> addFileToWorkspace(IFile sourceFile);

    // TODO: needs to remove File returns
    File getWorkspaceDir();

    void clearAllWorkSpaceFiles();

    void setModelBoundary(ModelBoundary boundary);

    ModelBoundary getModelBoundary();

    float getWorkTemperature(int index);

    void setWorkTemperature(float[] temperature);

    void setPrintModeXOffset(float xOffset);

    float getPrintModeXOffset();

    boolean isApplyMultiExtruder();

    void setApplyMultiExtruder(boolean applyMultiExtruder);

}
