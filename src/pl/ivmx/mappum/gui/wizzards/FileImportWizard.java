/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package pl.ivmx.mappum.gui.wizzards;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

public class FileImportWizard extends Wizard implements IImportWizard {
	private Logger logger = Logger.getLogger(FileImportWizard.class);
	FileImportWizardPage mainPage;
	IWorkbench workbench;

	public FileImportWizard() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		IFile file = mainPage.createNewFile();
		if (file == null)
			return false;
		else {
			logger.debug("Imported map file: " + file.getName());
			IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
					.getActivePage();
			if (file != null && page != null) {
				try {
					IDE.openEditor(page, file, "pl.ivmx.mappum.gui.editor");
				} catch (PartInitException e) {
					logger.error("Error performing finish operations: "
							+ e.getCause().getMessage());
					return false;
				}
			}
		}
		logger.debug("Map import wizzard ended.");
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		logger.debug("Map import wizzard started.");
		this.workbench = workbench;
		setWindowTitle("File Import Wizard"); // NON-NLS-1
		setNeedsProgressMonitor(true);
		mainPage = new FileImportWizardPage("Import Ruby Map", selection); // NON-NLS-1
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		super.addPages();
		addPage(mainPage);
	}

}
