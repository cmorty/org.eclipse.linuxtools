/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.experiment;

import org.eclipse.linuxtools.tmf.trace.ITmfLocation;

public class TmfLocationArray implements Comparable<TmfLocationArray>, Cloneable {
	public ITmfLocation<? extends Comparable<?>>[] locations;
	
	public TmfLocationArray(ITmfLocation<? extends Comparable<?>>[] locations) {
		this.locations = locations;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int compareTo(TmfLocationArray o) {
		for (int i = 0; i < locations.length; i++) {
			ITmfLocation<? extends Comparable> l1 = (ITmfLocation<? extends Comparable>) locations[i].getLocation();
			ITmfLocation<? extends Comparable> l2 = (ITmfLocation<? extends Comparable>) o.locations[i].getLocation();
			int result = l1.getLocation().compareTo(l2.getLocation());
			if (result != 0) {
				return result;
			}
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected TmfLocationArray clone() {
		ITmfLocation<? extends Comparable<?>>[] clones = (ITmfLocation<? extends Comparable<?>>[]) new ITmfLocation<?>[locations.length];
		for (int i = 0; i < locations.length; i++) {
			clones[i] = locations[i].clone();
		}
		return new TmfLocationArray(clones);
	}
	
}

