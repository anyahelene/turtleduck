package turtleduck.gl;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_3;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import turtleduck.display.Viewport;
import turtleduck.display.impl.BaseScreen.Dimensions;

public class GLMouse {
	protected static final boolean DEBUG_MOUSE = false;
	protected GLScreen screen;
	protected Vector2f mouseNrmPos = new Vector2f();
	protected Vector2f but1ClickPos = null;
	protected int mouseButtons;
	protected Vector2i mouseScrPos = new Vector2i();
	protected Vector3f mouseObjPos = new Vector3f();
	protected Viewport viewport;
	protected boolean hovered;

	public GLMouse(GLScreen glScreen, Viewport viewport) {
		screen = glScreen;
		this.viewport = viewport;
	}
	
	public void registerCallbacks(long window) {
		glfwSetMouseButtonCallback(window, this::callbackMouseButton);
		glfwSetCursorPosCallback(window, this::callbackMousePosition);
		glfwSetScrollCallback(window, this::callbackMouseScroll);
		glfwSetCursorEnterCallback(window, this::callbackCursorEnter);
	}

	public void unregisterCallbacks(long window) {
		if (window != NULL) {
			glfwSetMouseButtonCallback(window, null);
			glfwSetCursorPosCallback(window, null);
			glfwSetScrollCallback(window, null);
		}
	}
	
	void callbackCursorEnter(long window, boolean entered) {
		hovered = entered;
	}

	void callbackMouseButton(long window, int button, int action, int mods) {
		if (action == GLFW_PRESS) {
			if (button == GLFW_MOUSE_BUTTON_1) {
				but1ClickPos = new Vector2f(mouseNrmPos);
				mouseButtons |= 1;
			} else if (button == GLFW_MOUSE_BUTTON_2) {
				mouseButtons |= 2;
			} else if (button == GLFW_MOUSE_BUTTON_3) {
				mouseButtons |= 4;
			}
		} else if (action == GLFW_RELEASE) {
			if (button == GLFW_MOUSE_BUTTON_1) {
				but1ClickPos = null;
				mouseButtons &= ~1;
			} else if (button == GLFW_MOUSE_BUTTON_2) {
				mouseButtons &= ~2;
			} else if (button == GLFW_MOUSE_BUTTON_3) {
				mouseButtons &= ~4;
			}
		}
	}

	void callbackMouseScroll(long window, double xoffset, double yoffset) {
		if (yoffset < 0) {
			screen.zoomOut();
		} else {
			screen.zoomIn();
		}
	}

	void callbackMousePosition(long window, double x, double y) {
		if (window == NULL) {
			return;
		}
		mouseScrPos.set((int) x, (int) ((viewport.screenHeight() - 1) - y)); // store position in screen coords
		mouseNrmPos.set(mouseScrPos);
		screen.deviceToScreen(mouseNrmPos);
		screen.camera3.unproject(mouseScrPos, mouseObjPos); // store position in object coords

		if (but1ClickPos != null) {
			float dx = (mouseNrmPos.x - but1ClickPos.x) / 1;
			float dy = (mouseNrmPos.y - but1ClickPos.y) / 1;
			if (DEBUG_MOUSE) {
				System.out.printf("Clicked at: %s, current=(%5f,%5f), move=(%5f,%5f), orientation=%s%n", but1ClickPos,
						x, y, dx, dy, screen.camera3.orientation);
			}
			float zoom = (float) ((screen.camera3.fov - 10) / 120);
			screen.camera3.orientation.rotateX(dy).rotateY(dx).normalize();
			screen.camera3.position.add(dx * zoom, dy * zoom, 0, 0);
			System.out.println(screen.camera3.position);
			// cameraFront.add(dy, dx, 0, 0).normalize();
			but1ClickPos.set(mouseNrmPos);
			screen.camera3.updateView();
		}
		if (DEBUG_MOUSE) {
			System.out.println("mouseDevPos=" + mouseScrPos + ", mouseNrmPos=" + mouseNrmPos + ", mouseObjPos="
					+ mouseObjPos + ", cameraPos=" + screen.camera3.position);
		}

	}

	public Vector2f normalizedPosition() {
		return new Vector2f(mouseNrmPos);
	}

	/**
	 * Retrive current mouse position in normalized coordinates (-1…1)
	 * 
	 * @param dest vector to store coordinates in
	 */
	public void normalizedPosition(Vector2f dest) {
		dest.set(mouseNrmPos);
	}

	/**
	 * Retrive current mouse position in screen coordinates
	 * 
	 * @param dest vector to store coordinates in
	 */
	public void screenPosition(Vector2i dest) {
		dest.set(mouseScrPos);
	}
}
