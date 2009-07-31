/*******************************************************************************
 * Copyright (c) 2004, 2008 Elias Volanakis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elias Volanakis - initial API and implementation
 *******************************************************************************/
package pl.ivmx.mappum.gui;

import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteToolbar;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.jface.resource.ImageDescriptor;

import pl.ivmx.mappum.gui.model.Connection;

/**
 * Utility class that can create a GEF Palette.
 * 
 * @see #createPalette()
 * @author Elias Volanakis
 */
final class MappumEditorPaletteFactory {
	private static final String PALETTE_DOCK_LOCATION = "ShapesEditorPaletteFactory.Location";
	private static final String PALETTE_SIZE = "ShapesEditorPaletteFactory.Size";
	private static final String PALETTE_STATE = "ShapesEditorPaletteFactory.State";

	/**
	 * Creates the PaletteRoot and adds all palette elements.
	 */
	static PaletteRoot createPalette() {
		PaletteRoot palette = new PaletteRoot();
		palette.add(createToolsGroup(palette));
		return palette;
	}

	/** Create the "Tools" group. */
	private static PaletteContainer createToolsGroup(PaletteRoot palette) {
		PaletteToolbar toolbar = new PaletteToolbar("Tools");
		// Add a selection tool
		ToolEntry tool = new PanningSelectionToolEntry();
		toolbar.add(tool);
		palette.setDefaultEntry(tool);
		// Add connection tool
		tool = new ConnectionCreationToolEntry("Connection",
				"Create a connection", new CreationFactory() {
					public Object getNewObject() {
						return null;
					}
					public Object getObjectType() {
						//return null;
						return Connection.DUAL_SIDE;
					}
				}, ImageDescriptor.createFromFile(MappumPlugin.class,
						"icons/connection_s16.gif"), ImageDescriptor
						.createFromFile(MappumPlugin.class,
								"icons/connection_s24.gif"));
		toolbar.add(tool);

		return toolbar;
	}

	/** Utility class. */
	private MappumEditorPaletteFactory() {
		// Utility class
	}

}