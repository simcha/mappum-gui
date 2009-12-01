package pl.ivmx.mappum.gui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.palette.ToolEntry;

public interface IMappumEditor {

	void reload();

	void doSave(IProgressMonitor monitor);
	
	public IProject getProject();

	ToolEntry getCurrentPaletteTool();
}
