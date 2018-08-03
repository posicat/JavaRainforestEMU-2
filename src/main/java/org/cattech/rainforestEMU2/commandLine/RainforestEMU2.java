package org.cattech.rainforestEMU2.commandLine;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.cattech.rainforestEMU2.serialInterface.SerialRainforestCommunications;
import org.cattech.rainforestEMU2.xmlCommunications.RainforestCommunicationsInterface;
import org.cattech.rainforestEMU2.xmlCommunications.RainforestTranslate;
import org.xml.sax.SAXException;

public class RainforestEMU2 {

	static RainforestCommunicationsInterface callback;
	static Logger log = Logger.getLogger(RainforestEMU2.class.getName());

	private static SerialRainforestCommunications serialCommunication;
	private static String port;
	private static String connect;

	public static void main(String[] args) {
		
		parseCommandLine(args);
		
		try {
			serialCommunication = new SerialRainforestCommunications(port,new RainforestEMU2Callback());
			serialCommunication.run();

			serialCommunication.clearSchedule();
			
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

	protected static void parseCommandLine(String[] args) {
		Options options = new Options();
		
		Option optPort = new Option("p","port",true,"Serial port name");
		Option optConnect = new Option("c","connect",true,"Connect to datahub ip:port");
		
		optPort.setRequired(true);
		options.addOption(optPort);

		optConnect.setRequired(false);
		options.addOption(optConnect);
		
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;
		
		try {
			cmd = parser.parse(options,args);
		} catch(ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("RainforestEMU2", options);
			System.exit(1);
		}

		// Locally : -p /dev/ttyACM0
		
		port = cmd.getOptionValue(optPort.getLongOpt());
		connect = cmd.getOptionValue(optConnect.getLongOpt());
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
