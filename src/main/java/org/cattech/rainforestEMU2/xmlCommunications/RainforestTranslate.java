package org.cattech.rainforestEMU2.xmlCommunications;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RainforestTranslate {

	static Logger log = Logger.getLogger(RainforestTranslate.class.getName());
	
	public static JSONObject toHumanReadableJson(String xmlData) throws SAXException, IOException, ParserConfigurationException {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(new InputSource(new StringReader(xmlData)));

		Element rootNode = doc.getDocumentElement();

		RainforestAPINotification notification = RainforestAPINotification.lookupByName(rootNode.getNodeName());

		JSONObject rootJson = new JSONObject();

		if (null != notification) {
			processNodes(rootJson, rootNode);
			processToReadable(rootJson);
		}
		return rootJson;
	}

	private static void processToReadable(JSONObject json) {
		String[] names = JSONObject.getNames(json);
		for (String name : names) {
			if (RainforestAPINotification.lookupByName(name)!=null) {
				processToReadable(json.getJSONObject(name));
			}
			if (RainforestAPIDataElement.lookupByName(name)!=null) {
				RainforestAPIDataElement.formatElement(name, json);
			}
		}

	}

	private static void processNodes(JSONObject parentJson, Node node) {
		JSONObject json = new JSONObject();

		if (node.getNodeType() == Node.ELEMENT_NODE) {

			if (node.hasAttributes()) {
				NamedNodeMap attributes = node.getAttributes();
				for (int i = 0; i < attributes.getLength(); i++) {
					Node attr = attributes.item(i);
					parentJson.put(attr.getNodeName(), attr.getNodeValue());
				}
			}
			NodeList nodeList = node.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node childNode = nodeList.item(i);
				processNodes(json, childNode);
				if (json.length() > 0) {
					parentJson.put(node.getNodeName(), json);
				} else {
					if (null != node.getTextContent()) {
						parentJson.put(node.getNodeName(), node.getTextContent());
					}
				}
			}
		}
	}
}
