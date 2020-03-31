package turtleduck.events;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class KeyCodes {
	public static class Special {
		/** The Undefined key. */
		public static final int UNDEFINED = 0x210000;

		protected static final int FIRST_ID = 0x210000, LAST_ID = 0x210000;
	}

	public static class Modifier {
		/** The Alt key. */
		public static final int ALT = 0x220000;
		/** The Alt Graph key. */
		public static final int ALT_GRAPH = 0x220001;
		/** The Caps Lock key. */
		public static final int CAPS_LOCK = 0x220002;
		/** The Control key. */
		public static final int CONTROL = 0x220003;
		/** The Fn key. */
		public static final int FN = 0x220004;
		/** The Meta key. */
		public static final int META = 0x220005;
		/** The Os key. */
		public static final int OS = 0x220006;
		/** The Num Lock key. */
		public static final int NUM_LOCK = 0x220007;
		/** The Scroll Lock key. */
		public static final int SCROLL_LOCK = 0x220008;
		/** The Shift key. */
		public static final int SHIFT = 0x220009;
		/** The Symbol key. */
		public static final int SYMBOL = 0x22000a;

		protected static final int FIRST_ID = 0x220000, LAST_ID = 0x22000a;
	}

	public static class Whitespace {
		/** The Enter key. */
		public static final int ENTER = 0x230000;
		/** The Tab key. */
		public static final int TAB = 0x230001;

		protected static final int FIRST_ID = 0x230000, LAST_ID = 0x230001;
	}

	public static class Navigation {
		/** The Arrow Down key. */
		public static final int ARROW_DOWN = 0x240000;
		/** The Arrow Left key. */
		public static final int ARROW_LEFT = 0x240001;
		/** The Arrow Right key. */
		public static final int ARROW_RIGHT = 0x240002;
		/** The Arrow Up key. */
		public static final int ARROW_UP = 0x240003;
		/** The End key. */
		public static final int END = 0x240004;
		/** The Home key. */
		public static final int HOME = 0x240005;
		/** The Page Down key. */
		public static final int PAGE_DOWN = 0x240006;
		/** The Page Up key. */
		public static final int PAGE_UP = 0x240007;

		protected static final int FIRST_ID = 0x240000, LAST_ID = 0x240007;
	}

	public static class Editing {
		/** The Backspace key. */
		public static final int BACKSPACE = 0x250000;
		/** The Clear key. */
		public static final int CLEAR = 0x250001;
		/** The Copy key. */
		public static final int COPY = 0x250002;
		/** The Cr Sel key. */
		public static final int CR_SEL = 0x250003;
		/** The Cut key. */
		public static final int CUT = 0x250004;
		/** The Delete key. */
		public static final int DELETE = 0x250005;
		/** The Erase Eof key. */
		public static final int ERASE_EOF = 0x250006;
		/** The Ex Sel key. */
		public static final int EX_SEL = 0x250007;
		/** The Insert key. */
		public static final int INSERT = 0x250008;
		/** The Paste key. */
		public static final int PASTE = 0x250009;
		/** The Redo key. */
		public static final int REDO = 0x25000a;
		/** The Undo key. */
		public static final int UNDO = 0x25000b;

		protected static final int FIRST_ID = 0x250000, LAST_ID = 0x25000b;
	}

	public static class UI {
		/** The Accept key. */
		public static final int ACCEPT = 0x260000;
		/** The Attn key. */
		public static final int ATTN = 0x260001;
		/** The Cancel key. */
		public static final int CANCEL = 0x260002;
		/** The Context Menu key. */
		public static final int CONTEXT_MENU = 0x260003;
		/** The Escape key. */
		public static final int ESCAPE = 0x260004;
		/** The Execute key. */
		public static final int EXECUTE = 0x260005;
		/** The Find key. */
		public static final int FIND = 0x260006;
		/** The Help key. */
		public static final int HELP = 0x260007;
		/** The Pause key. */
		public static final int PAUSE = 0x260008;
		/** The Play key. */
		public static final int PLAY = 0x260009;
		/** The Select key. */
		public static final int SELECT = 0x26000a;
		/** The Zoom In key. */
		public static final int ZOOM_IN = 0x26000b;
		/** The Zoom Out key. */
		public static final int ZOOM_OUT = 0x26000c;

		protected static final int FIRST_ID = 0x260000, LAST_ID = 0x26000c;
	}

	public static class Device {
		/** The Brightness Down key. */
		public static final int BRIGHTNESS_DOWN = 0x270000;
		/** The Brightness Up key. */
		public static final int BRIGHTNESS_UP = 0x270001;
		/** The Eject key. */
		public static final int EJECT = 0x270002;
		/** The Log Off key. */
		public static final int LOG_OFF = 0x270003;
		/** The Power key. */
		public static final int POWER = 0x270004;
		/** The Power Off key. */
		public static final int POWER_OFF = 0x270005;
		/** The Print Screen key. */
		public static final int PRINT_SCREEN = 0x270006;
		/** The Hibernate key. */
		public static final int HIBERNATE = 0x270007;
		/** The Standby key. */
		public static final int STANDBY = 0x270008;
		/** The Wake Up key. */
		public static final int WAKE_UP = 0x270009;

		protected static final int FIRST_ID = 0x270000, LAST_ID = 0x270009;
	}

	public static class Composition {
		/** The All Candidates key. */
		public static final int ALL_CANDIDATES = 0x280000;
		/** The Alphanumeric key. */
		public static final int ALPHANUMERIC = 0x280001;
		/** The Code Input key. */
		public static final int CODE_INPUT = 0x280002;
		/** The Compose key. */
		public static final int COMPOSE = 0x280003;
		/** The Convert key. */
		public static final int CONVERT = 0x280004;
		/** The Dead key. */
		public static final int DEAD = 0x280005;
		/** The Final Mode key. */
		public static final int FINAL_MODE = 0x280006;
		//		missingkey();
		//		missingkey();
		//		missingkey();
		//		missingkey("JAPANESE_KATAKANA", "VK_JAPANESE_KATAKANA");
		//		missingkey("JAPANESE_HIRAGANA", "VK_JAPANESE_HIRAGANA");
		//		missingkey("JAPANESE_ROMAN", "VK_JAPANESE_ROMAN");
		//		missingkey("KANA", "VK_KANA");
		//		missingkey("INPUT_METHOD_ON_OFF", "VK_INPUT_METHOD_ON_OFF");
		/** The Group First key. */
		public static final int GROUP_FIRST = 0x280007;
		/** The Group Last key. */
		public static final int GROUP_LAST = 0x280008;
		/** The Group Next key. */
		public static final int GROUP_NEXT = 0x280009;
		/** The Group Previous key. */
		public static final int GROUP_PREVIOUS = 0x28000a;
		/** The Mode Change key. */
		public static final int MODE_CHANGE = 0x28000b;
		/** The Non Convert key. */
		public static final int NON_CONVERT = 0x28000c;
		/** The Previous Candidate key. */
		public static final int PREVIOUS_CANDIDATE = 0x28000d;
		/** The Process key. */
		public static final int PROCESS = 0x28000e;
		/** The Single Candidate key. */
		public static final int SINGLE_CANDIDATE = 0x28000f;
		/** The Hangul Mode key. */
		public static final int HANGUL_MODE = 0x280010;
		/** The Hanja Mode key. */
		public static final int HANJA_MODE = 0x280011;
		/** The Junja Mode key. */
		public static final int JUNJA_MODE = 0x280012;
		/** The Eisu key. */
		public static final int EISU = 0x280013;
		/** The Hankaku key. */
		public static final int HANKAKU = 0x280014;
		/** The Hiragana key. */
		public static final int HIRAGANA = 0x280015;
		/** The Hiragana Katakana key. */
		public static final int HIRAGANA_KATAKANA = 0x280016;
		/** The Kana Mode key. */
		public static final int KANA_MODE = 0x280017;
		/** The Kanji Mode key. */
		public static final int KANJI_MODE = 0x280018;
		/** The Katakana key. */
		public static final int KATAKANA = 0x280019;
		/** The Romaji key. */
		public static final int ROMAJI = 0x28001a;
		/** The Zenkaku key. */
		public static final int ZENKAKU = 0x28001b;
		/** The Zenkaku Hankaku key. */
		public static final int ZENKAKU_HANKAKU = 0x28001c;

		protected static final int FIRST_ID = 0x280000, LAST_ID = 0x28001c;
	}

	public static class Function {
		/** The F1 key. */
		public static final int F1 = 0x290000;
		/** The F2 key. */
		public static final int F2 = 0x290001;
		/** The F3 key. */
		public static final int F3 = 0x290002;
		/** The F4 key. */
		public static final int F4 = 0x290003;
		/** The F5 key. */
		public static final int F5 = 0x290004;
		/** The F6 key. */
		public static final int F6 = 0x290005;
		/** The F7 key. */
		public static final int F7 = 0x290006;
		/** The F8 key. */
		public static final int F8 = 0x290007;
		/** The F9 key. */
		public static final int F9 = 0x290008;
		/** The F10 key. */
		public static final int F10 = 0x290009;
		/** The F11 key. */
		public static final int F11 = 0x29000a;
		/** The F12 key. */
		public static final int F12 = 0x29000b;
		/** The F13 key. */
		public static final int F13 = 0x29000c;
		/** The F14 key. */
		public static final int F14 = 0x29000d;
		/** The F15 key. */
		public static final int F15 = 0x29000e;
		/** The F16 key. */
		public static final int F16 = 0x29000f;
		/** The F17 key. */
		public static final int F17 = 0x290010;
		/** The F18 key. */
		public static final int F18 = 0x290011;
		/** The F19 key. */
		public static final int F19 = 0x290012;
		/** The F20 key. */
		public static final int F20 = 0x290013;
		/** The F21 key. */
		public static final int F21 = 0x290014;
		/** The F22 key. */
		public static final int F22 = 0x290015;
		/** The F23 key. */
		public static final int F23 = 0x290016;
		/** The F24 key. */
		public static final int F24 = 0x290017;
		/** The F25 key. */
		public static final int F25 = 0x290018;
		/** The F26 key. */
		public static final int F26 = 0x290019;
		/** The F27 key. */
		public static final int F27 = 0x29001a;
		/** The F28 key. */
		public static final int F28 = 0x29001b;
		/** The F29 key. */
		public static final int F29 = 0x29001c;
		/** The F30 key. */
		public static final int F30 = 0x29001d;
		/** The F31 key. */
		public static final int F31 = 0x29001e;
		/** The F32 key. */
		public static final int F32 = 0x29001f;
		/** The F33 key. */
		public static final int F33 = 0x290020;
		/** The F34 key. */
		public static final int F34 = 0x290021;
		/** The F35 key. */
		public static final int F35 = 0x290022;

		protected static final int FIRST_ID = 0x290000, LAST_ID = 0x290022;
	}

	public static class Phone {
		/** The App Switch key. */
		public static final int APP_SWITCH = 0x2a0000;
		/** The Call key. */
		public static final int CALL = 0x2a0001;
		/** The Camera key. */
		public static final int CAMERA = 0x2a0002;
		/** The Camera Focus key. */
		public static final int CAMERA_FOCUS = 0x2a0003;
		/** The End Call key. */
		public static final int END_CALL = 0x2a0004;
		/** The Go Back key. */
		public static final int GO_BACK = 0x2a0005;
		/** The Go Home key. */
		public static final int GO_HOME = 0x2a0006;
		/** The Headset Hook key. */
		public static final int HEADSET_HOOK = 0x2a0007;
		/** The Last Number Redial key. */
		public static final int LAST_NUMBER_REDIAL = 0x2a0008;
		/** The Notification key. */
		public static final int NOTIFICATION = 0x2a0009;
		/** The Manner Mode key. */
		public static final int MANNER_MODE = 0x2a000a;
		/** The Voice Dial key. */
		public static final int VOICE_DIAL = 0x2a000b;

		protected static final int FIRST_ID = 0x2a0000, LAST_ID = 0x2a000b;
	}

	public static class Multimedia {
		/** The Channel Down key. */
		public static final int CHANNEL_DOWN = 0x2b0000;
		/** The Channel Up key. */
		public static final int CHANNEL_UP = 0x2b0001;
		/** The Media Fast Forward key. */
		public static final int MEDIA_FAST_FORWARD = 0x2b0002;
		/** The Media Pause key. */
		public static final int MEDIA_PAUSE = 0x2b0003;
		/** The Media Play key. */
		public static final int MEDIA_PLAY = 0x2b0004;
		/** The Media Play Pause key. */
		public static final int MEDIA_PLAY_PAUSE = 0x2b0005;
		/** The Media Record key. */
		public static final int MEDIA_RECORD = 0x2b0006;
		/** The Media Rewind key. */
		public static final int MEDIA_REWIND = 0x2b0007;
		/** The Media Stop key. */
		public static final int MEDIA_STOP = 0x2b0008;
		/** The Media Track Next key. */
		public static final int MEDIA_TRACK_NEXT = 0x2b0009;
		/** The Media Track Previous key. */
		public static final int MEDIA_TRACK_PREVIOUS = 0x2b000a;

		protected static final int FIRST_ID = 0x2b0000, LAST_ID = 0x2b000a;
	}

	public static class Audio {
		/** The Audio Balance Left key. */
		public static final int AUDIO_BALANCE_LEFT = 0x2c0000;
		/** The Audio Balance Right key. */
		public static final int AUDIO_BALANCE_RIGHT = 0x2c0001;
		/** The Audio Bass Down key. */
		public static final int AUDIO_BASS_DOWN = 0x2c0002;
		/** The Audio Bass Up key. */
		public static final int AUDIO_BASS_UP = 0x2c0003;
		/** The Audio Bass Boost Down key. */
		public static final int AUDIO_BASS_BOOST_DOWN = 0x2c0004;
		/** The Audio Bass Boost Up key. */
		public static final int AUDIO_BASS_BOOST_UP = 0x2c0005;
		/** The Audio Bass Boost Toggle key. */
		public static final int AUDIO_BASS_BOOST_TOGGLE = 0x2c0006;
		/** The Audio Fader Front key. */
		public static final int AUDIO_FADER_FRONT = 0x2c0007;
		/** The Audio Fader Rear key. */
		public static final int AUDIO_FADER_REAR = 0x2c0008;
		/** The Audio Surround Mode Next key. */
		public static final int AUDIO_SURROUND_MODE_NEXT = 0x2c0009;
		/** The Audio Treble Down key. */
		public static final int AUDIO_TREBLE_DOWN = 0x2c000a;
		/** The Audio Treble Up key. */
		public static final int AUDIO_TREBLE_UP = 0x2c000b;
		/** The Audio Volume Down key. */
		public static final int AUDIO_VOLUME_DOWN = 0x2c000c;
		/** The Audio Volume Up key. */
		public static final int AUDIO_VOLUME_UP = 0x2c000d;
		/** The Audio Volume Mute key. */
		public static final int AUDIO_VOLUME_MUTE = 0x2c000e;
		/** The Microphone Volume Mute key. */
		public static final int MICROPHONE_VOLUME_MUTE = 0x2c000f;
		/** The Microphone Volume Down key. */
		public static final int MICROPHONE_VOLUME_DOWN = 0x2c0010;
		/** The Microphone Volume Up key. */
		public static final int MICROPHONE_VOLUME_UP = 0x2c0011;
		/** The Microphone Toggle key. */
		public static final int MICROPHONE_TOGGLE = 0x2c0012;

		protected static final int FIRST_ID = 0x2c0000, LAST_ID = 0x2c0012;
	}

	public static class Launch {
		/** The Launch Calculator key. */
		public static final int LAUNCH_CALCULATOR = 0x2d0000;
		/** The Launch Calendar key. */
		public static final int LAUNCH_CALENDAR = 0x2d0001;
		/** The Launch Contacts key. */
		public static final int LAUNCH_CONTACTS = 0x2d0002;
		/** The Launch Mail key. */
		public static final int LAUNCH_MAIL = 0x2d0003;
		/** The Launch Media Player key. */
		public static final int LAUNCH_MEDIA_PLAYER = 0x2d0004;
		/** The Launch Music Player key. */
		public static final int LAUNCH_MUSIC_PLAYER = 0x2d0005;
		/** The Launch My Computer key. */
		public static final int LAUNCH_MY_COMPUTER = 0x2d0006;
		/** The Launch Screen Saver key. */
		public static final int LAUNCH_SCREEN_SAVER = 0x2d0007;
		/** The Launch Spreadsheet key. */
		public static final int LAUNCH_SPREADSHEET = 0x2d0008;
		/** The Launch Web Browser key. */
		public static final int LAUNCH_WEB_BROWSER = 0x2d0009;
		/** The Launch Web Cam key. */
		public static final int LAUNCH_WEB_CAM = 0x2d000a;
		/** The Launch Word Processor key. */
		public static final int LAUNCH_WORD_PROCESSOR = 0x2d000b;
		/** The Launch Application1 key. */
		public static final int LAUNCH_APPLICATION1 = 0x2d000c;
		/** The Launch Application2 key. */
		public static final int LAUNCH_APPLICATION2 = 0x2d000d;
		/** The Launch Application3 key. */
		public static final int LAUNCH_APPLICATION3 = 0x2d000e;
		/** The Launch Application4 key. */
		public static final int LAUNCH_APPLICATION4 = 0x2d000f;
		/** The Launch Application5 key. */
		public static final int LAUNCH_APPLICATION5 = 0x2d0010;
		/** The Launch Application6 key. */
		public static final int LAUNCH_APPLICATION6 = 0x2d0011;
		/** The Launch Application7 key. */
		public static final int LAUNCH_APPLICATION7 = 0x2d0012;
		/** The Launch Application8 key. */
		public static final int LAUNCH_APPLICATION8 = 0x2d0013;
		/** The Launch Application9 key. */
		public static final int LAUNCH_APPLICATION9 = 0x2d0014;
		/** The Launch Application10 key. */
		public static final int LAUNCH_APPLICATION10 = 0x2d0015;
		/** The Launch Application11 key. */
		public static final int LAUNCH_APPLICATION11 = 0x2d0016;
		/** The Launch Application12 key. */
		public static final int LAUNCH_APPLICATION12 = 0x2d0017;
		/** The Launch Application13 key. */
		public static final int LAUNCH_APPLICATION13 = 0x2d0018;
		/** The Launch Application14 key. */
		public static final int LAUNCH_APPLICATION14 = 0x2d0019;
		/** The Launch Application15 key. */
		public static final int LAUNCH_APPLICATION15 = 0x2d001a;
		/** The Launch Application16 key. */
		public static final int LAUNCH_APPLICATION16 = 0x2d001b;
		/** The Browser Back key. */
		public static final int BROWSER_BACK = 0x2d001c;
		/** The Browser Favorites key. */
		public static final int BROWSER_FAVORITES = 0x2d001d;
		/** The Browser Forward key. */
		public static final int BROWSER_FORWARD = 0x2d001e;
		/** The Browser Home key. */
		public static final int BROWSER_HOME = 0x2d001f;
		/** The Browser Refresh key. */
		public static final int BROWSER_REFRESH = 0x2d0020;
		/** The Browser Search key. */
		public static final int BROWSER_SEARCH = 0x2d0021;
		/** The Browser Stop key. */
		public static final int BROWSER_STOP = 0x2d0022;

		protected static final int FIRST_ID = 0x2d0000, LAST_ID = 0x2d0022;
	}


	public static class TV {
		/** The Tv key. */
		public static final int TV = 0x2e0000;
		/** The Tv 3d Mode key. */
		public static final int TV_3D_MODE = 0x2e0001;
		/** The Tv Antenna Cable key. */
		public static final int TV_ANTENNA_CABLE = 0x2e0002;
		/** The Tv Audio Description key. */
		public static final int TV_AUDIO_DESCRIPTION = 0x2e0003;
		/** The Tv Audio Description Mix Down key. */
		public static final int TV_AUDIO_DESCRIPTION_MIX_DOWN = 0x2e0004;
		/** The Tv Audio Description Mix Up key. */
		public static final int TV_AUDIO_DESCRIPTION_MIX_UP = 0x2e0005;
		/** The Tv Contents Menu key. */
		public static final int TV_CONTENTS_MENU = 0x2e0006;
		/** The Tv Data Service key. */
		public static final int TV_DATA_SERVICE = 0x2e0007;
		/** The Tv Input key. */
		public static final int TV_INPUT = 0x2e0008;
		/** The Tv Input Component1 key. */
		public static final int TV_INPUT_COMPONENT1 = 0x2e0009;
		/** The Tv Input Component2 key. */
		public static final int TV_INPUT_COMPONENT2 = 0x2e000a;
		/** The Tv Input Composite1 key. */
		public static final int TV_INPUT_COMPOSITE1 = 0x2e000b;
		/** The Tv Input Composite2 key. */
		public static final int TV_INPUT_COMPOSITE2 = 0x2e000c;
		/** The Tv Input Hdmi1 key. */
		public static final int TV_INPUT_HDMI1 = 0x2e000d;
		/** The Tv Input Hdmi2 key. */
		public static final int TV_INPUT_HDMI2 = 0x2e000e;
		/** The Tv Input Hdmi3 key. */
		public static final int TV_INPUT_HDMI3 = 0x2e000f;
		/** The Tv Input Hdmi4 key. */
		public static final int TV_INPUT_HDMI4 = 0x2e0010;
		/** The Tv Input Vga1 key. */
		public static final int TV_INPUT_VGA1 = 0x2e0011;
		/** The Tv Network key. */
		public static final int TV_NETWORK = 0x2e0012;
		/** The Tv Number Entry key. */
		public static final int TV_NUMBER_ENTRY = 0x2e0013;
		/** The Tv Power key. */
		public static final int TV_POWER = 0x2e0014;
		/** The Tv Radio Service key. */
		public static final int TV_RADIO_SERVICE = 0x2e0015;
		/** The Tv Satellite key. */
		public static final int TV_SATELLITE = 0x2e0016;
		/** The Tv Satellite Bs key. */
		public static final int TV_SATELLITE_BS = 0x2e0017;
		/** The Tv Satellite Cs key. */
		public static final int TV_SATELLITE_CS = 0x2e0018;
		/** The Tv Satellite Toggle key. */
		public static final int TV_SATELLITE_TOGGLE = 0x2e0019;
		/** The Tv Terrestrial Analog key. */
		public static final int TV_TERRESTRIAL_ANALOG = 0x2e001a;
		/** The Tv Terrestrial Digital key. */
		public static final int TV_TERRESTRIAL_DIGITAL = 0x2e001b;
		/** The Tv Timer key. */
		public static final int TV_TIMER = 0x2e001c;

		protected static final int FIRST_ID = 0x2e0000, LAST_ID = 0x2e001c;
	}

	public static class MediaController {
		/** The Avr Input key. */
		public static final int AVR_INPUT = 0x2f0000;
		/** The Avr Power key. */
		public static final int AVR_POWER = 0x2f0001;
		/** The Color F0 Red key. */
		public static final int COLOR_F0_RED = 0x2f0002;
		/** The Color F1 Green key. */
		public static final int COLOR_F1_GREEN = 0x2f0003;
		/** The Color F2 Yellow key. */
		public static final int COLOR_F2_YELLOW = 0x2f0004;
		/** The Color F3 Blue key. */
		public static final int COLOR_F3_BLUE = 0x2f0005;
		/** The Closed Caption Toggle key. */
		public static final int CLOSED_CAPTION_TOGGLE = 0x2f0006;
		/** The Dimmer key. */
		public static final int DIMMER = 0x2f0007;
		/** The Dvr key. */
		public static final int DVR = 0x2f0008;
		/** The Guide key. */
		public static final int GUIDE = 0x2f0009;
		/** The Info key. */
		public static final int INFO = 0x2f000a;
		/** The Media Audio Track key. */
		public static final int MEDIA_AUDIO_TRACK = 0x2f000b;
		/** The Media Last key. */
		public static final int MEDIA_LAST = 0x2f000c;
		/** The Media Top Menu key. */
		public static final int MEDIA_TOP_MENU = 0x2f000d;
		/** The Media Skip Backward key. */
		public static final int MEDIA_SKIP_BACKWARD = 0x2f000e;
		/** The Media Skip Forward key. */
		public static final int MEDIA_SKIP_FORWARD = 0x2f000f;
		/** The Media Step Backward key. */
		public static final int MEDIA_STEP_BACKWARD = 0x2f0010;
		/** The Media Step Forward key. */
		public static final int MEDIA_STEP_FORWARD = 0x2f0011;
		/** The Navigate In key. */
		public static final int NAVIGATE_IN = 0x2f0012;
		/** The Navigate Next key. */
		public static final int NAVIGATE_NEXT = 0x2f0013;
		/** The Navigate Out key. */
		public static final int NAVIGATE_OUT = 0x2f0014;
		/** The Navigate Previous key. */
		public static final int NAVIGATE_PREVIOUS = 0x2f0015;
		/** The Pairing key. */
		public static final int PAIRING = 0x2f0016;
		/** The Pin P Toggle key. */
		public static final int PIN_P_TOGGLE = 0x2f0017;
		/** The Random Toggle key. */
		public static final int RANDOM_TOGGLE = 0x2f0018;
		/** The Settings key. */
		public static final int SETTINGS = 0x2f0019;
		/** The Stb Input key. */
		public static final int STB_INPUT = 0x2f001a;
		/** The Stb Power key. */
		public static final int STB_POWER = 0x2f001b;
		/** The Subtitle key. */
		public static final int SUBTITLE = 0x2f001c;
		/** The Teletext key. */
		public static final int TELETEXT = 0x2f001d;
		/** The Video Mode Next key. */
		public static final int VIDEO_MODE_NEXT = 0x2f001e;
		/** The Zoom Toggle key. */
		public static final int ZOOM_TOGGLE = 0x2f001f;

		protected static final int FIRST_ID = 0x2f0000, LAST_ID = 0x2f001f;
	}

	public static class Document {
		/** The Close key. */
		public static final int CLOSE = 0x300000;
		/** The New key. */
		public static final int NEW = 0x300001;
		/** The Open key. */
		public static final int OPEN = 0x300002;
		/** The Print key. */
		public static final int PRINT = 0x300003;
		/** The Save key. */
		public static final int SAVE = 0x300004;
		/** The Mail Forward key. */
		public static final int MAIL_FORWARD = 0x300005;
		/** The Mail Reply key. */
		public static final int MAIL_REPLY = 0x300006;
		/** The Mail Send key. */
		public static final int MAIL_SEND = 0x300007;
		/** The Spell Check key. */
		public static final int SPELL_CHECK = 0x300008;

		protected static final int FIRST_ID = 0x300000, LAST_ID = 0x300008;
	}

	protected static final int FIRST_ID = 0x200000, LAST_ID = 0x300008;
	public static void initialize() {
		defineKey(Special.UNDEFINED, "UNDEFINED",  "Undefined",  "UNDEFINED",  "VK_UNDEFINED", "GLFW_KEY_UNKNOWN",  "Special");
		defineKey(Modifier.ALT, "ALT",  "Alt",  "ALT",  "VK_ALT", "GLFW_KEY_LEFT_ALT",  "Modifier");
		defineKey(Modifier.ALT_GRAPH, "ALT_GRAPH",  "AltGraph",  "ALT_GRAPH",  "VK_ALT_GRAPH", null,  "Modifier");
		defineKey(Modifier.CAPS_LOCK, "CAPS_LOCK",  "CapsLock",  "CAPS",  "VK_CAPS_LOCK", "GLFW_KEY_CAPS_LOCK",  "Modifier");
		defineKey(Modifier.CONTROL, "CONTROL",  "Control",  "CONTROL",  "VK_CONTROL", "GLFW_KEY_LEFT_CONTROL",  "Modifier");
		defineKey(Modifier.FN, "FN",  "Fn",  null,  null, null,  "Modifier");
		defineKey(Modifier.META, "META",  null,  "META",  "VK_META", null,  "Modifier");
		defineKey(Modifier.OS, "OS",  "Meta",  "WINDOWS",  "VK_WINDOWS", "GLFW_KEY_LEFT_SUPER",  "Modifier");
		defineKey(Modifier.NUM_LOCK, "NUM_LOCK",  "NumLock",  "NUM_LOCK",  "VK_NUM_LOCK", "GLFW_KEY_NUM_LOCK",  "Modifier");
		defineKey(Modifier.SCROLL_LOCK, "SCROLL_LOCK",  "ScrollLock",  "SCROLL_LOCK",  "VK_SCROLL_LOCK", "GLFW_KEY_SCROLL_LOCK",  "Modifier");
		defineKey(Modifier.SHIFT, "SHIFT",  "Shift",  "SHIFT",  "VK_SHIFT", "GLFW_KEY_LEFT_SHIFT",  "Modifier");
		defineKey(Modifier.SYMBOL, "SYMBOL",  "Symbol",  null,  null, null,  "Modifier");
		defineKey(Whitespace.ENTER, "ENTER",  "Enter",  "ENTER",  "VK_ENTER", "GLFW_KEY_ENTER",  "Whitespace");
		defineKey(Whitespace.TAB, "TAB",  "Tab",  "TAB",  "VK_TAB", "GLFW_KEY_TAB",  "Whitespace");
		defineKey(Navigation.ARROW_DOWN, "ARROW_DOWN",  "ArrowDown",  "DOWN",  "VK_DOWN", "GLFW_KEY_DOWN",  "Navigation");
		defineKey(Navigation.ARROW_LEFT, "ARROW_LEFT",  "ArrowLeft",  "LEFT",  "VK_LEFT", "GLFW_KEY_LEFT",  "Navigation");
		defineKey(Navigation.ARROW_RIGHT, "ARROW_RIGHT",  "ArrowRight",  "RIGHT",  "VK_RIGHT", "GLFW_KEY_RIGHT",  "Navigation");
		defineKey(Navigation.ARROW_UP, "ARROW_UP",  "ArrowUp",  "UP",  "VK_UP", "GLFW_KEY_UP",  "Navigation");
		defineKey(Navigation.END, "END",  "End",  "END",  "VK_END", "GLFW_KEY_END",  "Navigation");
		defineKey(Navigation.HOME, "HOME",  "Home",  "HOME",  "VK_HOME", "GLFW_KEY_HOME",  "Navigation");
		defineKey(Navigation.PAGE_DOWN, "PAGE_DOWN",  "PageDown",  "PAGE_DOWN",  "VK_PAGE_DOWN", "GLFW_KEY_PAGE_DOWN",  "Navigation");
		defineKey(Navigation.PAGE_UP, "PAGE_UP",  "PageUp",  "PAGE_UP",  "VK_PAGE_UP", "GLFW_KEY_PAGE_UP",  "Navigation");
		defineKey(Editing.BACKSPACE, "BACKSPACE",  "Backspace",  "BACK_SPACE",  "VK_BACK_SPACE", "GLFW_KEY_BACKSPACE",  "Editing");
		defineKey(Editing.CLEAR, "CLEAR",  "Clear",  "CLEAR",  "VK_CLEAR", null,  "Editing");
		defineKey(Editing.COPY, "COPY",  "Copy",  "COPY",  "VK_COPY", null,  "Editing");
		defineKey(Editing.CR_SEL, "CR_SEL",  "CrSel",  null,  null, null,  "Editing");
		defineKey(Editing.CUT, "CUT",  "Cut",  "CUT",  "VK_CUT", null,  "Editing");
		defineKey(Editing.DELETE, "DELETE",  "Delete",  "DELETE",  "VK_DELETE", "GLFW_KEY_DELETE",  "Editing");
		defineKey(Editing.ERASE_EOF, "ERASE_EOF",  "EraseEof",  null,  null, null,  "Editing");
		defineKey(Editing.EX_SEL, "EX_SEL",  "ExSel",  null,  null, null,  "Editing");
		defineKey(Editing.INSERT, "INSERT",  "Insert",  "INSERT",  "VK_INSERT", "GLFW_KEY_INSERT",  "Editing");
		defineKey(Editing.PASTE, "PASTE",  "Paste",  "PASTE",  "VK_PASTE", null,  "Editing");
		defineKey(Editing.REDO, "REDO",  "Redo",  "AGAIN",  "VK_AGAIN", null,  "Editing");
		defineKey(Editing.UNDO, "UNDO",  "Undo",  "UNDO",  "VK_UNDO", null,  "Editing");
		defineKey(UI.ACCEPT, "ACCEPT",  "Accept",  "ACCEPT",  "VK_ACCEPT", null,  "UI");
		defineKey(UI.ATTN, "ATTN",  "Attn",  null,  null, null,  "UI");
		defineKey(UI.CANCEL, "CANCEL",  "Cancel",  "CANCEL",  "VK_CANCEL", null,  "UI");
		defineKey(UI.CONTEXT_MENU, "CONTEXT_MENU",  "ContextMenu",  "CONTEXT_MENU",  "VK_CONTEXT_MENU", "GLFW_KEY_MENU",  "UI");
		defineKey(UI.ESCAPE, "ESCAPE",  "Escape",  "ESCAPE",  "VK_ESCAPE", "GLFW_KEY_ESCAPE",  "UI");
		defineKey(UI.EXECUTE, "EXECUTE",  "Execute",  null,  null, null,  "UI");
		defineKey(UI.FIND, "FIND",  "Find",  "FIND",  "VK_FIND", null,  "UI");
		defineKey(UI.HELP, "HELP",  "Help",  "HELP",  "VK_HELP", null,  "UI");
		defineKey(UI.PAUSE, "PAUSE",  "Pause",  null,  null, "GLFW_KEY_PAUSE",  "UI");
		defineKey(UI.PLAY, "PLAY",  "Play",  null,  null, null,  "UI");
		defineKey(UI.SELECT, "SELECT",  "Select",  null,  null, null,  "UI");
		defineKey(UI.ZOOM_IN, "ZOOM_IN",  "ZoomIn",  null,  null, null,  "UI");
		defineKey(UI.ZOOM_OUT, "ZOOM_OUT",  "ZoomOut",  null,  null, null,  "UI");
		defineKey(Device.BRIGHTNESS_DOWN, "BRIGHTNESS_DOWN",  "BrightnessDown",  null,  null, null,  "Device");
		defineKey(Device.BRIGHTNESS_UP, "BRIGHTNESS_UP",  "BrightnessUp",  null,  null, null,  "Device");
		defineKey(Device.EJECT, "EJECT",  "Eject",  "EJECT_TOGGLE",  null, null,  "Device");
		defineKey(Device.LOG_OFF, "LOG_OFF",  "LogOff",  null,  null, null,  "Device");
		defineKey(Device.POWER, "POWER",  "Power",  "POWER",  null, null,  "Device");
		defineKey(Device.POWER_OFF, "POWER_OFF",  "PowerOff",  null,  null, null,  "Device");
		defineKey(Device.PRINT_SCREEN, "PRINT_SCREEN",  "PrintScreen",  "PRINTSCREEN",  "VK_PRINTSCREEN", "GLFW_KEY_PRINT_SCREEN",  "Device");
		defineKey(Device.HIBERNATE, "HIBERNATE",  "Hibernate",  null,  null, null,  "Device");
		defineKey(Device.STANDBY, "STANDBY",  "Standby",  null,  null, null,  "Device");
		defineKey(Device.WAKE_UP, "WAKE_UP",  "WakeUp",  null,  null, null,  "Device");
		defineKey(Composition.ALL_CANDIDATES, "ALL_CANDIDATES",  "AllCandidates",  "ALL_CANDIDATES",  "VK_ALL_CANDIDATES", null,  "Composition");
		defineKey(Composition.ALPHANUMERIC, "ALPHANUMERIC",  "Alphanumeric",  "ALPHANUMERIC",  "VK_ALPHANUMERIC", null,  "Composition");
		defineKey(Composition.CODE_INPUT, "CODE_INPUT",  "CodeInput",  "CODE_INPUT",  "VK_CODE_INPUT", null,  "Composition");
		defineKey(Composition.COMPOSE, "COMPOSE",  "Compose",  "COMPOSE",  "VK_COMPOSE", null,  "Composition");
		defineKey(Composition.CONVERT, "CONVERT",  "Convert",  "CONVERT",  "VK_CONVERT", null,  "Composition");
		defineKey(Composition.DEAD, "DEAD",  "Dead",  null,  null, null,  "Composition");
		defineKey(Composition.FINAL_MODE, "FINAL_MODE",  "FinalMode",  "FINAL",  "VK_FINAL", null,  "Composition");
		defineKey(Composition.GROUP_FIRST, "GROUP_FIRST",  "GroupFirst",  null,  null, null,  "Composition");
		defineKey(Composition.GROUP_LAST, "GROUP_LAST",  "GroupLast",  null,  null, null,  "Composition");
		defineKey(Composition.GROUP_NEXT, "GROUP_NEXT",  "GroupNext",  null,  null, null,  "Composition");
		defineKey(Composition.GROUP_PREVIOUS, "GROUP_PREVIOUS",  "GroupPrevious",  null,  null, null,  "Composition");
		defineKey(Composition.MODE_CHANGE, "MODE_CHANGE",  "ModeChange",  "MODECHANGE",  "VK_MODECHANGE", null,  "Composition");
		defineKey(Composition.NON_CONVERT, "NON_CONVERT",  "NonConvert",  "NONCONVERT",  "VK_NONCONVERT", null,  "Composition");
		defineKey(Composition.PREVIOUS_CANDIDATE, "PREVIOUS_CANDIDATE",  "PreviousCandidate",  "PREVIOUS_CANDIDATE",  "VK_PREVIOUS_CANDIDATE", null,  "Composition");
		defineKey(Composition.PROCESS, "PROCESS",  "Process",  null,  null, null,  "Composition");
		defineKey(Composition.SINGLE_CANDIDATE, "SINGLE_CANDIDATE",  "SingleCandidate",  null,  null, null,  "Composition");
		defineKey(Composition.HANGUL_MODE, "HANGUL_MODE",  "HangulMode",  null,  null, null,  "Composition");
		defineKey(Composition.HANJA_MODE, "HANJA_MODE",  "HanjaMode",  null,  null, null,  "Composition");
		defineKey(Composition.JUNJA_MODE, "JUNJA_MODE",  "JunjaMode",  null,  null, null,  "Composition");
		defineKey(Composition.EISU, "EISU",  "Eisu",  null,  null, null,  "Composition");
		defineKey(Composition.HANKAKU, "HANKAKU",  "Hankaku",  "HALF_WIDTH",  "VK_HALF_WIDTH", null,  "Composition");
		defineKey(Composition.HIRAGANA, "HIRAGANA",  "Hiragana",  "HIRAGANA",  "VK_HIRAGANA", null,  "Composition");
		defineKey(Composition.HIRAGANA_KATAKANA, "HIRAGANA_KATAKANA",  "HiraganaKatakana",  null,  null, null,  "Composition");
		defineKey(Composition.KANA_MODE, "KANA_MODE",  "KanaMode",  "KANA_LOCK",  "VK_KANA_LOCK", null,  "Composition");
		defineKey(Composition.KANJI_MODE, "KANJI_MODE",  "KanjiMode",  "KANJI",  "VK_KANJI", null,  "Composition");
		defineKey(Composition.KATAKANA, "KATAKANA",  "Katakana",  "KATAKANA",  "VK_KATAKANA", null,  "Composition");
		defineKey(Composition.ROMAJI, "ROMAJI",  "Romaji",  "ROMAN_CHARACTERS",  "VK_ROMAN_CHARACTERS", null,  "Composition");
		defineKey(Composition.ZENKAKU, "ZENKAKU",  "Zenkaku",  "FULL_WIDTH",  "VK_FULL_WIDTH", null,  "Composition");
		defineKey(Composition.ZENKAKU_HANKAKU, "ZENKAKU_HANKAKU",  "ZenkakuHankaku",  null,  null, null,  "Composition");
		defineKey(Function.F1, "F1",  "F1",  "F1",  "VK_F1", "GLFW_KEY_F1",  "Function");
		defineKey(Function.F2, "F2",  "F2",  "F2",  "VK_F2", "GLFW_KEY_F2",  "Function");
		defineKey(Function.F3, "F3",  "F3",  "F3",  "VK_F3", "GLFW_KEY_F3",  "Function");
		defineKey(Function.F4, "F4",  "F4",  "F4",  "VK_F4", "GLFW_KEY_F4",  "Function");
		defineKey(Function.F5, "F5",  "F5",  "F5",  "VK_F5", "GLFW_KEY_F5",  "Function");
		defineKey(Function.F6, "F6",  "F6",  "F6",  "VK_F6", "GLFW_KEY_F6",  "Function");
		defineKey(Function.F7, "F7",  "F7",  "F7",  "VK_F7", "GLFW_KEY_F7",  "Function");
		defineKey(Function.F8, "F8",  "F8",  "F8",  "VK_F8", "GLFW_KEY_F8",  "Function");
		defineKey(Function.F9, "F9",  "F9",  "F9",  "VK_F9", "GLFW_KEY_F9",  "Function");
		defineKey(Function.F10, "F10",  "F10",  "F10",  "VK_F10", "GLFW_KEY_F10",  "Function");
		defineKey(Function.F11, "F11",  "F11",  "F11",  "VK_F11", "GLFW_KEY_F11",  "Function");
		defineKey(Function.F12, "F12",  "F12",  "F12",  "VK_F12", "GLFW_KEY_F12",  "Function");
		defineKey(Function.F13, "F13",  "F13",  "F13",  "VK_F13", "GLFW_KEY_F13",  "Function");
		defineKey(Function.F14, "F14",  "F14",  "F14",  "VK_F14", "GLFW_KEY_F14",  "Function");
		defineKey(Function.F15, "F15",  "F15",  "F15",  "VK_F15", "GLFW_KEY_F15",  "Function");
		defineKey(Function.F16, "F16",  "F16",  "F16",  "VK_F16", "GLFW_KEY_F16",  "Function");
		defineKey(Function.F17, "F17",  "F17",  "F17",  "VK_F17", "GLFW_KEY_F17",  "Function");
		defineKey(Function.F18, "F18",  "F18",  "F18",  "VK_F18", "GLFW_KEY_F18",  "Function");
		defineKey(Function.F19, "F19",  "F19",  "F19",  "VK_F19", "GLFW_KEY_F19",  "Function");
		defineKey(Function.F20, "F20",  "F20",  "F20",  "VK_F20", "GLFW_KEY_F20",  "Function");
		defineKey(Function.F21, "F21",  "F21",  "F21",  "VK_F21", "GLFW_KEY_F21",  "Function");
		defineKey(Function.F22, "F22",  "F22",  "F22",  "VK_F22", "GLFW_KEY_F22",  "Function");
		defineKey(Function.F23, "F23",  "F23",  "F23",  "VK_F23", "GLFW_KEY_F23",  "Function");
		defineKey(Function.F24, "F24",  "F24",  "F24",  "VK_F24", "GLFW_KEY_F24",  "Function");
		defineKey(Function.F25, "F25",  "F25",  null,  null, "GLFW_KEY_F25",  "Function");
		defineKey(Function.F26, "F26",  "F26",  null,  null, null,  "Function");
		defineKey(Function.F27, "F27",  "F27",  null,  null, null,  "Function");
		defineKey(Function.F28, "F28",  "F28",  null,  null, null,  "Function");
		defineKey(Function.F29, "F29",  "F29",  null,  null, null,  "Function");
		defineKey(Function.F30, "F30",  "F30",  null,  null, null,  "Function");
		defineKey(Function.F31, "F31",  "F31",  null,  null, null,  "Function");
		defineKey(Function.F32, "F32",  "F32",  null,  null, null,  "Function");
		defineKey(Function.F33, "F33",  "F33",  null,  null, null,  "Function");
		defineKey(Function.F34, "F34",  "F34",  null,  null, null,  "Function");
		defineKey(Function.F35, "F35",  "F35",  null,  null, null,  "Function");
		defineKey(Phone.APP_SWITCH, "APP_SWITCH",  "AppSwitch",  null,  null, null,  "Phone");
		defineKey(Phone.CALL, "CALL",  "Call",  null,  null, null,  "Phone");
		defineKey(Phone.CAMERA, "CAMERA",  "Camera",  null,  null, null,  "Phone");
		defineKey(Phone.CAMERA_FOCUS, "CAMERA_FOCUS",  "CameraFocus",  null,  null, null,  "Phone");
		defineKey(Phone.END_CALL, "END_CALL",  "EndCall",  null,  null, null,  "Phone");
		defineKey(Phone.GO_BACK, "GO_BACK",  "GoBack",  null,  null, null,  "Phone");
		defineKey(Phone.GO_HOME, "GO_HOME",  "GoHome",  null,  null, null,  "Phone");
		defineKey(Phone.HEADSET_HOOK, "HEADSET_HOOK",  "HeadsetHook",  null,  null, null,  "Phone");
		defineKey(Phone.LAST_NUMBER_REDIAL, "LAST_NUMBER_REDIAL",  "LastNumberRedial",  null,  null, null,  "Phone");
		defineKey(Phone.NOTIFICATION, "NOTIFICATION",  "Notification",  null,  null, null,  "Phone");
		defineKey(Phone.MANNER_MODE, "MANNER_MODE",  "MannerMode",  null,  null, null,  "Phone");
		defineKey(Phone.VOICE_DIAL, "VOICE_DIAL",  "VoiceDial",  null,  null, null,  "Phone");
		defineKey(Multimedia.CHANNEL_DOWN, "CHANNEL_DOWN",  "ChannelDown",  "CHANNEL_DOWN",  null, null,  "Multimedia");
		defineKey(Multimedia.CHANNEL_UP, "CHANNEL_UP",  "ChannelUp",  "CHANNEL_UP",  null, null,  "Multimedia");
		defineKey(Multimedia.MEDIA_FAST_FORWARD, "MEDIA_FAST_FORWARD",  "MediaFastForward",  "FAST_FWD",  null, null,  "Multimedia");
		defineKey(Multimedia.MEDIA_PAUSE, "MEDIA_PAUSE",  "MediaPause",  "PAUSE",  "VK_PAUSE", null,  "Multimedia");
		defineKey(Multimedia.MEDIA_PLAY, "MEDIA_PLAY",  "MediaPlay",  "PLAY",  null, null,  "Multimedia");
		defineKey(Multimedia.MEDIA_PLAY_PAUSE, "MEDIA_PLAY_PAUSE",  "MediaPlayPause",  null,  null, null,  "Multimedia");
		defineKey(Multimedia.MEDIA_RECORD, "MEDIA_RECORD",  "MediaRecord",  "RECORD",  null, null,  "Multimedia");
		defineKey(Multimedia.MEDIA_REWIND, "MEDIA_REWIND",  "MediaRewind",  "REWIND",  null, null,  "Multimedia");
		defineKey(Multimedia.MEDIA_STOP, "MEDIA_STOP",  "MediaStop",  "STOP",  "VK_STOP", null,  "Multimedia");
		defineKey(Multimedia.MEDIA_TRACK_NEXT, "MEDIA_TRACK_NEXT",  "MediaTrackNext",  "TRACK_NEXT",  null, null,  "Multimedia");
		defineKey(Multimedia.MEDIA_TRACK_PREVIOUS, "MEDIA_TRACK_PREVIOUS",  "MediaTrackPrevious",  "TRACK_PREV",  null, null,  "Multimedia");
		defineKey(Audio.AUDIO_BALANCE_LEFT, "AUDIO_BALANCE_LEFT",  "AudioBalanceLeft",  null,  null, null,  "Audio");
		defineKey(Audio.AUDIO_BALANCE_RIGHT, "AUDIO_BALANCE_RIGHT",  "AudioBalanceRight",  null,  null, null,  "Audio");
		defineKey(Audio.AUDIO_BASS_DOWN, "AUDIO_BASS_DOWN",  "AudioBassDown",  null,  null, null,  "Audio");
		defineKey(Audio.AUDIO_BASS_UP, "AUDIO_BASS_UP",  "AudioBassUp",  null,  null, null,  "Audio");
		defineKey(Audio.AUDIO_BASS_BOOST_DOWN, "AUDIO_BASS_BOOST_DOWN",  "AudioBassBoostDown",  null,  null, null,  "Audio");
		defineKey(Audio.AUDIO_BASS_BOOST_UP, "AUDIO_BASS_BOOST_UP",  "AudioBassBoostUp",  null,  null, null,  "Audio");
		defineKey(Audio.AUDIO_BASS_BOOST_TOGGLE, "AUDIO_BASS_BOOST_TOGGLE",  "AudioBassBoostToggle",  null,  null, null,  "Audio");
		defineKey(Audio.AUDIO_FADER_FRONT, "AUDIO_FADER_FRONT",  "AudioFaderFront",  null,  null, null,  "Audio");
		defineKey(Audio.AUDIO_FADER_REAR, "AUDIO_FADER_REAR",  "AudioFaderRear",  null,  null, null,  "Audio");
		defineKey(Audio.AUDIO_SURROUND_MODE_NEXT, "AUDIO_SURROUND_MODE_NEXT",  "AudioSurroundModeNext",  null,  null, null,  "Audio");
		defineKey(Audio.AUDIO_TREBLE_DOWN, "AUDIO_TREBLE_DOWN",  "AudioTrebleDown",  null,  null, null,  "Audio");
		defineKey(Audio.AUDIO_TREBLE_UP, "AUDIO_TREBLE_UP",  "AudioTrebleUp",  null,  null, null,  "Audio");
		defineKey(Audio.AUDIO_VOLUME_DOWN, "AUDIO_VOLUME_DOWN",  "AudioVolumeDown",  "VOLUME_DOWN",  null, null,  "Audio");
		defineKey(Audio.AUDIO_VOLUME_UP, "AUDIO_VOLUME_UP",  "AudioVolumeUp",  "VOLUME_UP",  null, null,  "Audio");
		defineKey(Audio.AUDIO_VOLUME_MUTE, "AUDIO_VOLUME_MUTE",  "AudioVolumeMute",  "MUTE",  null, null,  "Audio");
		defineKey(Audio.MICROPHONE_VOLUME_MUTE, "MICROPHONE_VOLUME_MUTE",  "MicrophoneVolumeMute",  null,  null, null,  "Audio");
		defineKey(Audio.MICROPHONE_VOLUME_DOWN, "MICROPHONE_VOLUME_DOWN",  "MicrophoneVolumeDown",  null,  null, null,  "Audio");
		defineKey(Audio.MICROPHONE_VOLUME_UP, "MICROPHONE_VOLUME_UP",  "MicrophoneVolumeUp",  null,  null, null,  "Audio");
		defineKey(Audio.MICROPHONE_TOGGLE, "MICROPHONE_TOGGLE",  "MicrophoneToggle",  null,  null, null,  "Audio");
		defineKey(Launch.LAUNCH_CALCULATOR, "LAUNCH_CALCULATOR",  "LaunchCalculator",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_CALENDAR, "LAUNCH_CALENDAR",  "LaunchCalendar",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_CONTACTS, "LAUNCH_CONTACTS",  "LaunchContacts",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_MAIL, "LAUNCH_MAIL",  "LaunchMail",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_MEDIA_PLAYER, "LAUNCH_MEDIA_PLAYER",  "LaunchMediaPlayer",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_MUSIC_PLAYER, "LAUNCH_MUSIC_PLAYER",  "LaunchMusicPlayer",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_MY_COMPUTER, "LAUNCH_MY_COMPUTER",  "LaunchMyComputer",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_SCREEN_SAVER, "LAUNCH_SCREEN_SAVER",  "LaunchScreenSaver",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_SPREADSHEET, "LAUNCH_SPREADSHEET",  "LaunchSpreadsheet",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_WEB_BROWSER, "LAUNCH_WEB_BROWSER",  "LaunchWebBrowser",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_WEB_CAM, "LAUNCH_WEB_CAM",  "LaunchWebCam",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_WORD_PROCESSOR, "LAUNCH_WORD_PROCESSOR",  "LaunchWordProcessor",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_APPLICATION1, "LAUNCH_APPLICATION1",  "LaunchApplication1",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_APPLICATION2, "LAUNCH_APPLICATION2",  "LaunchApplication2",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_APPLICATION3, "LAUNCH_APPLICATION3",  "LaunchApplication3",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_APPLICATION4, "LAUNCH_APPLICATION4",  "LaunchApplication4",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_APPLICATION5, "LAUNCH_APPLICATION5",  "LaunchApplication5",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_APPLICATION6, "LAUNCH_APPLICATION6",  "LaunchApplication6",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_APPLICATION7, "LAUNCH_APPLICATION7",  "LaunchApplication7",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_APPLICATION8, "LAUNCH_APPLICATION8",  "LaunchApplication8",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_APPLICATION9, "LAUNCH_APPLICATION9",  "LaunchApplication9",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_APPLICATION10, "LAUNCH_APPLICATION10",  "LaunchApplication10",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_APPLICATION11, "LAUNCH_APPLICATION11",  "LaunchApplication11",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_APPLICATION12, "LAUNCH_APPLICATION12",  "LaunchApplication12",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_APPLICATION13, "LAUNCH_APPLICATION13",  "LaunchApplication13",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_APPLICATION14, "LAUNCH_APPLICATION14",  "LaunchApplication14",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_APPLICATION15, "LAUNCH_APPLICATION15",  "LaunchApplication15",  null,  null, null,  "Launch");
		defineKey(Launch.LAUNCH_APPLICATION16, "LAUNCH_APPLICATION16",  "LaunchApplication16",  null,  null, null,  "Launch");
		defineKey(Launch.BROWSER_BACK, "BROWSER_BACK",  "BrowserBack",  null,  null, null,  "Launch");
		defineKey(Launch.BROWSER_FAVORITES, "BROWSER_FAVORITES",  "BrowserFavorites",  null,  null, null,  "Launch");
		defineKey(Launch.BROWSER_FORWARD, "BROWSER_FORWARD",  "BrowserForward",  null,  null, null,  "Launch");
		defineKey(Launch.BROWSER_HOME, "BROWSER_HOME",  "BrowserHome",  null,  null, null,  "Launch");
		defineKey(Launch.BROWSER_REFRESH, "BROWSER_REFRESH",  "BrowserRefresh",  null,  null, null,  "Launch");
		defineKey(Launch.BROWSER_SEARCH, "BROWSER_SEARCH",  "BrowserSearch",  null,  null, null,  "Launch");
		defineKey(Launch.BROWSER_STOP, "BROWSER_STOP",  "BrowserStop",  null,  null, null,  "Launch");
		defineKey(TV.TV, "TV",  "TV",  null,  null, null,  "TV");
		defineKey(TV.TV_3D_MODE, "TV_3D_MODE",  "TV3DMode",  null,  null, null,  "TV");
		defineKey(TV.TV_ANTENNA_CABLE, "TV_ANTENNA_CABLE",  "TVAntennaCable",  null,  null, null,  "TV");
		defineKey(TV.TV_AUDIO_DESCRIPTION, "TV_AUDIO_DESCRIPTION",  "TVAudioDescription",  null,  null, null,  "TV");
		defineKey(TV.TV_AUDIO_DESCRIPTION_MIX_DOWN, "TV_AUDIO_DESCRIPTION_MIX_DOWN",  "TVAudioDescriptionMixDown",  null,  null, null,  "TV");
		defineKey(TV.TV_AUDIO_DESCRIPTION_MIX_UP, "TV_AUDIO_DESCRIPTION_MIX_UP",  "TVAudioDescriptionMixUp",  null,  null, null,  "TV");
		defineKey(TV.TV_CONTENTS_MENU, "TV_CONTENTS_MENU",  "TVContentsMenu",  null,  null, null,  "TV");
		defineKey(TV.TV_DATA_SERVICE, "TV_DATA_SERVICE",  "TVDataService",  null,  null, null,  "TV");
		defineKey(TV.TV_INPUT, "TV_INPUT",  "TVInput",  null,  null, null,  "TV");
		defineKey(TV.TV_INPUT_COMPONENT1, "TV_INPUT_COMPONENT1",  "TVInputComponent1",  null,  null, null,  "TV");
		defineKey(TV.TV_INPUT_COMPONENT2, "TV_INPUT_COMPONENT2",  "TVInputComponent2",  null,  null, null,  "TV");
		defineKey(TV.TV_INPUT_COMPOSITE1, "TV_INPUT_COMPOSITE1",  "TVInputComposite1",  null,  null, null,  "TV");
		defineKey(TV.TV_INPUT_COMPOSITE2, "TV_INPUT_COMPOSITE2",  "TVInputComposite2",  null,  null, null,  "TV");
		defineKey(TV.TV_INPUT_HDMI1, "TV_INPUT_HDMI1",  "TVInputHDMI1",  null,  null, null,  "TV");
		defineKey(TV.TV_INPUT_HDMI2, "TV_INPUT_HDMI2",  "TVInputHDMI2",  null,  null, null,  "TV");
		defineKey(TV.TV_INPUT_HDMI3, "TV_INPUT_HDMI3",  "TVInputHDMI3",  null,  null, null,  "TV");
		defineKey(TV.TV_INPUT_HDMI4, "TV_INPUT_HDMI4",  "TVInputHDMI4",  null,  null, null,  "TV");
		defineKey(TV.TV_INPUT_VGA1, "TV_INPUT_VGA1",  "TVInputVGA1",  null,  null, null,  "TV");
		defineKey(TV.TV_NETWORK, "TV_NETWORK",  "TVNetwork",  null,  null, null,  "TV");
		defineKey(TV.TV_NUMBER_ENTRY, "TV_NUMBER_ENTRY",  "TVNumberEntry",  null,  null, null,  "TV");
		defineKey(TV.TV_POWER, "TV_POWER",  "TVPower",  null,  null, null,  "TV");
		defineKey(TV.TV_RADIO_SERVICE, "TV_RADIO_SERVICE",  "TVRadioService",  null,  null, null,  "TV");
		defineKey(TV.TV_SATELLITE, "TV_SATELLITE",  "TVSatellite",  null,  null, null,  "TV");
		defineKey(TV.TV_SATELLITE_BS, "TV_SATELLITE_BS",  "TVSatelliteBS",  null,  null, null,  "TV");
		defineKey(TV.TV_SATELLITE_CS, "TV_SATELLITE_CS",  "TVSatelliteCS",  null,  null, null,  "TV");
		defineKey(TV.TV_SATELLITE_TOGGLE, "TV_SATELLITE_TOGGLE",  "TVSatelliteToggle",  null,  null, null,  "TV");
		defineKey(TV.TV_TERRESTRIAL_ANALOG, "TV_TERRESTRIAL_ANALOG",  "TVTerrestrialAnalog",  null,  null, null,  "TV");
		defineKey(TV.TV_TERRESTRIAL_DIGITAL, "TV_TERRESTRIAL_DIGITAL",  "TVTerrestrialDigital",  null,  null, null,  "TV");
		defineKey(TV.TV_TIMER, "TV_TIMER",  "TVTimer",  null,  null, null,  "TV");
		defineKey(MediaController.AVR_INPUT, "AVR_INPUT",  "AVRInput",  null,  null, null,  "MediaController");
		defineKey(MediaController.AVR_POWER, "AVR_POWER",  "AVRPower",  null,  null, null,  "MediaController");
		defineKey(MediaController.COLOR_F0_RED, "COLOR_F0_RED",  "ColorF0Red",  "COLORED_KEY_0",  null, null,  "MediaController");
		defineKey(MediaController.COLOR_F1_GREEN, "COLOR_F1_GREEN",  "ColorF1Green",  "COLORED_KEY_1",  null, null,  "MediaController");
		defineKey(MediaController.COLOR_F2_YELLOW, "COLOR_F2_YELLOW",  "ColorF2Yellow",  "COLORED_KEY_2",  null, null,  "MediaController");
		defineKey(MediaController.COLOR_F3_BLUE, "COLOR_F3_BLUE",  "ColorF3Blue",  "COLORED_KEY_3",  null, null,  "MediaController");
		defineKey(MediaController.CLOSED_CAPTION_TOGGLE, "CLOSED_CAPTION_TOGGLE",  "ClosedCaptionToggle",  null,  null, null,  "MediaController");
		defineKey(MediaController.DIMMER, "DIMMER",  "Dimmer",  null,  null, null,  "MediaController");
		defineKey(MediaController.DVR, "DVR",  "DVR",  null,  null, null,  "MediaController");
		defineKey(MediaController.GUIDE, "GUIDE",  "Guide",  null,  null, null,  "MediaController");
		defineKey(MediaController.INFO, "INFO",  "Info",  "INFO",  null, null,  "MediaController");
		defineKey(MediaController.MEDIA_AUDIO_TRACK, "MEDIA_AUDIO_TRACK",  "MediaAudioTrack",  null,  null, null,  "MediaController");
		defineKey(MediaController.MEDIA_LAST, "MEDIA_LAST",  "MediaLast",  null,  null, null,  "MediaController");
		defineKey(MediaController.MEDIA_TOP_MENU, "MEDIA_TOP_MENU",  "MediaTopMenu",  null,  null, null,  "MediaController");
		defineKey(MediaController.MEDIA_SKIP_BACKWARD, "MEDIA_SKIP_BACKWARD",  "MediaSkipBackward",  null,  null, null,  "MediaController");
		defineKey(MediaController.MEDIA_SKIP_FORWARD, "MEDIA_SKIP_FORWARD",  "MediaSkipForward",  null,  null, null,  "MediaController");
		defineKey(MediaController.MEDIA_STEP_BACKWARD, "MEDIA_STEP_BACKWARD",  "MediaStepBackward",  null,  null, null,  "MediaController");
		defineKey(MediaController.MEDIA_STEP_FORWARD, "MEDIA_STEP_FORWARD",  "MediaStepForward",  null,  null, null,  "MediaController");
		defineKey(MediaController.NAVIGATE_IN, "NAVIGATE_IN",  "NavigateIn",  null,  null, null,  "MediaController");
		defineKey(MediaController.NAVIGATE_NEXT, "NAVIGATE_NEXT",  "NavigateNext",  null,  null, null,  "MediaController");
		defineKey(MediaController.NAVIGATE_OUT, "NAVIGATE_OUT",  "NavigateOut",  null,  null, null,  "MediaController");
		defineKey(MediaController.NAVIGATE_PREVIOUS, "NAVIGATE_PREVIOUS",  "NavigatePrevious",  null,  null, null,  "MediaController");
		defineKey(MediaController.PAIRING, "PAIRING",  "Pairing",  null,  null, null,  "MediaController");
		defineKey(MediaController.PIN_P_TOGGLE, "PIN_P_TOGGLE",  "PinPToggle",  null,  null, null,  "MediaController");
		defineKey(MediaController.RANDOM_TOGGLE, "RANDOM_TOGGLE",  "RandomToggle",  null,  null, null,  "MediaController");
		defineKey(MediaController.SETTINGS, "SETTINGS",  "Settings",  null,  null, null,  "MediaController");
		defineKey(MediaController.STB_INPUT, "STB_INPUT",  "STBInput",  null,  null, null,  "MediaController");
		defineKey(MediaController.STB_POWER, "STB_POWER",  "STBPower",  null,  null, null,  "MediaController");
		defineKey(MediaController.SUBTITLE, "SUBTITLE",  "Subtitle",  null,  null, null,  "MediaController");
		defineKey(MediaController.TELETEXT, "TELETEXT",  "Teletext",  null,  null, null,  "MediaController");
		defineKey(MediaController.VIDEO_MODE_NEXT, "VIDEO_MODE_NEXT",  "VideoModeNext",  null,  null, null,  "MediaController");
		defineKey(MediaController.ZOOM_TOGGLE, "ZOOM_TOGGLE",  "ZoomToggle",  null,  null, null,  "MediaController");
		defineKey(Document.CLOSE, "CLOSE",  "Close",  null,  null, null,  "Document");
		defineKey(Document.NEW, "NEW",  "New",  null,  null, null,  "Document");
		defineKey(Document.OPEN, "OPEN",  "Open",  null,  null, null,  "Document");
		defineKey(Document.PRINT, "PRINT",  "Print",  null,  null, null,  "Document");
		defineKey(Document.SAVE, "SAVE",  "Save",  null,  null, null,  "Document");
		defineKey(Document.MAIL_FORWARD, "MAIL_FORWARD",  "MailForward",  null,  null, null,  "Document");
		defineKey(Document.MAIL_REPLY, "MAIL_REPLY",  "MailReply",  null,  null, null,  "Document");
		defineKey(Document.MAIL_SEND, "MAIL_SEND",  "MailSend",  null,  null, null,  "Document");
		defineKey(Document.SPELL_CHECK, "SPELL_CHECK",  "SpellCheck",  null,  null, null,  "Document");



		//		GLFW_KEY_WORLD_1
		//		GLFW_KEY_WORLD_2

		plainKey(Modifier.SHIFT, null, null, "GLFW_KEY_RIGHT_SHIFT"); 
		plainKey(Modifier.CONTROL, null, null, "GLFW_KEY_RIGHT_CONTROL");
		plainKey(Modifier.ALT, null, null, "GLFW_KEY_RIGHT_ALT");
		plainKey(Modifier.OS, null, null, "GLFW_KEY_RIGHT_SUPER");


		plainKey(' ', "SPACE", "VK_SPACE" , "GLFW_KEY_SPACE");
		plainKey(',', "COMMA", "VK_COMMA", "GLFW_KEY_COMMA");
		plainKey('-', "MINUS", "VK_MINUS", "GLFW_KEY_MINUS");
		plainKey('.', "PERIOD", "VK_PERIOD", "GLFW_KEY_PERIOD");
		plainKey('/', "SLASH", "VK_SLASH", "GLFW_KEY_SLASH");
		plainKey('0', "DIGIT0", "VK_0", "GLFW_KEY_0");
		plainKey('1', "DIGIT1", "VK_1", "GLFW_KEY_1");
		plainKey('2', "DIGIT2", "VK_2", "GLFW_KEY_2");
		plainKey('3', "DIGIT3", "VK_3", "GLFW_KEY_3");
		plainKey('4', "DIGIT4", "VK_4", "GLFW_KEY_4");
		plainKey('5', "DIGIT5", "VK_5", "GLFW_KEY_5");
		plainKey('6', "DIGIT6", "VK_6", "GLFW_KEY_6");
		plainKey('7', "DIGIT7", "VK_7", "GLFW_KEY_7");
		plainKey('8', "DIGIT8", "VK_8", "GLFW_KEY_8");
		plainKey('9', "DIGIT9", "VK_9", "GLFW_KEY_9");
		plainKey(';', "SEMICOLON", "VK_SEMICOLON", "GLFW_KEY_SEMICOLON");
		plainKey('=', "EQUALS", "VK_EQUALS", "GLFW_KEY_EQUAL");
		plainKey('A', "A", "VK_A", "GLFW_KEY_A");
		plainKey('B', "B", "VK_B", "GLFW_KEY_B");
		plainKey('C', "C", "VK_C", "GLFW_KEY_C");
		plainKey('D', "D", "VK_D", "GLFW_KEY_D");
		plainKey('E', "E", "VK_E", "GLFW_KEY_E");
		plainKey('F', "F", "VK_F", "GLFW_KEY_F");
		plainKey('G', "G", "VK_G", "GLFW_KEY_G");
		plainKey('H', "H", "VK_H", "GLFW_KEY_H");
		plainKey('I', "I", "VK_I", "GLFW_KEY_I");
		plainKey('J', "J", "VK_J", "GLFW_KEY_J");
		plainKey('K', "K", "VK_K", "GLFW_KEY_K");
		plainKey('L', "L", "VK_L", "GLFW_KEY_L");
		plainKey('M', "M", "VK_M", "GLFW_KEY_M");
		plainKey('N', "N", "VK_N", "GLFW_KEY_N");
		plainKey('O', "O", "VK_O", "GLFW_KEY_O");
		plainKey('P', "P", "VK_P", "GLFW_KEY_P");
		plainKey('Q', "Q", "VK_Q", "GLFW_KEY_Q");
		plainKey('R', "R", "VK_R", "GLFW_KEY_R");
		plainKey('S', "S", "VK_S", "GLFW_KEY_S");
		plainKey('T', "T", "VK_T", "GLFW_KEY_T");
		plainKey('U', "U", "VK_U", "GLFW_KEY_U");
		plainKey('V', "V", "VK_V", "GLFW_KEY_V");
		plainKey('W', "W", "VK_W", "GLFW_KEY_W");
		plainKey('X', "X", "VK_X", "GLFW_KEY_X");
		plainKey('Y', "Y", "VK_Y", "GLFW_KEY_Y");
		plainKey('Z', "Z", "VK_Z", "GLFW_KEY_Z");
		plainKey('[', "OPEN_BRACKET", "VK_OPEN_BRACKET", "GLFW_KEY_LEFT_BRACKET");
		plainKey('\\', "BACK_SLASH", "VK_BACK_SLASH", "GLFW_KEY_BACKSLASH");
		plainKey(']', "CLOSE_BRACKET", "VK_CLOSE_BRACKET", "GLFW_KEY_RIGHT_BRACKET");
		plainKey('0', "NUMPAD0", "VK_NUMPAD0", "GLFW_KEY_KP_0",  KeyEvent.KEY_TYPE_KEYPAD);
		plainKey('1', "NUMPAD1", "VK_NUMPAD1", "GLFW_KEY_KP_1", KeyEvent.KEY_TYPE_KEYPAD);
		plainKey('2', "NUMPAD2", "VK_NUMPAD2", "GLFW_KEY_KP_2", KeyEvent.KEY_TYPE_KEYPAD);
		plainKey('3', "NUMPAD3", "VK_NUMPAD3", "GLFW_KEY_KP_3", KeyEvent.KEY_TYPE_KEYPAD);
		plainKey('4', "NUMPAD4", "VK_NUMPAD4", "GLFW_KEY_KP_4", KeyEvent.KEY_TYPE_KEYPAD);
		plainKey('5', "NUMPAD5", "VK_NUMPAD5", "GLFW_KEY_KP_5", KeyEvent.KEY_TYPE_KEYPAD);
		plainKey('6', "NUMPAD6", "VK_NUMPAD6", "GLFW_KEY_KP_6", KeyEvent.KEY_TYPE_KEYPAD);
		plainKey('7', "NUMPAD7", "VK_NUMPAD7", "GLFW_KEY_KP_7", KeyEvent.KEY_TYPE_KEYPAD);
		plainKey('8', "NUMPAD8", "VK_NUMPAD8", "GLFW_KEY_KP_8", KeyEvent.KEY_TYPE_KEYPAD);
		plainKey('9', "NUMPAD9", "VK_NUMPAD9", "GLFW_KEY_KP_9", KeyEvent.KEY_TYPE_KEYPAD);
		plainKey(Whitespace.ENTER, null, null, "GLFW_KEY_KP_ENTER", KeyEvent.KEY_TYPE_KEYPAD);
		plainKey('=', null, null, "GLFW_KEY_KP_EQUAL", KeyEvent.KEY_TYPE_KEYPAD);
		plainKey('*', "MULTIPLY", "VK_MULTIPLY", "GLFW_KEY_KP_MULTIPLY", KeyEvent.KEY_TYPE_KEYPAD);
		plainKey('+', "ADD", "VK_ADD", "GLFW_KEY_KP_ADD",KeyEvent.KEY_TYPE_KEYPAD);
		//			plainKey("SEPARATOR", "VK_SEPARATOR", KeyEvent.KEY_TYPE_KEYPAD); // , or .
		plainKey('-', "SUBTRACT", "VK_SUBTRACT", "GLFW_KEY_KP_SUBTRACT", KeyEvent.KEY_TYPE_KEYPAD);
		plainKey('.', null, "VK_DECIMAL", "GLFW_KEY_KP_DECIMAL", KeyEvent.KEY_TYPE_KEYPAD); // . or ,
		plainKey('/', "DIVIDE", "VK_DIVIDE", "GLFW_KEY_KP_DIVIDE", KeyEvent.KEY_TYPE_KEYPAD);
		plainKey('`', "BACK_QUOTE", "VK_BACK_QUOTE", "GLFW_KEY_GRAVE_ACCENT");
		plainKey('\'', "QUOTE", "VK_QUOTE", "GLFW_KEY_APOSTROPHE");
		plainKey(Navigation.ARROW_UP, "KP_UP", "VK_KP_UP", null, KeyEvent.KEY_TYPE_KEYPAD);
		plainKey(Navigation.ARROW_DOWN, "KP_DOWN", "VK_KP_DOWN", null, KeyEvent.KEY_TYPE_KEYPAD);
		plainKey(Navigation.ARROW_LEFT, "KP_LEFT", "VK_KP_LEFT", null, KeyEvent.KEY_TYPE_KEYPAD);
		plainKey(Navigation.ARROW_RIGHT, "KP_RIGHT", "VK_KP_RIGHT", null, KeyEvent.KEY_TYPE_KEYPAD);
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
		plainKey('&',"AMPERSAND", "VK_AMPERSAND", null);
		plainKey('*', "ASTERISK", "VK_ASTERISK", null);
		plainKey('"', "QUOTEDBL", "VK_QUOTEDBL", null);
		plainKey('<', "LESS", "VK_LESS", null);
		plainKey('>', "GREATER", "VK_GREATER", null);
		plainKey('{', "BRACELEFT", "VK_BRACELEFT", null);
		plainKey('}', "BRACERIGHT", "VK_BRACERIGHT", null);
		plainKey('@', "AT", "VK_AT", null);
		plainKey(':', "COLON", "VK_COLON", null);
		plainKey('^', "CIRCUMFLEX", "VK_CIRCUMFLEX", null);
		plainKey('$', "DOLLAR", "VK_DOLLAR", null);
		plainKey('€', "EURO_SIGN", "VK_EURO_SIGN", null);
		plainKey('!', "EXCLAMATION_MARK", "VK_EXCLAMATION_MARK", null);
		plainKey('¡', "INVERTED_EXCLAMATION_MARK", "VK_INVERTED_EXCLAMATION_MARK", null);
		plainKey('(', "LEFT_PARENTHESIS", "VK_LEFT_PARENTHESIS", null);
		plainKey('#', "NUMBER_SIGN", "VK_NUMBER_SIGN", null);
		plainKey('+', "PLUS", "VK_PLUS", null);
		plainKey(')', "RIGHT_PARENTHESIS", "VK_RIGHT_PARENTHESIS", null);
		plainKey('_', "UNDERSCORE", "VK_UNDERSCORE", null);
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
		public static final Map<String,Integer> GLFW_MAP = new HashMap<>(); 
		public static final Map<String,Integer> NAME_MAP = new HashMap<>(); 
		public static final Map<Integer,String> CODE_MAP = new HashMap<>(); 
		public static final Map<Integer,String> CATEGORIES = new HashMap<>(); 
	}
	
	public static void main(String[] args) {
		initialize();
		
		writeMapping("JSKeyCodes", Mappings.JS_MAP);
	}

	private static void writeMapping(String classname, Map<String, Integer> jsMap) {
		try(PrintStream out = new PrintStream("src/main/java/turtleduck/events/" + classname + ".java")) {
			out.print("package turtleduck.events;\n");
			out.printf("public class %s {\n", classname);
			out.print("\tpublic static int toKeyCode(String name) {\n");
			out.print("\t\tswitch(name) {\n");
			for(Entry<String, Integer> entry : jsMap.entrySet()) {
				out.printf("\t\tcase \"%s\": return 0x%06x;\n", entry.getKey(), entry.getValue());
			}
			out.printf("\t\tdefault: return 0x%06x;\n", Special.UNDEFINED);
			out.print("\t\t}\n\t}\n}\n");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static int nextKey = -1;
	protected static void jsAlias(String altJsName, String jsName) {
		Mappings.JS_MAP.put(altJsName, Mappings.JS_MAP.getOrDefault(jsName, jsName.codePointAt(0)));
	}
	protected static void plainKey(int code, String fxName, String awtName, String glName) {
		if(fxName != null)
			Mappings.FX_MAP.put(fxName, code);
		if(awtName != null)
			Mappings.AWT_MAP.put(awtName, code);
		if(glName != null)
			Mappings.AWT_MAP.put(glName, code);
	}
	protected static void plainKey(int code, String fxName, String awtName, String glName, int flags) {
		if(fxName != null)
			Mappings.FX_MAP.put(fxName, code|flags);
		if(awtName != null)
			Mappings.AWT_MAP.put(awtName, code|flags);
		if(glName != null)
			Mappings.AWT_MAP.put(glName, code|flags);
	}
	protected static int defineKey(int code, String name, String jsName, String fxName, String awtName, String glName, String category) {
		Mappings.NAME_MAP.put(name, code);
		Mappings.CODE_MAP.put(code, name);
		if(jsName != null)
			Mappings.JS_MAP.put(jsName, code);
		if(fxName != null)
			Mappings.FX_MAP.put(fxName, code);
		if(glName != null)
			Mappings.GLFW_MAP.put(glName, code);
		if(awtName != null)
			Mappings.AWT_MAP.put(awtName, code);
		Mappings.CATEGORIES.put(code, category);
		return code;
	}
	
	public static String keyName(int code) {
		return keyName(code, null);
	}
	
	public static String keyName(int code, String defaultValue) {
		if(Mappings.CODE_MAP.isEmpty())
			initialize();
		return Mappings.CODE_MAP.getOrDefault(code, defaultValue);
	}
}