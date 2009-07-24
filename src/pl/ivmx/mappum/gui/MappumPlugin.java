/*******************************************************************************
 * Copyright (c) 2004, 2005 Elias Volanakis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elias Volanakis - initial API and implementation
 *******************************************************************************/
package pl.ivmx.mappum.gui;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The plugin class (singleton).
 * <p>
 * This instance can be shared between all extensions in the plugin. Information
 * shared between extensions can be persisted by using the PreferenceStore.
 * </p>
 * @see org.eclipse.ui.plugin.AbstractUIPlugin#getPreferenceStore()
 * @author Elias Volanakis
 */
public class MappumPlugin extends AbstractUIPlugin {

/** Single plugin instance. */
private static MappumPlugin singleton;

/**
 * Returns the shared plugin instance.
 */
public static MappumPlugin getDefault() {
	return singleton;
}

/** 
 * The constructor. 
 */
public MappumPlugin() {
	if (singleton == null) {
		singleton = this;
	}
}

}