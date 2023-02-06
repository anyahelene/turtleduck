package turtleduck.gl.objects;

import static turtleduck.gl.objects.Util.ioResourceToByteBuffer;
import static turtleduck.gl.GLScreen.gl;
import static turtleduck.gl.compat.GLA.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.AIVector3D.Buffer;
import org.lwjgl.assimp.Assimp;

import turtleduck.buffer.VertexLayout;
import turtleduck.gl.GLScreen;

public class MeshModel extends AbstractModel {
	List<Integer> idxBuffer = new ArrayList<>();
	public MeshModel(String pathName) {
		try {
			VertexLayout format = VertexLayout.create() //
			.declare("aPos", Vector3f.class)
			.declare("aColor", Vector3f.class)
			.declare("aNormal", Vector3f.class)
			.declare("aTexCoord", Vector2f.class).done();

			VertexArrayBuilder builder = buildVertexArray(format);

			ByteBuffer buf = ioResourceToByteBuffer(pathName, 8 * 1024);
			ByteBuffer b2 = BufferUtils.createByteBuffer(buf.limit()+1);
			b2.put(buf);
			b2.put((byte)0);
			b2.flip();
			AIScene scene = Assimp.aiImportFileFromMemory(b2, //
					Assimp.aiProcess_Triangulate //
					//					|Assimp.aiProcess_PreTransformVertices //
					//					|Assimp.aiProcess_GlobalScale //
					//					|Assimp.aiProcess_ValidateDataStructure //
					//					|Assimp.aiProcess_OptimizeMeshes //
					//					|Assimp.aiProcess_OptimizeGraph //
					|Assimp.aiProcess_FlipUVs //
					|Assimp.aiProcess_FixInfacingNormals //
					|Assimp.aiProcess_GenSmoothNormals //
					//					|Assimp.aiProcess_SortByPType //
					|Assimp.aiProcess_JoinIdenticalVertices //
					,pathName);
			if(scene == null || (scene.mFlags()&Assimp.AI_SCENE_FLAGS_INCOMPLETE) != 0 || scene.mRootNode() == null) {
				throw new RuntimeException(Assimp.aiGetErrorString());
			}

			processNode(scene.mRootNode(), scene, builder);
			builder.bindArrayBuffer();

			ebo = gl.glGenBuffers();
			gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
			IntBuffer buf2 = BufferUtils.createIntBuffer(idxBuffer.size());
			for(int i : idxBuffer) {
				buf2.put(i);
			}
			buf2.flip();
			System.out.println("Mesh loaded, " + idxBuffer.size() + " indexes");
			gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, buf2, GL_STATIC_DRAW);
			gl.glBindVertexArray(0);
			gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

		} catch (IOException e) {
			throw new RuntimeException(e);

		}
	}

	private void processNode(AINode node, AIScene scene, VertexArrayBuilder builder) {
		System.out.println("Processing node " + node + ": " + node.mName().dataString());
		PointerBuffer sceneMeshes = scene.mMeshes();
		int numMeshes = node.mNumMeshes();
		int numChildren = node.mNumChildren();
		IntBuffer mMeshes = node.mMeshes();
		for(int i = 0; i < numMeshes; i++) {
			AIMesh mesh = AIMesh.createSafe(sceneMeshes.get(mMeshes.get(i)));
			processMesh(mesh, builder);
		}
		PointerBuffer children = node.mChildren();
		for(int i = 0; i < numChildren; i++) {
			AINode child = AINode.create(children.get(i));
			processNode(child, scene, builder);
		}
	}

	private void processMesh(AIMesh mesh, VertexArrayBuilder builder) {
		int vertexOffset = builder.nVertices();
		AIVector3D.Buffer normals = mesh.mNormals();
		AIVector3D.Buffer textureCoords = mesh.mTextureCoords(0);
		AIColor4D.Buffer colors = mesh.mColors(0);
		Buffer vertices = mesh.mVertices();
		while(vertices.hasRemaining()) {
			AIVector3D v = vertices.get();
			AIVector3D n = normals.get();

			builder.vec3(v.x(), v.y(), v.z());
			if(colors != null) {
				AIColor4D c = colors.get();
				builder.vec3(c.r(),c.g(),c.b());
			} else {
				builder.vec3(.8f,.8f,.8f);
			}
			builder.vec3(n.x(), n.y(), n.z());
			if(textureCoords != null) {
				AIVector3D t = textureCoords.get();
				builder.vec2(t.x(), t.y());
			} else {
				builder.vec2(0,0);
			}
		}
		AIFace.Buffer faces = mesh.mFaces();
		while(faces.hasRemaining()) {
			AIFace aiFace = faces.get();
			if(aiFace.mNumIndices() == 3) {
				IntBuffer indices = aiFace.mIndices();
				while(indices.hasRemaining()) {
					idxBuffer.add(indices.get()+vertexOffset);
				}
			}else {
				System.out.println(aiFace.mNumIndices());
			}
		}
	}

	@Override
	public void render(GLScreen glm) {
		renderStart(glm);
		renderBindBuffers(glm);
		gl.glDisable(GL_CULL_FACE);

		gl.glDrawElements(GL_TRIANGLES, idxBuffer.size(), GL_UNSIGNED_INT, 0);
	}

	public void transformMesh(Vector3f offset, Vector3f scale, Vector3f rotation) {
		innerTransform = new Matrix4f();
		innerNormalTransform = new Matrix4f();
		if(offset != null) {
			innerTransform.translation(offset);
		}
		if(scale != null) {
			innerTransform.scale(scale);
		}
		if(rotation != null) {
			innerTransform.rotateXYZ(rotation);
			innerNormalTransform.rotateXYZ(rotation);
		}
	}
}
