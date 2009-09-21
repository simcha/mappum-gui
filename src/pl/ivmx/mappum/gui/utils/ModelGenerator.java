package pl.ivmx.mappum.gui.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.jrubyparser.Parser;
import org.jrubyparser.ast.ArrayNode;
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
import org.jrubyparser.ast.StrNode;
import org.jrubyparser.ast.SymbolNode;
import org.jrubyparser.ast.VCallNode;
import org.jrubyparser.ast.XStrNode;
import org.jrubyparser.parser.ParserConfiguration;
import org.jrubyparser.rewriter.ReWriteVisitor;

import pl.ivmx.mappum.gui.model.Connection;
import pl.ivmx.mappum.gui.model.Shape;

public class ModelGenerator {
	private static final int SIMPLE_MAP_OR_WITH_FUNCTION_CALL = 1;
	private static final int MAP_WITH_DICTIONARY = 2;
	private static final int MAP_WITH_CODE = 3;
	private static final int MAP_WITH_SELF = 5;
	private static final int MAP_WITH_SUBOBJECT = 6;
	private static final int MAP_WITH_SUBMAP = 7;
	private static final int MAP_WITH_CONSTANT = 8;
	private static final int MAP_WITH_FUNCTION = 9;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Parser parser;
	private Reader content;
	private ParserConfiguration configuration;
	private static int lastUsedElementNumberInArray = -1;

	private static final ModelGenerator INSTANCE = new ModelGenerator();

	private Logger logger = Logger.getLogger(ModelGenerator.class);
	private ParserConfiguration parserConfiguration;

	private ModelGenerator() {
	}

	public static final ModelGenerator getInstance() {
		return INSTANCE;
	}

	/**
	 * Parses file containing mapping in ruby code, and returns ruby tree model
	 * 
	 * @param file
	 * @return
	 * @throws CoreException
	 */
	private Node parseRubbyFile(final IFile file) throws CoreException {
		parser = new Parser();
		content = new InputStreamReader(file.getContents());
		configuration = new ParserConfiguration();
		return parser.parse(file.getName(), content, configuration);
	}

	public Node parseExternalRubbyCode(String code) {
		Parser parser2 = new Parser();
		InputStreamReader inputStreamReader = new InputStreamReader(
				new ByteArrayInputStream(code.getBytes()));
		parserConfiguration = new ParserConfiguration();
		return parser2.parse("", inputStreamReader, parserConfiguration);
	}

	/**
	 * Generates GUI tree model from ruby tree model root elements
	 * 
	 * @param file
	 * @throws CoreException
	 */
	public void generateModelRootElements(IFile file) throws CoreException {
		RootNodeHolder
				.getInstance()
				.setRootNode(
						RootNodeHolder
								.correctNodeIterationBlocks(parseRubbyFile(file)));
		new TestNodeTreeWindow(RootNodeHolder.getInstance().getRootNode());
		createRootMapElements(RootNodeHolder.getInstance().getRootNode());
	}

	/**
	 * Generates GUI tree model from ruby tree model
	 * 
	 * @param file
	 * @throws CoreException
	 */
	public void generateModelChildElements() throws CoreException {
		findRootMap(RootNodeHolder.getInstance().getRootNode());
	}

	/**
	 * Creates root map elements
	 * 
	 * @param node
	 */
	private void createRootMapElements(Node node) {
		boolean iterate = true;
		for (Node child : node.childNodes()) {
			if (child instanceof FCallNode) {
				if (((FCallNode) child).getName() == "map") {
					// TODO generating comments for root Map
					iterate = false;
					Pair parents = new Pair();
					createComplexElements(child, parents);
					return;
				}
			}
			if (iterate == true)
				createRootMapElements(child);
		}
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
					// Pair parents = new Pair();
					// createComplexElements(child, parents);
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
		case MAP_WITH_FUNCTION:
			FCallNode functnode = (FCallNode) node.childNodes().get(0);
			return operateOnMapWithFunction(functnode, parents, comment);
		case MAP_WITH_CONSTANT:
			CallNode mapnode = (CallNode) node.childNodes().get(0).childNodes()
					.get(0).childNodes().get(0);
			return operateOnMapWithConstant(mapnode, parents, comment);
		case SIMPLE_MAP_OR_WITH_FUNCTION_CALL:
			CallNode callnode = (CallNode) node.childNodes().get(0)
					.childNodes().get(0).childNodes().get(0);
			return operateOnSimpleMapOrWithFunctionCall(callnode, parents,
					comment);
			// case SIMPLE_ARRAY_MAP:
			// CallNode arrayCallNode = (CallNode) node.childNodes().get(0)
			// .childNodes().get(0).childNodes().get(0);
			// return operateOnArrayMap(arrayCallNode, parents, comment);
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
		case MAP_WITH_SELF:
			CallNode selfCallnode = (CallNode) node.childNodes().get(0)
					.childNodes().get(0).childNodes().get(0);
			return operateOnMapWithSelf(selfCallnode, parents, comment);
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

	private String getFunction(Node node, String previousName) {
		if (previousName == null) {
			previousName = "";
		}
		if (node instanceof VCallNode) {
			return ((VCallNode) node).getName();
		} else if (node instanceof CallNode) {
			if (previousName.equals("")) {
				previousName = ((CallNode) node).getName();
			} else {
				previousName = ((CallNode) node).getName() + "." + previousName;
			}
			if (node.childNodes().size() > 0) {
				previousName = getFunction(node.childNodes().get(0),
						previousName);
			}

		} else if (node instanceof ConstNode) {
			previousName = ((ConstNode) node).getName() + "." + previousName;
		}
		return previousName;
	}

	private Connection operateOnMapWithFunction(FCallNode mapNode,
			Pair parents, XStrNode comment) {
		CallNode assignNode = (CallNode) mapNode.childNodes().get(0)
				.childNodes().get(0);
		Pair pair = null;
		if (assignNode.getReceiverNode() instanceof VCallNode) {
			pair = new Pair(parents.getLeftShape(), createRightShape(
					assignNode, parents));
		} else {
			assert assignNode.getArgsNode().childNodes().get(0) instanceof VCallNode;
			pair = new Pair(createLeftShape(assignNode, parents), parents
					.getRightShape());
		}
		int side = Connection.translateSideFromStringToInt((assignNode)
				.getName());

		boolean canCreate = Connection.connectionNotExists(pair);
		Connection connection = null;
		if (canCreate) {
			connection = new Connection(pair.getLeftShape(), pair
					.getRightShape(), side, Connection.FUN_TO_VAR_CONN);
			if (comment != null) {
				connection.setComment(comment.getValue());
			}
			if (connection.getSource().isArrayType()
					&& !connection.getTarget().isArrayType()) {
				if (connection.getSource().getArrayCounters().size() > 0)
					connection.setArrayNumber(connection.getSource()
							.getArrayCounters().get(
									connection.getSource().getArrayCounters()
											.size() - 1));
			} else if (!connection.getSource().isArrayType()
					&& connection.getTarget().isArrayType()) {
				if (connection.getTarget().getArrayCounters().size() > 0)
					connection.setArrayNumber(connection.getTarget()
							.getArrayCounters().get(
									connection.getTarget().getArrayCounters()
											.size() - 1));
			}
			BlockNode blockNode = (BlockNode) mapNode.childNodes().get(1)
					.childNodes().get(0);
			for (Node node : blockNode.childNodes()) {
				if (node instanceof NewlineNode) {
					connection.addFunction(getFunction(((NewlineNode) node)
							.getNextNode(), null));
				}
			}
		}

		return connection;
	}

	private Connection operateOnMapWithConstant(CallNode mapnode, Pair parents,
			XStrNode comment) {
		Pair pair = null;
		String constantName = null;
		if (mapnode.getReceiverNode() instanceof StrNode) {
			pair = new Pair(parents.getLeftShape(), createRightShape(mapnode,
					parents));
			constantName = ((StrNode) mapnode.getReceiverNode()).getValue();
		} else {
			assert mapnode.getArgsNode().childNodes().get(0) instanceof StrNode;
			pair = new Pair(createLeftShape(mapnode, parents), parents
					.getRightShape());
			constantName = ((StrNode) mapnode.getArgsNode().childNodes().get(0))
					.getValue();
		}
		int side = Connection.translateSideFromStringToInt((mapnode).getName());
		boolean canCreate = Connection.connectionNotExists(pair);
		Connection connection = null;
		if (canCreate) {
			connection = new Connection(pair.getLeftShape(), pair
					.getRightShape(), side, Connection.CONST_TO_VAR_CONN);
			connection.setConstantName(constantName);
			if (connection.getSource().isArrayType()
					&& !connection.getTarget().isArrayType()) {
				if (connection.getSource().getArrayCounters().size() > 0)
					connection.setArrayNumber(connection.getSource()
							.getArrayCounters().get(
									connection.getSource().getArrayCounters()
											.size() - 1));
			} else if (!connection.getSource().isArrayType()
					&& connection.getTarget().isArrayType()) {
				if (connection.getTarget().getArrayCounters().size() > 0)
					connection.setArrayNumber(connection.getTarget()
							.getArrayCounters().get(
									connection.getTarget().getArrayCounters()
											.size() - 1));
			}
			if (comment != null) {
				connection.setComment(comment.getValue());
			}
		}

		return connection;
	}

	private Connection operateOnMapWithSelf(CallNode callnode, Pair parents,
			XStrNode comment) {
		int side = Connection
				.translateSideFromStringToInt((callnode).getName());
		Pair pair;
		if (RootNodeHolder.checkLeftSideMappingName(callnode).equals("self")) {
			pair = new Pair(parents.getLeftShape(), createRightShape(callnode,
					parents));
		} else {
			pair = new Pair(createLeftShape(callnode, parents), parents
					.getRightShape());
		}
		boolean canCreate = Connection.connectionNotExists(pair);
		Connection connection = null;
		if (canCreate) {
			connection = new Connection(pair.getLeftShape(), pair
					.getRightShape(), side, Connection.VAR_TO_VAR_CONN);
			if (connection.getSource().isArrayType()
					&& !connection.getTarget().isArrayType()) {
				if (connection.getSource().getArrayCounters().size() > 0)
					connection.setArrayNumber(connection.getSource()
							.getArrayCounters().get(
									connection.getSource().getArrayCounters()
											.size() - 1));
			} else if (!connection.getSource().isArrayType()
					&& connection.getTarget().isArrayType()) {
				if (connection.getTarget().getArrayCounters().size() > 0)
					connection.setArrayNumber(connection.getTarget()
							.getArrayCounters().get(
									connection.getTarget().getArrayCounters()
											.size() - 1));
			}

			if (comment != null) {
				connection.setComment(comment.getValue());
			}
		}

		return connection;

	}

	private Connection operateOnMapWithSubobject(FCallNode subobjectFcallnode,
			Pair parents, XStrNode comment) {
		Connection connection = null;
		CallNode parentCallNode = (CallNode) subobjectFcallnode.childNodes()
				.get(0).childNodes().get(0);
		BlockNode blockNode = (BlockNode) subobjectFcallnode.childNodes()
				.get(1).childNodes().get(1);
		XStrNode childComment = null;
		for (Node node : blockNode.childNodes()) {
			if (node instanceof NewlineNode) {
				if (((NewlineNode) node).getNextNode() instanceof XStrNode) {
					childComment = (XStrNode) ((NewlineNode) node)
							.getNextNode();
				} else if (((NewlineNode) node).getNextNode() instanceof FCallNode) {
					if (RootNodeHolder.checkLeftSideMappingName(parentCallNode)
							.equals("self")) {
						connection = operateOnInternalMap((NewlineNode) node,
								new Pair(parents.getLeftShape(),
										createRightShape(parentCallNode,
												parents)), childComment);

					} else if (RootNodeHolder.checkRightSideMappingName(
							parentCallNode).equals("self")) {
						connection = operateOnInternalMap((NewlineNode) node,
								new Pair(createLeftShape(parentCallNode,
										parents), parents.getRightShape()),
								childComment);
					}
					childComment = null;

					if (connection != null) {
						if (parents.getLeftShape().isArrayType()
								&& !parents.getRightShape().isArrayType()) {
							if (parents.getLeftShape().getArrayCounters()
									.size() > 0)
								connection.setArrayNumber(parents
										.getLeftShape().getArrayCounters().get(
												parents.getLeftShape()
														.getArrayCounters()
														.size() - 1));
						} else if (!parents.getLeftShape().isArrayType()
								&& parents.getRightShape().isArrayType()) {
							if (parents.getRightShape().getArrayCounters()
									.size() > 0)
								connection.setArrayNumber(parents
										.getRightShape().getArrayCounters()
										.get(
												parents.getRightShape()
														.getArrayCounters()
														.size() - 1));
						}
						if (comment != null) {
							connection.setComment(comment.getValue());
						}
					}
				}

			}
		}
		return connection;
	}

	private Connection operateOnMapWithSubmap(FCallNode fcallnode,
			Pair parents, XStrNode comment) {
		Connection connection = null;
		CallNode parentCallNode = (CallNode) fcallnode.childNodes().get(0)
				.childNodes().get(0);
		Pair mainPair = createShapesPair(parentCallNode, parents);
		IterNode iterNode = (IterNode) fcallnode.childNodes().get(1);
		XStrNode childComment = null;
		for (Node node : iterNode.getBodyNode().childNodes()) {
			if (node instanceof NewlineNode) {
				if (((NewlineNode) node).getNextNode() instanceof XStrNode) {
					childComment = (XStrNode) ((NewlineNode) node)
							.getNextNode();
				} else if (((NewlineNode) node).getNextNode() instanceof FCallNode) {
					connection = operateOnInternalMap((NewlineNode) node,
							mainPair, childComment);

					if (connection != null) {
						if (mainPair.getLeftShape().isArrayType()
								&& !mainPair.getRightShape().isArrayType()) {
							if (mainPair.getLeftShape().getArrayCounters()
									.size() > 0)
								connection.setArrayNumber(mainPair
										.getLeftShape().getArrayCounters().get(
												mainPair.getLeftShape()
														.getArrayCounters()
														.size() - 1));
						} else if (!mainPair.getLeftShape().isArrayType()
								&& mainPair.getRightShape().isArrayType()) {
							if (mainPair.getRightShape().getArrayCounters()
									.size() > 0)
								connection.setArrayNumber(mainPair
										.getRightShape().getArrayCounters()
										.get(
												mainPair.getRightShape()
														.getArrayCounters()
														.size() - 1));
						}
					}

				} else if (((NewlineNode) node).getNextNode() instanceof CallNode) {
					connection = operateOnMapWithConstant(
							(CallNode) ((NewlineNode) node).getNextNode(),
							mainPair, childComment);

					if (connection != null) {
						if (mainPair.getLeftShape().isArrayType()
								&& !mainPair.getRightShape().isArrayType()) {
							if (mainPair.getLeftShape().getArrayCounters()
									.size() > 0)
								connection.setArrayNumber(mainPair
										.getLeftShape().getArrayCounters().get(
												mainPair.getLeftShape()
														.getArrayCounters()
														.size() - 1));
						} else if (!mainPair.getLeftShape().isArrayType()
								&& mainPair.getRightShape().isArrayType()) {
							if (mainPair.getRightShape().getArrayCounters()
									.size() > 0)
								connection.setArrayNumber(mainPair
										.getRightShape().getArrayCounters()
										.get(
												mainPair.getRightShape()
														.getArrayCounters()
														.size() - 1));
						}
						if (comment != null) {
							connection.setComment(comment.getValue());
						}
					}

				}

			}
		}

		return connection;
	}

	private Connection operateOnSimpleMapOrWithFunctionCall(CallNode callnode,
			Pair parents, XStrNode comment) {
		Pair pair = createShapesPair(callnode, parents);
		int side = Connection
				.translateSideFromStringToInt((callnode).getName());
		boolean canCreate = Connection.connectionNotExists(pair);
		Connection connection = null;
		if (canCreate) {
			connection = new Connection(pair.getLeftShape(), pair
					.getRightShape(), side, Connection.VAR_TO_VAR_CONN);

			if (connection.getSource().isArrayType()
					&& !connection.getTarget().isArrayType()) {
				if (connection.getSource().getArrayCounters().size() > 0)
					connection.setArrayNumber(connection.getSource()
							.getArrayCounters().get(
									connection.getSource().getArrayCounters()
											.size() - 1));
			} else if (!connection.getSource().isArrayType()
					&& connection.getTarget().isArrayType()) {
				if (connection.getTarget().getArrayCounters().size() > 0)
					connection.setArrayNumber(connection.getTarget()
							.getArrayCounters().get(
									connection.getTarget().getArrayCounters()
											.size() - 1));
			}

			if (comment != null) {
				connection.setComment(comment.getValue());
			}

		}

		return connection;

	}

	private Pair createShapesPair(CallNode callnode, Pair parents) {
		return new Pair(createLeftShape(callnode, parents), createRightShape(
				callnode, parents));
	}

	/**
	 * Creates left Shape on the ShapeDiagram
	 * 
	 * @param callnode
	 * @param parents
	 * @return
	 */
	private Shape createLeftShape(CallNode callnode, Pair parents) {
		
		CallNode rootNode = null;
		CallNode leftNode = null;
		Shape leftShape = null;
		if (callnode.childNodes().get(0) instanceof CallNode) {
			rootNode = (CallNode) callnode.childNodes().get(0);
			leftNode = findLastCallNodeInTree(callnode.childNodes().get(0));
			leftShape = Shape.createShape(leftNode.getName(), null, parents
					.getLeftShape(), Shape.LEFT_SIDE, rootNode);
			leftShape.addToParent();
			if (rootNode != null) {

				if (findNameNode(rootNode, "[]") != null
						|| rootNode.getName().equals("[]"))
					leftShape.setArrayType(true);
				CallNode arrayNode = (CallNode) findNameNode(rootNode, "[]");
				if (arrayNode == null)
					arrayNode = rootNode;
				if (arrayNode.childNodes().size() > 1
						&& arrayNode.childNodes().get(1) instanceof ArrayNode) {
					if (arrayNode.childNodes().get(1).childNodes().get(0) instanceof FixnumNode) {
						FixnumNode numberNode = (FixnumNode) arrayNode
								.childNodes().get(1).childNodes().get(0);
						leftShape
								.addArrayCounter(((int) numberNode.getValue()));
						setLastUsedElementNumberInArray((int) numberNode.getValue());
					}
				}
			}
		}

		return leftShape;

	}

	/**
	 * Creates right Shape on the ShapeDiagram
	 * 
	 * @param callnode
	 * @param parents
	 * @return
	 */
	private Shape createRightShape(CallNode callnode, Pair parents) {
		CallNode rootNode = null;
		CallNode rightNode = null;
		Shape rightShape = null;
		if (callnode.childNodes().get(1).childNodes().get(0) instanceof CallNode) {
			rootNode = (CallNode) callnode.childNodes().get(1).childNodes()
					.get(0);
			rightNode = findLastCallNodeInTree(callnode.childNodes().get(1)
					.childNodes().get(0));
			rightShape = Shape.createShape(rightNode.getName(), null, parents
					.getRightShape(), Shape.RIGHT_SIDE, (CallNode) callnode
					.childNodes().get(1).childNodes().get(0));
			rightShape.addToParent();
			if (rootNode != null) {

				if (findNameNode(rootNode, "[]") != null
						|| rootNode.getName().equals("[]"))
					rightShape.setArrayType(true);
				CallNode arrayNode = (CallNode) findNameNode(rootNode, "[]");
				if (arrayNode == null)
					arrayNode = rootNode;
				if (arrayNode.childNodes().size() > 1
						&& arrayNode.childNodes().get(1) instanceof ArrayNode) {
					if (arrayNode.childNodes().get(1).childNodes().get(0) instanceof FixnumNode) {
						FixnumNode numberNode = (FixnumNode) arrayNode
								.childNodes().get(1).childNodes().get(0);
						rightShape
								.addArrayCounter(((int) numberNode.getValue()));
						setLastUsedElementNumberInArray((int) numberNode.getValue());
					}
				}
			}
		}
		return rightShape;

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
		if (callnode.childNodes().size() > 0
				&& callnode.childNodes().get(0) instanceof CallNode) {
			returnNode = findLastCallNodeInTree(callnode.childNodes().get(0));
		} else {
			if (callnode instanceof CallNode)
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
					return nameNode = (INameNode) child;
				}
			}
			nameNode = findNameNode(child, nodeName);
			if (nameNode != null) {
				return nameNode;
			}

		}
		return nameNode;
	}

	/**
	 * Check what type of mapping contains given FCallNode
	 * 
	 * @param callNode
	 * @return
	 */
	public int checkMappingType(FCallNode callNode) {
		// jest bez paremtrow
		if (callNode.childNodes().size() == 1) {
			if (callNode.childNodes().get(0).childNodes().size() == 1) {
				// SIMPLE_MAP_OR_WITH_FUNCTION_CALL, SIMPLE_ARRAY_MAP
				CallNode mapNode = (CallNode) callNode.childNodes().get(0)
						.childNodes().get(0);
				// if (findNameNode(mapNode, "[]") != null) {
				// return SIMPLE_ARRAY_MAP;
				if (findNameNode(mapNode, "self") != null) {
					return MAP_WITH_SELF;
				} else {
					if (mapNode.getArgsNode().childNodes().get(0) instanceof StrNode
							|| mapNode.getReceiverNode() instanceof StrNode) {
						return MAP_WITH_CONSTANT;
					} else
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
				} else {
					return MAP_WITH_SUBMAP;
				}
			} else if (findNameNode(callNode, "func") != null) {
				return MAP_WITH_FUNCTION;
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
							.getName(), ((ConstNode) ((Colon2Node) preChild)
							.getLeftNode()).getName(), parents.getLeftShape(),
							Shape.LEFT_SIDE, null);
					left = false;
				} else {
					rightElement = Shape.createShape(((Colon2Node) preChild)
							.getName(), ((ConstNode) ((Colon2Node) preChild)
							.getLeftNode()).getName(), parents.getRightShape(),
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

	public String generateRubyCodeFromRootNode() {
		return generateRubyCodeFromNode(RootNodeHolder.getInstance()
				.getRootNode());
	}

	public String generateRubyCodeFromNode(Node node) {
		// TODO: "" == 'lekkie' naduzycie
		return ReWriteVisitor.createCodeFromNode(node, "");
	}

	public static void setLastUsedElementNumberInArray(
			int lastUsedElementNumberInArray) {
		ModelGenerator.lastUsedElementNumberInArray = lastUsedElementNumberInArray;
	}

	public static int getLastUsedElementNumberInArray() {
		return lastUsedElementNumberInArray;
	}
}