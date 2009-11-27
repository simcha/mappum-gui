package pl.ivmx.mappum.gui.launch;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class MapserverLaunchConfigurationTab implements ILaunchConfigurationTab {

	private String name;

	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		// TODO Auto-generated method stub

	}

	public boolean canSave() {
		// TODO Auto-generated method stub
		return true;
	}

	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
	}

	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Control getControl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return "Error";
	}

	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMessage() {
		// TODO Auto-generated method stub
		return "Message";
	}

	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		// TODO Auto-generated method stub
		
	}

	public boolean isValid(ILaunchConfiguration launchConfig) {
		// TODO Auto-generated method stub
		return true;
	}

	public void launched(ILaunch launch) {
		// TODO Auto-generated method stub

	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub
		name = configuration.getName();
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub

	}

	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		// TODO Auto-generated method stub

	}

}
