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

public final class MappumEditorPaletteFactory {

	private static final String ICON_CONN_S16 = "icons/connection_s16.gif";
	private static final String ICON_CONN_S24 = "icons/connection_s24.gif";
	private static final String ICON_CONN_D16 = "icons/connection_d16.gif";
	private static final String ICON_CONN_D24 = "icons/connection_d24.gif";
	private static final String ICON_CONN_C16 = "icons/connection_c16.gif";
	private static final String ICON_CONN_C24 = "icons/connection_c24.gif";

	public static final ToolEntry SELECTION_TOOL = new PanningSelectionToolEntry();

	public static final ToolEntry CONNECTION_DUAL_TOOL = createTool(
			"Connection", "Creates dual side connection", ICON_CONN_D16,
			ICON_CONN_D24, new Connection.Info(Connection.Side.DUAL,
					Connection.Type.VAR_TO_VAR_CONN));

	public static final ToolEntry CONNECTION_SIMPLE_TOOL = createTool(
			"Connection", "Creates single side connection", ICON_CONN_S16,
			ICON_CONN_S24, new Connection.Info(Connection.Side.LEFT_TO_RIGHT,
					Connection.Type.VAR_TO_VAR_CONN));

//	public static final ToolEntry CONNECTION_CONSTANT_TOOL = createTool(
//			"Constant", "Creates constant", ICON_CONN_C16, ICON_CONN_C24,
//			new Connection.Info(Connection.Side.LEFT_TO_RIGHT,
//					Connection.Type.CONST_TO_VAR_CONN));

	private static ToolEntry createTool(final String label,
			final String shortDesc, final String iconSmall,
			final String iconLarge, final Connection.Info info) {
		return new ConnectionCreationToolEntry(label, shortDesc,
				new CreationFactory() {

					public Object getNewObject() {
						return null;
					}

					public Object getObjectType() {
						return info;
					}
				}, createImage(iconSmall), createImage(iconLarge));
	}

	private static ImageDescriptor createImage(final String fileName) {
		return ImageDescriptor.createFromFile(MappumPlugin.class, fileName);
	}

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
		toolbar.add(CONNECTION_DUAL_TOOL);
		toolbar.add(CONNECTION_SIMPLE_TOOL);
		//toolbar.add(CONNECTION_CONSTANT_TOOL);
		palette.setDefaultEntry(SELECTION_TOOL);

		return toolbar;
	}
}