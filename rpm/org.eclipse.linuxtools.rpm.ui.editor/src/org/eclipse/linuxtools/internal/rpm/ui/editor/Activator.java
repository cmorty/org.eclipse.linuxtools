/*******************************************************************************
 * Copyright (c) 2007, 2021 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *    Alphonse Van Assche
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.rpm.ui.editor"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private ContributionTemplateStore fTemplateStore;
	private ContributionContextTypeRegistry fContextTypeRegistry;

	// RPM Groups
	private List<String> rpmGroups = new ArrayList<>();

	// RPM package list
	public static RpmPackageProposalsList packagesList;

	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		RpmPackageBuildProposalsJob.setPropertyChangeListener(true);
		RpmPackageBuildProposalsJob.update(false);
		// Do some sanity checks.
		UiUtils.pluginSanityCheck();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		RpmPackageBuildProposalsJob.setPropertyChangeListener(false);
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public TemplateStore getTemplateStore() {
		if (fTemplateStore == null) {
			fTemplateStore = new ContributionTemplateStore(getContextTypeRegistry(), getPreferenceStore(), "templates"); //$NON-NLS-1$
			try {
				fTemplateStore.load();
			} catch (IOException e) {
				SpecfileLog.logError(e);
			}
		}

		return fTemplateStore;
	}

	public RpmMacroProposalsList getRpmMacroList() {
		return new RpmMacroProposalsList();
	}

	public RpmPackageProposalsList getRpmPackageList() {
		if (packagesList == null || packagesList.getProposals("").isEmpty()) { //$NON-NLS-1$
			packagesList = new RpmPackageProposalsList();
		}
		return packagesList;
	}

	public List<String> getRpmGroups() {
		if (rpmGroups.isEmpty()) {
			// FIXME: Can we assume that all distros place
			// documentations files in the below path?
			String docDir = "/usr/share/doc/"; //$NON-NLS-1$
			File dir = new File(docDir);
			if (dir.exists()) {
				File files[] = dir.listFiles((FilenameFilter) (dir1, name) -> name.startsWith("rpm-")); //$NON-NLS-1$
				try {
					// We can not be sure that there is only one directory here
					// starting with rpm-
					// (e.g. rpm-apidocs is the wrong directory.)
					for (File file : files) {
						File groupsFile = new File(file, "GROUPS"); //$NON-NLS-1$
						if (groupsFile.exists()) {

							try (LineNumberReader reader = new LineNumberReader(new FileReader(groupsFile))) {
								String line;
								while ((line = reader.readLine()) != null) {
									rpmGroups.add(line);
								}
							}
							break;
						}
					}
				} catch (IOException e) {
					SpecfileLog.logError(e);
				}
			}
		}
		return rpmGroups;
	}

	public ContextTypeRegistry getContextTypeRegistry() {
		if (fContextTypeRegistry == null) {
			fContextTypeRegistry = new ContributionContextTypeRegistry();
			fContextTypeRegistry.addContextType("org.eclipse.linuxtools.rpm.ui.editor.preambleSection"); //$NON-NLS-1$
			fContextTypeRegistry.addContextType("org.eclipse.linuxtools.rpm.ui.editor.preSection"); //$NON-NLS-1$
			fContextTypeRegistry.addContextType("org.eclipse.linuxtools.rpm.ui.editor.buildSection"); //$NON-NLS-1$
			fContextTypeRegistry.addContextType("org.eclipse.linuxtools.rpm.ui.editor.installSection"); //$NON-NLS-1$
			fContextTypeRegistry.addContextType("org.eclipse.linuxtools.rpm.ui.editor.changelogSection"); //$NON-NLS-1$
		}
		return fContextTypeRegistry;
	}

	/**
	 * Get a <code>Image</code> object for the given relative path.
	 *
	 * @param imageRelativePath The relative path to the image.
	 * @return a <code>Image</code>
	 */
	public Image getImage(String imageRelativePath) {
		ImageRegistry registry = getImageRegistry();
		Image image = registry.get(imageRelativePath);
		if (image == null) {
			ImageDescriptor desc = ResourceLocator.imageDescriptorFromBundle(PLUGIN_ID, imageRelativePath).get();
			registry.put(imageRelativePath, desc);
			image = registry.get(imageRelativePath);
		}
		return image;
	}

}
