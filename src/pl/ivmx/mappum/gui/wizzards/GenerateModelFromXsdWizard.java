package pl.ivmx.mappum.gui.wizzards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.jruby.RubyArray;

import pl.ivmx.mappum.gui.utils.ModelGeneratorFromXML;
import pl.ivmx.mappum.gui.utils.RootNodeHolder;

public class GenerateModelFromXsdWizard extends Wizard {
	private GenerateModelFromXsdWizardPage page;
	private Logger logger = Logger.getLogger(GenerateModelFromXsdWizard.class);
	private RubyArray model;
	private String leftChoosenElement = null;
	private String rightChoosenElement = null;
	private IWorkbench workbench;
	private IProject project;

	public GenerateModelFromXsdWizard(RubyArray model) {
		super();
		setModel(model);
	}

	@Override
	public boolean performFinish() {
		final String leftElement = leftChoosenElement;
		final String rightElement = rightChoosenElement;

		if (leftElement != null && rightElement != null) {
			IFile file = getMapFile(null);
			/*
			 * IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
			 * .getActivePage(); if (file != null && page != null) { try {
			 * IDE.openEditor(page, file, "pl.ivmx.mappum.gui.editor"); } catch
			 * (PartInitException e) {
			 * logger.error("Error performing finish operations: " +
			 * e.getCause().getMessage()); } }
			 */

			logger.debug("Generate model from XSD wizard ended.");
			return true;
		}
		return false;

	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		logger.debug("Generate model from XSD schema wizard started.");
		setWindowTitle("Generate model from XSD schema"); // NON-NLS-1
		setNeedsProgressMonitor(true);
		this.workbench = workbench;
		if (selection.getFirstElement() instanceof IProject) {
			project = (IProject) selection.getFirstElement();
		}
		page = new GenerateModelFromXsdWizardPage(
				"Generate model from XSD schema");
	}

	public void addPages() {
		super.addPages();
		addPage(page);
	}

	private void setModel(RubyArray model) {
		this.model = model;
	}

	public RubyArray getModel() {
		return model;
	}

	public void setLeftChoosenElement(String leftChoosenElement) {
		this.leftChoosenElement = leftChoosenElement;
	}

	public String getLeftChoosenElement() {
		return leftChoosenElement;
	}

	public void setRightChoosenElement(String rightChoosenElement) {
		this.rightChoosenElement = rightChoosenElement;
	}

	public String getRightChoosenElement() {
		return rightChoosenElement;
	}

	private IFile getMapFile(IProgressMonitor monitor) {
		String leftElementSimple = leftChoosenElement.split("::")[leftChoosenElement
				.split("::").length - 1];
		String rightElementSimple = rightChoosenElement.split("::")[rightChoosenElement
				.split("::").length - 1];
		IPath fullPath = project.getFullPath();

		IFolder mapFolder = ResourcesPlugin
				.getWorkspace()
				.getRoot()
				.getFolder(
						fullPath
								.append(ModelGeneratorFromXML.DEFAULT_WORKING_MAP_FOLDER));
		if (mapFolder.exists()) {
			IPath filePath = fullPath.append(
					ModelGeneratorFromXML.DEFAULT_WORKING_MAP_FOLDER).append(
					leftElementSimple + rightElementSimple + ".rb");
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(
					filePath);
			if (file == null || !file.exists()) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				try {
					out.write(generateRubyCode().getBytes());
					out.close();
					IFile newFile = project.getFile(filePath);
					file.create(new ByteArrayInputStream(out.toByteArray()),
							true, monitor);
					return newFile;
				} catch (CoreException e) {
					e.printStackTrace();
					logger.error("Error while generating model:  "
							+ e.getCause().getMessage());
					return null;
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("Error while generating model:  "
							+ e.getCause().getMessage());
					return null;
				}

			} else {
				return file;
			}

		}
		return null;

	}

	private String generateRubyCode() {
		String leftPrefix = RootNodeHolder.getInstance().generateRandomIdent(
				RootNodeHolder.IDENT_LENGTH);
		String rightPrefix = RootNodeHolder.getInstance().generateRandomIdent(
				RootNodeHolder.IDENT_LENGTH);
		StringBuffer params = new StringBuffer();
		params.append("\n");
		params.append("require 'mappum'");
		params.append("\n\n");
		params.append("Mappum.catalogue_add do");
		params.append("\n");
		params.append("  map " + leftChoosenElement + ", "
				+ rightChoosenElement + " do |" + leftPrefix + ", "
				+ rightPrefix + "|");
		params.append("\n");
		params.append("  end");
		params.append("\n");
		params.append("end");
		params.append("\n");
		return params.toString();
	}
}
