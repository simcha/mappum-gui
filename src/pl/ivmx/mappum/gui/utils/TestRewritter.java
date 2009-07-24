package pl.ivmx.mappum.gui.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jrubyparser.Parser;
import org.jrubyparser.SourcePosition;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.StrNode;
import org.jrubyparser.parser.ParserConfiguration;
import org.jrubyparser.rewriter.ReWriteVisitor;

public class TestRewritter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		FileInputStream fileInputStream = new FileInputStream("d:/example_mapping.rb");
		FileInputStream fileInputStream2 = new FileInputStream("d:/example_mapping.rb");
		Parser parser = new Parser();
		BufferedReader content = new BufferedReader(new InputStreamReader(fileInputStream));
		BufferedReader content2 = new BufferedReader(new InputStreamReader(fileInputStream2));
		String source = "";
		String s = "";
			while ((s = content.readLine()) != null){
				source+= s;
			}


		ParserConfiguration configuration = new ParserConfiguration();
		Node node = parser.parse("d:/example_mapping.rb", content2, configuration);
		System.out.println(node.childNodes());
		Node newNode = (new StrNode(new SourcePosition("", 20, 20), "testtest"));
		Node nd = node.childNodes().get(0).childNodes().get(0).childNodes().get(0).childNodes().get(0).childNodes().set(0,newNode);
		
		System.out.println(nd);
		//StrNode strNode = new StrNode(new SourcePosition(), source);
		System.out.println(ReWriteVisitor.createCodeFromNode(node, source));
		

	}

}
