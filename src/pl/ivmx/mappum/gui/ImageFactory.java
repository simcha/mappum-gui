/**
 * 
 */
package pl.ivmx.mappum.gui;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.graphics.Image;

public class ImageFactory {
	public static final int CLASS_IMAGE = 1;
	public static final int FIELD_IMAGE = 2;
	public static final int METHOD_IMAGE = 3;
	public static final int CONNECTION_SMALL_IMAGE = 4;
	public static final int CONNECTION_LARGE_IMAGE = 5;

	public static Image getImage(int imageType) {
		String path = "/icons/";
		String filename = null;
		try {
			if ((filename = getFilename(imageType)) != null) {
				path += filename;
				InputStream stream = MappumPlugin.class
						.getResourceAsStream(path);
				Image image = new Image(null, stream);
				try {
					stream.close();
				} catch (IOException ioe) {
				}
				return image;
			} else
				return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	private static String getFilename(int imageType) {
		switch (imageType) {
		case ImageFactory.CLASS_IMAGE:
			return "class_obj.gif";
		case ImageFactory.FIELD_IMAGE:
			return "field_private_obj.gif";
		case ImageFactory.METHOD_IMAGE:
			return "methpub_obj.gif";
		case ImageFactory.CONNECTION_SMALL_IMAGE:
			return "connection_s16.gif";
		case ImageFactory.CONNECTION_LARGE_IMAGE:
			return "connection_s24.gif";
		default:
			return null;
		}

	}
}
