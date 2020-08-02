package turtleduck.gl.objects;

import static org.lwjgl.opengl.GL30.*;

import org.joml.Vector2f;
import org.joml.Vector3f;

import turtleduck.gl.GLScreen;

public class CubeModel extends AbstractModel {

	private int[] indices;

	public CubeModel() {
		VertexArrayFormat format = new VertexArrayFormat();
		format.addField("aPos",  Vector3f.class);
		format.addField("aColor", Vector3f.class);
		format.addField("aNormal", Vector3f.class);
		format.addField("aTexCoord", Vector2f.class);	
		VertexArrayBuilder builder = buildVertexArray(format);

		builder.vec3(-.5f,  .5f,  .5f).vec3(0,1,1).vec3(-.5f, .5f, .5f).vec2(0,1); // 0
		builder.vec3( .5f,  .5f,  .5f).vec3(1,1,1).vec3( .5f, .5f, .5f).vec2(1,1); // 1
		builder.vec3(-.5f, -.5f,  .5f).vec3(0,0,1).vec3(-.5f,-.5f, .5f).vec2(0,0); // 2
		builder.vec3( .5f, -.5f,  .5f).vec3(1,0,1).vec3( .5f,-.5f, .5f).vec2(1,0); // 3
		builder.vec3(-.5f,  .5f, -.5f).vec3(0,1,0).vec3(-.5f, .5f,-.5f).vec2(0,1); // 4
		builder.vec3( .5f,  .5f, -.5f).vec3(1,1,0).vec3( .5f, .5f,-.5f).vec2(1,1); // 5
		builder.vec3(-.5f, -.5f, -.5f).vec3(0,0,0).vec3(-.5f,-.5f,-.5f).vec2(0,0); // 6
		builder.vec3( .5f, -.5f, -.5f).vec3(1,0,0).vec3( .5f,-.5f,-.5f).vec2(1,0); // 7
		builder.bindArrayBuffer();

		indices = new int[]{2,3,0,1,5,0,4};
		indices = new int[]{0,2,1,3,5,7,4,6,0,2,2,6,3,7,7,1,1,5,0,4};//,/*3*/6,7,7,5,5,1,4,0};

		//2,3,6,7
//		glBindVertexArray(vao);

		ebo = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
		glBindVertexArray(0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	public void render(GLScreen glm) {
		renderStart(glm);
		renderBindBuffers(glm);
				glFrontFace(GL_CW);
				glCullFace(GL_FRONT);
				glEnable(GL_CULL_FACE);
		//		glDrawArrays(GL_TRIANGLE_STRIP, 0, 8);

//				glEnable(GL_BLEND);
//				glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glDrawElements(GL_TRIANGLE_STRIP, indices.length, GL_UNSIGNED_INT, 0);
		glDisable(GL_CULL_FACE);
//				glDisable(GL_BLEND);
	}
}
