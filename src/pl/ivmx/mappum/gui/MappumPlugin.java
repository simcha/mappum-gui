package pl.ivmx.mappum.gui;

import org.eclipse.ui.plugin.AbstractUIPlugin;

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