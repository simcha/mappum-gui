package pl.ivmx.mappum.gui.wizzards;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.jrubyparser.ast.NewlineNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.lexer.SyntaxException;

import pl.ivmx.mappum.gui.IMappumEditor;
import pl.ivmx.mappum.gui.model.Connection;
import pl.ivmx.mappum.gui.utils.ModelGenerator;
import pl.ivmx.mappum.gui.utils.RootNodeHolder;

public class ChangeConnectionPropsWizard extends Wizard {
	private int mappingSide;
	private String comment;
	private final Connection connection;
	private final IMappumEditor editor;
	private NewlineNode mappingNode;
	

	private Logger logger = Logger.getLogger(ChangeConnectionPropsWizard.class);
	ChangeConnectionPropsWizardPage mainPage;

	public ChangeConnectionPropsWizard(final Connection connection, final IMappumEditor editor) {
		this.connection = connection;
		this.editor = editor;
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
		final String rubyCode = mainPage.getRubyCode();
		if (!rubyCode.equals(mainPage.getCode())) {
			Node newNode = null;

			try {
				newNode = generateModelFromCode(rubyCode);
			} catch (final SyntaxException e) {
				final MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR);
				mb.setMessage("Parsing failed. Please correct the code.");
				mb.open();
				return false;
			}

			final MessageBox mb = new MessageBox(getShell(), SWT.ICON_QUESTION
					| SWT.YES | SWT.NO);
			mb
					.setMessage("This operation requires saving. This will discard all undo information.\n"
							+ "Do you wish to continue?");
			if (mb.open() == SWT.YES) {
				final RootNodeHolder nodeHolder = RootNodeHolder.getInstance();
				nodeHolder.changeMappingAtributes(connection, null, mainPage
						.getRubyComment());
				final Node parentNode = nodeHolder.getParentNode(mappingNode,
						nodeHolder.getRootNode());
				int pointer = 0;
				for (Node child : parentNode.childNodes()) {
					if (mappingNode.equals(child)) {
						break;
					}
					pointer++;
				}
				int moveCounter = 0;
				for (Node newChild : newNode.childNodes()) {
					parentNode.childNodes().add(pointer, newChild);
					moveCounter++;
				}
				parentNode.childNodes().remove(pointer + moveCounter);
				editor.doSave(new NullProgressMonitor());
				editor.reload();
				return true;
			}
			return false;
		}
		return false;
	}

	private Node generateModelFromCode(String code) throws SyntaxException {
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

	public Connection getConnection() {
		return connection;
	}

}
