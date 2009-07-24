package pl.ivmx.mappum.gui.wizzards;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.jruby.RubyArray;

public class GenerateModelFromXsdWizard extends Wizard {
	private GenerateModelFromXsdWizardPage page;
	private Logger logger = Logger.getLogger(GenerateModelFromXsdWizard.class);
	private RubyArray model;
	private String leftChoosenElement = null;
	private String rightChoosenElement = null;
	private IWorkbench workbench;

	public GenerateModelFromXsdWizard(RubyArray model) {
		super();
		setModel(model);
	}

	@Override
	public boolean performFinish() {
		final String leftElement = leftChoosenElement;
		final String rightElement = rightChoosenElement;
		if (leftElement != null && rightElement != null) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Generating model for " + leftElement
							+ " and " + rightElement, 0);
					// TODO add model generation (stworzyc nowy plik w wizardzie
					// i zrobic w nim zalazek mapy Rubiego i przekazac go do
					// edytora, a w edytorze zrobic w taki sposob ladowanie
					// inputu, ¿e najpierw importuje mape, pozniej otwiera sobie
					// model wygenerowany z xsd przez ModelGeneratorFromXML i
					// zaczytuje brakuj¹ce elementy.
					for (int i = 0; i < 2; i++) {
						Thread.sleep(1000);
					}
					IWorkbenchPage workbenchPage = workbench
							.getActiveWorkbenchWindow().getActivePage();
					// IDE.openEditor(workbenchPage, ,
					// "pl.ivmx.mappum.gui.editor");
					monitor.done();
				}
			};
			try {
				getContainer().run(true, false, runnable);
			} catch (InvocationTargetException e) {
				logger.error("Error while generating model"
						+ e.getCause().getMessage());
				MessageDialog.openError(getContainer().getShell(),
						"Error while generating model: ", e.getCause()
								.getMessage());
			} catch (InterruptedException e) {
				logger.error("Error while generating model: "
						+ e.getCause().getMessage());
				MessageDialog.openError(getContainer().getShell(),
						"Error while generating model", e.getCause()
								.getMessage());
			}
			logger.debug("Generate model from XSD wizard ended.");
			return true;
		}
		return false;

	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		logger.debug("Generate model from XSD schema wizard started.");
		setWindowTitle("Generate model from XSD schema"); // NON-NLS-1
		setNeedsProgressMonitor(true);
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

}
