package org.cattech.rainforestEMU2.commandLine;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.cattech.rainforestEMU2.serialInterface.SerialRainforestCommunications;
import org.cattech.rainforestEMU2.xmlCommunications.RainforestCommunicationsInterface;
import org.cattech.rainforestEMU2.xmlCommunications.RainforestTranslate;
import org.xml.sax.SAXException;

public class RainforestEMU2 {

	static RainforestCommunicationsInterface callback;
	static Logger log = Logger.getLogger(RainforestEMU2.class.getName());

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

	static class RainforestEMU2Callback implements RainforestCommunicationsInterface {

		public RainforestEMU2Callback() {
		}

		public void readReplyXML(String xmlData) {
			log.debug("Read:\n" + xmlData);

			// Processing the XML data into something a little bit more useful.
			String json = "";
			try {
				json = RainforestTranslate.toHumanReadableJson(xmlData).toString();
			} catch (SAXException | IOException | ParserConfigurationException e) {
				json = "{'error':'" + e.getMessage() + "'}";
				e.printStackTrace();
			}
			System.out.println(json);
		}

		public void onShutdown(Exception e) {
			if (null != e) {
				log.debug("Bailing on error : " + e.getMessage());
				e.printStackTrace();
			}
			serialCommunication.shutDown();
			// We die, any last requests?
		}

		public void onNonFatalException(Exception e) {
			e.printStackTrace();
		}

	}
}
