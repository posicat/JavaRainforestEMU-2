package xmlCommunications;

public interface RainforestCommunications {

	abstract public void readReplyXML(String string);
	public abstract void onShutdown(Exception e);
	public abstract void onNonFatalException(Exception e);
	
}
