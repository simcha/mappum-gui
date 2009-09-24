/**
 * 
 */
package pl.ivmx.mappum.gui;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.graphics.Image;

public class ImageFactory {

	public static enum ImageType {
		CLASS_IMAGE, FIELD_IMAGE, METHOD_IMAGE, CONNECTION_SMALL_IMAGE, CONNECTION_LARGE_IMAGE
	}

	public static Image getImage(final ImageType imageType) {
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

	private static String getFilename(final ImageType imageType) {
		switch (imageType) {
		case CLASS_IMAGE:
			return "class_obj.gif";
		case FIELD_IMAGE:
			return "field_private_obj.gif";
		case METHOD_IMAGE:
			return "methpub_obj.gif";
		case CONNECTION_SMALL_IMAGE:
			return "connection_s16.gif";
		case CONNECTION_LARGE_IMAGE:
			return "connection_s24.gif";
		default:
			return null;
		}
	}
}
