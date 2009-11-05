package pl.ivmx.mappum.gui.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.jrubyparser.Parser;
import org.jrubyparser.ast.ArrayNode;
import org.jrubyparser.ast.BlockNode;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.Colon2Node;
import org.jrubyparser.ast.ConstNode;
import org.jrubyparser.ast.DAsgnNode;
import org.jrubyparser.ast.DVarNode;
import org.jrubyparser.ast.FCallNode;
import org.jrubyparser.ast.FixnumNode;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.IterNode;
import org.jrubyparser.ast.MultipleAsgnNode;
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
import pl.ivmx.mappum.gui.model.Shape.SourceType;
import pl.ivmx.mappum.gui.model.test.TestNodeTreeWindow;

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
	private Node parseRubyFile(final IFile file) throws CoreException {
		parser = new Parser();
		try {
			content = new InputStreamReader(file.getContents());
			configuration = new ParserConfiguration();
			return parser.parse(file.getName(), content, configuration);
		} finally {
			if (content != null) {
				try {
					content.close();
				} catch (IOException e) {
					logger.warn("Failed to close input stream");
				}
			}
		}
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
								.correctNodeIterationBlocks(parseRubyFile(file)));
		TestNodeTreeWindow.show(RootNodeHolder.getInstance().getRootNode());
		createRootMapElements(RootNodeHolder.getInstance().getRootNode());
	}

	/**
	 * Generates GUI tree model from ruby tree model
	 * 
	 * @param file
	 * @throws Exception
	 */
	public void generateModelChildElements() throws Exception {
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
	 * @throws Exception
	 */
	private void findRootMap(Node node) throws Exception {
		String leftChildAlias = null;
		String rightChildAlias = null;
		boolean iterate = true;
		for (Node child : node.childNodes()) {
			if (child instanceof FCallNode) {
				if (((FCallNode) child).getName() == "map") {
					// TODO generating comments for root Map
					iterate = false;
					// Pair parents = new Pair();
					// createComplexElements(child, parents);
					if (((IterNode) ((FCallNode) child).getIterNode())
							.getVarNode() instanceof MultipleAsgnNode) {
						DAsgnNode asgnLeftNode = (DAsgnNode) ((IterNode) ((FCallNode) child)
								.getIterNode()).getVarNode().childNodes()
								.get(0).childNodes().get(0);
						DAsgnNode asgnRightNode = (DAsgnNode) ((IterNode) ((FCallNode) child)
								.getIterNode()).getVarNode().childNodes()
								.get(0).childNodes().get(1);
						leftChildAlias = asgnLeftNode.getName();
						rightChildAlias = asgnRightNode.getName();
					}
					if (((IterNode) ((FCallNode) child).getIterNode())
							.getBodyNode() instanceof BlockNode) {
						final StringBuilder comment = new StringBuilder();
						for (Node newline : ((IterNode) ((FCallNode) child)
								.getIterNode()).getBodyNode().childNodes()) {
							if (((NewlineNode) newline).getNextNode() instanceof XStrNode) {
								if (comment.length() > 0) {
									comment.append("\n");
								}
								comment
										.append(((XStrNode) ((NewlineNode) newline)
												.getNextNode()).getValue());
							} else if (((NewlineNode) newline).getNextNode() instanceof FCallNode) {
								System.out.println(Shape.getRootShapes());
								operateOnInternalMap((NewlineNode) newline,
										new Pair(Shape.getRootShapes().get(0),
												Shape.getRootShapes().get(1)),
										comment.length() == 0 ? null : comment
												.toString(), leftChildAlias,
										rightChildAlias);
								comment.setLength(0);
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
	 * @throws Exception
	 */
	private Connection operateOnInternalMap(NewlineNode node, Pair parents,
			final String comment, String leftAlias, String rightAlias)
			throws Exception {
		int mappingType = 0;
		for (Node child : node.childNodes()) {
			if (child instanceof FCallNode)
				mappingType = checkMappingType((FCallNode) child);
		}
		switch (mappingType) {
		case MAP_WITH_FUNCTION:
			FCallNode functnode = (FCallNode) node.childNodes().get(0);
			return operateOnMapWithFunction(functnode, parents, comment,
					leftAlias, rightAlias);
		case MAP_WITH_CONSTANT:
			CallNode mapnode = (CallNode) node.childNodes().get(0).childNodes()
					.get(0).childNodes().get(0);
			return operateOnMapWithConstant(mapnode, parents, comment,
					leftAlias, rightAlias);
		case SIMPLE_MAP_OR_WITH_FUNCTION_CALL:
			CallNode callnode = (CallNode) node.childNodes().get(0)
					.childNodes().get(0).childNodes().get(0);
			return operateOnSimpleMapOrWithFunctionCall(callnode, parents,
					comment, leftAlias, rightAlias);
			// case SIMPLE_ARRAY_MAP:
			// CallNode arrayCallNode = (CallNode) node.childNodes().get(0)
			// .childNodes().get(0).childNodes().get(0);
			// return operateOnArrayMap(arrayCallNode, parents, comment);
		case MAP_WITH_DICTIONARY:
			// narazie zwykle mapowanie
			CallNode dictionaryCallnode = (CallNode) node.childNodes().get(0)
					.childNodes().get(0).childNodes().get(0);
			return operateOnSimpleMapOrWithFunctionCall(dictionaryCallnode,
					parents, comment, leftAlias, rightAlias);
		case MAP_WITH_SUBOBJECT:
			FCallNode subobjectFcallnode = (FCallNode) node.childNodes().get(0);
			return operateOnMapWithSubobject(subobjectFcallnode, parents,
					comment, leftAlias, rightAlias);
		case MAP_WITH_SELF:
			CallNode selfCallnode = (CallNode) node.childNodes().get(0)
					.childNodes().get(0).childNodes().get(0);
			return operateOnMapWithSelf(selfCallnode, parents, comment,
					leftAlias, rightAlias);
		case MAP_WITH_SUBMAP:
			FCallNode submapFcallnode = (FCallNode) node.childNodes().get(0);
			return operateOnMapWithSubmap(submapFcallnode, parents, comment,
					leftAlias, rightAlias);
		case MAP_WITH_CODE:
			// TODO obsluga kodu (narazie zwykle proste mapowanie)
			CallNode MapWithCodeNode = (CallNode) node.childNodes().get(0)
					.childNodes().get(0).childNodes().get(0);
			return operateOnSimpleMapOrWithFunctionCall(MapWithCodeNode,
					parents, comment, leftAlias, rightAlias);
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
			Pair parents, final String comment, String leftAlias,
			String rightAlias) throws Exception {
		CallNode assignNode = (CallNode) mapNode.childNodes().get(0)
				.childNodes().get(0);
		Pair pair = null;
		Shape shape = null;
		if (assignNode.getReceiverNode() instanceof VCallNode) {
			shape = createRightShape(assignNode, parents, leftAlias, rightAlias);
		} else {
			assert assignNode.getArgsNode().childNodes().get(0) instanceof VCallNode;
			shape = createLeftShape(assignNode, parents, leftAlias, rightAlias);
		}

		if (shape != null) {
			if (shape.getSide() == Shape.Side.LEFT) {
				pair = new Pair(shape, parents.getRightShape());
			} else {
				pair = new Pair(parents.getLeftShape(), shape);
			}
		}

		final Connection.Side side = Connection
				.translateSideFromStringToInt((assignNode).getName());

		boolean canCreate = Connection.connectionNotExists(pair);
		Connection connection = null;
		if (canCreate) {
			connection = new Connection(pair.getLeftShape(), pair
					.getRightShape(), side, Connection.Type.FUN_TO_VAR_CONN);
			if (comment != null) {
				connection.setComment(comment);
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
			final String comment, String leftAlias, String rightAlias)
			throws Exception {
		Pair pair = null;
		String constantName = null;
		Shape shape = null;
		if (mapnode.getReceiverNode() instanceof StrNode) {
			shape = createRightShape(mapnode, parents, leftAlias, rightAlias);
			constantName = ((StrNode) mapnode.getReceiverNode()).getValue();
		} else {
			assert mapnode.getArgsNode().childNodes().get(0) instanceof StrNode;
			shape = createLeftShape(mapnode, parents, leftAlias, rightAlias);
			constantName = ((StrNode) mapnode.getArgsNode().childNodes().get(0))
					.getValue();
		}
		if (shape != null) {
			if (shape.getSide() == Shape.Side.LEFT) {
				pair = new Pair(shape, parents.getRightShape());
			} else {
				pair = new Pair(parents.getLeftShape(), shape);
			}
		}

		final Connection.Side side = Connection
				.translateSideFromStringToInt((mapnode).getName());
		boolean canCreate = Connection.connectionNotExists(pair);
		Connection connection = null;
		if (canCreate) {
			connection = new Connection(pair.getLeftShape(), pair
					.getRightShape(), side, Connection.Type.CONST_TO_VAR_CONN);
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
				connection.setComment(comment);
			}
		}
		return connection;
	}

	private Connection operateOnMapWithSelf(CallNode callnode, Pair parents,
			final String comment, String leftAlias, String rightAlias)
			throws Exception {
		final Connection.Side side = Connection
				.translateSideFromStringToInt((callnode).getName());
		Pair pair = null;
		Shape shape = null;
		if (RootNodeHolder.checkLeftSideMappingName(callnode).equals("self")) {
			shape = createRightShape(callnode, parents, leftAlias, rightAlias);
		} else {
			shape = createLeftShape(callnode, parents, leftAlias, rightAlias);
		}

		if (shape != null) {
			if (shape.getSide() == Shape.Side.LEFT) {
				pair = new Pair(shape, parents.getRightShape());
			} else {
				pair = new Pair(parents.getLeftShape(), shape);
			}
		}

		boolean canCreate = Connection.connectionNotExists(pair);
		Connection connection = null;
		if (canCreate) {
			connection = new Connection(pair.getLeftShape(), pair
					.getRightShape(), side, Connection.Type.VAR_TO_VAR_CONN);
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
				connection.setComment(comment);
			}
		}
		return connection;
	}

	private Connection operateOnMapWithSubobject(FCallNode subobjectFcallnode,
			Pair parents, final String comment, String leftAlias,
			String rightAlias) throws Exception {
		String leftChildAlias = null;
		String rightChildAlias = null;
		Connection connection = null;
		CallNode parentCallNode = (CallNode) subobjectFcallnode.childNodes()
				.get(0).childNodes().get(0);

		BlockNode blockNode = (BlockNode) subobjectFcallnode.childNodes()
				.get(1).childNodes().get(1);
		final StringBuffer childComment = new StringBuffer();
		IterNode iterNode = (IterNode) subobjectFcallnode.childNodes().get(1);
		if (iterNode.getVarNode() instanceof MultipleAsgnNode) {
			DAsgnNode asgnLeftNode = (DAsgnNode) iterNode.getVarNode()
					.childNodes().get(0).childNodes().get(0);
			DAsgnNode asgnRightNode = (DAsgnNode) iterNode.getVarNode()
					.childNodes().get(0).childNodes().get(1);
			leftChildAlias = asgnLeftNode.getName();
			rightChildAlias = asgnRightNode.getName();
		}

		for (Node node : blockNode.childNodes()) {
			if (node instanceof NewlineNode) {
				if (((NewlineNode) node).getNextNode() instanceof XStrNode) {
					childComment.append(((XStrNode) ((NewlineNode) node)
							.getNextNode()).toString());
				} else if (((NewlineNode) node).getNextNode() instanceof FCallNode) {
					Shape shape = null;
					if (RootNodeHolder.checkLeftSideMappingName(parentCallNode)
							.equals("self")) {
						shape = createRightShape(parentCallNode, parents,
								leftAlias, rightAlias);

					} else if (RootNodeHolder.checkRightSideMappingName(
							parentCallNode).equals("self")) {
						shape = createLeftShape(parentCallNode, parents,
								leftAlias, rightAlias);

					}

					if (shape != null) {
						if (shape.getSide() == Shape.Side.LEFT) {
							connection = operateOnInternalMap(
									(NewlineNode) node, new Pair(shape, parents
											.getRightShape()), childComment
											.length() == 0 ? null
											: childComment.toString(),
									leftChildAlias, rightChildAlias);
						} else {
							connection = operateOnInternalMap(
									(NewlineNode) node, new Pair(parents
											.getLeftShape(), shape),
									childComment.length() == 0 ? null
											: childComment.toString(),
									leftChildAlias, rightChildAlias);
						}
					}

					childComment.setLength(0);

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
							connection.setComment(comment);
						}
					}
				}
			}
		}
		return connection;
	}

	private Connection operateOnMapWithSubmap(FCallNode fcallnode,
			Pair parents, final String comment, String leftAlias,
			String rightAlias) throws Exception {
		String leftChildAlias = null;
		String rightChildAlias = null;
		Connection connection = null;
		CallNode parentCallNode = (CallNode) fcallnode.childNodes().get(0)
				.childNodes().get(0);
		Pair mainPair = createShapesPair(parentCallNode, parents, leftAlias,
				rightAlias);
		IterNode iterNode = (IterNode) fcallnode.childNodes().get(1);
		final StringBuilder childComment = new StringBuilder();
		if (iterNode.getVarNode() instanceof MultipleAsgnNode) {
			DAsgnNode asgnLeftNode = (DAsgnNode) iterNode.getVarNode()
					.childNodes().get(0).childNodes().get(0);
			DAsgnNode asgnRightNode = (DAsgnNode) iterNode.getVarNode()
					.childNodes().get(0).childNodes().get(1);
			leftChildAlias = asgnLeftNode.getName();
			rightChildAlias = asgnRightNode.getName();
		}
		for (Node node : iterNode.getBodyNode().childNodes()) {
			if (node instanceof NewlineNode) {
				if (((NewlineNode) node).getNextNode() instanceof XStrNode) {
					if (childComment.length() > 0) {
						childComment.append("\n");
					}
					childComment.append(((XStrNode) ((NewlineNode) node)
							.getNextNode()).getValue());
				} else if (((NewlineNode) node).getNextNode() instanceof FCallNode) {
					connection = operateOnInternalMap((NewlineNode) node,
							mainPair, childComment.length() == 0 ? null
									: childComment.toString(), leftChildAlias,
							rightChildAlias);
					childComment.setLength(0);
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
							mainPair, childComment.length() == 0 ? null
									: childComment.toString(), leftChildAlias,
							rightChildAlias);
					childComment.setLength(0);
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
							connection.setComment(comment);
						}
					}
				}
			}
		}

		return connection;
	}

	private Connection operateOnSimpleMapOrWithFunctionCall(CallNode callnode,
			Pair parents, final String comment, String leftAlias,
			String rightAlias) throws Exception {
		Pair pair = createShapesPair(callnode, parents, leftAlias, rightAlias);
		final Connection.Side side = Connection
				.translateSideFromStringToInt((callnode).getName());
		boolean canCreate = Connection.connectionNotExists(pair);
		Connection connection = null;
		if (canCreate) {
			connection = new Connection(pair.getLeftShape(), pair
					.getRightShape(), side, Connection.Type.VAR_TO_VAR_CONN);

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
				connection.setComment(comment);
			}

		}

		return connection;

	}

	private Pair createShapesPair(CallNode callnode, Pair parents,
			String leftAlias, String rightAlias) throws Exception {
		return new Pair(createLeftShape(callnode, parents, leftAlias,
				rightAlias), createRightShape(callnode, parents, leftAlias,
				rightAlias));
	}

	/**
	 * Creates left Shape on the ShapeDiagram
	 * 
	 * @param callnode
	 * @param parents
	 * @return
	 * @throws Exception
	 */
	private Shape createLeftShape(CallNode callnode, Pair parents,
			String leftAlias, String rightAlias) throws Exception {

		CallNode rootNode = null;
		CallNode leftNode = null;
		Shape leftShape = null;
		if (callnode.childNodes().get(0) instanceof CallNode) {
			rootNode = (CallNode) callnode.childNodes().get(0);
			leftNode = findLastCallNodeInTree(callnode.childNodes().get(0));
			DVarNode varNode = (DVarNode) leftNode.childNodes().get(0);
			if (varNode.getName().equals(leftAlias)
					|| varNode.getName().equals(rightAlias)) {
				if (varNode.getName().equals(leftAlias)) {
					leftShape = Shape.createShape(leftNode.getName(), null,
							parents.getLeftShape(), Shape.Side.LEFT, rootNode);
				} else {
					leftShape = Shape
							.createShape(leftNode.getName(), null, parents
									.getRightShape(), Shape.Side.RIGHT,
									rootNode);
				}

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
							leftShape.addArrayCounter(((int) numberNode
									.getValue()));
							setLastUsedElementNumberInArray((int) numberNode
									.getValue());
						}
					}
				}
			}
		}
		if (leftShape == null) {
			throw new Exception("Ruby map has errors for node: " + callnode);
		}
		return leftShape;

	}

	/**
	 * Creates right Shape on the ShapeDiagram
	 * 
	 * @param callnode
	 * @param parents
	 * @return
	 * @throws Exception
	 */
	private Shape createRightShape(CallNode callnode, Pair parents,
			String leftAlias, String rightAlias) throws Exception {
		CallNode rootNode = null;
		CallNode rightNode = null;
		Shape rightShape = null;
		if (callnode.childNodes().get(1).childNodes().get(0) instanceof CallNode) {
			rootNode = (CallNode) callnode.childNodes().get(1).childNodes()
					.get(0);
			rightNode = findLastCallNodeInTree(callnode.childNodes().get(1)
					.childNodes().get(0));
			DVarNode varNode = (DVarNode) rightNode.childNodes().get(0);
			if (varNode.getName().equals(leftAlias)
					|| varNode.getName().equals(rightAlias)) {
				if (varNode.getName().equals(leftAlias)) {
					rightShape = Shape.createShape(rightNode.getName(), null,
							parents.getLeftShape(), Shape.Side.LEFT, rootNode);
				} else {
					rightShape = Shape
							.createShape(rightNode.getName(), null, parents
									.getRightShape(), Shape.Side.RIGHT,
									rootNode);
				}
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
							rightShape.addArrayCounter(((int) numberNode
									.getValue()));
							setLastUsedElementNumberInArray((int) numberNode
									.getValue());
						}
					}
				}
			}
		}
		if (rightShape == null) {
			throw new Exception("Ruby map has errors for node: " + callnode);
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

	private String getElementType(Node element) {
		String name = null;
		List<String> subTypes = new ArrayList<String>();
		while (true) {
			if (element.childNodes().size() > 0
					&& (element.childNodes().get(0) instanceof INameNode)) {
				subTypes.add(((INameNode) element.childNodes().get(0))
						.getName());
				element = element.childNodes().get(0);
			} else {
				break;
			}
		}
		Collections.reverse(subTypes);
		for (String tmp : subTypes) {
			if (name == null) {
				name = tmp;
			} else {
				name = name + "::" + tmp;
			}
		}
		return name;
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
					String type = getElementType(preChild);
					leftElement = Shape.createShape(((Colon2Node) preChild)
							.getName(), type, parents.getLeftShape(),
							Shape.Side.LEFT, null);
					if (type.startsWith("Java")) {
						leftElement.setSourceType(SourceType.JAVA);
					}
					left = false;
				} else {
					String type = getElementType(preChild);
					rightElement = Shape.createShape(((Colon2Node) preChild)
							.getName(), type, parents.getRightShape(),
							Shape.Side.RIGHT, null);
					if (type.startsWith("Java")) {
						rightElement.setSourceType(SourceType.JAVA);
					}
					left = true;
				}
			} else if (preChild instanceof SymbolNode) {
				if (left) {
					leftElement = Shape.createShape(((SymbolNode) preChild)
							.getName(), null, parents.getLeftShape(),
							Shape.Side.LEFT, null);
					left = false;
				} else {
					rightElement = Shape.createShape(((SymbolNode) preChild)
							.getName(), null, parents.getRightShape(),
							Shape.Side.RIGHT, null);
					left = true;
				}
			} else if (preChild instanceof ConstNode) {
				if (left) {
					leftElement = Shape.createShape(((ConstNode) preChild)
							.getName(), null, parents.getLeftShape(),
							Shape.Side.LEFT, null);
					left = false;
				} else {
					rightElement = Shape.createShape(((ConstNode) preChild)
							.getName(), null, parents.getRightShape(),
							Shape.Side.RIGHT, null);
					left = true;
				}
			} else if (preChild instanceof CallNode) {
				if (left) {
					leftElement = Shape.createShape(((CallNode) preChild)
							.getName(), null, parents.getLeftShape(),
							Shape.Side.LEFT, null);
					String prefix = "";
					Node tmpNode = preChild.childNodes().get(0);
					while (tmpNode instanceof CallNode) {
						if (prefix.equals("")) {
							prefix = ((CallNode) tmpNode).getName();
						} else {
							prefix = ((CallNode) tmpNode).getName() + "."
									+ prefix;
						}
						if (tmpNode.childNodes().size() > 0) {
							tmpNode = tmpNode.childNodes().get(0);
						} else {
							break;
						}
					}
					if (tmpNode instanceof ConstNode) {
						if (((ConstNode) tmpNode).getName().equals("Java"))
							leftElement.setSourceType(SourceType.JAVA);
					}
					leftElement.setOptionalJavaPackage(prefix);
					left = false;
				} else {
					rightElement = Shape.createShape(((CallNode) preChild)
							.getName(), null, parents.getRightShape(),
							Shape.Side.RIGHT, null);
					String prefix = "";
					Node tmpNode = preChild.childNodes().get(0);
					while (tmpNode instanceof CallNode) {
						if (prefix.equals("")) {
							prefix = ((CallNode) tmpNode).getName();
						} else {
							prefix = ((CallNode) tmpNode).getName() + "."
									+ prefix;
						}
						if (tmpNode.childNodes().size() > 0) {
							tmpNode = tmpNode.childNodes().get(0);
						} else {
							break;
						}
					}
					if (tmpNode instanceof ConstNode) {
						if (((ConstNode) tmpNode).getName().equals("Java"))
							rightElement.setSourceType(SourceType.JAVA);
					}
					rightElement.setOptionalJavaPackage(prefix);
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