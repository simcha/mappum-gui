package pl.ivmx.mappum.gui.handlers;

import java.lang.reflect.InvocationTargetException;

import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jruby.RubyArray;
import org.jruby.exceptions.RaiseException;

import pl.ivmx.mappum.gui.utils.ModelGeneratorFromXML;
import pl.ivmx.mappum.gui.wizzards.GenerateModelFromXsdWizard;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class GenerateModelFromXsdHandler extends AbstractHandler {
	private IWorkbenchWindow window;
	private Logger logger = Logger.getLogger(GenerateModelFromXsdHandler.class);

	/**
	 * The constructor.
	 */
	public GenerateModelFromXsdHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		logger.debug("Command execute");
		window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
				.getActiveMenuSelection(event);
		Object firstElement = selection.getFirstElement();
		if (firstElement instanceof IProject
				&& validateProject((IProject) firstElement)) {
			if (checkIfProjectSchemasExists((IProject) firstElement)) {
				RubyArray model = generateModel((IProject) firstElement);
				if (model != null) {
					GenerateModelFromXsdWizard wizard = new GenerateModelFromXsdWizard(
							model);
					wizard.init(selection);
					WizardDialog dialog = new WizardDialog(window
							.getWorkbench().getActiveWorkbenchWindow()
							.getShell(), wizard);
					dialog.open();
				}
			} else {
				MessageDialog.openError(window.getShell(),
						"Error while generating classes",
						"Project doesn't contains any XSD schemas");
			}

		} else {
			MessageDialog.openError(window.getShell(),
					"Error while generating classes",
					"Project is not proper Mappum project");
		}

		// IWorkbenchWindow window =
		// HandlerUtil.getActiveWorkbenchWindowChecked(event);
		// MessageDialog.openInformation(
		// window.getShell(),
		// "Mappum-GUI",
		// "Hello, Eclipse world");
		return null;
	}
/**
 * Validates if project is proper mappum project
 * @param project
 * @return
 */
	private boolean validateProject(IProject project) {
		if (project.getFolder(ModelGeneratorFromXML.DEFAULT_MAP_FOLDER) != null
				&& project
						.getFolder(ModelGeneratorFromXML.DEFAULT_GENERATED_CLASSES_FOLDER) != null
				&& project
						.getFolder(ModelGeneratorFromXML.DEFAULT_SCHEMA_FOLDER) != null
				&& project
						.getFolder(ModelGeneratorFromXML.DEFAULT_WORKING_MAP_FOLDER) != null
				&& project
						.getFile(ModelGeneratorFromXML.DEFAULT_PROJECT_PROPERTIES_FILE) != null) {
			return true;
		}
		return false;
	}
/**
 * Checks if schema folder is not empty
 * @param project
 * @return
 */
	private boolean checkIfProjectSchemasExists(IProject project) {
		try {
			if (project.getFolder("schema").members().length > 0) {
				return true;
			}
		} catch (CoreException e) {
			return false;
		}
		return false;
	}
	
/**
 * Generates model from XSD Schemas
 * @param project
 * @return
 */
	private RubyArray generateModel(IProject project) {
		final IProject finalProject = project;

		try {
			new ProgressMonitorDialog(window.getShell()).run(false, // don't
					// fork
					false, // not cancelable
					new WorkspaceModifyOperation() { // run this operation
						public void execute(final IProgressMonitor monitor) {
							monitor.beginTask("Generating classes...", 0);

							try {
								ModelGeneratorFromXML generator = ModelGeneratorFromXML
										.getInstance();
								generator.generateModel(finalProject);
								monitor.done();
							} catch (ScriptException e) {
								e.printStackTrace();
								logger.error("Error while generating classes: "
										+ e.getCause().getMessage());
							}

						}
					});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			logger.error("Error while generating classes: "
					+ e.getCause().getMessage());
			MessageDialog
					.openError(window.getShell(),
							"Error while generating classes", e.getCause()
									.getMessage());

		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Error performing finish operations: "
					+ e.getCause().getMessage());
			MessageDialog
					.openError(window.getShell(),
							"Error while generating classes", e.getCause()
									.getMessage());

		}

		try {
			return ModelGeneratorFromXML.getInstance().getModelArray();
		} catch (ScriptException e) {
			e.printStackTrace();
			if (e.getCause() != null) {
				if (e.getCause() instanceof RaiseException) {
					logger.error("Error performing finish operations: "
							+ e.getCause());
					MessageDialog.openError(window.getShell(),
							"Error while generating classes", e.getCause()
									.getMessage());
				}

			} else {
				MessageDialog.openError(window.getShell(),
						"Error while generating classes",
						"e.getCouse() is null");
			}

		}
		return null;
	}
}
