package com.willmeyer.jbetabrite;

import java.util.*;

public abstract class Protocol {

	public static final byte SOH = 0x01;
	public static final byte ESC = 0x1B;
	public static final byte EOT = 0x04; 
	public static final byte NUL = 0x00; 
	public static final byte STX = 0x02;
	public static final byte TYPECODE = 0x5e; 
	
	public static abstract class TextModes {
		
		public static final byte ROTATE = 0x61;
		public static final byte HOLD = 0x62;
		public static final byte FLASH = 0x63;
		public static final byte ROLL_UP = 0x65;
		public static final byte ROLL_DOWN = 0x66;
		public static final byte ROLL_LEFT = 0x67;
		public static final byte ROLL_RIGHT = 0x68;
		public static final byte WIPE_UP = 0x69;
		public static final byte WIPE_DOWN = 0x6a;
		public static final byte WIPE_LEFT = 0x6b;
		public static final byte WIPE_RIGHT = 0x6c;
		public static final byte SCROLL = 0x6d;
		public static final byte AUTOMODE = 0x6f;
		public static final byte ROLL_IN = 0x70;
		public static final byte ROLL_OUT = 0x71;
		public static final byte WIPE_IN = 0x72;
		public static final byte WIPE_OUT = 0x73;
		public static final byte COMPRESSED_RATE = 0x74;
		public static final byte EXPLODE = 0x75;
		public static final byte CLOCK = 0x76;
		//public static final byte SPECIAL = 0x6e; // needs additional code in this case
	}
	
	/**
	 * The abstract base of any individual protocol command.  Handles basic packet formatting.
	 */
	public static abstract class CommandBase {
		
		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}

		private static byte m_cmdCode;

		public CommandBase(byte cmdCode) {
			m_cmdCode = cmdCode;
		}
		
		
		public final byte[] toPacket(byte typeCode, byte signAddress) {
			byte[] preDataField = new byte[11];
			preDataField[0] = NUL;
			preDataField[1] = NUL;
			preDataField[2] = NUL;
			preDataField[3] = NUL;
			preDataField[4] = NUL;
			preDataField[5] = SOH;
			preDataField[6] = TYPECODE;
			preDataField[7] = '0'; // sign addr byte 0
			preDataField[8] = '?'; // sign addr byte 1
			preDataField[9] = STX;
			preDataField[10] = m_cmdCode;
			byte[] dataField = this.buildDataField();
			byte[] packet = new byte[dataField.length + 11 + 1];
			int i;
			for (i = 0; i < 11; i++) {
				packet[i] = preDataField[i];
			}
			for (i = 0; i < dataField.length; i++) {
				packet[11 + i] = dataField[i];
			}
			packet[11+i] = EOT;
			return packet;
		}
		
		protected abstract byte[] buildDataField();
		
	}
	
	public static class WriteTextFile extends CommandBase {

		@Override
		public String toString() {
			return super.toString() + " -> \"" + m_text + "\"";
		}

		private byte m_file;
		private String m_text;
		private byte m_displayPos = ' ';
		private byte m_modeCode = Protocol.TextModes.HOLD;
		private byte m_specialSpecifier = ' '; // NOOP
		
		public WriteTextFile(byte file, String text, byte modeCode) {
			super((byte)0x41);
			m_file = file;
			m_text = text;
			m_modeCode = modeCode;
		}

		private boolean expandTags() {
			boolean tagsFound = false;
			
			// Grab the first open tag
			int idx1;
			if ((idx1 = m_text.indexOf("<")) != -1) {
				int idx2 = m_text.indexOf('>', idx1);
				if ((idx2 != -1) && (idx2 > idx1)) {
					
					// Got a tag
					String tag = m_text.substring(idx1+1, idx2);
					String name = null;
					String val = null;
					if ((idx1 = tag.indexOf('=')) != -1) {
						name = tag.substring(0, idx1);
						val = tag.substring(idx1+1);
					} else {
						name = tag;
					}
					
					// Handle the tag
					String replacement = null;
					if (name.equals("color")) {
						if (val.equals("red")) {
							replacement = (char)0x1c + "1";
						} else if (val.equals("green")) {
							replacement = (char)0x1c + "2";
						} else if (val.equals("amber")) {
							replacement = (char)0x1c + "3";
						} else if (val.equals("dimred")) {
							replacement = (char)0x1c + "4";
						} else if (val.equals("dimgreen")) {
							replacement = (char)0x1c + "5";
						} else if (val.equals("brown")) {
							replacement = (char)0x1c + "6";
						} else if (val.equals("orange")) {
							replacement = (char)0x1c + "7";
						} else if (val.equals("yellow")) {
							replacement = (char)0x1c + "8";
						} else if (val.equals("rainbow1")) {
							replacement = (char)0x1c + "9";
						} else if (val.equals("rainbow2")) {
							replacement = (char)0x1c + "A";
						} else if (val.equals("colormix")) {
							replacement = (char)0x1c + "B";
						} else if (val.equals("autocolor")) {
							replacement = (char)0x1c + "C";
						}
					} else if (name.equals("speed")) {
						if (val.equals("1")) {
							replacement = (char)0x15 + "";
						} else if (val.equals("2")) {
							replacement = (char)0x16 + "";
						} else if (val.equals("3")) {
							replacement = (char)0x17 + "";
						} else if (val.equals("4")) {
							replacement = (char)0x18 + "";
						} else if (val.equals("5")) {
							replacement = (char)0x19 + "";
						}
					} else if (name.equals("font")) {
						if (val.equals("1")) {
							replacement = (char)0x1a + "1";
						} else if (val.equals("2")) {
							replacement = (char)0x1a + "2";
						} else if (val.equals("3")) {
							replacement = (char)0x1a + "3";
						} else if (val.equals("4")) {
							replacement = (char)0x1a + "4";
						} else if (val.equals("5")) {
							replacement = (char)0x1a + "5";
						} else if (val.equals("6")) {
							replacement = (char)0x1a + "6";
						} else if (val.equals("7")) {
							replacement = (char)0x1a + "7";
						} else if (val.equals("8")) {
							replacement = (char)0x1a + "8";
						} else if (val.equals("9")) {
							replacement = (char)0x1a + "9";
						} else if (val.equals("10")) {
							replacement = (char)0x1a + ":";
						} else if (val.equals("11")) {
							replacement = (char)0x1a + ";";
						} else if (val.equals("12")) {
							replacement = (char)0x1a + "<";
						} else if (val.equals("13")) {
							replacement = (char)0x1a + "=";
						} else if (val.equals("14")) {
							replacement = (char)0x1a + ">";
						}
					} else {
					}
					
					// Replace the tag
					if (replacement != null) {
						m_text = m_text.replace("<" + tag + ">", replacement);
						tagsFound = true;
					}
				}
			}
			if (tagsFound) {
				return this.expandTags();
			} else {
				return false;
			}
		}
		
		@Override
		public byte[] buildDataField() {
			this.expandTags();
			byte[] preText = new byte[5];
			preText[0] = m_file;
			preText[1] = ESC;
			preText[2] = m_displayPos;
			preText[3] = m_modeCode;
			preText[4] = m_specialSpecifier;
			byte[] text = m_text.getBytes();
			byte[] dataField = new byte[text.length + 5];
			int i = 0;
			for (i = 0; i < 5; i++) {
				dataField[i] = preText[i];
			}
			for (i = 0; i < text.length; i++) {
				dataField[5+i] = text[i];
			}
			return dataField;
		}
		
	}

	public static abstract class WriteSpecialFunction extends CommandBase {

		private byte m_specialLabel = 0;
		
		public WriteSpecialFunction(byte specialLabel) {
			super((byte)0x45);
			m_specialLabel = specialLabel;
		}
		
		@Override
		public byte[] buildDataField() {
			byte[] specialFunctionDataField = this.buildSpecialFunctionDataField(); 
			byte[] dataField = new byte[specialFunctionDataField.length + 1];
			dataField[0] = m_specialLabel;
			for (int i = 0; i < specialFunctionDataField.length; i++)
				dataField[1 + i] = specialFunctionDataField[i];
			return dataField;
		}
		
		public abstract byte[] buildSpecialFunctionDataField();

	}
	
	public static final class MemFileInfo {
		public static final byte TYPE_TEXT = 0x41;
		public static final byte TYPE_STRING = 0x42;
		public static final byte TYPE_DOTS = 0x43;

		private byte m_fileLabel;
		private byte m_fileType;
		private int m_sizeB = 0;

		public MemFileInfo(byte fileLabel, byte fileType, int sizeB) {
			m_fileLabel = fileLabel;
			m_fileType = fileType;
			m_sizeB = sizeB;
		}
	}
	
	public static final class SetMemoryConfig extends WriteSpecialFunction {

		private static final byte KYBD_UNLOCKED = 0x55;
		private static final byte KYBD_LOCKED = 0x4C;

		private ArrayList<MemFileInfo> m_files = new ArrayList<MemFileInfo>();
		
		public SetMemoryConfig(ArrayList<MemFileInfo> files) {
			super((byte)0x24);
			assert files != null;
			m_files = files;
		}
		
		public SetMemoryConfig(MemFileInfo file) {
			super((byte)0x24);
			assert file != null;
			m_files = new ArrayList<MemFileInfo>();
			m_files.add(file);
		}
		
		@Override
		public byte[] buildSpecialFunctionDataField() {
			byte[] packet = new byte[m_files.size() * 11];
			int i = 0;
			for (MemFileInfo file : m_files) {
				byte SIZE[] = {'0', '5', '0', '0'};
				byte QQQQ[] = {'F', 'F', '0', '0'}; // start time ALWAYS
				packet[i+0] = file.m_fileLabel;
				packet[i+1] = file.m_fileType;
				packet[i+2] = KYBD_LOCKED;
				int j;
				for (j = 0; j < 4; j++) {
					packet[i + 3 + j] = SIZE[j];
					packet[i + 7 + j] = QQQQ[j];
				}
				i += 11;
			}
			return packet;
		}
	}
	
	public static final class ClearMemory extends WriteSpecialFunction {

		public ClearMemory() {
			super((byte)0x24);
		}
		
		@Override
		public byte[] buildSpecialFunctionDataField() {
			return new byte[] {'E', '$'};
		}
		
	}
}
