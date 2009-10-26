package pl.ivmx.mappum.gui.wizzards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import pl.ivmx.mappum.TreeElement;
import pl.ivmx.mappum.gui.utils.ModelGeneratorFromXML;
import pl.ivmx.mappum.gui.utils.RootNodeHolder;

public class GenerateModelFromXsdWizard extends Wizard {
	private GenerateModelFromXsdWizardPage page;
	private Logger logger = Logger.getLogger(GenerateModelFromXsdWizard.class);
	private List<TreeElement> model;
	private IProject project;

	public GenerateModelFromXsdWizard(final List<TreeElement> model) {
		setModel(model);
	}

	@Override
	public boolean performFinish() {
		try {
			if (getLeftChosenElement() != null
					&& getRightChosenElement() != null) {
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
		if (selection.getFirstElement() instanceof IJavaProject) {
			project = ((IJavaProject) selection.getFirstElement()).getProject();
		}
		page = new GenerateModelFromXsdWizardPage(
				"Generate model from XSD schema");
	}

	public void addPages() {
		super.addPages();
		addPage(page);
	}

	private void setModel(final List<TreeElement> model) {
		this.model = model;
	}

	public List<TreeElement> getModel() {
		return model;
	}

	public SelectedType getLeftChosenElement() {
		return page.getLeftSelectedType();
	}

	public SelectedType getRightChosenElement() {
		return page.getRightSelectedType();
	}

	private IFile getMapFile(IProgressMonitor monitor) throws Exception {

		final String leftElementSimple = getLeftChosenElement().getName();
		final String rightElementSimple = getRightChosenElement().getName();

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
		params.append("  map " + getLeftChosenElement().getPrefixedName() + ", "
				+ getRightChosenElement().getPrefixedName() + " do |" + leftPrefix
				+ ", " + rightPrefix + "|");
		params.append("\n");
		params.append("  end");
		params.append("\n");
		params.append("end");
		params.append("\n");
		return params.toString();
	}
}
