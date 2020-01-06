package turtleduck.events;

public enum KeyCode {
	TAB, BACK_SPACE, SPACE, PERIOD, COMMA, SHIFT, CONTROL, DIGIT0, DIGIT1, DIGIT2, DIGIT3, DIGIT4, DIGIT5, DIGIT6, DIGIT7, DIGIT8, DIGIT9, PLUS, MINUS, ENTER,
	A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, LEFT, RIGHT, UP, DOWN, F11;

	<T> T as(Class<T> type) {
		return null;
	}
}
