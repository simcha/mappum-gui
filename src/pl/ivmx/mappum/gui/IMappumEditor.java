package pl.ivmx.mappum.gui;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IMappumEditor {

	void reload();

	void doSave(IProgressMonitor monitor);
}
