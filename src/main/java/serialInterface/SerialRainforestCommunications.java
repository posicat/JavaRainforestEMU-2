package serialInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import xmlCommunications.RainforestCommunications;

public class SerialRainforestCommunications implements Runnable {

	private RainforestCommunications callback;
	private CommPort commPort;
	private StringBuilder xmlData = new StringBuilder();
	private InputStream in;
	private OutputStream out;
	private String firstLine;

	public SerialRainforestCommunications(RainforestCommunications callback) {
		this.callback = callback;
	}

	public void run() {
		/**
		 * Some of this code borrowed and expanded on from the example code on serial
		 * port listener.
		 * http://rxtx.qbang.org/wiki/index.php/Event_based_two_way_Communication
		 */
		String serialDevice = "/dev/ttyACM0";

		System.setProperty("gnu.io.rxtx.SerialPorts", serialDevice);

		try {

			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(serialDevice);
			if (portIdentifier.isCurrentlyOwned()) {
				System.out.println("Error: Port is currently in use");
			} else {
				commPort = portIdentifier.open(this.getClass().getName(), 2000);

				if (commPort instanceof SerialPort) {
					SerialPort serialPort = (SerialPort) commPort;
					serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);

					in = serialPort.getInputStream();
					out = serialPort.getOutputStream();

					(new Thread(new SerialWriter(out))).start();

					serialPort.addEventListener(new SerialReader(in));
					serialPort.notifyOnDataAvailable(true);

				} else {
					System.out.println("Error: Only serial ports are handled by this example.");
					callback.onShutdown(null);
				}
			}
		} catch (Exception e) {
			callback.onShutdown(e);
		}
	}

	public void shutDown() {
		this.commPort.close();
		callback.onShutdown(null);
	}

	private void addXMLLine(String addXML) {
		if (xmlData.length() == 0) {
			firstLine = addXML.replaceAll("<", "</");
		}
		xmlData.append(addXML);
		if (firstLine.equals(addXML)) {
			sendXMLData();
		}
	}

	public void sendXMLData() {
		callback.readReplyXML(xmlData.toString());
		xmlData = new StringBuilder();
	}

	public void sendCommandXML(String xmlCommand) throws IOException {
		out.write(xmlCommand.getBytes());
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
		private byte[] buffer = new byte[1024];

		public SerialReader(InputStream in) {
			this.in = in;
		}

		public void serialEvent(SerialPortEvent arg0) {
			int data;

			try {
				int len = 0;
				while ((data = in.read()) > -1) {
					if (data == '\n') {
						break;
					}
					buffer[len++] = (byte) data;
				}
				addXMLLine(new String(buffer, 0, len));
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