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
import org.jruby.RubyArray;

import pl.ivmx.mappum.gui.utils.ModelGeneratorFromXML;
import pl.ivmx.mappum.gui.utils.RootNodeHolder;

public class GenerateModelFromXsdWizard extends Wizard {
	private GenerateModelFromXsdWizardPage page;
	private Logger logger = Logger.getLogger(GenerateModelFromXsdWizard.class);
	private RubyArray model;
	private String leftChoosenElement = null;
	private String rightChoosenElement = null;
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
			getMapFile(null);
			logger.debug("Generate model from XSD wizard ended.");
			return true;
		}
		return false;
	}

	public void init(IStructuredSelection selection) {
		logger.debug("Generate model from XSD schema wizard started.");
		setWindowTitle("Generate model from XSD schema"); // NON-NLS-1
		setNeedsProgressMonitor(true);
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

	private String getSimpleElementName(final String element) {
		return element.split("::")[element.split("::").length - 1];
	}

	private IFile getMapFile(IProgressMonitor monitor) {

		final String leftElementSimple = getSimpleElementName(leftChoosenElement);
		final String rightElementSimple = getSimpleElementName(rightChoosenElement);

		final IPath fullPath = project.getFullPath();

		final IFolder mapFolder = ResourcesPlugin
				.getWorkspace()
				.getRoot()
				.getFolder(
						fullPath
								.append(ModelGeneratorFromXML.DEFAULT_WORKING_MAP_FOLDER));
		if (mapFolder.exists()) {
			final IPath filePath = fullPath.append(
					ModelGeneratorFromXML.DEFAULT_WORKING_MAP_FOLDER).append(
					leftElementSimple + rightElementSimple + ".rb");
			final IFile file = ResourcesPlugin.getWorkspace().getRoot()
					.getFile(filePath);
			if (!file.exists()) {
				try {
					final ByteArrayOutputStream out = new ByteArrayOutputStream();
					out.write(generateRubyCode().getBytes());
					out.close();
					file.create(new ByteArrayInputStream(out.toByteArray()),
							true, monitor);
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
			}
			return file;
		}
		return null;
	}

	private String generateRubyCode() {
		String leftPrefix = RootNodeHolder.getInstance().generateRandomIdent();
		String rightPrefix = RootNodeHolder.getInstance().generateRandomIdent();
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
