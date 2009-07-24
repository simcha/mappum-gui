package pl.ivmx.mappum.gui.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.jruby.RubyArray;

public class ModelGeneratorFromXML {
	public static final String LANGUAGE_RUBY = "jruby";

	public static final String DEFAULT_MAP_FOLDER = "map";
	public static final String DEFAULT_SCHEMA_FOLDER = "schema";
	public static final String DEFAULT_GENERATED_CLASSES_FOLDER = ".classes";
	public static final String DEFAULT_PROJECT_PROPERTIES_FILE = "project.properties";

	private String mapFolder;
	private String schemaFolder;
	private String generatedClassesFolder;

	private ScriptEngine engine;
	private ScriptEngineManager manager;
	private String language = LANGUAGE_RUBY;

	private String xsd2rubyScriptCode;
	private InputStreamReader xsd2rubyScript;

	private RubyArray modelArray;

	private Logger logger = Logger.getLogger(ModelGeneratorFromXML.class);

	private static final ModelGeneratorFromXML INSTANCE = new ModelGeneratorFromXML();

	private ModelGeneratorFromXML() {
	}

	public static final ModelGeneratorFromXML getInstance() {
		return INSTANCE;
	}

	public String getMapFolder() {
		return mapFolder;
	}

	public void setMapFolder(String mapFolder) {
		this.mapFolder = mapFolder;
	}

	public String getSchemaFolder() {
		return schemaFolder;
	}

	public void setSchemaFolder(String schemaFolder) {
		this.schemaFolder = schemaFolder;
	}

	public String getGeneratedClassesFolder() {
		return generatedClassesFolder;
	}

	public void setGeneratedClassesFolder(String generatedClassesFolder) {
		this.generatedClassesFolder = generatedClassesFolder;
	}

	public void generateAndRequire() throws ScriptException {
		// ScriptEngineFactory factory = (ScriptEngineFactory) new
		// com.sun.script.jruby.JRubyScriptEngineFactory();
		// ScriptEngine engine = factory.getScriptEngine();
		if (manager == null)
			manager = new ScriptEngineManager();
		if (engine == null)
			engine = manager.getEngineByName(language);

		StringBuffer params = new StringBuffer();
		params.append("\n");
		params.append("require 'mappum/xml_transform'");
		params.append("\n\n");
		params.append("wl = Mappum::WorkdirLoader.new(\"");
		params.append(getSchemaFolder());
		params.append("\", \"");
		params.append(getMapFolder());
		params.append("\", \"");
		params.append(getGeneratedClassesFolder());
		params.append("\")");
		params.append("\n");
		params.append("wl.generate_and_require");
		params.append("\n");
		params.append("wl.defined_element_trees(nil)");
		params.append("\n");

		xsd2rubyScriptCode = params.toString();
		logger.debug("Generated ruby code for ruby script engine:"
				+ xsd2rubyScriptCode);
		xsd2rubyScript = new InputStreamReader(new ByteArrayInputStream(
				xsd2rubyScriptCode.getBytes()));

		// execute the script (non-compiled!)
		logger.debug("Starting generating classes...");
		modelArray = (RubyArray) engine.eval(xsd2rubyScript);
		logger.debug("Classes generated");
	}

	public RubyArray getModelArray() throws ScriptException {
		if (modelArray != null) {
			return modelArray;
		} else {
			throw new ScriptException(
					"Error while returning model. Model is null. Check logs for more details.");
		}
	}
}
