/*******************************************************************************
 * Copyright (c) 2008, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.ui;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ProfileUIPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.profiling.ui"; //$NON-NLS-1$

    // The shared instance
    private static ProfileUIPlugin plugin;

    /**
     * The constructor
     */
    public ProfileUIPlugin() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static ProfileUIPlugin getDefault() {
        return plugin;
    }

    public static Shell getActiveWorkbenchShell() {
        IWorkbenchWindow window = getDefault().getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            return window.getShell();
        }
        return null;
    }

}
