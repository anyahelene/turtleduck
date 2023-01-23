package turtleduck.gl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_DISCONNECTED;
import static org.lwjgl.glfw.GLFW.glfwGetGamepadName;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickName;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetJoystickCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.system.MemoryUtil.NULL;
import static turtleduck.events.KeyCodes.*;

import turtleduck.events.Controls;
import turtleduck.events.InputControl;
import turtleduck.events.KeyCodes;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.system.MemoryUtil;

import turtleduck.gl.objects.Util;

public class GLStick {
	private static final int BUTTONS[] = { GLFW_GAMEPAD_BUTTON_A, GLFW_GAMEPAD_BUTTON_B, GLFW_GAMEPAD_BUTTON_X,
			GLFW_GAMEPAD_BUTTON_Y, GLFW_GAMEPAD_BUTTON_LEFT_BUMPER, GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER,
			GLFW_GAMEPAD_BUTTON_BACK, GLFW_GAMEPAD_BUTTON_START, GLFW_GAMEPAD_BUTTON_GUIDE,
			GLFW_GAMEPAD_BUTTON_LEFT_THUMB, GLFW_GAMEPAD_BUTTON_RIGHT_THUMB, GLFW_GAMEPAD_BUTTON_DPAD_UP,
			GLFW_GAMEPAD_BUTTON_DPAD_RIGHT, GLFW_GAMEPAD_BUTTON_DPAD_DOWN, GLFW_GAMEPAD_BUTTON_DPAD_LEFT };
	private static final int BUTTON_CODES[] = { GamePad.BUTTON_A, GamePad.BUTTON_B, GamePad.BUTTON_X, GamePad.BUTTON_Y,
			GamePad.BUTTON_LEFT_BUMPER, GamePad.BUTTON_RIGHT_BUMPER, GamePad.BUTTON_BACK, GamePad.BUTTON_START,
			GamePad.BUTTON_GUIDE, GamePad.BUTTON_LEFT_THUMB, GamePad.BUTTON_RIGHT_THUMB, GamePad.BUTTON_DPAD_UP,
			GamePad.BUTTON_DPAD_RIGHT, GamePad.BUTTON_DPAD_DOWN, GamePad.BUTTON_DPAD_LEFT };
	private static final int AXES[] = { GLFW_GAMEPAD_AXIS_LEFT_X, GLFW_GAMEPAD_AXIS_LEFT_Y, GLFW_GAMEPAD_AXIS_RIGHT_X,
			GLFW_GAMEPAD_AXIS_RIGHT_Y, GLFW_GAMEPAD_AXIS_LEFT_TRIGGER, GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER };
	private static final int AXIS_CODES[] = { GamePad.AXIS_LEFT_X, GamePad.AXIS_LEFT_Y, GamePad.AXIS_RIGHT_X,
			GamePad.AXIS_RIGHT_Y, GamePad.AXIS_LEFT_TRIGGER, GamePad.AXIS_RIGHT_TRIGGER };
	private static final Map<Integer, Integer> BUTTON_MAP = new HashMap<>();
	private static final Map<Integer, Integer> AXIS_MAP = new HashMap<>();

	static {
		for (int i = 0; i < AXIS_CODES.length; i++) {
			AXIS_MAP.put(AXIS_CODES[i], i);
		}
		for (int i = 0; i < BUTTON_CODES.length; i++) {
			BUTTON_MAP.put(BUTTON_CODES[i], i);
		}
	}
	private Map<Integer, Joystick> sticks = new HashMap<>();
	private int numSticks = 0;
	private GLScreen screen;

	protected GLStick(GLScreen screen) {
		this.screen = screen;
	}

	class Joystick {
		boolean isGamepad;
		private int id;
		private int jid;
		private String name;
		private String padName;
		private GLFWGamepadState state;
		private int buttonState;
		private float axes[] = new float[AXES.length], axes_tmp[] = new float[AXES.length];

		public Joystick(int jid, int id) {

			this.isGamepad = glfwJoystickIsGamepad(jid);
			this.name = glfwGetJoystickName(jid);
			this.padName = glfwGetGamepadName(jid);
			this.jid = jid;
			this.id = id;
			if (isGamepad)
				state = GLFWGamepadState.create();
			System.out.println("new controller: " + glfwGetJoystickGUID(jid) + ": " + name + " (gamepad: " + isGamepad
					+ " " + padName + "): " + toString());
		}

		public String toString() {
			if (padName != null)
				return "gamepad(jid=" + jid + ", name=" + padName + ")";
			else
				return "joystick(jid=" + jid + ", name=" + name + ")";
		}

		public void processInput() {
			if (isGamepad) {
				if (glfwGetGamepadState(jid, state)) {
					int buttons = 0;
					for (int i : BUTTONS) {
						if (state.buttons(i) == GLFW_PRESS)
							buttons |= 1;
						buttons <<= 1;
					}
					if (buttons != buttonState)
						System.out.printf("[%04x]%n", buttons);
					buttonState = buttons;
					boolean diff = false;
					for (int i : AXES) {
						axes_tmp[i] = .06250f*axes[i]+.93750f*state.axes(i);
						if (axes[i] != axes_tmp[i])
							diff = true;
					}
					if (diff)
						System.out.println(Arrays.toString(axes_tmp));
					float[] tmp = axes_tmp;
					axes_tmp = axes;
					axes = tmp;
				}

//				FloatBuffer axes = glfwGetJoystickAxes(jid);
			}
		}

		@SuppressWarnings("unchecked")
		public <T> InputControl<T> createControl(Class<T> type, int code) {
			Integer button = BUTTON_MAP.get(code);
			Integer axis = AXIS_MAP.get(code);

			if (axis != null) {
				int a = axis;
				if (type == Float.class)
					return (InputControl<T>) Controls.create(KeyCodes.keyName(code), () -> axes[a]);
				else if (type == Integer.class)
					return (InputControl<T>) Controls.create(KeyCodes.keyName(code), () -> Math.round(255 * axes[a]));
				else
					throw new IllegalArgumentException("Illegal control value type for axis: " + type.getName());

			} else if (button != null) {
				int b = button;
				if (type == Boolean.class)
					return (InputControl<T>) Controls.create(KeyCodes.keyName(code),
							() -> (buttonState & (1 << b)) != 0);
				else if (type == Integer.class)
					return (InputControl<T>) Controls.create(KeyCodes.keyName(code), () -> (buttonState & (1 << b)));
				else
					throw new IllegalArgumentException("Illegal control value type for axis: " + type.getName());

			} else {
				throw new IllegalArgumentException("Unknown key code " + code + " (" + KeyCodes.keyName(code) + ")");
			}
		}

	}

	public void processInput() {
		for (Joystick js : sticks.values()) {
			js.processInput();
		}
	}

	public void init() {
		try {
			ByteBuffer db = Util.ioResourceToByteBuffer("/gamecontrollerdb.txt", 256 * 1024);
//			db.position(db.limit()-1);
			System.out.println("Read gamecontrollerdb.txt: " + db.limit() + " bytes");
			db.limit(db.limit() + 1);
			db.put(db.limit() - 1, (byte) 0);
//			db.flip();
			boolean result = GLFW.glfwUpdateGamepadMappings(db);
			System.out.println("Updating game pad mappings: " + result);
			GLScreen.gl.checkError();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void registerCallbacks() {
		glfwSetJoystickCallback(this::callbackJoystickConfig);
		synchronized (this) {
			for (int jid = GLFW_JOYSTICK_1; jid <= GLFW_JOYSTICK_LAST; jid++) {
				if (glfwJoystickPresent(jid)) {
					int id = ++numSticks;
					glfwSetJoystickUserPointer(jid, id);
					sticks.put(id, new Joystick(jid, id));
				}
			}
		}
	}

	public void unregisterCallbacks() {
		glfwSetJoystickCallback(null);
	}

	void callbackJoystickConfig(int jid, int event) {
		synchronized (this) {
			if (event == GLFW_CONNECTED) {
				System.out.println(" connected jid=" + jid);
				int id = ++numSticks;
				glfwSetJoystickUserPointer(jid, id);
				sticks.put(id, new Joystick(jid, id));
			} else if (event == GLFW_DISCONNECTED) {
				System.out.println(" disconnected");
				int id = (int) glfwGetJoystickUserPointer(jid);
				Joystick js = sticks.remove(id);
				System.out.println("disconnected: " + js + " jid=" + jid);

			}
		}
	}

	public <T> InputControl<T> inputControl(Class<T> type, int code, int controller) {
		Joystick joystick = sticks.get(controller);
		if (joystick != null) {
			return joystick.createControl(type, code);
		} else {
			return null;
		}
	}
}
