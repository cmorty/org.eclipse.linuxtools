/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat Inc.
 *******************************************************************************/

package org.eclipse.linuxtools.profiling.ui;

/**
 * <h1> Wrapper with get/set functionality for a boolean.</h1>
 *
 * <p>
 * The package-local {@link MessageDialogSyncedRunnable  MessageDialogSyncedRunnable} class relies <br>
 * upon a way to set/get a boolean (it is used to transfer a boolean between synced threads). <br>
 * This class was created for this purpose.
 * </p>
 * @since 3.1
 */
public class BooleanWithGetSet {
    private boolean val;

    /**
     * <h1> Boolean Constructor. </h1>
     * Pass in a boolean to construct this class.
     *
     * @param val  true/false boolean
     */
    public BooleanWithGetSet(boolean val) {
        this.val = val;
    }

    /**
     * <h1> Get Value. </h1>
     * Retrieve the boolean value of this instance.
     *
     * @return     boolean value of this instance
     */
    public boolean getVal() {
        return this.val;
    }

    /**
     * <h1> Set value </h1>
     * Set the content of the boolean class.
     *
     * @param val  true/false
     */
    public void setVal(boolean val) {
        this.val = val;
    }
}