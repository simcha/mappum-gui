package pl.ivmx.mappum.gui.utils;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.core.runtime.CoreException;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.IterNode;
import org.jrubyparser.ast.Node;

public class TestNodeTreeWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TestNodeTreeWindow(Node node) throws CoreException {
		super("TEST");
		setSize(150, 150);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				dispose();
				System.exit(0);
			}
		});
		init(node);
		pack();
		setVisible(true);
	}

	private void init(Node node) throws CoreException {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Parseds File:");
		addTreeNodes(root, node);
		JScrollPane js = new JScrollPane(new JTree(root));
		getContentPane().add(js);

	}

	private void addTreeNodes(DefaultMutableTreeNode node, Node rootNode) {
		DefaultMutableTreeNode newNode;
		for (Node child : rootNode.childNodes()) {
			if (child instanceof INameNode) {
				newNode = new DefaultMutableTreeNode(child.getNodeType() + ":"
						+ ((INameNode) child).getName());
			} else {
				if (child instanceof IterNode) {
					System.out.println("IterNode scope: "
							+ ((IterNode) child).getScope() + ", varNode: "
							+ ((IterNode) child).getVarNode() + ", bodyNode: "
							+ ((IterNode) child).getBodyNode());
				}
				newNode = new DefaultMutableTreeNode(child.getNodeType());
			}
			// System.out.println(child.getNodeType()+":"+child.getPosition());
			node.add(newNode);
			addTreeNodes(newNode, child);
		}
	}
}
