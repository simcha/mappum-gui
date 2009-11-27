package pl.ivmx.mappum.gui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import pl.ivmx.mappum.MappumApi;

public class MapServerLauncher implements ILaunchConfigurationDelegate{

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		try{
		MappumApi mappumApi = new MappumApi();
		mappumApi.getWorkdirLoader();
		mappumApi.loadMaps();
		mappumApi.startServer();
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, "MappumGUI", e.getMessage(), e);
			throw new DebugException(status);
		}
	}

}
