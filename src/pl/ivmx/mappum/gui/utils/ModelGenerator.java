package pl.ivmx.mappum.gui.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.jrubyparser.Parser;
import org.jrubyparser.ast.BlockNode;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.Colon2Node;
import org.jrubyparser.ast.ConstNode;
import org.jrubyparser.ast.FCallNode;
import org.jrubyparser.ast.FixnumNode;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.IterNode;
import org.jrubyparser.ast.NewlineNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.SymbolNode;
import org.jrubyparser.ast.XStrNode;
import org.jrubyparser.parser.ParserConfiguration;
import org.jrubyparser.rewriter.ReWriteVisitor;

import pl.ivmx.mappum.gui.model.Connection;
import pl.ivmx.mappum.gui.model.Shape;

public class ModelGenerator {
	private static final int SIMPLE_MAP_OR_WITH_FUNCTION_CALL = 1;
	private static final int MAP_WITH_DICTIONARY = 2;
	private static final int MAP_WITH_CODE = 3;
	private static final int SIMPLE_ARRAY_MAP = 4;
	private static final int ARRAY_MAP_WITH_SELF = 5;
	private static final int MAP_WITH_SUBOBJECT = 6;
	private static final int MAP_WITH_SUBMAP = 7;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Parser parser;
	private Reader content;
	private IFile file;
	private ParserConfiguration configuration;

	private static final ModelGenerator INSTANCE = new ModelGenerator();

	private Logger logger = Logger.getLogger(ModelGenerator.class);

	private ModelGenerator() {
	}

	public static final ModelGenerator getInstance() {
		return INSTANCE;
	}

	private Node parseRubbyFile(IFile file) throws CoreException {
		this.file = file;
		parser = new Parser();
		content = new InputStreamReader(file.getContents());
		configuration = new ParserConfiguration();
		return parser.parse(file.getName(), content, configuration);
	}

	private List<Node> getMapNodes(Node rootNode) {
		for (Node child : rootNode.childNodes()) {
			if (child instanceof FCallNode
					&& ((FCallNode) child).getName() == "map") {

				/* if (child instanceof ConstNode){ */
				getMapNodes(child);
			} else {
				getMapNodes(child);
			}
			;
		}

		return null;
	}

	public void generateModel(IFile file) throws CoreException {
		RootNodeHolder
				.getInstance()
				.setRootNode(
						RootNodeHolder
								.correctNodeIterationBlocks(parseRubbyFile(file)));

		findRootMap(RootNodeHolder.getInstance().getRootNode());
	}

	/**
	 * Iterates as long as it finds first map
	 * 
	 * @param node
	 */
	private void findRootMap(Node node) {
		boolean iterate = true;
		for (Node child : node.childNodes()) {
			if (child instanceof FCallNode) {
				if (((FCallNode) child).getName() == "map") {
					// TODO generating comments for root Map
					iterate = false;
					Pair parents = new Pair();
					createComplexElements(child, parents);
					if (((IterNode) ((FCallNode) child).getIterNode())
							.getBodyNode() instanceof BlockNode) {
						XStrNode comment = null;
						for (Node newline : ((IterNode) ((FCallNode) child)
								.getIterNode()).getBodyNode().childNodes()) {
							if (((NewlineNode) newline).getNextNode() instanceof XStrNode) {
								comment = (XStrNode) ((NewlineNode) newline)
										.getNextNode();
							} else if (((NewlineNode) newline).getNextNode() instanceof FCallNode) {
								System.out.println(Shape.getRootShapes());
								operateOnInternalMap((NewlineNode) newline,
										new Pair(Shape.getRootShapes().get(0),
												Shape.getRootShapes().get(1)),
										comment);
								comment = null;
							}

						}
					}

				}
			}
			if (iterate == true)
				findRootMap(child);
		}
	}

	/**
	 * Finds map in NewlineNode and operates on it
	 * 
	 * @param node
	 * @param parents
	 */
	private Connection operateOnInternalMap(NewlineNode node, Pair parents,
			XStrNode comment) {
		int mappingType = 0;
		for (Node child : node.childNodes()) {
			if (child instanceof FCallNode)
				mappingType = checkMappingType((FCallNode) child);
		}
		switch (mappingType) {
		case SIMPLE_MAP_OR_WITH_FUNCTION_CALL:
			CallNode callnode = (CallNode) node.childNodes().get(0)
					.childNodes().get(0).childNodes().get(0);
			return operateOnSimpleMapOrWithFunctionCall(callnode, parents,
					comment);
		case SIMPLE_ARRAY_MAP:
			CallNode arrayCallNode = (CallNode) node.childNodes().get(0)
					.childNodes().get(0).childNodes().get(0);
			return operateOnArrayMap(arrayCallNode, parents, comment);
		case MAP_WITH_DICTIONARY:
			// narazie zwykle mapowanie
			CallNode dictionaryCallnode = (CallNode) node.childNodes().get(0)
					.childNodes().get(0).childNodes().get(0);
			return operateOnSimpleMapOrWithFunctionCall(dictionaryCallnode,
					parents, comment);
		case MAP_WITH_SUBOBJECT:
			FCallNode subobjectFcallnode = (FCallNode) node.childNodes().get(0);
			return operateOnMapWithSubobject(subobjectFcallnode, parents,
					comment);
		case ARRAY_MAP_WITH_SELF:
			FCallNode mapWithSelfFcallnode = (FCallNode) node.childNodes().get(
					0);
			return operateOnArrayMapWithSelf(mapWithSelfFcallnode, parents,
					comment);
		case MAP_WITH_SUBMAP:
			FCallNode submapFcallnode = (FCallNode) node.childNodes().get(0);
			return operateOnMapWithSubmap(submapFcallnode, parents, comment);
		case MAP_WITH_CODE:
			// TODO obs³uga kodu (narazie zwyk³e proste mapowanie)
			CallNode MapWithCodeNode = (CallNode) node.childNodes().get(0)
					.childNodes().get(0).childNodes().get(0);
			return operateOnSimpleMapOrWithFunctionCall(MapWithCodeNode,
					parents, comment);
		default:
			logger.error("Coudln't recognize mapping type for node" + node);
			return null;
		}

	}

	private Connection operateOnSimpleArrayMapWithNoElements(CallNode callnode,
			Pair parents, XStrNode comment) {
		int side = Connection
				.translateSideFromStringToInt((callnode).getName());
		CallNode leftSide = findLastCallNodeInTree(callnode.childNodes().get(0));
		CallNode rightSide = findLastCallNodeInTree(callnode.childNodes()
				.get(1).childNodes().get(0));
		Shape leftShape = Shape.createShape(leftSide.getName() + "[]", null,
				parents.getLeftShape(), Shape.LEFT_SIDE, (CallNode) callnode
						.childNodes().get(0));
		Shape rightShape = Shape.createShape(rightSide.getName() + "[]", null,
				parents.getRightShape(), Shape.RIGHT_SIDE, (CallNode) callnode
						.childNodes().get(1).childNodes().get(0));
		parents.getLeftShape().addShapeChild(leftShape);
		parents.getRightShape().addShapeChild(rightShape);
		if (comment != null) {
			return new Connection(leftShape, rightShape, side, comment
					.getValue());
		} else {
			return new Connection(leftShape, rightShape, side);
		}

	}

	private Connection operateOnArrayMapWithSelf(FCallNode fcallnode,
			Pair parents, XStrNode comment) {
		Connection connection = null;
		int selfSide = 0;
		CallNode parentCallNode = (CallNode) fcallnode.childNodes().get(0)
				.childNodes().get(0);
		Connection parentConnection = operateOnSimpleArrayMapWithNoElements(
				parentCallNode, parents, comment);
		IterNode iterNode = (IterNode) fcallnode.childNodes().get(1);
		CallNode callnode = null;
		XStrNode childComment = null;
		for (Node node : iterNode.getBodyNode().childNodes()) {
			if (node instanceof NewlineNode) {

				for (Node child : node.childNodes()) {
					if (child instanceof FCallNode) {
						for (Node preChild : child.childNodes().get(0)
								.childNodes()) {
							if (preChild instanceof CallNode) {
								callnode = (CallNode) preChild;
							}
						}
					} else if (child instanceof XStrNode) {
						childComment = (XStrNode) child;
					}

				}
			}
			// (CallNode) node.childNodes().get(0)
			// .childNodes().get(0).childNodes().get(0);
			if (callnode != null) {

				Connection tmpConnection = operateOnSimpleMapOrWithFunctionCall(
						callnode, new Pair(parentConnection.getSource(),
								parentConnection.getTarget()), null);
				if (tmpConnection.getSource().getName().equals("self")) {
					connection = new Connection(parentConnection.getSource(),
							tmpConnection.getTarget(), tmpConnection
									.getMappingSide());
					Shape.removeShape(tmpConnection.getSource());
				} else {
					connection = new Connection(tmpConnection.getSource(),
							parentConnection.getTarget(), tmpConnection
									.getMappingSide());

					Shape.removeShape(tmpConnection.getTarget());
				}
				if (childComment != null) {
					connection.setComment(childComment.getValue());
				}
				childComment = null;
				tmpConnection = null;
			}
		}
		Connection.removeConnection(parentConnection);
		return connection;

	}

	private Connection operateOnMapWithSubobject(FCallNode subobjectFcallnode,
			Pair parents, XStrNode comment) {
		int side = 0;
		Connection connection = null;
		CallNode parentCallNode = (CallNode) subobjectFcallnode.childNodes()
				.get(0).childNodes().get(0);
		Connection parentConnection = operateOnSimpleMapOrWithFunctionCall(
				parentCallNode, parents, comment);
		BlockNode blockNode = (BlockNode) subobjectFcallnode.childNodes()
				.get(1).childNodes().get(1);
		XStrNode childComment = null;
		for (Node node : blockNode.childNodes()) {
			if (node instanceof NewlineNode) {
				if (((NewlineNode) node).getNextNode() instanceof XStrNode) {
					childComment = (XStrNode) ((NewlineNode) node)
							.getNextNode();
				} else if (((NewlineNode) node).getNextNode() instanceof FCallNode) {
					if (parentConnection.getSource().getName().equals("self")) {
						side = Shape.LEFT_SIDE;
						connection = operateOnInternalMap((NewlineNode) node,
								new Pair(parents.getLeftShape(),
										parentConnection.getTarget()),
								childComment);

					} else if (parentConnection.getTarget().getName().equals(
							"self")) {
						side = Shape.RIGHT_SIDE;
						connection = operateOnInternalMap((NewlineNode) node,
								new Pair(parentConnection.getSource(), parents
										.getRightShape()), childComment);
					}
					childComment = null;
				}

			}
		}
		if (side == Shape.LEFT_SIDE) {
			Shape.removeShape(parentConnection.getSource());
			parentConnection = null;
		} else if (side == Shape.RIGHT_SIDE) {
			Shape.removeShape(parentConnection.getTarget());
			parentConnection = null;
		}
		return connection;
	}

	private Connection operateOnMapWithSubmap(FCallNode fcallnode,
			Pair parents, XStrNode comment) {
		Connection connection = null;
		CallNode parentCallNode = (CallNode) fcallnode.childNodes().get(0)
				.childNodes().get(0);
		Connection parentConnection = operateOnSimpleMapOrWithFunctionCall(
				parentCallNode, parents, comment);
		IterNode iterNode = (IterNode) fcallnode.childNodes().get(1);
		XStrNode childComment = null;
		for (Node node : iterNode.getBodyNode().childNodes()) {
			if (node instanceof NewlineNode) {
				if (((NewlineNode) node).getNextNode() instanceof XStrNode) {
					childComment = (XStrNode) ((NewlineNode) node)
							.getNextNode();
				} else if (((NewlineNode) node).getNextNode() instanceof FCallNode) {
					connection = operateOnInternalMap((NewlineNode) node,
							new Pair(parentConnection.getSource(),
									parentConnection.getTarget()), childComment);
				}

			}
		}
		Connection.removeConnection(parentConnection);
		return connection;
	}

	private Connection operateOnArrayMap(CallNode arrayCallNode, Pair parents,
			XStrNode comment) {
		System.out.println("Parents:" + parents);
		int side = Connection.translateSideFromStringToInt((arrayCallNode)
				.getName());
		CallNode leftCallNode = (CallNode) arrayCallNode.childNodes().get(0);
		CallNode rightCallNode = (CallNode) arrayCallNode.childNodes().get(1)
				.childNodes().get(0);
		CallNode leftNode = findLastCallNodeInTree(leftCallNode);
		CallNode rightNode = findLastCallNodeInTree(rightCallNode);
		Connection connection = null;
		if ((findNameNode(leftCallNode, "[]") != null && findNameNode(
				rightCallNode, "[]") != null)
				|| (leftCallNode.getName().equals("[]") && rightCallNode
						.getName().equals("[]"))) {
			connection = new Connection(Shape.createShape(leftNode.getName()
					+ "[]", null, parents.getLeftShape(), Shape.LEFT_SIDE,
					leftCallNode), Shape.createShape(
					rightNode.getName() + "[]", null, parents.getRightShape(),
					Shape.RIGHT_SIDE, rightCallNode), side);
			connection.getSource().getShapeParent().addToParent();
			connection.getTarget().getShapeParent().addToParent();
		} else if (findNameNode(leftCallNode, "[]") != null
				|| leftCallNode.getName().equals("[]")) {
			connection = new Connection(Shape.createShape(leftNode.getName()
					+ "[]", null, parents.getLeftShape(), Shape.LEFT_SIDE,
					leftCallNode), Shape.createShape(rightNode.getName(), null,
					parents.getRightShape(), Shape.RIGHT_SIDE, rightCallNode),
					side);
			connection.getSource().getShapeParent().addToParent();
			connection.getTarget().getShapeParent().addToParent();
		} else if (findNameNode(rightCallNode, "[]") != null
				|| rightCallNode.getName().equals("[]")) {
			connection = new Connection(Shape
					.createShape(leftNode.getName(), null, parents
							.getLeftShape(), Shape.LEFT_SIDE, leftCallNode),
					Shape.createShape(rightNode.getName() + "[]", null, parents
							.getRightShape(), Shape.RIGHT_SIDE, rightCallNode),
					side);
			connection.getSource().addToParent();
			connection.getTarget().addToParent();
		}
		if (comment != null) {
			connection.setComment(comment.getValue());
		}

		return connection;
	}

	private Connection operateOnArrayMapOld(CallNode arrayCallNode, Pair parents) {
		int side = Connection.translateSideFromStringToInt((arrayCallNode)
				.getName());
		// TODO co w przypadku images[] <=> photos[]?

		CallNode arrayNode;
		if ((arrayNode = (CallNode) arrayCallNode.childNodes().get(0))
				.getName().equals("[]")) {
			CallNode rightNode = findLastCallNodeInTree(arrayCallNode
					.childNodes().get(1).childNodes().get(0));

			CallNode arrayNameNode = (CallNode) arrayNode.childNodes().get(0);
			FixnumNode fixNumberNode = (FixnumNode) arrayNode.childNodes().get(
					1).childNodes().get(0);
			boolean addArrayVariable = true;
			String leftShapeName = arrayNameNode.getName() + "["
					+ fixNumberNode.getValue() + "]";
			int shapeIndex = 0;
			for (shapeIndex = 0; shapeIndex < Shape.getShapes().size(); shapeIndex++) {
				if (Shape.getShapes().get(shapeIndex).getName().equals(
						arrayNameNode.getName() + "[]")) {
					addArrayVariable = false;
					break;
				}
			}
			if (Connection.connectionNotExists(leftShapeName, rightNode
					.getName())) {
				Shape rightShape = Shape.createShape(rightNode.getName(), null,
						parents.getRightShape(), Shape.RIGHT_SIDE, null);
				if (addArrayVariable) {
					Shape arrayShape = Shape.createShape(arrayNameNode
							.getName()
							+ "[]", null, parents.getLeftShape(),
							Shape.LEFT_SIDE, null);
					Shape leftShape = Shape.createShape(leftShapeName, null,
							arrayShape, Shape.LEFT_SIDE, null);
					return new Connection(leftShape, rightShape, side);
				} else {
					Shape leftShape = Shape.createShape(leftShapeName, null,
							Shape.getShapes().get(shapeIndex), Shape.LEFT_SIDE,
							null);
					return new Connection(leftShape, rightShape, side);
				}
			}

		}

		else if ((arrayNode = (CallNode) arrayCallNode.childNodes().get(1)
				.childNodes().get(0)).getName().equals("[]")) {
			CallNode leftNode = findLastCallNodeInTree(arrayCallNode
					.childNodes().get(0));

			CallNode arrayNameNode = (CallNode) arrayNode.childNodes().get(0);
			FixnumNode fixNumberNode = (FixnumNode) arrayNode.childNodes().get(
					1).childNodes().get(0);
			boolean addArrayVariable = true;
			String rightShapeName = arrayNameNode.getName() + "["
					+ fixNumberNode.getValue() + "]";
			int shapeIndex = 0;
			for (shapeIndex = 0; shapeIndex < Shape.getShapes().size(); shapeIndex++) {
				if (Shape.getShapes().get(shapeIndex).getName().equals(
						arrayNameNode.getName() + "[]")) {
					addArrayVariable = false;
					break;
				}
			}
			if (Connection.connectionNotExists(leftNode.getName(),
					rightShapeName)) {
				Shape leftShape = Shape.createShape(leftNode.getName(), null,
						parents.getLeftShape(), Shape.LEFT_SIDE, null);
				if (addArrayVariable) {
					Shape arrayShape = Shape.createShape(arrayNameNode
							.getName()
							+ "[]", null, parents.getRightShape(),
							Shape.RIGHT_SIDE, null);

					Shape rightShape = Shape.createShape(rightShapeName, null,
							arrayShape, Shape.RIGHT_SIDE, null);
					return new Connection(leftShape, rightShape, side);
				} else {
					Shape rightShape = Shape.createShape(rightShapeName, null,
							Shape.getShapes().get(shapeIndex),
							Shape.RIGHT_SIDE, null);
					return new Connection(leftShape, rightShape, side);
				}
			}

		}
		return null;
	}

	private Connection operateOnSimpleMapOrWithFunctionCall(CallNode callnode,
			Pair parents, XStrNode comment) {
		CallNode leftNode = findLastCallNodeInTree(callnode.childNodes().get(0));
		CallNode rightNode = findLastCallNodeInTree(callnode.childNodes()
				.get(1).childNodes().get(0));
		int side = Connection
				.translateSideFromStringToInt((callnode).getName());
		boolean canCreate = Connection.connectionNotExists(leftNode.getName(),
				rightNode.getName());
		Connection connection = null;
		if (canCreate) {
			connection = new Connection(Shape.createShape(leftNode.getName(),
					null, parents.getLeftShape(), Shape.LEFT_SIDE, leftNode),
					Shape.createShape(rightNode.getName(), null, parents
							.getRightShape(), Shape.RIGHT_SIDE, rightNode),
					side);
			connection.getSource().addToParent();
			connection.getTarget().addToParent();
		}
		if (comment != null) {
			connection.setComment(comment.getValue());
		}
		return connection;

	}

	/**
	 * Finds last appearance of callNode in tree (last child). Method used for
	 * operation on simple mappping
	 * 
	 * @param callnode
	 * @return
	 */
	public static CallNode findLastCallNodeInTree(Node callnode) {
		CallNode returnNode = null;
		if (callnode.childNodes().get(0) instanceof CallNode) {
			returnNode = findLastCallNodeInTree(callnode.childNodes().get(0));
		} else {
			returnNode = (CallNode) callnode;
		}
		return returnNode;
	}

	/**
	 * Find and returns child node of the inserted name
	 * 
	 * @param parent
	 * @param nodeName
	 * @return
	 */
	public static INameNode findNameNode(Node parent, String nodeName) {
		INameNode nameNode = null;
		for (Node child : parent.childNodes()) {
			if (child instanceof INameNode) {
				if (((INameNode) child).getName().equals(nodeName)) {
					nameNode = (INameNode) child;
				} else {
					nameNode = findNameNode(child, nodeName);
				}
			} else {
				nameNode = findNameNode(child, nodeName);
			}

		}
		return nameNode;
	}

	public int checkMappingType(FCallNode callNode) {
		// jest bez paremtrow
		if (callNode.childNodes().size() == 1) {
			if (callNode.childNodes().get(0).childNodes().size() == 1) {
				// SIMPLE_MAP_OR_WITH_FUNCTION_CALL, SIMPLE_ARRAY_MAP
				CallNode mapType = (CallNode) callNode.childNodes().get(0)
						.childNodes().get(0);
				if (findNameNode(mapType, "[]") != null) {
					return SIMPLE_ARRAY_MAP;
				} else {
					return SIMPLE_MAP_OR_WITH_FUNCTION_CALL;
				}
			} else {
				// MAP_WITH_DICTIONARY,
				return MAP_WITH_DICTIONARY;
			}
		} else {
			// MAP_WITH_CODE, MAP_WITH_SUBMAP, ARRAY_MAP_WITH_SELF,
			// MAP_WITH_SUBOBJECT
			if (findNameNode(callNode.childNodes().get(1), "<<") != null
					|| findNameNode(callNode.childNodes().get(1), ">>") != null
					|| findNameNode(callNode.childNodes().get(1), "<=>") != null) {
				if (findNameNode(callNode.childNodes().get(0), "self") != null) {
					return MAP_WITH_SUBOBJECT;
				} else if (findNameNode(callNode.childNodes().get(1), "self") != null) {
					return ARRAY_MAP_WITH_SELF;
				} else {
					return MAP_WITH_SUBMAP;
				}
			} else {
				return MAP_WITH_CODE;
			}

		}

	}

	/**
	 * Creates complex elements (with childs)
	 * 
	 * @param node
	 * @param parents
	 * @return
	 */
	private Pair createComplexElements(Node node, Pair parents) {
		Shape leftElement = null;
		Shape rightElement = null;
		boolean left = true;
		for (Node preChild : (((FCallNode) node).getArgsNode().childNodes())) {
			if (preChild instanceof Colon2Node) {
				if (left) {
					leftElement = Shape.createShape(((Colon2Node) preChild)
							.getName(), null, parents.getLeftShape(),
							Shape.LEFT_SIDE, null);
					left = false;
				} else {
					rightElement = Shape.createShape(((Colon2Node) preChild)
							.getName(), null, parents.getRightShape(),
							Shape.RIGHT_SIDE, null);
					left = true;
				}
			} else if (preChild instanceof SymbolNode) {
				if (left) {
					leftElement = Shape.createShape(((SymbolNode) preChild)
							.getName(), null, parents.getLeftShape(),
							Shape.LEFT_SIDE, null);
					left = false;
				} else {
					rightElement = Shape.createShape(((SymbolNode) preChild)
							.getName(), null, parents.getRightShape(),
							Shape.RIGHT_SIDE, null);
					left = true;
				}
			} else if (preChild instanceof ConstNode) {
				if (left) {
					leftElement = Shape.createShape(((ConstNode) preChild)
							.getName(), null, parents.getLeftShape(),
							Shape.LEFT_SIDE, null);
					left = false;
				} else {
					rightElement = Shape.createShape(((ConstNode) preChild)
							.getName(), null, parents.getRightShape(),
							Shape.RIGHT_SIDE, null);
					left = true;
				}
			}
		}

		return new Pair(leftElement, rightElement);
	}

	public String generateRubyCode() throws IOException, CoreException {

		String source = "";
		if (file != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					file.getContents()));
			String s = "";
			while ((s = reader.readLine()) != null) {
				source += s;
			}
		}

		return ReWriteVisitor.createCodeFromNode(RootNodeHolder.getInstance()
				.getRootNode(), source);
	}


}