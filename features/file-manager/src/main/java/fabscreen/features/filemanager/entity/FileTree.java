package fabscreen.features.filemanager.entity;

import java.util.ArrayList;

import fabscreen.platform.base.lib.file.IFile;

public class FileTree {
    IFile mFile;
    ArrayList<FileTree> mChildNode;

    public FileTree(IFile file) {
        mFile = file;
        mChildNode = new ArrayList<>();
    }

    public IFile getFile() {
        return mFile;
    }

    public ArrayList<FileTree> getChildNode() {
        return mChildNode;
    }

    public void addChildFileTree(FileTree fileTree) {
        mChildNode.add(fileTree);
    }
}
