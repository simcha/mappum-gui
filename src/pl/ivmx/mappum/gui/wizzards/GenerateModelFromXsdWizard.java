package pl.ivmx.mappum.gui.wizzards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.jruby.RubyArray;

import pl.ivmx.mappum.gui.utils.ModelGeneratorFromXML;
import pl.ivmx.mappum.gui.utils.RootNodeHolder;

public class GenerateModelFromXsdWizard extends Wizard {
	private GenerateModelFromXsdWizardPage page;
	private Logger logger = Logger.getLogger(GenerateModelFromXsdWizard.class);
	private RubyArray model;
	private String leftChosenElement = null;
	private String rightChosenElement = null;
	private IProject project;

	public GenerateModelFromXsdWizard(RubyArray model) {
		super();
		setModel(model);
	}

	@Override
	public boolean performFinish() {
		try {
			if (leftChosenElement != null && rightChosenElement != null) {
				getMapFile(null);
				return true;
			}
			return false;
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Error while generating model:  " + e.getMessage());
			final MessageBox mb = new MessageBox(getShell(), SWT.ERROR);
			mb.setMessage("Model generation failed:\n" + e.getMessage());
			mb.setText("Problem occurred");
			mb.open();
			return false;
		} finally {
			logger.debug("Generate model from XSD wizard ended.");
		}
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
		this.leftChosenElement = leftChoosenElement;
	}

	public String getLeftChoosenElement() {
		return leftChosenElement;
	}

	public void setRightChoosenElement(String rightChoosenElement) {
		this.rightChosenElement = rightChoosenElement;
	}

	public String getRightChoosenElement() {
		return rightChosenElement;
	}

	private String getSimpleElementName(final String element) {
		return element.split("::")[element.split("::").length - 1];
	}

	private IFile getMapFile(IProgressMonitor monitor) throws Exception {

		final String leftElementSimple = getSimpleElementName(leftChosenElement);
		final String rightElementSimple = getSimpleElementName(rightChosenElement);

		final IPath fullPath = project.getFullPath();

		final IFolder mapFolder = ResourcesPlugin
				.getWorkspace()
				.getRoot()
				.getFolder(
						fullPath
								.append(ModelGeneratorFromXML.DEFAULT_WORKING_MAP_FOLDER));
		if (!mapFolder.exists()) {
			mapFolder.create(false, false, null);
		}
		final IPath filePath = fullPath.append(
				ModelGeneratorFromXML.DEFAULT_WORKING_MAP_FOLDER).append(
				leftElementSimple + rightElementSimple + ".rb");
		final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(
				filePath);
		if (!file.exists()) {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			out.write(generateRubyCode().getBytes());
			out.close();
			file.create(new ByteArrayInputStream(out.toByteArray()), true,
					monitor);
		}
		return file;
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
		params.append("  map " + leftChosenElement + ", " + rightChosenElement
				+ " do |" + leftPrefix + ", " + rightPrefix + "|");
		params.append("\n");
		params.append("  end");
		params.append("\n");
		params.append("end");
		params.append("\n");
		return params.toString();
	}
}
