package io.freefair.gradle.plugins.compress.tree;

import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.internal.file.collections.FileSystemMirroringFileTree;
import org.gradle.api.internal.tasks.TaskDependencyContainer;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.provider.Provider;
import org.gradle.cache.internal.DecompressionCoordinator;

import java.io.File;

/**
 * Abstract base class for a {@link org.gradle.api.file.FileTree FileTree} that is backed by an archive file.
 *
 * Will decompress the archive file to the given cache.
 */
/* package */ abstract class AbstractArchiveFileTree implements FileSystemMirroringFileTree, TaskDependencyContainer {
    protected final DecompressionCoordinator decompressionCoordinator;

    protected AbstractArchiveFileTree(DecompressionCoordinator decompressionCoordinator) {
        this.decompressionCoordinator = decompressionCoordinator;
    }

    abstract protected Provider<File> getBackingFileProvider();

    private File getBackingFile() {
        return getBackingFileProvider().get();
    }

    @Override
    public void visitStructure(MinimalFileTreeStructureVisitor visitor, FileTreeInternal owner) {
        File backingFile = getBackingFile();
        visitor.visitFileTreeBackedByFile(backingFile, owner, this);
    }

    @Override
    public void visitDependencies(TaskDependencyResolveContext context) {
        context.add(getBackingFileProvider());
    }
}
