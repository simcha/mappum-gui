package pl.ivmx.mappum.gui.wizzards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.jruby.RubyArray;
import org.jruby.RubyClass;

public class GenerateModelFromXsdWizardPage extends WizardPage implements
		Listener {

	// widgets on this page
	private List leftMappingList;
	private List rightMappingList;

	protected GenerateModelFromXsdWizardPage(String arg0) {
		super(arg0);
		setTitle("Generate model from XSD schema");
		setDescription("Specify left and right side of mapping model");
	}

	private List createList(final Composite c) {

		final List list = new List(c, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		final GridData gd_list = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd_list.heightHint = 188;
		list.setLayoutData(gd_list);
		list.addListener(SWT.Selection, this);

		return list;
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		composite.setLayout(gl);

		final Label leftLabel = new Label(composite, SWT.NONE);
		leftLabel.setText("Left mapping model side:");

		final Label rightLabel = new Label(composite, SWT.NONE);
		rightLabel.setText("Right mapping model side:");

		leftMappingList = createList(composite);
		rightMappingList = createList(composite);

		setControl(composite);
		onEnterPage();
		setPageComplete(false);
	}

	public boolean canFlipToNextPage() {
		// no next page for this path through the wizard
		return false;
	}

	/*
	 * Process the events: when the user has entered all information the wizard
	 * can be finished
	 */
	public void handleEvent(Event e) {
		setPageComplete(isPageComplete());
		getWizard().getContainer().updateButtons();
	}

	/*
	 * Sets the completed field on the wizard class when all the information is
	 * entered and the wizard can be completed
	 */
	public boolean isPageComplete() {
		if (leftMappingList.getSelectionCount() == 0
				&& rightMappingList.getSelectionCount() == 0) {
			return false;
		}
		GenerateModelFromXsdWizard wizard = (GenerateModelFromXsdWizard) getWizard();
		wizard.setLeftChoosenElement(leftMappingList.getSelection()[0]);
		wizard.setRightChoosenElement(rightMappingList.getSelection()[0]);
		return true;
	}

	void onEnterPage() {

		GenerateModelFromXsdWizard wizard = (GenerateModelFromXsdWizard) getWizard();
		RubyArray model = wizard.getModel();

		for (int i = 0; i < model.size(); i++) {
			RubyClass clazz = (RubyClass) ((RubyArray) model.get(i)).get(0);

			leftMappingList.add(clazz.getName());
			rightMappingList.add(clazz.getName());
		}
	}
}
