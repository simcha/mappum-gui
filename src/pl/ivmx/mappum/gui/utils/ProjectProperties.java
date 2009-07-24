package pl.ivmx.mappum.gui.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class ProjectProperties {
	private Properties properties = new Properties();
	private IFile propertiesFile;
	public static String PROPERTIES_FILE = "project.propierties";
	public static String PROJECT_NAME_PROPS = "projectName";
	public static String MAP_DIRECTORY_PROPS = "mapDirectory";
	public static String CLASSES_DIRECTORY_PROPS = "classesDirectory";
	public static String SCHEMA_DIRECTORY_PROPS = "schemaDirectory";

	public ProjectProperties(IProject project) {
		IPath propertiesPath = project.getFullPath().append(PROPERTIES_FILE);
		propertiesFile = ResourcesPlugin.getWorkspace().getRoot().getFile(
				propertiesPath);
		if (!propertiesFile.exists()) {
			properties.setProperty(PROJECT_NAME_PROPS, project.getName());
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			try {
				properties.store(stream, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				propertiesFile.create(new ByteArrayInputStream(stream
						.toByteArray()), true, null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			properties
					.load(new InputStreamReader(propertiesFile.getContents()));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public void setProperty(String key, String value) {
		properties.setProperty(key, value);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			properties.store(stream, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			propertiesFile.setContents(new ByteArrayInputStream(stream
					.toByteArray()), true, false, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
