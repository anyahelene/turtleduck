module turtleduck.gl {
	exports turtleduck.gl;
	exports turtleduck.gl.objects;
	requires transitive turtleduck.base;
	requires org.lwjgl;
	requires org.lwjgl.glfw;
	requires org.lwjgl.opengl;
	requires org.lwjgl.opengles;
	requires org.joml;
	requires org.lwjgl.stb;
	requires org.lwjgl.assimp;
	requires earcut4j;
	provides turtleduck.display.MouseCursor with turtleduck.gl.GLCursor;
	provides turtleduck.Launcher with turtleduck.gl.GLLauncher;
	provides turtleduck.display.DisplayInfo with turtleduck.gl.GLDisplayInfo;
	provides turtleduck.image.ImageFactory with turtleduck.gl.GLImageFactory;
requires java.annotation;
requires turtleduck.anno;
}
