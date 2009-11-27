package pl.ivmx.mappum.gui.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;

public class MapserverLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup {

	public MapserverLaunchConfigurationTabGroup() {
		super();
	}

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		setTabs(new MapserverLaunchConfigurationTab[]{new MapserverLaunchConfigurationTab()});
	}
}
