package io.freefair.gradle.plugins.compress.internal;

import io.freefair.gradle.plugins.compress.CompressFileOperations;
import io.freefair.gradle.plugins.compress.tree.*;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.arj.ArjArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.api.internal.file.collections.DirectoryFileTreeFactory;
import org.gradle.api.internal.file.collections.FileTreeAdapter;
import org.gradle.api.internal.file.temp.TemporaryFileProvider;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.internal.Factory;
import org.gradle.internal.hash.FileHasher;
import org.gradle.internal.nativeintegration.filesystem.FileSystem;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author Lars Grefer
 */
public class CompressFileOperationsImpl implements CompressFileOperations {

    private final FileOperations fileOperations;

    private final TemporaryFileProvider temporaryFileProvider;
    private final FileHasher fileHasher;
    private final FileSystem fileSystem;
    private final DirectoryFileTreeFactory directoryFileTreeFactory;
    private final Factory<PatternSet> patternSetFactory;

    public CompressFileOperationsImpl(ProjectInternal project) {
        fileOperations = project.getFileOperations();

        temporaryFileProvider = project.getServices().get(TemporaryFileProvider.class);
        fileHasher = project.getServices().get(FileHasher.class);
        fileSystem = project.getServices().get(FileSystem.class);
        directoryFileTreeFactory = project.getServices().get(DirectoryFileTreeFactory.class);
        patternSetFactory = project.getServices().getFactory(PatternSet.class);
    }

    @Override
    public FileTree arTree(Object arPath) {
        File file = fileOperations.file(arPath);
        ArFileTree arFileTree = new ArFileTree(file, f -> new ArArchiveInputStream(new FileInputStream(f)), getExpandDir(), fileSystem, directoryFileTreeFactory, fileHasher);
        return new FileTreeAdapter(arFileTree, patternSetFactory);
    }

    @Override
    public FileTree arjTree(Object arjFile) {
        return arjTree(arjFile, f -> new ArjArchiveInputStream(new FileInputStream(f)));
    }

    @Override
    public FileTree arjTree(Object arjFile, String charsetName) {
        return arjTree(arjFile, f -> new ArjArchiveInputStream(new FileInputStream(f), charsetName));
    }

    private FileTree arjTree(Object arjFile, ArchiveInputStreamProvider<ArjArchiveInputStream> inputStreamProvider) {
        File file = fileOperations.file(arjFile);
        ArjFileTree arjFileTree = new ArjFileTree(file, inputStreamProvider, getExpandDir(), fileSystem, directoryFileTreeFactory, fileHasher);
        return new FileTreeAdapter(arjFileTree, patternSetFactory);
    }

    @Override
    public FileTree cpioTree(Object cpioFile) {
        return cpioTree(cpioFile, f -> new CpioArchiveInputStream(new FileInputStream(f)));
    }

    @Override
    public FileTree cpioTree(Object cpioFile, int blockSize) {
        return cpioTree(cpioFile, f -> new CpioArchiveInputStream(new FileInputStream(f), blockSize));
    }

    @Override
    public FileTree cpioTree(Object cpioFile, String encoding) {
        return cpioTree(cpioFile, f -> new CpioArchiveInputStream(new FileInputStream(f), encoding));
    }

    @Override
    public FileTree cpioTree(Object cpioFile, int blockSize, String encoding) {
        return cpioTree(cpioFile, f -> new CpioArchiveInputStream(new FileInputStream(f), blockSize, encoding));
    }

    private FileTree cpioTree(Object arPath, ArchiveInputStreamProvider<CpioArchiveInputStream> inputStreamProvider) {
        File file = fileOperations.file(arPath);
        ArchiveFileTree<CpioArchiveInputStream, CpioArchiveEntry> cpioFileTree = new ArchiveFileTree<>(file, inputStreamProvider, getExpandDir(), fileSystem, directoryFileTreeFactory, fileHasher);
        return new FileTreeAdapter(cpioFileTree, patternSetFactory);
    }

    @Override
    public FileTree sevenZipTree(Object sevenZipFile) {
        return sevenZipTree(sevenZipFile, f -> new SevenZipArchiveInputStream(new SevenZFile(f)));
    }

    @Override
    public FileTree sevenZipTree(Object sevenZipFile, char[] password) {
        return sevenZipTree(sevenZipFile, f -> new SevenZipArchiveInputStream(new SevenZFile(f, password)));
    }

    private FileTree sevenZipTree(Object sevenZipFile, ArchiveInputStreamProvider<SevenZipArchiveInputStream> inputStreamProvider) {
        File file = fileOperations.file(sevenZipFile);
        SevenZipFileTree sevenZipFileTree = new SevenZipFileTree(file, inputStreamProvider, getExpandDir(), fileSystem, directoryFileTreeFactory, fileHasher);
        return new FileTreeAdapter(sevenZipFileTree, patternSetFactory);
    }

    @Override
    public FileTree dumpTree(Object dumpFile) {
        return dumpTree(dumpFile, f -> new DumpArchiveInputStream(new FileInputStream(f)));
    }

    @Override
    public FileTree dumpTree(Object dumpFile, String encoding) {
        return dumpTree(dumpFile, f -> new DumpArchiveInputStream(new FileInputStream(f), encoding));
    }

    private FileTree dumpTree(Object dumpFile, ArchiveInputStreamProvider<DumpArchiveInputStream> inputStreamProvider) {
        File file = fileOperations.file(dumpFile);
        DumpFileTree dumpFileTree = new DumpFileTree(file, inputStreamProvider, getExpandDir(), fileSystem, directoryFileTreeFactory, fileHasher);
        return new FileTreeAdapter(dumpFileTree, patternSetFactory);
    }

    private File getExpandDir() {
        return temporaryFileProvider.newTemporaryFile("expandedArchives");
    }

}
