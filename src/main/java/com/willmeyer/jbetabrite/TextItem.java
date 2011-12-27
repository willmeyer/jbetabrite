/**
 * 
 */
package com.willmeyer.jbetabrite;

public final class TextItem {

	public String text = "<not set>";
	public int mode = Protocol.TextModes.HOLD;
	
	public TextItem() {}
	
	public TextItem(String msg, byte textMode) {
		text = msg;
		mode = textMode;
	}
	
	public byte getTextMode() {
		return (byte)mode;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String newText) {
		text = newText;
	}

}