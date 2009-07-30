package pl.ivmx.mappum.gui.wizzards;

import org.apache.log4j.Logger;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import pl.ivmx.mappum.gui.utils.ModelGeneratorFromXML;
import pl.ivmx.mappum.gui.utils.ProjectProperties;

public class NewProjectWizard extends Wizard implements INewWizard {
	private NewProjectWizardPage page;
	private Logger logger = Logger.getLogger(NewProjectWizard.class);

	public NewProjectWizard() {
		super();
	}

	private IFolder createFolderHandle(IPath folderPath) {

		return ResourcesPlugin.getWorkspace().getRoot().getFolder(folderPath);
	}

	@Override
	public boolean performFinish() {
		IProject projectHandle = page.getProjectHandle();
		try {
			projectHandle.create(null);
			projectHandle.open(null);
		} catch (CoreException e) {
			logger.error("Error performing finish operations: "
					+ e.getCause().getMessage());
		}
		Workspace workspace = (Workspace) projectHandle.getWorkspace();

		IPath projectPath = projectHandle.getFullPath();
		IPath folderMapPath = projectPath
				.append(ModelGeneratorFromXML.DEFAULT_MAP_FOLDER);
		IPath folderWorkingMapPath = projectPath
				.append(ModelGeneratorFromXML.DEFAULT_WORKING_MAP_FOLDER);
		IPath folderSchemaPath = projectPath
				.append(ModelGeneratorFromXML.DEFAULT_SCHEMA_FOLDER);
		IPath folderClassesPath = projectPath
				.append(ModelGeneratorFromXML.DEFAULT_GENERATED_CLASSES_FOLDER);
		IFolder folderMap = createFolderHandle(folderMapPath);
		IFolder folderWorkingMap = createFolderHandle(folderWorkingMapPath);
		IFolder folderSchema = createFolderHandle(folderSchemaPath);
		IFolder folderClasses = createFolderHandle(folderClassesPath);
		try {
			folderMap.create(false, true, null);
			folderWorkingMap.create(false, true, null);
			folderSchema.create(false, true, null);
			folderClasses.create(false, true, null);
		} catch (CoreException e) {
			logger.error("Error performing finish operations: "
					+ e.getCause().getMessage());
		}
		ProjectProperties properties = new ProjectProperties(projectHandle);
		properties.setProperty(ProjectProperties.CLASSES_DIRECTORY_PROPS,
				projectHandle.getLocation().append(
						ModelGeneratorFromXML.DEFAULT_GENERATED_CLASSES_FOLDER)
						.toPortableString());
		properties.setProperty(ProjectProperties.MAP_DIRECTORY_PROPS,
				projectHandle.getLocation().append(
						ModelGeneratorFromXML.DEFAULT_MAP_FOLDER)
						.toPortableString());
		properties.setProperty(ProjectProperties.SCHEMA_DIRECTORY_PROPS,
				projectHandle.getLocation().append(
						ModelGeneratorFromXML.DEFAULT_SCHEMA_FOLDER)
						.toPortableString());
		properties.setProperty(ProjectProperties.WORKING_MAP_DIRECTORY_PROPS,
				projectHandle.getLocation().append(
						ModelGeneratorFromXML.DEFAULT_WORKING_MAP_FOLDER)
						.toPortableString());

		/*
		 * ModelGeneratorFromXML model = ModelGeneratorFromXML.getInstance();
		 * model
		 * .setGeneratedClassesFolder(projectHandle.getLocation().append("classes"
		 * ).toPortableString());
		 * model.setMapFolder(projectHandle.getLocation().
		 * append("map").toPortableString());
		 * model.setSchemaFolder(projectHandle
		 * .getLocation().append("schema").toPortableString());
		 */
		// System.out.println("Paths: " + "classes="
		// + model.getGeneratedClassesFolder() + ", map="
		// + model.getMapFolder() + ", schema=" + model.getSchemaFolder());
		/*
		 * IRunnableWithProgress runnable = new IRunnableWithProgress() {
		 * 
		 * @Override public void run(IProgressMonitor monitor) throws
		 * InvocationTargetException, InterruptedException { try {
		 * projectHandle.create(null); projectHandle.open(null); } catch
		 * (CoreException e1) { // TODO Auto-generated catch block
		 * e1.printStackTrace(); }
		 * 
		 * } };
		 */

		logger.debug("New Project wizzard ended. Created new mappum project: "
				+ projectHandle.getName());
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		logger.debug("New Project wizzard started.");
		setWindowTitle("New Mappum Project Wizard"); // NON-NLS-1
		setNeedsProgressMonitor(true);
		page = new NewProjectWizardPage("New Mappum Project Wizard");
	}

	public void addPages() {
		super.addPages();
		addPage(page);
	}

}
