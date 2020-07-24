package turtleduck.image;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

import turtleduck.colors.Color;
import turtleduck.image.Image.Transpose;
import turtleduck.image.impl.PixelData;

public interface ImageFactory {
	static ImageFactory defaultFactory() {
		return SharedState.DEFAULT_FACTORY;
	}

	static ImageFactory get(String moduleName) {
		if (moduleName == null)
			return SharedState.DEFAULT_FACTORY;
		else if (SharedState.factories.containsKey(moduleName)) {
			return SharedState.factories.get(moduleName);
		} else {
			throw new NoSuchElementException(moduleName);
		}
	}

	default Image imageFromUrl(URL url, int width, int height, ImageMode mode) {
		return new AbstractImage.UrlImageSource(url, width, height, mode);
	}

	default Image imageFromResource(String resourceName, int width, int height, ImageMode mode) {
		return new AbstractImage.ResourceImageSource(resourceName, width, height, mode);
	}

	default Image croppedImage(Image source, int x, int y, int newWidth, int newHeight) {
		return new AbstractImage.CroppedImage(source, x, y, newWidth, newHeight);
	}

	default Image scaledImage(Image source, int newWidth, int newHeight) {
		return new AbstractImage.ScaledImage(source, newWidth, newHeight, Resampling.NEAREST);
	}

	default Image scaledImage(Image source, int newWidth, int newHeight, Resampling filter) {
		return new AbstractImage.ScaledImage(source, newWidth, newHeight, filter);
	}

	default Image transposedImage(Image source, Transpose method) {
		return new AbstractImage.TransposedImage(source, method);
	}
	default Image imageFromPixels(int width, int height, Color border, ImageMode mode, int[] data) {
		return new PixelData(width, height, border, mode, data);
	}

	static class SharedState {
		protected static final ImageFactory DEFAULT_FACTORY;
		protected static final Map<String, ImageFactory> factories = new HashMap<>();

		static {
			ServiceLoader<ImageFactory> loader = ServiceLoader.load(ImageFactory.class);
			ImageFactory first = null;
			for (ImageFactory fac : loader) {
				String name = fac.getClass().getModule().getName();
				System.out.println("Available factory: " + fac + " from " + name);
				if (first == null)
					first = fac;
				factories.put(name, fac);
			}
			;
			if (first == null)
				first = new ImageFactory() {
				};
			DEFAULT_FACTORY = first;
		}
	}

}
