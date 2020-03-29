package turtleduck.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyCodes {
	public static class Special {
		public static final int UNDEFINED = defineKey("UNDEFINED", "Undefined", "UNDEFINED", "VK_UNDEFINED", "Special");
	}

	public static class Modifier {
		public static final int ALT = defineKey("ALT", "Alt", "ALT", "VK_ALT", "Modifier");
		public static final int ALT_GRAPH = defineKey("ALT_GRAPH", "AltGraph", "ALT_GRAPH", "VK_ALT_GRAPH", "Modifier");
		public static final int CAPS_LOCK = defineKey("CAPS_LOCK", "CapsLock", "CAPS", "VK_CAPS_LOCK", "Modifier");
		public static final int CONTROL = defineKey("CONTROL", "Control", "CONTROL", "VK_CONTROL", "Modifier");
		public static final int FN = defineKey("FN", "Fn", null, null, "Modifier");
		public static final int META = defineKey("META", null, "META", "VK_META", "Modifier");
		public static final int OS = defineKey("OS", "Meta", "WINDOWS", "VK_WINDOWS", "Modifier");
		public static final int NUM_LOCK = defineKey("NUM_LOCK", "NumLock", "NUM_LOCK", "VK_NUM_LOCK", "Modifier");
		public static final int SCROLL_LOCK = defineKey("SCROLL_LOCK", "ScrollLock", "SCROLL_LOCK", "VK_SCROLL_LOCK", "Modifier");
		public static final int SHIFT = defineKey("SHIFT", "Shift", "SHIFT", "VK_SHIFT", "Modifier");
		public static final int SYMBOL = defineKey("SYMBOL", "Symbol", null, null, "Modifier");
	}

	public static class Whitespace {
		public static final int ENTER = defineKey("ENTER", "Enter", "ENTER", "VK_ENTER", "Whitespace");
		public static final int TAB = defineKey("TAB", "Tab", "TAB", "VK_TAB", "Whitespace");
	}

	public static class Navigation {
		public static final int ARROW_DOWN = defineKey("ARROW_DOWN", "ArrowDown", "DOWN", "VK_DOWN", "Navigation");
		public static final int ARROW_LEFT = defineKey("ARROW_LEFT", "ArrowLeft", "LEFT", "VK_LEFT", "Navigation");
		public static final int ARROW_RIGHT = defineKey("ARROW_RIGHT", "ArrowRight", "RIGHT", "VK_RIGHT", "Navigation");
		public static final int ARROW_UP = defineKey("ARROW_UP", "ArrowUp", "UP", "VK_UP", "Navigation");
		public static final int END = defineKey("END", "End", "END", "VK_END", "Navigation");
		public static final int HOME = defineKey("HOME", "Home", "HOME", "VK_HOME", "Navigation");
		public static final int PAGE_DOWN = defineKey("PAGE_DOWN", "PageDown", "PAGE_DOWN", "VK_PAGE_DOWN", "Navigation");
		public static final int PAGE_UP = defineKey("PAGE_UP", "PageUp", "PAGE_UP", "VK_PAGE_UP", "Navigation");
	}

	public static class Editing {
		public static final int BACKSPACE = defineKey("BACKSPACE", "Backspace", "BACK_SPACE", "VK_BACK_SPACE", "Editing");
		public static final int CLEAR = defineKey("CLEAR", "Clear", "CLEAR", "VK_CLEAR", "Editing");
		public static final int COPY = defineKey("COPY", "Copy", "COPY", "VK_COPY", "Editing");
		public static final int CR_SEL = defineKey("CR_SEL", "CrSel", null, null, "Editing");
		public static final int CUT = defineKey("CUT", "Cut", "CUT", "VK_CUT", "Editing");
		public static final int DELETE = defineKey("DELETE", "Delete", "DELETE", "VK_DELETE", "Editing");
		public static final int ERASE_EOF = defineKey("ERASE_EOF", "EraseEof", null, null, "Editing");
		public static final int EX_SEL = defineKey("EX_SEL", "ExSel", null, null, "Editing");
		public static final int INSERT = defineKey("INSERT", "Insert", "INSERT", "VK_INSERT", "Editing");
		public static final int PASTE = defineKey("PASTE", "Paste", "PASTE", "VK_PASTE", "Editing");
		public static final int REDO = defineKey("REDO", "Redo", "AGAIN", "VK_AGAIN", "Editing");
		public static final int UNDO = defineKey("UNDO", "Undo", "UNDO", "VK_UNDO", "Editing");
	}

	public static class UI {
		public static final int ACCEPT = defineKey("ACCEPT", "Accept", "ACCEPT", "VK_ACCEPT", "UI");
		public static final int ATTN = defineKey("ATTN", "Attn", null, null, "UI");
		public static final int CANCEL = defineKey("CANCEL", "Cancel", "CANCEL", "VK_CANCEL", "UI");
		public static final int CONTEXT_MENU = defineKey("CONTEXT_MENU", "ContextMenu", "CONTEXT_MENU", "VK_CONTEXT_MENU", "UI");
		public static final int ESCAPE = defineKey("ESCAPE", "Escape", "ESCAPE", "VK_ESCAPE", "UI");
		public static final int EXECUTE = defineKey("EXECUTE", "Execute", null, null, "UI");
		public static final int FIND = defineKey("FIND", "Find", "FIND", "VK_FIND", "UI");
		public static final int HELP = defineKey("HELP", "Help", "HELP", "VK_HELP", "UI");
		public static final int PAUSE = defineKey("PAUSE", "Pause", null, null, "UI");
		public static final int PLAY = defineKey("PLAY", "Play", null, null, "UI");
		public static final int SELECT = defineKey("SELECT", "Select", null, null, "UI");
		public static final int ZOOM_IN = defineKey("ZOOM_IN", "ZoomIn", null, null, "UI");
		public static final int ZOOM_OUT = defineKey("ZOOM_OUT", "ZoomOut", null, null, "UI");
	}

	public static class Device {
		public static final int BRIGHTNESS_DOWN = defineKey("BRIGHTNESS_DOWN", "BrightnessDown", null, null, "Device");
		public static final int BRIGHTNESS_UP = defineKey("BRIGHTNESS_UP", "BrightnessUp", null, null, "Device");
		public static final int EJECT = defineKey("EJECT", "Eject", "EJECT_TOGGLE", null, "Device");
		public static final int LOG_OFF = defineKey("LOG_OFF", "LogOff", null, null, "Device");
		public static final int POWER = defineKey("POWER", "Power", "POWER", null, "Device");
		public static final int POWER_OFF = defineKey("POWER_OFF", "PowerOff", null, null, "Device");
		public static final int PRINT_SCREEN = defineKey("PRINT_SCREEN", "PrintScreen", "PRINTSCREEN", "VK_PRINTSCREEN", "Device");
		public static final int HIBERNATE = defineKey("HIBERNATE", "Hibernate", null, null, "Device");
		public static final int STANDBY = defineKey("STANDBY", "Standby", null, null, "Device");
		public static final int WAKE_UP = defineKey("WAKE_UP", "WakeUp", null, null, "Device");
	}

	public static class Composition {
		public static final int ALL_CANDIDATES = defineKey("ALL_CANDIDATES", "AllCandidates", "ALL_CANDIDATES", "VK_ALL_CANDIDATES", "Composition");
		public static final int ALPHANUMERIC = defineKey("ALPHANUMERIC", "Alphanumeric", "ALPHANUMERIC", "VK_ALPHANUMERIC", "Composition");
		public static final int CODE_INPUT = defineKey("CODE_INPUT", "CodeInput", "CODE_INPUT", "VK_CODE_INPUT", "Composition");
		public static final int COMPOSE = defineKey("COMPOSE", "Compose", "COMPOSE", "VK_COMPOSE", "Composition");
		public static final int CONVERT = defineKey("CONVERT", "Convert", "CONVERT", "VK_CONVERT", "Composition");
		public static final int DEAD = defineKey("DEAD", "Dead", null, null, "Composition");
		public static final int FINAL_MODE = defineKey("FINAL_MODE", "FinalMode", "FINAL", "VK_FINAL", "Composition");
//		missingkey();
//		missingkey();
//		missingkey();
//		missingkey("JAPANESE_KATAKANA", "VK_JAPANESE_KATAKANA");
//		missingkey("JAPANESE_HIRAGANA", "VK_JAPANESE_HIRAGANA");
//		missingkey("JAPANESE_ROMAN", "VK_JAPANESE_ROMAN");
//		missingkey("KANA", "VK_KANA");
//		missingkey("INPUT_METHOD_ON_OFF", "VK_INPUT_METHOD_ON_OFF");
			public static final int GROUP_FIRST = defineKey("GROUP_FIRST", "GroupFirst", null, null, "Composition");
		public static final int GROUP_LAST = defineKey("GROUP_LAST", "GroupLast", null, null, "Composition");
		public static final int GROUP_NEXT = defineKey("GROUP_NEXT", "GroupNext", null, null, "Composition");
		public static final int GROUP_PREVIOUS = defineKey("GROUP_PREVIOUS", "GroupPrevious", null, null, "Composition");
		public static final int MODE_CHANGE = defineKey("MODE_CHANGE", "ModeChange", "MODECHANGE", "VK_MODECHANGE", "Composition");
		public static final int NON_CONVERT = defineKey("NON_CONVERT", "NonConvert", "NONCONVERT", "VK_NONCONVERT", "Composition");
		public static final int PREVIOUS_CANDIDATE = defineKey("PREVIOUS_CANDIDATE", "PreviousCandidate", "PREVIOUS_CANDIDATE", "VK_PREVIOUS_CANDIDATE", "Composition");
		public static final int PROCESS = defineKey("PROCESS", "Process", null, null, "Composition");
		public static final int SINGLE_CANDIDATE = defineKey("SINGLE_CANDIDATE", "SingleCandidate", null, null, "Composition");
		public static final int HANGUL_MODE = defineKey("HANGUL_MODE", "HangulMode", null, null, "Composition");
		public static final int HANJA_MODE = defineKey("HANJA_MODE", "HanjaMode", null, null, "Composition");
		public static final int JUNJA_MODE = defineKey("JUNJA_MODE", "JunjaMode", null, null, "Composition");
		public static final int EISU = defineKey("EISU", "Eisu", null, null, "Composition");
		public static final int HANKAKU = defineKey("HANKAKU", "Hankaku", "HALF_WIDTH", "VK_HALF_WIDTH", "Composition");
		public static final int HIRAGANA = defineKey("HIRAGANA", "Hiragana", "HIRAGANA", "VK_HIRAGANA", "Composition");
		public static final int HIRAGANA_KATAKANA = defineKey("HIRAGANA_KATAKANA", "HiraganaKatakana", null, null, "Composition");
		public static final int KANA_MODE = defineKey("KANA_MODE", "KanaMode", "KANA_LOCK", "VK_KANA_LOCK", "Composition");
		public static final int KANJI_MODE = defineKey("KANJI_MODE", "KanjiMode", "KANJI", "VK_KANJI", "Composition");
		public static final int KATAKANA = defineKey("KATAKANA", "Katakana", "KATAKANA", "VK_KATAKANA", "Composition");
		public static final int ROMAJI = defineKey("ROMAJI", "Romaji", "ROMAN_CHARACTERS", "VK_ROMAN_CHARACTERS", "Composition");
		public static final int ZENKAKU = defineKey("ZENKAKU", "Zenkaku", "FULL_WIDTH", "VK_FULL_WIDTH", "Composition");
		public static final int ZENKAKU_HANKAKU = defineKey("ZENKAKU_HANKAKU", "ZenkakuHankaku", null, null, "Composition");
	}

	public static class Function {
		public static final int F1 = defineKey("F1", "F1", "F1", "VK_F1", "Function");
		public static final int F2 = defineKey("F2", "F2", "F2", "VK_F2", "Function");
		public static final int F3 = defineKey("F3", "F3", "F3", "VK_F3", "Function");
		public static final int F4 = defineKey("F4", "F4", "F4", "VK_F4", "Function");
		public static final int F5 = defineKey("F5", "F5", "F5", "VK_F5", "Function");
		public static final int F6 = defineKey("F6", "F6", "F6", "VK_F6", "Function");
		public static final int F7 = defineKey("F7", "F7", "F7", "VK_F7", "Function");
		public static final int F8 = defineKey("F8", "F8", "F8", "VK_F8", "Function");
		public static final int F9 = defineKey("F9", "F9", "F9", "VK_F9", "Function");
		public static final int F10 = defineKey("F10", "F10", "F10", "VK_F10", "Function");
		public static final int F11 = defineKey("F11", "F11", "F11", "VK_F11", "Function");
		public static final int F12 = defineKey("F12", "F12", "F12", "VK_F12", "Function");
		public static final int F13 = defineKey("F13", "F13", "F13", "VK_F13", "Function");
		public static final int F14 = defineKey("F14", "F14", "F14", "VK_F14", "Function");
		public static final int F15 = defineKey("F15", "F15", "F15", "VK_F15", "Function");
		public static final int F16 = defineKey("F16", "F16", "F16", "VK_F16", "Function");
		public static final int F17 = defineKey("F17", "F17", "F17", "VK_F17", "Function");
		public static final int F18 = defineKey("F18", "F18", "F18", "VK_F18", "Function");
		public static final int F19 = defineKey("F19", "F19", "F19", "VK_F19", "Function");
		public static final int F20 = defineKey("F20", "F20", "F20", "VK_F20", "Function");
		public static final int F21 = defineKey("F21", "F21", "F21", "VK_F21", "Function");
		public static final int F22 = defineKey("F22", "F22", "F22", "VK_F22", "Function");
		public static final int F23 = defineKey("F23", "F23", "F23", "VK_F23", "Function");
		public static final int F24 = defineKey("F24", "F24", "F24", "VK_F24", "Function");
		public static final int F25 = defineKey("F25", "F25", null, null, "Function");
		public static final int F26 = defineKey("F26", "F26", null, null, "Function");
		public static final int F27 = defineKey("F27", "F27", null, null, "Function");
		public static final int F28 = defineKey("F28", "F28", null, null, "Function");
		public static final int F29 = defineKey("F29", "F29", null, null, "Function");
		public static final int F30 = defineKey("F30", "F30", null, null, "Function");
		public static final int F31 = defineKey("F31", "F31", null, null, "Function");
		public static final int F32 = defineKey("F32", "F32", null, null, "Function");
		public static final int F33 = defineKey("F33", "F33", null, null, "Function");
		public static final int F34 = defineKey("F34", "F34", null, null, "Function");
		public static final int F35 = defineKey("F35", "F35", null, null, "Function");
	}

	public static class Phone {
		public static final int APP_SWITCH = defineKey("APP_SWITCH", "AppSwitch", null, null, "Phone");
		public static final int CALL = defineKey("CALL", "Call", null, null, "Phone");
		public static final int CAMERA = defineKey("CAMERA", "Camera", null, null, "Phone");
		public static final int CAMERA_FOCUS = defineKey("CAMERA_FOCUS", "CameraFocus", null, null, "Phone");
		public static final int END_CALL = defineKey("END_CALL", "EndCall", null, null, "Phone");
		public static final int GO_BACK = defineKey("GO_BACK", "GoBack", null, null, "Phone");
		public static final int GO_HOME = defineKey("GO_HOME", "GoHome", null, null, "Phone");
		public static final int HEADSET_HOOK = defineKey("HEADSET_HOOK", "HeadsetHook", null, null, "Phone");
		public static final int LAST_NUMBER_REDIAL = defineKey("LAST_NUMBER_REDIAL", "LastNumberRedial", null, null, "Phone");
		public static final int NOTIFICATION = defineKey("NOTIFICATION", "Notification", null, null, "Phone");
		public static final int MANNER_MODE = defineKey("MANNER_MODE", "MannerMode", null, null, "Phone");
		public static final int VOICE_DIAL = defineKey("VOICE_DIAL", "VoiceDial", null, null, "Phone");
	}

	public static class Multimedia {
		public static final int CHANNEL_DOWN = defineKey("CHANNEL_DOWN", "ChannelDown", "CHANNEL_DOWN", null, "Multimedia");
		public static final int CHANNEL_UP = defineKey("CHANNEL_UP", "ChannelUp", "CHANNEL_UP", null, "Multimedia");
		public static final int MEDIA_FAST_FORWARD = defineKey("MEDIA_FAST_FORWARD", "MediaFastForward", "FAST_FWD", null, "Multimedia");
		public static final int MEDIA_PAUSE = defineKey("MEDIA_PAUSE", "MediaPause", "PAUSE", "VK_PAUSE", "Multimedia");
		public static final int MEDIA_PLAY = defineKey("MEDIA_PLAY", "MediaPlay", "PLAY", null, "Multimedia");
		public static final int MEDIA_PLAY_PAUSE = defineKey("MEDIA_PLAY_PAUSE", "MediaPlayPause", null, null, "Multimedia");
		public static final int MEDIA_RECORD = defineKey("MEDIA_RECORD", "MediaRecord", "RECORD", null, "Multimedia");
		public static final int MEDIA_REWIND = defineKey("MEDIA_REWIND", "MediaRewind", "REWIND", null, "Multimedia");
		public static final int MEDIA_STOP = defineKey("MEDIA_STOP", "MediaStop", "STOP", "VK_STOP", "Multimedia"); // TODO
		public static final int MEDIA_TRACK_NEXT = defineKey("MEDIA_TRACK_NEXT", "MediaTrackNext", "TRACK_NEXT", null, "Multimedia");
		public static final int MEDIA_TRACK_PREVIOUS = defineKey("MEDIA_TRACK_PREVIOUS", "MediaTrackPrevious", "TRACK_PREV", null, "Multimedia");
	}

	public static class Audio {
		public static final int AUDIO_BALANCE_LEFT = defineKey("AUDIO_BALANCE_LEFT", "AudioBalanceLeft", null, null, "Audio");
		public static final int AUDIO_BALANCE_RIGHT = defineKey("AUDIO_BALANCE_RIGHT", "AudioBalanceRight", null, null, "Audio");
		public static final int AUDIO_BASS_DOWN = defineKey("AUDIO_BASS_DOWN", "AudioBassDown", null, null, "Audio");
		public static final int AUDIO_BASS_UP = defineKey("AUDIO_BASS_UP", "AudioBassUp", null, null, "Audio");
		public static final int AUDIO_BASS_BOOST_DOWN = defineKey("AUDIO_BASS_BOOST_DOWN", "AudioBassBoostDown", null, null, "Audio");
		public static final int AUDIO_BASS_BOOST_UP = defineKey("AUDIO_BASS_BOOST_UP", "AudioBassBoostUp", null, null, "Audio");
		public static final int AUDIO_BASS_BOOST_TOGGLE = defineKey("AUDIO_BASS_BOOST_TOGGLE", "AudioBassBoostToggle", null, null, "Audio");
		public static final int AUDIO_FADER_FRONT = defineKey("AUDIO_FADER_FRONT", "AudioFaderFront", null, null, "Audio");
		public static final int AUDIO_FADER_REAR = defineKey("AUDIO_FADER_REAR", "AudioFaderRear", null, null, "Audio");
		public static final int AUDIO_SURROUND_MODE_NEXT = defineKey("AUDIO_SURROUND_MODE_NEXT", "AudioSurroundModeNext", null, null, "Audio");
		public static final int AUDIO_TREBLE_DOWN = defineKey("AUDIO_TREBLE_DOWN", "AudioTrebleDown", null, null, "Audio");
		public static final int AUDIO_TREBLE_UP = defineKey("AUDIO_TREBLE_UP", "AudioTrebleUp", null, null, "Audio");
		public static final int AUDIO_VOLUME_DOWN = defineKey("AUDIO_VOLUME_DOWN", "AudioVolumeDown", "VOLUME_DOWN", null, "Audio");
		public static final int AUDIO_VOLUME_UP = defineKey("AUDIO_VOLUME_UP", "AudioVolumeUp", "VOLUME_UP", null, "Audio");
		public static final int AUDIO_VOLUME_MUTE = defineKey("AUDIO_VOLUME_MUTE", "AudioVolumeMute", "MUTE", null, "Audio");
		public static final int MICROPHONE_VOLUME_MUTE = defineKey("MICROPHONE_VOLUME_MUTE", "MicrophoneVolumeMute", null, null, "Audio");
		public static final int MICROPHONE_VOLUME_DOWN = defineKey("MICROPHONE_VOLUME_DOWN", "MicrophoneVolumeDown", null, null, "Audio");
		public static final int MICROPHONE_VOLUME_UP = defineKey("MICROPHONE_VOLUME_UP", "MicrophoneVolumeUp", null, null, "Audio");
		public static final int MICROPHONE_TOGGLE = defineKey("MICROPHONE_TOGGLE", "MicrophoneToggle", null, null, "Audio");
	}

	public static class Launch {
		public static final int LAUNCH_CALCULATOR = defineKey("LAUNCH_CALCULATOR", "LaunchCalculator", null, null, "Launch");
		public static final int LAUNCH_CALENDAR = defineKey("LAUNCH_CALENDAR", "LaunchCalendar", null, null, "Launch");
		public static final int LAUNCH_CONTACTS = defineKey("LAUNCH_CONTACTS", "LaunchContacts", null, null, "Launch");
		public static final int LAUNCH_MAIL = defineKey("LAUNCH_MAIL", "LaunchMail", null, null, "Launch");
		public static final int LAUNCH_MEDIA_PLAYER = defineKey("LAUNCH_MEDIA_PLAYER", "LaunchMediaPlayer", null, null, "Launch");
		public static final int LAUNCH_MUSIC_PLAYER = defineKey("LAUNCH_MUSIC_PLAYER", "LaunchMusicPlayer", null, null, "Launch");
		public static final int LAUNCH_MY_COMPUTER = defineKey("LAUNCH_MY_COMPUTER", "LaunchMyComputer", null, null, "Launch");
		public static final int LAUNCH_SCREEN_SAVER = defineKey("LAUNCH_SCREEN_SAVER", "LaunchScreenSaver", null, null, "Launch");
		public static final int LAUNCH_SPREADSHEET = defineKey("LAUNCH_SPREADSHEET", "LaunchSpreadsheet", null, null, "Launch");
		public static final int LAUNCH_WEB_BROWSER = defineKey("LAUNCH_WEB_BROWSER", "LaunchWebBrowser", null, null, "Launch");
		public static final int LAUNCH_WEB_CAM = defineKey("LAUNCH_WEB_CAM", "LaunchWebCam", null, null, "Launch");
		public static final int LAUNCH_WORD_PROCESSOR = defineKey("LAUNCH_WORD_PROCESSOR", "LaunchWordProcessor", null, null, "Launch");
		public static final int LAUNCH_APPLICATION1 = defineKey("LAUNCH_APPLICATION1", "LaunchApplication1", null, null, "Launch");
		public static final int LAUNCH_APPLICATION2 = defineKey("LAUNCH_APPLICATION2", "LaunchApplication2", null, null, "Launch");
		public static final int LAUNCH_APPLICATION3 = defineKey("LAUNCH_APPLICATION3", "LaunchApplication3", null, null, "Launch");
		public static final int LAUNCH_APPLICATION4 = defineKey("LAUNCH_APPLICATION4", "LaunchApplication4", null, null, "Launch");
		public static final int LAUNCH_APPLICATION5 = defineKey("LAUNCH_APPLICATION5", "LaunchApplication5", null, null, "Launch");
		public static final int LAUNCH_APPLICATION6 = defineKey("LAUNCH_APPLICATION6", "LaunchApplication6", null, null, "Launch");
		public static final int LAUNCH_APPLICATION7 = defineKey("LAUNCH_APPLICATION7", "LaunchApplication7", null, null, "Launch");
		public static final int LAUNCH_APPLICATION8 = defineKey("LAUNCH_APPLICATION8", "LaunchApplication8", null, null, "Launch");
		public static final int LAUNCH_APPLICATION9 = defineKey("LAUNCH_APPLICATION9", "LaunchApplication9", null, null, "Launch");
		public static final int LAUNCH_APPLICATION10 = defineKey("LAUNCH_APPLICATION10", "LaunchApplication10", null, null, "Launch");
		public static final int LAUNCH_APPLICATION11 = defineKey("LAUNCH_APPLICATION11", "LaunchApplication11", null, null, "Launch");
		public static final int LAUNCH_APPLICATION12 = defineKey("LAUNCH_APPLICATION12", "LaunchApplication12", null, null, "Launch");
		public static final int LAUNCH_APPLICATION13 = defineKey("LAUNCH_APPLICATION13", "LaunchApplication13", null, null, "Launch");
		public static final int LAUNCH_APPLICATION14 = defineKey("LAUNCH_APPLICATION14", "LaunchApplication14", null, null, "Launch");
		public static final int LAUNCH_APPLICATION15 = defineKey("LAUNCH_APPLICATION15", "LaunchApplication15", null, null, "Launch");
		public static final int LAUNCH_APPLICATION16 = defineKey("LAUNCH_APPLICATION16", "LaunchApplication16", null, null, "Launch");
		public static final int BROWSER_BACK = defineKey("BROWSER_BACK", "BrowserBack", null, null, "Launch");
		public static final int BROWSER_FAVORITES = defineKey("BROWSER_FAVORITES", "BrowserFavorites", null, null, "Launch");
		public static final int BROWSER_FORWARD = defineKey("BROWSER_FORWARD", "BrowserForward", null, null, "Launch");
		public static final int BROWSER_HOME = defineKey("BROWSER_HOME", "BrowserHome", null, null, "Launch");
		public static final int BROWSER_REFRESH = defineKey("BROWSER_REFRESH", "BrowserRefresh", null, null, "Launch");
		public static final int BROWSER_SEARCH = defineKey("BROWSER_SEARCH", "BrowserSearch", null, null, "Launch");
		public static final int BROWSER_STOP = defineKey("BROWSER_STOP", "BrowserStop", null, null, "Launch");
	}


	public static class TV {
		public static final int TV = defineKey("TV", "TV", null, null, "TV");
		public static final int TV_3D_MODE = defineKey("TV_3D_MODE", "TV3DMode", null, null, "TV");
		public static final int TV_ANTENNA_CABLE = defineKey("TV_ANTENNA_CABLE", "TVAntennaCable", null, null, "TV");
		public static final int TV_AUDIO_DESCRIPTION = defineKey("TV_AUDIO_DESCRIPTION", "TVAudioDescription", null, null, "TV");
		public static final int TV_AUDIO_DESCRIPTION_MIX_DOWN = defineKey("TV_AUDIO_DESCRIPTION_MIX_DOWN", "TVAudioDescriptionMixDown", null, null, "TV");
		public static final int TV_AUDIO_DESCRIPTION_MIX_UP = defineKey("TV_AUDIO_DESCRIPTION_MIX_UP", "TVAudioDescriptionMixUp", null, null, "TV");
		public static final int TV_CONTENTS_MENU = defineKey("TV_CONTENTS_MENU", "TVContentsMenu", null, null, "TV");
		public static final int TV_DATA_SERVICE = defineKey("TV_DATA_SERVICE", "TVDataService", null, null, "TV");
		public static final int TV_INPUT = defineKey("TV_INPUT", "TVInput", null, null, "TV");
		public static final int TV_INPUT_COMPONENT1 = defineKey("TV_INPUT_COMPONENT1", "TVInputComponent1", null, null, "TV");
		public static final int TV_INPUT_COMPONENT2 = defineKey("TV_INPUT_COMPONENT2", "TVInputComponent2", null, null, "TV");
		public static final int TV_INPUT_COMPOSITE1 = defineKey("TV_INPUT_COMPOSITE1", "TVInputComposite1", null, null, "TV");
		public static final int TV_INPUT_COMPOSITE2 = defineKey("TV_INPUT_COMPOSITE2", "TVInputComposite2", null, null, "TV");
		public static final int TV_INPUT_HDMI1 = defineKey("TV_INPUT_HDMI1", "TVInputHDMI1", null, null, "TV");
		public static final int TV_INPUT_HDMI2 = defineKey("TV_INPUT_HDMI2", "TVInputHDMI2", null, null, "TV");
		public static final int TV_INPUT_HDMI3 = defineKey("TV_INPUT_HDMI3", "TVInputHDMI3", null, null, "TV");
		public static final int TV_INPUT_HDMI4 = defineKey("TV_INPUT_HDMI4", "TVInputHDMI4", null, null, "TV");
		public static final int TV_INPUT_VGA1 = defineKey("TV_INPUT_VGA1", "TVInputVGA1", null, null, "TV");
		public static final int TV_NETWORK = defineKey("TV_NETWORK", "TVNetwork", null, null, "TV");
		public static final int TV_NUMBER_ENTRY = defineKey("TV_NUMBER_ENTRY", "TVNumberEntry", null, null, "TV");
		public static final int TV_POWER = defineKey("TV_POWER", "TVPower", null, null, "TV");
		public static final int TV_RADIO_SERVICE = defineKey("TV_RADIO_SERVICE", "TVRadioService", null, null, "TV");
		public static final int TV_SATELLITE = defineKey("TV_SATELLITE", "TVSatellite", null, null, "TV");
		public static final int TV_SATELLITE_BS = defineKey("TV_SATELLITE_BS", "TVSatelliteBS", null, null, "TV");
		public static final int TV_SATELLITE_CS = defineKey("TV_SATELLITE_CS", "TVSatelliteCS", null, null, "TV");
		public static final int TV_SATELLITE_TOGGLE = defineKey("TV_SATELLITE_TOGGLE", "TVSatelliteToggle", null, null, "TV");
		public static final int TV_TERRESTRIAL_ANALOG = defineKey("TV_TERRESTRIAL_ANALOG", "TVTerrestrialAnalog", null, null, "TV");
		public static final int TV_TERRESTRIAL_DIGITAL = defineKey("TV_TERRESTRIAL_DIGITAL", "TVTerrestrialDigital", null, null, "TV");
		public static final int TV_TIMER = defineKey("TV_TIMER", "TVTimer", null, null, "TV");
	}

	public static class MediaController {
		public static final int AVR_INPUT = defineKey("AVR_INPUT", "AVRInput", null, null, "MediaController");
		public static final int AVR_POWER = defineKey("AVR_POWER", "AVRPower", null, null, "MediaController");
		public static final int COLOR_F0_RED = defineKey("COLOR_F0_RED", "ColorF0Red", "COLORED_KEY_0", null, "MediaController");
		public static final int COLOR_F1_GREEN = defineKey("COLOR_F1_GREEN", "ColorF1Green", "COLORED_KEY_1", null, "MediaController");
		public static final int COLOR_F2_YELLOW = defineKey("COLOR_F2_YELLOW", "ColorF2Yellow", "COLORED_KEY_2", null, "MediaController");
		public static final int COLOR_F3_BLUE = defineKey("COLOR_F3_BLUE", "ColorF3Blue", "COLORED_KEY_3", null, "MediaController");
		public static final int CLOSED_CAPTION_TOGGLE = defineKey("CLOSED_CAPTION_TOGGLE", "ClosedCaptionToggle", null, null, "MediaController");
		public static final int DIMMER = defineKey("DIMMER", "Dimmer", null, null, "MediaController");
		public static final int DVR = defineKey("DVR", "DVR", null, null, "MediaController");
		public static final int GUIDE = defineKey("GUIDE", "Guide", null, null, "MediaController");
		public static final int INFO = defineKey("INFO", "Info", "INFO", null, "MediaController");
		public static final int MEDIA_AUDIO_TRACK = defineKey("MEDIA_AUDIO_TRACK", "MediaAudioTrack", null, null, "MediaController");
		public static final int MEDIA_LAST = defineKey("MEDIA_LAST", "MediaLast", null, null, "MediaController");
		public static final int MEDIA_TOP_MENU = defineKey("MEDIA_TOP_MENU", "MediaTopMenu", null, null, "MediaController");
		public static final int MEDIA_SKIP_BACKWARD = defineKey("MEDIA_SKIP_BACKWARD", "MediaSkipBackward", null, null, "MediaController");
		public static final int MEDIA_SKIP_FORWARD = defineKey("MEDIA_SKIP_FORWARD", "MediaSkipForward", null, null, "MediaController");
		public static final int MEDIA_STEP_BACKWARD = defineKey("MEDIA_STEP_BACKWARD", "MediaStepBackward", null, null, "MediaController");
		public static final int MEDIA_STEP_FORWARD = defineKey("MEDIA_STEP_FORWARD", "MediaStepForward", null, null, "MediaController");
		public static final int NAVIGATE_IN = defineKey("NAVIGATE_IN", "NavigateIn", null, null, "MediaController");
		public static final int NAVIGATE_NEXT = defineKey("NAVIGATE_NEXT", "NavigateNext", null, null, "MediaController");
		public static final int NAVIGATE_OUT = defineKey("NAVIGATE_OUT", "NavigateOut", null, null, "MediaController");
		public static final int NAVIGATE_PREVIOUS = defineKey("NAVIGATE_PREVIOUS", "NavigatePrevious", null, null, "MediaController");
		public static final int PAIRING = defineKey("PAIRING", "Pairing", null, null, "MediaController");
		public static final int PIN_P_TOGGLE = defineKey("PIN_P_TOGGLE", "PinPToggle", null, null, "MediaController");
		public static final int RANDOM_TOGGLE = defineKey("RANDOM_TOGGLE", "RandomToggle", null, null, "MediaController");
		public static final int SETTINGS = defineKey("SETTINGS", "Settings", null, null, "MediaController");
		public static final int STB_INPUT = defineKey("STB_INPUT", "STBInput", null, null, "MediaController");
		public static final int STB_POWER = defineKey("STB_POWER", "STBPower", null, null, "MediaController");
		public static final int SUBTITLE = defineKey("SUBTITLE", "Subtitle", null, null, "MediaController");
		public static final int TELETEXT = defineKey("TELETEXT", "Teletext", null, null, "MediaController");
		public static final int VIDEO_MODE_NEXT = defineKey("VIDEO_MODE_NEXT", "VideoModeNext", null, null, "MediaController");
		public static final int ZOOM_TOGGLE = defineKey("ZOOM_TOGGLE", "ZoomToggle", null, null, "MediaController");
	}

	public static class Document {
		public static final int CLOSE = defineKey("CLOSE", "Close", null, null, "Document");
		public static final int NEW = defineKey("NEW", "New", null, null, "Document");
		public static final int OPEN = defineKey("OPEN", "Open", null, null, "Document");
		public static final int PRINT = defineKey("PRINT", "Print", null, null, "Document");
		public static final int SAVE = defineKey("SAVE", "Save", null, null, "Document");
		public static final int MAIL_FORWARD = defineKey("MAIL_FORWARD", "MailForward", null, null, "Document");
		public static final int MAIL_REPLY = defineKey("MAIL_REPLY", "MailReply", null, null, "Document");
		public static final int MAIL_SEND = defineKey("MAIL_SEND", "MailSend", null, null, "Document");
		public static final int SPELL_CHECK = defineKey("SPELL_CHECK", "SpellCheck", null, null, "Document");
	}
		static {
			plainKey(' ', "SPACE", "VK_SPACE");
			plainKey(',', "COMMA", "VK_COMMA");
			plainKey('-', "MINUS", "VK_MINUS");
			plainKey('.', "PERIOD", "VK_PERIOD");
			plainKey('/', "SLASH", "VK_SLASH");
			plainKey('0', "DIGIT0", "VK_9");
			plainKey('1', "DIGIT1", "VK_1");
			plainKey('2', "DIGIT2", "VK_2");
			plainKey('3', "DIGIT3", "VK_3");
			plainKey('4', "DIGIT4", "VK_4");
			plainKey('5', "DIGIT5", "VK_5");
			plainKey('6', "DIGIT6", "VK_6");
			plainKey('7', "DIGIT7", "VK_7");
			plainKey('8', "DIGIT8", "VK_8");
			plainKey('9', "DIGIT9", "VK_9");
			plainKey(';', "SEMICOLON", "VK_SEMICOLON");
			plainKey('=', "EQUALS", "VK_EQUALS");
			plainKey('A', "A", "VK_A");
			plainKey('B', "B", "VK_B");
			plainKey('C', "C", "VK_C");
			plainKey('D', "D", "VK_D");
			plainKey('E', "E", "VK_E");
			plainKey('F', "F", "VK_F");
			plainKey('G', "G", "VK_G");
			plainKey('H', "H", "VK_H");
			plainKey('I', "I", "VK_I");
			plainKey('J', "J", "VK_J");
			plainKey('K', "K", "VK_K");
			plainKey('L', "L", "VK_L");
			plainKey('M', "M", "VK_M");
			plainKey('N', "N", "VK_N");
			plainKey('O', "O", "VK_O");
			plainKey('P', "P", "VK_P");
			plainKey('Q', "Q", "VK_Q");
			plainKey('R', "R", "VK_R");
			plainKey('S', "S", "VK_S");
			plainKey('T', "T", "VK_T");
			plainKey('U', "U", "VK_U");
			plainKey('V', "V", "VK_V");
			plainKey('W', "W", "VK_W");
			plainKey('X', "X", "VK_X");
			plainKey('Y', "Y", "VK_Y");
			plainKey('Z', "Z", "VK_Z");
			plainKey('[', "OPEN_BRACKET", "VK_OPEN_BRACKET");
			plainKey('\\', "BACK_SLASH", "VK_BACK_SLASH");
			plainKey(']', "CLOSE_BRACKET", "VK_CLOSE_BRACKET");
			plainKey('0', "NUMPAD0", "VK_NUMPAD0", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey('1', "NUMPAD1", "VK_NUMPAD1", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey('2', "NUMPAD2", "VK_NUMPAD2", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey('3', "NUMPAD3", "VK_NUMPAD3", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey('4', "NUMPAD4", "VK_NUMPAD4", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey('5', "NUMPAD5", "VK_NUMPAD5", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey('6', "NUMPAD6", "VK_NUMPAD6", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey('7', "NUMPAD7", "VK_NUMPAD7", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey('8', "NUMPAD8", "VK_NUMPAD8", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey('9', "NUMPAD9", "VK_NUMPAD9", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey('*', "MULTIPLY", "VK_MULTIPLY", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey('+', "ADD", "VK_ADD", KeyEvent.KEY_TYPE_KEYPAD);
//			plainKey("SEPARATOR", "VK_SEPARATOR", KeyEvent.KEY_TYPE_KEYPAD); // , or .
			plainKey('-', "SUBTRACT", "VK_SUBTRACT", KeyEvent.KEY_TYPE_KEYPAD);
//			plainKey("DECIMAL", "VK_DECIMAL", KeyEvent.KEY_TYPE_KEYPAD); // . or ,
			plainKey('/', "DIVIDE", "VK_DIVIDE", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey('`', "BACK_QUOTE", "VK_BACK_QUOTE", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey('\'', "QUOTE", "VK_QUOTE", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey(Navigation.ARROW_UP, "KP_UP", "VK_KP_UP", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey(Navigation.ARROW_DOWN, "KP_DOWN", "VK_KP_DOWN", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey(Navigation.ARROW_LEFT, "KP_LEFT", "VK_KP_LEFT", KeyEvent.KEY_TYPE_KEYPAD);
			plainKey(Navigation.ARROW_RIGHT, "KP_RIGHT", "VK_KP_RIGHT", KeyEvent.KEY_TYPE_KEYPAD);
//			plainKey("DEAD_GRAVE", "VK_DEAD_GRAVE", KeyEvent.KEY_TYPE_DEAD);
//			plainKey("DEAD_ACUTE", "VK_DEAD_ACUTE", KeyEvent.KEY_TYPE_DEAD);
//			plainKey("DEAD_CIRCUMFLEX", "VK_DEAD_CIRCUMFLEX", KeyEvent.KEY_TYPE_DEAD);
//			plainKey("DEAD_TILDE", "VK_DEAD_TILDE", KeyEvent.KEY_TYPE_DEAD);
//			plainKey("DEAD_MACRON", "VK_DEAD_MACRON", KeyEvent.KEY_TYPE_DEAD);
//			plainKey("DEAD_BREVE", "VK_DEAD_BREVE", KeyEvent.KEY_TYPE_DEAD);
//			plainKey("DEAD_ABOVEDOT", "VK_DEAD_ABOVEDOT", KeyEvent.KEY_TYPE_DEAD);
//			plainKey("DEAD_DIAERESIS", "VK_DEAD_DIAERESIS", KeyEvent.KEY_TYPE_DEAD);
//			plainKey("DEAD_ABOVERING", "VK_DEAD_ABOVERING", KeyEvent.KEY_TYPE_DEAD);
//			plainKey("DEAD_DOUBLEACUTE", "VK_DEAD_DOUBLEACUTE", KeyEvent.KEY_TYPE_DEAD);
//			plainKey("DEAD_CARON", "VK_DEAD_CARON", KeyEvent.KEY_TYPE_DEAD);
//			plainKey("DEAD_CEDILLA", "VK_DEAD_CEDILLA", KeyEvent.KEY_TYPE_DEAD);
//			plainKey("DEAD_OGONEK", "VK_DEAD_OGONEK", KeyEvent.KEY_TYPE_DEAD);
//			plainKey("DEAD_IOTA", "VK_DEAD_IOTA", KeyEvent.KEY_TYPE_DEAD);
//			plainKey("DEAD_VOICED_SOUND", "VK_DEAD_VOICED_SOUND", KeyEvent.KEY_TYPE_DEAD);
//			plainKey("DEAD_SEMIVOICED_SOUND", "VK_DEAD_SEMIVOICED_SOUND", KeyEvent.KEY_TYPE_DEAD);
			plainKey('&',"AMPERSAND", "VK_AMPERSAND");
			plainKey('*', "ASTERISK", "VK_ASTERISK");
			plainKey('"', "QUOTEDBL", "VK_QUOTEDBL");
			plainKey('<', "LESS", "VK_LESS");
			plainKey('>', "GREATER", "VK_GREATER");
			plainKey('{', "BRACELEFT", "VK_BRACELEFT");
			plainKey('}', "BRACERIGHT", "VK_BRACERIGHT");
			plainKey('@', "AT", "VK_AT");
			plainKey(':', "COLON", "VK_COLON");
			plainKey('^', "CIRCUMFLEX", "VK_CIRCUMFLEX");
			plainKey('$', "DOLLAR", "VK_DOLLAR");
			plainKey('€', "EURO_SIGN", "VK_EURO_SIGN");
			plainKey('!', "EXCLAMATION_MARK", "VK_EXCLAMATION_MARK");
			plainKey('¡', "INVERTED_EXCLAMATION_MARK", "VK_INVERTED_EXCLAMATION_MARK");
			plainKey('(', "LEFT_PARENTHESIS", "VK_LEFT_PARENTHESIS");
			plainKey('#', "NUMBER_SIGN", "VK_NUMBER_SIGN");
			plainKey('+', "PLUS", "VK_PLUS");
			plainKey(')', "RIGHT_PARENTHESIS", "VK_RIGHT_PARENTHESIS");
			plainKey('_', "UNDERSCORE", "VK_UNDERSCORE");
//			missingkey("PROPS", "VK_PROPS");
//			missingkey("BEGIN", "VK_BEGIN");
//			missingkey("SOFTKEY_0", "None");
//			missingkey("SOFTKEY_1", "None");
//			missingkey("SOFTKEY_2", "None");
//			missingkey("SOFTKEY_3", "None");
//			missingkey("SOFTKEY_4", "None");
//			missingkey("SOFTKEY_5", "None");
//			missingkey("SOFTKEY_6", "None");
//			missingkey("SOFTKEY_7", "None");
//			missingkey("SOFTKEY_8", "None");
//			missingkey("SOFTKEY_9", "None");
//			missingkey("GAME_A", "None");
//			missingkey("GAME_B", "None");
//			missingkey("GAME_C", "None");
//			missingkey("GAME_D", "None");
//			missingkey("STAR", "None");
//			missingkey("POUND", "None");
//			missingkey("COMMAND", "None");
//			missingkey("None", "VK_SEPARATER");
			jsAlias("OS", "Meta");
			jsAlias("Spacebar", " ");
			jsAlias("VolumeUp", "AudioVolumeUp");
			jsAlias("VolumeDown", "AudioVolumeDown");
			jsAlias("VolumeMute", "AudioVolumeMute");
	}

	public static class Mappings {
		public static final Map<String,Integer> JS_MAP = new HashMap<>(); 
		public static final Map<String,Integer> FX_MAP = new HashMap<>(); 
		public static final Map<String,Integer> AWT_MAP = new HashMap<>(); 
		public static final Map<String,Integer> NAME_MAP = new HashMap<>(); 
		public static final Map<Integer,String> CODE_MAP = new HashMap<>(); 
		public static final List<String> CATEGORIES = new ArrayList<>(); 
	}
	
	private static int nextKey = -1;
	protected static void jsAlias(String altJsName, String jsName) {
		Mappings.JS_MAP.put(altJsName, Mappings.JS_MAP.get(jsName));
	}
	protected static void plainKey(int code, String fxName, String awtName) {
		if(fxName != null)
			Mappings.FX_MAP.put(fxName, code);
		if(awtName != null)
			Mappings.AWT_MAP.put(awtName, code);
	}
	protected static void plainKey(int code, String fxName, String awtName, int flags) {
		if(fxName != null)
			Mappings.FX_MAP.put(fxName, code);
		if(awtName != null)
			Mappings.AWT_MAP.put(awtName, code);
	}
	protected static int defineKey(String name, String jsName, String fxName, String awtName, String category) {
		int code = nextKey--;
		Mappings.NAME_MAP.put(name, code);
		Mappings.CODE_MAP.put(code, name);
		if(jsName != null)
			Mappings.JS_MAP.put(jsName, code);
		if(fxName != null)
			Mappings.FX_MAP.put(fxName, code);
		if(awtName != null)
			Mappings.AWT_MAP.put(awtName, code);
		Mappings.CATEGORIES.add(-code-1, name);
		return code;
	}
}