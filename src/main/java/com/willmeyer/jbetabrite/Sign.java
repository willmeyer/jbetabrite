package com.willmeyer.jbetabrite;

import java.io.*;
import java.util.*;

import com.willmeyer.jrs232.*;

import org.slf4j.*;

public final class Sign extends Rs232Device {

	protected final Logger logger = LoggerFactory.getLogger(Sign.class);
	
	public synchronized void writeMessageSet(ArrayList<TextItem> messages) throws Exception {
		ArrayList<Protocol.MemFileInfo> infos = new ArrayList<Protocol.MemFileInfo>();
		char label = 'A';
		for (TextItem msg : messages) {
			Protocol.MemFileInfo info = new Protocol.MemFileInfo((byte)label, Protocol.MemFileInfo.TYPE_TEXT, msg.text.length());
			infos.add(info);
			label += 1;
		}
		Protocol.SetMemoryConfig mem = new Protocol.SetMemoryConfig(infos);
		sendPacket(mem);
		label = 'A';
		for (TextItem msg : messages) {
			Thread.sleep(400);
			Protocol.WriteTextFile text = new Protocol.WriteTextFile((byte)label, msg.getText(), msg.getTextMode());
			sendPacket(text);
			label++;
		}
	}

	public void writeSingleMessage(String message) throws Exception {
		this.writeSingleMessage(new TextItem(message, Protocol.TextModes.HOLD));
	}

	public synchronized void writeSingleMessage(TextItem message) throws Exception {
		clearMemory();
		Protocol.SetMemoryConfig mem = new Protocol.SetMemoryConfig(new Protocol.MemFileInfo((byte)'A', Protocol.MemFileInfo.TYPE_TEXT, message.text.length()));
		sendPacket(mem);
		Thread.sleep(1000);
		Protocol.WriteTextFile text = new Protocol.WriteTextFile((byte)'A', message.getText(), message.getTextMode());
		sendPacket(text);
	}

	protected void testShit() throws Exception {
		
		// Write memory config
		byte[] mem = {
		(byte)0x00,		
		(byte)0x00,		
		(byte)0x00,		
		(byte)0x00,		
		(byte)0x00,		
		Protocol.SOH,
		Protocol.TYPECODE,
		'0',
		'?',
		Protocol.STX,
		(byte)0x45, // special
		(byte)0x24, // mem config
		'A', // F
		0x41, // T
		0x55, // P
		'0', // S
		'5', // I
		'0', // Z
		'0', // E
		(byte)'F', // Q
		(byte)'F', // Q
		(byte)'0', // Q
		(byte)'0', // Q
		'B', // F
		0x41, // T
		0x55, // P
		'0', // S
		'5', // I
		'0', // Z
		'0', // E
		(byte)'F', // Q
		(byte)'F', // Q
		(byte)'0', // Q
		(byte)'0', // Q
		Protocol.EOT
		};
		Sign.echoBytes(mem);
		this.sendBytes(mem);
		Thread.sleep(1000);
		Protocol.WriteTextFile text = new Protocol.WriteTextFile((byte)'A', "File A", Protocol.TextModes.HOLD);
		sendPacket(text);
		Thread.sleep(1000);
		Protocol.WriteTextFile textB = new Protocol.WriteTextFile((byte)'B', "File B", Protocol.TextModes.HOLD);
		sendPacket(textB);
	}
	
	public void clearMemory() throws Exception {
		Protocol.ClearMemory clear = new Protocol.ClearMemory();
		sendPacket(clear);
	}

	private static String echoBytes(byte[] bytes) {
		String str = "BYTES: ";
		for (int i = 0; i < bytes.length; i++) {
			str += "\r\n" + i + ": 0x" +  String.format("%1$h", bytes[i]) + " -> " + (char)bytes[i] + " ";
		}
		return str;
	}
	
	private void sendPacket(Protocol.CommandBase cmd) throws IOException {
		byte[] packet = cmd.toPacket((byte)'s', (byte)'a');
		String msg = "Writing command: " +  cmd.toString();
		logger.debug(msg);
		this.sendBytes(packet);
	}
	
	/**
	 * 
	 * @param portName The COM port name, or MOCK for dummy
	 */
	public Sign(String portName) throws Exception {
		super(portName, 9600, false);
	}

	public static void main(String[] params) {
		Sign sign = null;
		try {
			sign = new Sign("COM13");
			sign.connect();
			
			//sign.clearMemory();
			
			//sign.writeSingleMessage("This is a test...");
			//String msg = "<color=yellow>This is a test...<color=green>FOO!";
			//sign.writeSingleMessage(new TextItem(msg, Protocol.TextModes.ROLL_DOWN));
			
			ArrayList<TextItem> msgs = new ArrayList<TextItem>();
			msgs.add(new TextItem("<color=green><speed=1><font=6>This is the first message", Protocol.TextModes.ROLL_DOWN));
			msgs.add(new TextItem("<color=red><speed=3><font=9>And this is the second...", Protocol.TextModes.ROLL_LEFT));
			for (int i = 0; i < 50; i++) {
				msgs.add(new TextItem("Test " + i + "...", Protocol.TextModes.ROLL_LEFT));
			}
			sign.writeMessageSet(msgs);
			
			//sign.testShit();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (sign != null)
			sign.disconnect();
	}
}
