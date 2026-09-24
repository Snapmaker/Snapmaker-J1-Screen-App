package fabscreen.platform.base.lib.file;

import java.util.Stack;

public abstract class AbstractPartition implements IPartition {

    protected Stack<IFile> mStack = new Stack<>();

    @Override
    public Boolean init() {
        mStack.clear();
        IFile rootFile = getRootFile();
        mStack.add(rootFile);
        return true;
    }

}
