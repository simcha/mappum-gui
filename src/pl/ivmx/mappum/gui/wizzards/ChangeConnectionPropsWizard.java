package pl.ivmx.mappum.gui.wizzards;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.Wizard;
import org.jrubyparser.ast.NewlineNode;
import org.jrubyparser.ast.Node;

import pl.ivmx.mappum.gui.model.Connection;
import pl.ivmx.mappum.gui.utils.ModelGenerator;
import pl.ivmx.mappum.gui.utils.RootNodeHolder;

public class ChangeConnectionPropsWizard extends Wizard {
	private int mappingSide;
	private String comment;
	private Connection connection;
	private NewlineNode mappingNode;

	private Logger logger = Logger.getLogger(ChangeConnectionPropsWizard.class);
	ChangeConnectionPropsWizardPage mainPage;

	public ChangeConnectionPropsWizard(Connection connection) {
		super();
		this.setConnection(connection);
		RootNodeHolder rootNodeHolder = RootNodeHolder.getInstance();
		this
				.setMappingNode(rootNodeHolder.findMappingNode(connection,
						rootNodeHolder.findRootBlockNode(rootNodeHolder
								.getRootNode())));
		this.setMappingSide(connection.getMappingSide());
		this.setComment(connection.getComment());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		String rubyCode = mainPage.getRubyCode();
		if(!rubyCode.equals(mainPage.getCode())){
			Node node = null;
			try {
				node = generateModelFromCode(rubyCode);
			} catch (CoreException e) {
			}
			if (node != null) {
				RootNodeHolder nodeHolder = RootNodeHolder.getInstance();
				nodeHolder.changeMappingAtributes(connection, null, mainPage
						.getRubyComment());
				Node parentNode = nodeHolder.getParentNode(mappingNode, nodeHolder
						.getRootNode());
				int pointer = 0;
				for (Node child : parentNode.childNodes()) {
					if (child.equals(mappingNode)) {
						break;
					}
					pointer++;
				}
				int moveCounter = 0;
				for (Node newChild : node.childNodes()) {
					parentNode.childNodes().add(pointer, newChild);
					moveCounter++;
				}
				parentNode.childNodes().remove(pointer + moveCounter);

			}
			return true;
		}
		return true;
	}

	private Node generateModelFromCode(String code) throws CoreException {
		return ModelGenerator.getInstance().parseExternalRubbyCode(code);
	}

	public boolean performCancel() {
		RootNodeHolder rootNodeHolder = RootNodeHolder.getInstance();
		rootNodeHolder.changeMappingAtributes(getConnection(), Connection
				.translateSideFromIntToString(mappingSide), null);
		getConnection().setMappingSide(mappingSide);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init() {
		logger.debug("Change Connection props wizzard started.");
		setWindowTitle("Mapping properties"); // NON-NLS-1
		setNeedsProgressMonitor(true);
		mainPage = new ChangeConnectionPropsWizardPage("Mapping properties"); // NON-NLS-1
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		super.addPages();
		addPage(mainPage);
	}

	public void setMappingSide(int mappingSide) {
		this.mappingSide = mappingSide;
	}

	public int getMappingSide() {
		return mappingSide;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public void setMappingNode(NewlineNode mappingNode) {
		this.mappingNode = mappingNode;
	}

	public NewlineNode getMappingNode() {
		return mappingNode;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public Connection getConnection() {
		return connection;
	}

}
