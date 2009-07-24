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

public class XMLSchemaImportWizard extends Wizard implements IImportWizard {
	private Logger logger = Logger.getLogger(XMLSchemaImportWizard.class);
	private XMLSchemaImportWizardPage firstPage;
	private XMLSchemaImportWizardPage secondPage;

	public XMLSchemaImportWizard() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		final IFile file1 = firstPage.createNewFile();
		final IFile file2 = secondPage.createNewFile();
		if (file1 == null || file2 == null)
			return false;
		logger.debug("Imported 2 xsd schemas: " + file1.getName() + ", "
				+ file2.getName());
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		logger.debug("XML Schema Import wizzard started.");
		setWindowTitle("File Import Wizard"); // NON-NLS-1
		setNeedsProgressMonitor(true);
		firstPage = new XMLSchemaImportWizardPage(
				"Import first Ruby XML Schema", selection);
		secondPage = new XMLSchemaImportWizardPage(
				"Import second Ruby XML Schema", selection); // NON-NLS-1

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		super.addPages();
		addPage(firstPage);
		addPage(secondPage);
	}

}
