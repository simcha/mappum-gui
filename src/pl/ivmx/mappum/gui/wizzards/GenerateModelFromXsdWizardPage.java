package pl.ivmx.mappum.gui.wizzards;

import org.eclipse.jface.dialogs.IDialogPage;
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

/**
 * Wizard page shown when the user has chosen plane as means of transport
 */

public class GenerateModelFromXsdWizardPage extends WizardPage implements
		Listener {

	// widgets on this page
	private List leftMappingList;
	private List rightMappingList;
	private Label leftLabel;
	private Label rightLabel;

	final static float standardPrice = 100;
	final static String[] seatChoices = { "Window", "Aisle", "Center" };
	final static double discountRate = 0.9;

	float price = standardPrice;

	/**
	 * Constructor for PlanePage.
	 */
	protected GenerateModelFromXsdWizardPage(String arg0) {
		super(arg0);
		setTitle("Generate model from XSD schema");
		setDescription("Specify left and right side of mapping model");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		composite.setLayout(gl);

		leftLabel = new Label(composite, SWT.NONE);
		leftLabel.setText("Left mapping model side:");

		rightLabel = new Label(composite, SWT.NONE);
		rightLabel.setText("Right mapping model side:");

		leftMappingList = new List(composite, SWT.BORDER);
		final GridData gd_leftList = new GridData(SWT.FILL, SWT.FILL, true,
				false);
		gd_leftList.heightHint = 188;
		leftMappingList.setLayoutData(gd_leftList);
		leftMappingList.addListener(SWT.Selection, this);

		rightMappingList = new List(composite, SWT.BORDER);
		final GridData gd_rightList = new GridData(SWT.FILL, SWT.FILL, true,
				false);
		gd_rightList.heightHint = 188;
		rightMappingList.setLayoutData(gd_rightList);
		rightMappingList.addListener(SWT.Selection, this);
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
		// if (e.widget == priceButton) {
		// if (flightsList.getSelectionCount() >0) {
		// if (((HolidayWizard)getWizard()).model.discounted)
		// price *= discountRate;
		// MessageDialog.openInformation(this.getShell(),"", "Flight price "+
		// price);
		// }
		// }

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
		// Gets the model
		// HolidayWizard wizard = (HolidayWizard)getWizard();
		// HolidayModel model = wizard.model;
		//		
		// String data = model.departure+" to "+model.destination+":";
		// // arbitrary values
		// String text1 = data +" price £400 - British Airways";
		// String text2 = data +" price £500 - Air France";
		// if (model.resetFlights) {
		// wizard.planeCompleted = false;
		// flightsList.removeAll();
		// flightsList.add(text1);
		// flightsList.add(text2);
		// }
	}
}
