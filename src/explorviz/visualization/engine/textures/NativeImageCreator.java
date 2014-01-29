package explorviz.visualization.engine.textures;

import com.google.gwt.user.client.ui.Image;

import elemental.html.ImageElement;

public class NativeImageCreator {
	public static ImageElement createImage(final Image img) {
		return (ImageElement) img.getElement();
	}
}
