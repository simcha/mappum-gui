package pl.ivmx.mappum.gui.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jrubyparser.BlockStaticScope;
import org.jrubyparser.SourcePosition;
import org.jrubyparser.StaticScope;
import org.jrubyparser.ast.ArrayNode;
import org.jrubyparser.ast.BlockNode;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.ConstNode;
import org.jrubyparser.ast.DAsgnNode;
import org.jrubyparser.ast.DVarNode;
import org.jrubyparser.ast.FCallNode;
import org.jrubyparser.ast.IterNode;
import org.jrubyparser.ast.ListNode;
import org.jrubyparser.ast.MultipleAsgnNode;
import org.jrubyparser.ast.NewlineNode;
import org.jrubyparser.ast.NilImplicitNode;
import org.jrubyparser.ast.NilNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.RootNode;
import org.jrubyparser.ast.StrNode;
import org.jrubyparser.ast.XStrNode;

import pl.ivmx.mappum.gui.model.Connection;
import pl.ivmx.mappum.gui.model.Shape;

public class RootNodeHolder {
	public static final int IDENT_LENGTH = 1;
	private static final RootNodeHolder INSTANCE = new RootNodeHolder();
	private static final String MAPPUM_STR = "mappum";
	private static final String REQUIRE_STR = "require";
	private static final String CATALOGUE_STR = "catalogue_add";
	private Node rootNode;
	private List<String> usedIdent = new ArrayList<String>();
	private int usedCharIndex = 0;
	private char[] charArray = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
			'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
			'w', 'x', 'y', 'z' };
	private Logger logger = Logger.getLogger(RootNodeHolder.class);
	
	private RootNodeHolder() {
	}

	public static final RootNodeHolder getInstance() {
		return INSTANCE;
	}

	public Node getRootNode() {
		return rootNode;
	}

	public void setRootNode(Node rootNode) {
		usedCharIndex = 0;
		this.rootNode = rootNode;
	}

	public void addMappingNewlineNode(NewlineNode node) {
		findRootBlockNode(rootNode).add(node);
	}

	private BlockNode findRootBlockNode(Node node) {
		boolean iterate = true;
		BlockNode blockNode = null;
		for (Node child : node.childNodes()) {
			if (child instanceof FCallNode) {
				if (((FCallNode) child).getName() == "map") {
					iterate = false;
					if (((FCallNode) child).getIterNode() != null) {
						blockNode = (BlockNode) ((FCallNode) child)
								.getIterNode().childNodes().get(1);
					}

				}
			}
			if (iterate == true)
				blockNode = findRootBlockNode(child);
		}
		return blockNode;
	}

	private NewlineNode findRootMappingNode(Node node) {
		boolean iterate = true;
		NewlineNode newlineNode = null;
		for (Node child : node.childNodes()) {
			if (child instanceof FCallNode) {
				if (((FCallNode) child).getName() == "map") {
					iterate = false;
					return (NewlineNode) node;
				}
			}
			if (findRootBlockNode(child) != null)
				return findRootMappingNode(child);
		}
		return null;
	}

	private BlockNode getBlockNode(NewlineNode node) {
		if (node.childNodes().get(0) instanceof FCallNode) {
			if (node.childNodes().get(0).childNodes().size() > 1) {
				if (node.childNodes().get(0).childNodes().get(1) instanceof IterNode) {
					return (BlockNode) ((IterNode) node.childNodes().get(0)
							.childNodes().get(1)).getBodyNode();
				}
			}
		}
		return null;
	}

	private String[] findRootDVars(Node node) {
		boolean iterate = true;
		String[] dvarsArray = new String[2];
		for (Node child : node.childNodes()) {
			if (child instanceof FCallNode) {
				if (((FCallNode) child).getName().equals("map")) {
					iterate = false;
					dvarsArray[0] = ((DAsgnNode) ((FCallNode) child)
							.getIterNode().childNodes().get(0).childNodes()
							.get(0).childNodes().get(0)).getName();
					usedIdent.add(dvarsArray[0]);
					dvarsArray[1] = ((DAsgnNode) ((FCallNode) child)
							.getIterNode().childNodes().get(0).childNodes()
							.get(0).childNodes().get(1)).getName();
					usedIdent.add(dvarsArray[1]);
				}
			}
			if (iterate == true)
				dvarsArray = findRootDVars(child);
		}
		return dvarsArray;
	}

	private String[] findDVars(Node node) {
		String[] dvarsArray = new String[2];
		for (Node child : node.childNodes()) {
			if (child instanceof FCallNode) {
				if (((FCallNode) child).getName().equals("map")) {
					dvarsArray[0] = ((DAsgnNode) ((FCallNode) child)
							.getIterNode().childNodes().get(0).childNodes()
							.get(0).childNodes().get(0)).getName();
					usedIdent.add(dvarsArray[0]);
					dvarsArray[1] = ((DAsgnNode) ((FCallNode) child)
							.getIterNode().childNodes().get(0).childNodes()
							.get(0).childNodes().get(1)).getName();
					usedIdent.add(dvarsArray[1]);
				}
			}
		}
		return dvarsArray;
	}

	public boolean changeMappingAtributes(Connection connection,
			String newSide, String newComment) {
		NewlineNode newlineNode = findMappingNode(connection,
				findRootBlockNode(rootNode));
		CallNode callnode = (CallNode) ((FCallNode) newlineNode.getNextNode())
				.getArgsNode().childNodes().get(0);
		if (newSide != null) {
			callnode.setName(newSide);
			return true;
		} else if (newComment != null) {
			NewlineNode comment = generateComment(newComment);
			int n = 0;
			Node parent = getParentNode(newlineNode, rootNode);
			for (Node tmpNode : parent.childNodes()) {
				if (tmpNode.equals(newlineNode)) {
					break;
				}
				n++;
			}
			if (connection.getComment() == null
					|| connection.getComment().equals("")) {
				parent.childNodes().add(n, comment);
				return true;
			} else {
				parent.childNodes().add(n, comment);
				parent.childNodes().remove(n - 1);
				return true;
			}
		}
		return false;

	}

	private Node getParentNode(Node nodeToFind, Node NodeToSearchIn) {
		Node node = null;
		for (Node child : NodeToSearchIn.childNodes()) {
			if (child.equals(nodeToFind)) {
				return NodeToSearchIn;
			} else {
				node = getParentNode(nodeToFind, child);
			}
		}
		return node;
	}

	private NewlineNode findMappingNode(Connection connection, Node node) {
		NewlineNode newlineNode = null;
		if (node instanceof BlockNode) {
			for (Node newline : node.childNodes()) {
				for (Node child : newline.childNodes()) {
					if (child instanceof FCallNode) {
						if (((FCallNode) child).getName().equals("map")) {
							if ((((FCallNode) child).getArgsNode()) instanceof ArrayNode) {
								if ((((FCallNode) child).getArgsNode())
										.childNodes().get(0) instanceof CallNode) {
									CallNode callnode = (CallNode) (((FCallNode) child)
											.getArgsNode()).childNodes().get(0);
									if (Connection
											.translateSideFromIntToString(
													connection.getMappingSide())
											.equals(callnode.getName())) {
										if (ModelGenerator
												.findLastCallNodeInTree(
														callnode
																.getReceiverNode())
												.equals(
														connection.getSource()
																.getShapeNode())) {
											System.out.println(callnode);
											if (ModelGenerator
													.findLastCallNodeInTree(
															callnode
																	.getArgsNode()
																	.childNodes()
																	.get(0))
													.equals(
															connection
																	.getTarget()
																	.getShapeNode())) {
												return (NewlineNode) newline;
											}
										}
									}
								}
							}
						}
					}
					if (getBlockNode((NewlineNode) newline) != null)
						newlineNode = findMappingNode(connection,
								(getBlockNode((NewlineNode) newline)));
				}
			}
		}

		return newlineNode;
	}

	private NewlineNode generateSimpleMapping(CallNode leftSide,
			CallNode rightSide, String side, NewlineNode parentMapping) {
		ArrayNode argsNode = new ArrayNode(new SourcePosition(), rightSide);
		CallNode callNode = new CallNode(new SourcePosition(), leftSide, side,
				argsNode);
		ArrayNode arrayNode = new ArrayNode(new SourcePosition(), callNode);
		FCallNode fcallMapNode = new FCallNode(new SourcePosition(), "map",
				arrayNode);
		NewlineNode newlineNode = new NewlineNode(new SourcePosition(),
				fcallMapNode);
		String[] s = findDVars(parentMapping);
		changeMappingDVars(newlineNode, s[0], s[1]);
		return newlineNode;
	}

	public void addSimpleMapping(CallNode leftSide, CallNode rightSide,
			String side) {
		// addMappingNewlineNode(generateSimpleMapping(leftSide, rightSide,
		// side));

	}

	private NewlineNode generateComment(String comment) {
		if (comment.equals("") || comment == null) {
			return null;
		}
		XStrNode commentNode = new XStrNode(new SourcePosition(), comment);
		return new NewlineNode(new SourcePosition(), commentNode);
	}

	public void addMapping(Shape leftShape, Shape rightShape, String side,
			String comment) {
		List<Integer> path = findMappingPath(leftShape, rightShape);
		NewlineNode node = findRootMappingNode(rootNode);
		for (int i : path) {
			node = (NewlineNode) getBlockNode(node).get(i);
		}
		List<Integer> route = new ArrayList<Integer>();
		List<Shape> leftShapeList = leftShape.getShapeStack();
		List<Shape> rightShapeList = rightShape.getShapeStack();
		Collections.reverse(leftShapeList);
		Collections.reverse(rightShapeList);

		for (int i = 0; i < path.size(); i++) {
			leftShapeList.remove(0);
			rightShapeList.remove(0);
		}

		int n = 0;
		if (leftShapeList.size() > rightShapeList.size()) {
			n = leftShapeList.size();
		} else {
			n = rightShapeList.size();
		}
		NewlineNode tmpNode = null;
		for (int i = 0; i < n; i++) {

			if ((leftShapeList.size() > 1 && rightShapeList.size() > 1)
					|| (leftShapeList.size() > 1 && rightShapeList.size() == 1)
					|| (leftShapeList.size() == 1 && rightShapeList.size() > 1)) {
				tmpNode = generateComplexMapping(
						leftShapeList.get(0).getShapeNode(),
						rightShapeList.get(0).getShapeNode(),
						Connection
								.translateSideFromIntToString(Connection.DUAL_SIDE),
						node);
				NewlineNode newLineNode;
				if ((newLineNode = generateComment(comment)) != null) {
					getBlockNode(node).add(newLineNode);
				}
				getBlockNode(node).add(tmpNode);
				leftShapeList.remove(0);
				rightShapeList.remove(0);
			}

			else if (leftShapeList.size() == 1 && rightShapeList.size() == 1) {
				tmpNode = generateSimpleMapping(leftShapeList.get(0)
						.getShapeNode(), rightShapeList.get(0).getShapeNode(),
						side, (NewlineNode) node);
				NewlineNode newLineNode;
				if ((newLineNode = generateComment(comment)) != null) {
					getBlockNode(node).add(newLineNode);
				}
				getBlockNode(node).add(tmpNode);
				leftShapeList.clear();
				rightShapeList.clear();
			}

			else if (leftShapeList.size() > 0 && rightShapeList.size() == 0) {
				ListNode listNode = new ListNode(new SourcePosition());
				DVarNode dVarNode = new DVarNode(new SourcePosition(), 0,
						"changeMe");
				CallNode selfNode = new CallNode(new SourcePosition(),
						dVarNode, "self", listNode);
				int index = leftShapeList.size() - 1;
				tmpNode = generateSimpleMapping(leftShapeList.get(index)
						.getShapeNode(), selfNode, side, tmpNode);
				leftShapeList.clear();
				NewlineNode newLineNode;
				if ((newLineNode = generateComment(comment)) != null) {
					getBlockNode(node).add(newLineNode);
				}
				getBlockNode(node).add(tmpNode);
			}

			else if (leftShapeList.size() == 0 && rightShapeList.size() > 0) {
				ListNode listNode = new ListNode(new SourcePosition());
				DVarNode dVarNode = new DVarNode(new SourcePosition(), 1,
						"changeMe");
				CallNode selfNode = new CallNode(new SourcePosition(),
						dVarNode, "self", listNode);
				int index = rightShapeList.size() - 1;

				tmpNode = generateSimpleMapping(selfNode, rightShapeList.get(
						index).getShapeNode(), side, tmpNode);
				rightShapeList.clear();
				NewlineNode newLineNode;
				if ((newLineNode = generateComment(comment)) != null) {
					getBlockNode(node).add(newLineNode);
				}
				getBlockNode(node).add(tmpNode);

			}
			node = tmpNode;

		}
		// try {
		// new TestNodeTreeWindow(rootNode);
		// } catch (CoreException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	/**
	 * Corrects nodes (inserts BlockNodes between Itarate and Newline if not
	 * exists. This usually happened when there is only one Newline after
	 * Iterate
	 * 
	 * @param node
	 * @return
	 */
	public static Node correctNodeIterationBlocks(Node node) {
		boolean correct = false;
		if (node instanceof IterNode) {
			if (((IterNode) node).getBodyNode() instanceof NewlineNode) {
				BlockNode blockNode = new BlockNode(new SourcePosition());
				blockNode.add(((IterNode) node).getBodyNode());
				((IterNode) node).setBodyNode(blockNode);
			}
		}
		if (node.childNodes().size() > 0) {
			for (int i = 0; i < node.childNodes().size(); i++) {
				node.childNodes().set(i,
						correctNodeIterationBlocks(node.childNodes().get(i)));
			}
		}
		return node;
	}

	/**
	 * Finds longest path of mappings already created
	 * 
	 * @param leftShape
	 * @param rightShape
	 * @return
	 */
	public List<Integer> findMappingPath(Shape leftShape, Shape rightShape) {
		return findMappingPath(leftShape, rightShape,
				findRootBlockNode(rootNode), 0);
	}

	private List<Integer> findMappingPath(Shape leftShape, Shape rightShape,
			BlockNode rootNode, int level) {
		List<Integer> route = new ArrayList<Integer>();
		List<Shape> leftShapeList = leftShape.getShapeStack();
		List<Shape> rightShapeList = rightShape.getShapeStack();
		Collections.reverse(leftShapeList);
		Collections.reverse(rightShapeList);
		int elements;
		if (leftShapeList.size() > rightShapeList.size())
			elements = rightShapeList.size();
		else
			elements = leftShapeList.size();
		if (level < elements) {
			List<List<Integer>> routeArray = new ArrayList<List<Integer>>();
			for (int i = 0; i < rootNode.childNodes().size(); i++) {
				if (mappingExists(leftShapeList.get(level), rightShapeList
						.get(level), (NewlineNode) rootNode.childNodes().get(i))) {
					List<Integer> tmpList = new ArrayList<Integer>();
					tmpList.add(i);
					if (getNextChildBlockNode((NewlineNode) rootNode
							.childNodes().get(i)) != null) {
						tmpList.addAll(findMappingPath(leftShape, rightShape,
								getNextChildBlockNode((NewlineNode) rootNode
										.childNodes().get(i)), ++level));

					}
					routeArray.add(tmpList);
				}
			}

			List<Integer> tmpList = new ArrayList<Integer>();
			for (List<Integer> list : routeArray) {
				if (tmpList.size() < list.size())
					tmpList = list;
			}
			route.addAll(tmpList);
		}

		return route;
	}

	/**
	 * Returns nect BlockNode of inserted Complex Mapping Node
	 * 
	 * @param node
	 * @return
	 */
	private BlockNode getNextChildBlockNode(NewlineNode node) {
		if (node.getNextNode() instanceof FCallNode
				&& ((FCallNode) node.getNextNode()).getIterNode() instanceof IterNode) {
			return (BlockNode) ((IterNode) ((FCallNode) node.getNextNode())
					.getIterNode()).getBodyNode();

		} else
			return null;
	}

	/**
	 * Check if mapping exists in inserted Node
	 * 
	 * @param leftShape
	 * @param rightShape
	 * @param newlineNode
	 * @return
	 */
	private boolean mappingExists(Shape leftShape, Shape rightShape,
			NewlineNode newlineNode) {
		if (newlineNode.getNextNode() instanceof FCallNode)
			if (((FCallNode) newlineNode.getNextNode()).getName().equals("map")) {
				CallNode callnode = (CallNode) newlineNode.getNextNode()
						.childNodes().get(0).childNodes().get(0);
				CallNode leftNode = ModelGenerator
						.findLastCallNodeInTree(callnode.childNodes().get(0));
				CallNode rightNode = ModelGenerator
						.findLastCallNodeInTree(callnode.childNodes().get(1)
								.childNodes().get(0));
				if (leftNode.getName().equals(leftShape.getName())
						&& rightNode.getName().equals(rightShape.getName())) {
					return true;
				}
			}
		return false;
	}

	private StaticScope getNodeScope(NewlineNode node) {
		return ((IterNode) ((FCallNode) node.childNodes().get(0)).getIterNode())
				.getScope();
	}

	private NewlineNode addChildNodeToParent(NewlineNode parentNode,
			NewlineNode childNode) {
		((BlockNode) ((IterNode) ((FCallNode) parentNode.childNodes().get(0))
				.getIterNode()).getBodyNode()).add(childNode);
		return parentNode;
	}

	private NewlineNode generateComplexMapping(CallNode leftSide,
			CallNode rightSide, String side, NewlineNode parentMapping) {
		String leftPrefix = generateRandomIdent();
		String rightPrefix = generateRandomIdent();
		NilNode leftNilNode = new NilNode(new SourcePosition());
		NilNode rightNilNode = new NilNode(new SourcePosition());
		DAsgnNode leftAsgnNode = new DAsgnNode(new SourcePosition(),
				leftPrefix, 0, null);
		DAsgnNode rightAsgnNode = new DAsgnNode(new SourcePosition(),
				rightPrefix, 1, null);
		ArrayNode dasgnArrayNode = new ArrayNode(new SourcePosition());
		dasgnArrayNode.add(leftAsgnNode);
		dasgnArrayNode.add(rightAsgnNode);
		MultipleAsgnNode multipleAsgnNode = new MultipleAsgnNode(
				new SourcePosition(), dasgnArrayNode, null);
		StaticScope scope = new BlockStaticScope(getNodeScope(parentMapping),
				new String[] { leftPrefix, rightPrefix });
		BlockNode blockNode = new BlockNode(new SourcePosition());
		IterNode iterNode = new IterNode(new SourcePosition(),
				multipleAsgnNode, scope, blockNode);

		ArrayNode argsNode = new ArrayNode(new SourcePosition(), rightSide);
		CallNode callNode = new CallNode(new SourcePosition(), leftSide, side,
				argsNode);
		ArrayNode arrayNode = new ArrayNode(new SourcePosition(), callNode);
		FCallNode fcallMapNode = new FCallNode(new SourcePosition(), "map",
				arrayNode);
		fcallMapNode.setIterNode(iterNode);
		NewlineNode newlineNode = new NewlineNode(new SourcePosition(),
				fcallMapNode);
		String[] s = findDVars(parentMapping);
		changeMappingDVars(newlineNode, s[0], s[1]);
		return newlineNode;
	}

	// private

	private boolean checkIfSimple(NewlineNode node) {
		if (node.childNodes().get(0).childNodes().size() > 1) {
			return false;
		} else {
			return true;
		}
	}

	private void changeMappingDVars(NewlineNode node, String leftDVarName,
			String rightDVarName) {
		changeDVar(getMappingCallNode(node).childNodes().get(0), leftDVarName);
		changeDVar(getMappingCallNode(node).childNodes().get(1).childNodes()
				.get(0), rightDVarName);
	}

	private void changeDVar(Node node, String name) {
		if (node instanceof DVarNode) {
			((DVarNode) node).setName(name);
		} else if (node.childNodes().size() > 0) {
			changeDVar(node.childNodes().get(0), name);
		}

	}

	private CallNode getMappingCallNode(NewlineNode node) {
		return (CallNode) node.childNodes().get(0).childNodes().get(0)
				.childNodes().get(0);
	}

	public String generateRandomIdent() {
		usedCharIndex++;

		String myRandom = "";
		int firstDigitIndex = usedCharIndex / 625;
		int secondDigitIndex = (usedCharIndex % 625) / 25;
		int identIndex = usedCharIndex % 25;
		if (firstDigitIndex > 0) {
			myRandom = "" + charArray[firstDigitIndex - 1] + ""
					+ charArray[secondDigitIndex - 1] + ""
					+ charArray[identIndex - 1];
		} else if (secondDigitIndex > 0) {
			myRandom = "" + charArray[secondDigitIndex - 1] + ""
					+ charArray[identIndex - 1];
		} else {
			myRandom = "" + charArray[identIndex - 1];
		}

		int steps = 0;
		while (usedIdent.contains(myRandom)) {
			myRandom = generateRandomIdent();

		}
		usedIdent.add(myRandom);
		return myRandom;
	}

	/**
	 * Generate root tree if mapping is generated from XSD schemas
	 * 
	 * @param leftElement
	 * @param rightElement
	 * @param requirements
	 */
	public void generateRootNode(String leftElement, String rightElement,
			List<String> requirements) {
		BlockStaticScope rootStaticScope = new BlockStaticScope(null);
		// creating Node
		BlockNode blockNode = new BlockNode(new SourcePosition());
		this.rootNode = new RootNode(new SourcePosition(), rootStaticScope,
				blockNode);
		new BlockNode(new SourcePosition());

		if (requirements == null) {
			requirements = new ArrayList<String>();
		}
		requirements.add(0, MAPPUM_STR);

		// adding requirements
		for (String req : requirements) {
			StrNode stringNode = new StrNode(new SourcePosition(), req);
			ArrayNode arrayNode = new ArrayNode(new SourcePosition(),
					stringNode);
			FCallNode requireNode = new FCallNode(new SourcePosition(),
					REQUIRE_STR, arrayNode);
			NewlineNode newlineNode = new NewlineNode(new SourcePosition(),
					requireNode);
			blockNode.add(newlineNode);
		}
		BlockNode rootBlockNode = new BlockNode(new SourcePosition());

		ConstNode constNode = new ConstNode(new SourcePosition(), "Mappum");
		ListNode listNode = new ListNode(new SourcePosition());
		BlockStaticScope blockStaticScope = new BlockStaticScope(
				rootStaticScope);
		IterNode rootIterNode = new IterNode(new SourcePosition(), null,
				blockStaticScope, rootBlockNode);
		CallNode rootCallNode = new CallNode(new SourcePosition(), constNode,
				CATALOGUE_STR, listNode, rootIterNode);
		NewlineNode newlineCallNode = new NewlineNode(new SourcePosition(),
				rootCallNode);
		blockNode.add(newlineCallNode);
		// ArrayNode z mappingiem
		String leftPrefix = generateRandomIdent();
		String rightPrefix = generateRandomIdent();
		NilImplicitNode leftNiNode = new NilImplicitNode();
		NilImplicitNode rightNiNode = new NilImplicitNode();
		DAsgnNode leftAsgnNode = new DAsgnNode(new SourcePosition(),
				leftPrefix, 0, leftNiNode);
		DAsgnNode rightAsgnNode = new DAsgnNode(new SourcePosition(),
				rightPrefix, 1, rightNiNode);
		ArrayNode dasgnArrayNode = new ArrayNode(new SourcePosition());
		dasgnArrayNode.add(leftAsgnNode);
		dasgnArrayNode.add(rightAsgnNode);
		MultipleAsgnNode multipleAsgnNode = new MultipleAsgnNode(
				new SourcePosition(), dasgnArrayNode, null);
		StaticScope scope = new BlockStaticScope(blockStaticScope,
				new String[] { leftPrefix, rightPrefix });
		// BlockNode mappingBlockNode = new BlockNode(new SourcePosition());
		// IterNode iterNode = new IterNode(new SourcePosition(),
		// multipleAsgnNode, scope, mappingBlockNode);
		IterNode iterNode = new IterNode(new SourcePosition(),
				multipleAsgnNode, scope, null);

		ConstNode leftConstNode = new ConstNode(new SourcePosition(),
				leftElement);
		ConstNode rightConstNode = new ConstNode(new SourcePosition(),
				rightElement);

		ArrayNode arrayNode = new ArrayNode(new SourcePosition());
		arrayNode.add(leftConstNode);
		arrayNode.add(rightConstNode);
		FCallNode fcallMapNode = new FCallNode(new SourcePosition(), "map",
				arrayNode);
		fcallMapNode.setIterNode(iterNode);
		NewlineNode newlineNode = new NewlineNode(new SourcePosition(),
				fcallMapNode);
		rootBlockNode.add(newlineNode);
	}

	public static void generateRootBlockNode(Node node) {
		boolean iterate = true;
		for (Node child : node.childNodes()) {
			if (child instanceof FCallNode) {
				if (((FCallNode) child).getName() == "map") {
					iterate = false;
					if (((FCallNode) child).getIterNode() != null) {
						if (((FCallNode) child).getIterNode() instanceof IterNode)
							if (((FCallNode) child).getIterNode().childNodes()
									.size() < 2) {
								IterNode iterNode = (IterNode) ((FCallNode) child)
										.getIterNode();
								iterNode.setBodyNode(new BlockNode(
										new SourcePosition()));
							}
					}

				}
			}
			if (iterate == true)
				generateRootBlockNode(child);
		}
	}
}
