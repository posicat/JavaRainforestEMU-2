package org.cattech.rainforestEMU2.xmlCommunications;

public interface RainforestCommunicationsInterface {

	abstract public void readReplyXML(String string);
	public abstract void onShutdown(Exception e);
	public abstract void onNonFatalException(Exception e);
	
}
