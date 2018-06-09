package commandLine;

import org.w3c.dom.Document;

import serialInterface.SerialRainforestCommunications;
import xmlCommunications.RainforestCommunications;

public class RainforestEMU2 {

	static RainforestCommunications callback;

	private static SerialRainforestCommunications serialCommunication;

	public static void main(String[] args) {
		try {
			serialCommunication = new SerialRainforestCommunications(new RainforestEMU2Callback());
			serialCommunication.run();

			String xmlCommand = "<Command><Name>get_instantaneous_demand</Name><Refresh>N</Refresh></Command>";
			serialCommunication.sendCommandXML(xmlCommand);

			synchronized (serialCommunication) {
				serialCommunication.wait();
			}
		} catch (Exception e) {
			e.printStackTrace();
			callback.onShutdown(e);
		} finally {
			serialCommunication.shutDown();
		}
	}

	static class RainforestEMU2Callback implements RainforestCommunications {

		public RainforestEMU2Callback() {
		}

		public void readReplyXML(String xmlData) {
			System.out.println("Read:\n" + xmlData);
		}

		public void onShutdown(Exception e) {
			System.out.println("Bailing on error : " + e.getMessage());
			e.printStackTrace();
			serialCommunication.shutDown();
			// We die, any last requests?
		}

		@Override
		public void onNonFatalException(Exception e) {
			e.printStackTrace();
		}

	}
}
