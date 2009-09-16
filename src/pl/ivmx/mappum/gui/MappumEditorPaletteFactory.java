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

final class MappumEditorPaletteFactory {

	public static final ToolEntry SELECTION_TOOL = new PanningSelectionToolEntry();
	public static final ToolEntry CONNECTION_TOOL = new ConnectionCreationToolEntry(
			"Connection", "Create a connection", new CreationFactory() {
				public Object getNewObject() {
					return null;
				}

				public Object getObjectType() {
					// return null;
					return Connection.DUAL_SIDE;
				}
			}, ImageDescriptor.createFromFile(MappumPlugin.class,
					"icons/connection_s16.gif"), ImageDescriptor
					.createFromFile(MappumPlugin.class,
							"icons/connection_s24.gif"));

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
		toolbar.add(SELECTION_TOOL);
		toolbar.add(CONNECTION_TOOL);
		palette.setDefaultEntry(SELECTION_TOOL);

		return toolbar;
	}
}