package org.cattech.rainforestEMU2.serialInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.cattech.rainforestEMU2.xmlCommunications.RainforestAPICommand;
import org.cattech.rainforestEMU2.xmlCommunications.RainforestCommunicationsInterface;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class SerialRainforestCommunications implements Runnable {

	static Logger log = Logger.getLogger(SerialRainforestCommunications.class.getName());

	private RainforestCommunicationsInterface callback;
	private CommPort commPort;
	private StringBuilder xmlData = new StringBuilder();
	private InputStream in;
	private OutputStream out;
	private String firstLine;
	private String serialDevice;
	public volatile boolean running = false;

	public SerialRainforestCommunications(String serialDevice, RainforestCommunicationsInterface callback) {
		this.serialDevice = serialDevice;
		this.callback = callback;
		this.running = true;
	}

	public void run() {
		/**
		 * Some of this code borrowed and expanded on from the example code on serial
		 * port listener.
		 * http://rxtx.qbang.org/wiki/index.php/Event_based_two_way_Communication
		 */

		try {

			System.setProperty("gnu.io.rxtx.SerialPorts", serialDevice); // Enable the port passed in.

			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(serialDevice);

			if (portIdentifier.isCurrentlyOwned()) {
				log.debug("Error: Port is currently in use");
			} else {
				commPort = portIdentifier.open(this.getClass().getName(), 2000);

				if (commPort instanceof SerialPort) {
					SerialPort serialPort = (SerialPort) commPort;
					serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

					in = serialPort.getInputStream();
					out = serialPort.getOutputStream();

					(new Thread(new SerialWriter(out))).start();

					serialPort.addEventListener(new SerialReader(in));
					serialPort.notifyOnDataAvailable(true);

				} else {
					log.debug("Error: Only serial ports are handled by this example.");
					callback.onShutdown(null);
				}
			}
		} catch (Exception e) {
			log.error("Error opening port " + serialDevice);
			callback.onShutdown(e);
		}
	}

	public void shutDown() {
		this.running=false;
		this.commPort.close();
		callback.onShutdown(null);
	}

	private void addXMLLine(String addXML) {
		log.debug("XML : \n" + xmlData.toString() + "\tADD:" + addXML);
		if (xmlData.length() == 0) {
			firstLine = addXML.replaceAll("<", "</");
		}
		xmlData.append(addXML);
		if (firstLine.equals(addXML)) {
			String send = xmlData.toString();
			xmlData.setLength(0);
			sendXMLData(send);
		}
	}

	private void sendXMLData(String data) {
		callback.readReplyXML(data);
	}

	private void sendCommandXML(String xmlCommand) throws IOException {
		log.debug("TO EMU : " + xmlCommand.toString());
		out.write(xmlCommand.getBytes());
	}

	public void clearSchedule() throws IOException {

		for (String event : RainforestAPICommand.getSchedualable()) {
			log.debug("Turning off scheduled for event : " + event);
			setSchedule(null, event, null, false, "rest");
			sleepNoThrow(100);  // If we send too fast it seems to get lost
		}
	}

	protected void sleepNoThrow(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}

	public void getSchedule(String deviceMacId, String event, String mode) throws IOException {
		StringBuilder message = new StringBuilder();

		message.append("<Command>");
		message.append("<Name>get_schedule</Name>");
		if (null != event) {
			message.append("<Event>").append(event).append("</Event>");
		}
		if (null != deviceMacId) {
			message.append("<MeterMacId>").append(deviceMacId).append("</MeterMacId>");

		}
		if (null != mode) {
			message.append("<Mode>").append(mode).append("</Mode>");
		}
		message.append("</Command>");

		sendCommandXML(message.toString());
	}

	public void setSchedule(String deviceMacId, String event, Integer frequencySeconds, Boolean enabled, String mode) throws IOException {
		StringBuilder message = new StringBuilder();

		message.append("<Command>");
		message.append("<Name>set_schedule</Name>");
		if (null != deviceMacId) {
			message.append("<MacId>").append(deviceMacId).append("</MacId>");
		}
		if (null != event) {
			message.append("<Event>").append(event).append("</Event>");
		}
		if (null != mode) {
			message.append("<Mode>").append(mode).append("</Mode>");
		}
		if (null != frequencySeconds) {
			message.append("<Frequency>0x").append(Integer.toHexString(frequencySeconds)).append("</Frequency>");

		}
		if (null != enabled) {
			message.append("<Enabled>").append(enabled.booleanValue() ? "Y" : "N").append("</Enabled>");

		}
		message.append("</Command>");

		sendCommandXML(message.toString());

	}
	public boolean isRunning() {
		return running;
	}


	// ----------------------------------------------------------------------------------------
	// Handles serial IO via sub-classes
	// ----------------------------------------------------------------------------------------

	/**
	 * Handles the input coming from the serial port. A new line character is
	 * treated as the end of a block in this example.
	 */
	public class SerialReader implements SerialPortEventListener {
		private InputStream in;
		private char[] buffer = new char[1024];

		public SerialReader(InputStream in) {
			this.in = in;
		}

		public void serialEvent(SerialPortEvent arg0) {
			int data;
			String read = "";

			try {
				int len = 0;
				while ((data = in.read()) > -1) {
					if (data == '\n') {
						read = String.copyValueOf(buffer, 0, len);
						len = 0;
						addXMLLine(read);
					} else {
						buffer[len++] = (char) data;
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
				commPort.close();
				callback.onShutdown(e);
			}
		}
	}

	public class SerialWriter implements Runnable {
		OutputStream out;

		public SerialWriter(OutputStream out) {
			this.out = out;
		}

		public void run() {
			try {
				int c = 0;
				while ((c = System.in.read()) > -1) {
					this.out.write(c);
				}
			} catch (IOException e) {
				e.printStackTrace();
				callback.onShutdown(e);
			}
		}
	}

}