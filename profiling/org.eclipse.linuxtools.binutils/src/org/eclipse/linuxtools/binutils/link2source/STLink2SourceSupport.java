/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.binutils.link2source;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.binutils.Activator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This class provides a support for link-to-source
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class STLink2SourceSupport {

    /**
     * Shared instance of this class Note: perhaps it's better to put all methods static ?
     */
    public static final STLink2SourceSupport sharedInstance = new STLink2SourceSupport();

    protected STLink2SourceSupport() {
    }

    /**
     * Open a C Editor at the given location
     *
     * @param binaryLoc
     *            a path to a binary file
     * @param sourceLoc
     *            the location of the source file
     * @param lineNumber
     * @return <code>true</code> if the link-to-source was successful, <code>false</code> otherwise
     */
    private boolean openSourceFileAtLocation(IPath binaryLoc, IPath sourceLoc, int lineNumber) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IFile binary = root.getFileForLocation(binaryLoc);
        IProject project = null;
        if (binary != null)
            project = binary.getProject();
        return openFileImpl(project, sourceLoc, lineNumber);
    }

    /**
     * Open a C Editor at the given location
     *
     * @param project
     *            the parent project
     * @param sourceLoc
     *            the location of the source file
     * @param lineNumber
     * @return <code>true</code> if the link-to-source was successful, <code>false</code> otherwise
     */
    public boolean openSourceFileAtLocation(IProject project, IPath sourceLoc, int lineNumber) {
        return openFileImpl(project, sourceLoc, lineNumber);
    }

    /**
     * Open a C Editor at the given location
     *
     * @param binary
     *            a binary file
     * @param sourceLoc
     *            the location of the source file
     * @param lineNumber
     * @return <code>true</code> if the link-to-source was successful, <code>false</code> otherwise
     */
    public boolean openSourceFileAtLocation(IBinaryObject binary, String sourceLoc, int lineNumber) {
        if (sourceLoc == null)
            return false;
        IPath p = new Path(sourceLoc);
        return openSourceFileAtLocation(binary.getPath(), p, lineNumber);
    }

    private boolean openFileImpl(IProject project, IPath sourceLoc, int lineNumber) {
        if (sourceLoc == null || "??".equals(sourceLoc.toString())) //$NON-NLS-1$
            return false;
        try {
            IEditorInput editorInput = getEditorInput(sourceLoc, project);
            IWorkbenchPage p = CUIPlugin.getActivePage();
            if (p != null) {
                if (editorInput == null) {
                    p.openEditor(new STCSourceNotFoundEditorInput(project, sourceLoc, lineNumber),
                            STCSourceNotFoundEditor.ID, true);
                } else {
                    IEditorPart editor = p.openEditor(editorInput, CUIPlugin.EDITOR_ID, true);
                    if (lineNumber > 0 && editor instanceof ITextEditor) {
                        IDocumentProvider provider = ((ITextEditor) editor).getDocumentProvider();
                        IDocument document = provider.getDocument(editor.getEditorInput());
                        try {
                            int start = document.getLineOffset(lineNumber - 1);
                            ((ITextEditor) editor).selectAndReveal(start, 0);
                            IWorkbenchPage page = editor.getSite().getPage();
                            page.activate(editor);
                            return true;
                        } catch (BadLocationException x) {
                            // ignore
                        }
                    }
                }
            }
        } catch (Exception _) {
        }
        return false;
    }

    public IEditorInput getEditorInput(IPath p, IProject project) {
        IFile f = getFileForPath(p, project);
        if (f != null && f.exists()) {
            return new FileEditorInput(f);
        }
        if (p.isAbsolute()) {
            File file = p.toFile();
            if (file.exists()) {
                try {
                    IFileStore ifs = EFS.getStore(file.toURI());
                    return new FileStoreEditorInput(ifs);
                } catch (CoreException _) {
                    Activator.getDefault().getLog().log(_.getStatus());
                }
            }
        }
        return findFileInCommonSourceLookup(p);
    }

    private IEditorInput findFileInCommonSourceLookup(IPath path) {
        try {
            AbstractSourceLookupDirector director = CDebugCorePlugin.getDefault().getCommonSourceLookupDirector();
            ISourceContainer[] c = director.getSourceContainers();
            for (ISourceContainer sourceContainer : c) {
                Object[] o = sourceContainer.findSourceElements(path.toOSString());
                for (Object object : o) {
                    if (object instanceof IFile) {
                        return new FileEditorInput((IFile) object);
                    } else if (object instanceof LocalFileStorage) {
                        LocalFileStorage storage = (LocalFileStorage) object;
                        IFileStore ifs = EFS.getStore(storage.getFile().toURI());
                        return new FileStoreEditorInput(ifs);
                    }
                }
            }
        } catch (Exception _) {
            // do nothing
        }
        return null;
    }

    /**
	 * @since 5.0
	 */
    public IFile getFileForPath(IPath path, IProject project) {
        IFile f = getFileForPathImpl(path, project);
        if (f == null) {
            Set<IProject> allProjects = new HashSet<>();
            try {
                getAllReferencedProjects(allProjects, project);
            } catch (CoreException _) {
                Activator.getDefault().getLog().log(_.getStatus());
            }
            if (allProjects != null) {
                for (IProject project2 : allProjects) {
                    f = getFileForPathImpl(path, project2);
                    if (f != null)
                        break;
                }
            }
        }
        return f;
    }

    private IFile getFileForPathImpl(IPath path, IProject project) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        if (path.isAbsolute()) {
            IFile c = root.getFileForLocation(path);
            return c;
        }
        if (project != null && project.exists()) {
            ICProject cproject = CoreModel.getDefault().create(project);
            if (cproject != null) {
                try {
                    ISourceRoot[] roots = cproject.getAllSourceRoots();
                    for (ISourceRoot sourceRoot : roots) {
                        IResource r = sourceRoot.getResource();
                        if (r instanceof IContainer) {
                            IContainer parent = (IContainer) r;
                            IResource res = parent.findMember(path);
                            if (res != null && res.exists() && res instanceof IFile) {
                                IFile file = (IFile) res;
                                return file;
                            }
                        }
                    }

                    IOutputEntry entries[] = cproject.getOutputEntries();
                    for (IOutputEntry pathEntry : entries) {
                        IPath p = pathEntry.getPath();
                        IResource r = root.findMember(p);
                        if (r instanceof IContainer) {
                            IContainer parent = (IContainer) r;
                            IResource res = parent.findMember(path);
                            if (res != null && res.exists() && res instanceof IFile) {
                                IFile file = (IFile) res;
                                return file;
                            }
                        }
                    }

                } catch (CModelException _) {
                    Activator.getDefault().getLog().log(_.getStatus());
                }
            }
        }
        return null;
    }

    private void getAllReferencedProjects(Set<IProject> all, IProject project) throws CoreException {
        if (project != null) {
            IProject[] refs = project.getReferencedProjects();
            for (int i = 0; i < refs.length; i++) {
                if (!all.contains(refs[i]) && refs[i].exists() && refs[i].isOpen()) {
                    all.add(refs[i]);
                    getAllReferencedProjects(all, refs[i]);
                }
            }
        }
    }

}
