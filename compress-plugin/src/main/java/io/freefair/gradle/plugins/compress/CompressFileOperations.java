package io.freefair.gradle.plugins.compress;

import org.gradle.api.file.FileTree;

/**
 * @author Lars Grefer
 * @see org.gradle.api.internal.file.FileOperations
 */
public interface CompressFileOperations {

    FileTree arTree(Object arFile);

    FileTree arjTree(Object arjFile);
    FileTree arjTree(Object arjFile, String charsetName);

    FileTree cpioTree(Object cpioFile);
    FileTree cpioTree(Object cpioFile, String encoding);
    FileTree cpioTree(Object cpioFile, int blockSize);
    FileTree cpioTree(Object cpioFile, int blockSize, String encoding);

    FileTree sevenZipTree(Object sevenZipFile);
    FileTree sevenZipTree(Object sevenZipFile, char[] password);

    FileTree dumpTree(Object dumpFile);
    FileTree dumpTree(Object dumpFile, String encoding);
}
