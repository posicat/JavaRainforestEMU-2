package org.cattech.rainforestEMU2.xmlCommunications;

import java.util.HashMap;
import java.util.Map;

public enum RainforestAPINotification {

	NetworkInfo,
	NetworkStatus,
	InstantaneousDemand,
	PriceCluster,
	MessageCluster,
	CurrentSummation,
	HistoryData,
	ScheduleInfo;

	private static final Map<String,RainforestAPINotification> nameIndex = new HashMap<String,RainforestAPINotification>(RainforestAPINotification.values().length);
	
	static {
		for (RainforestAPINotification elem : RainforestAPINotification.values()) {
			nameIndex.put(elem.name(), elem);
		}
	}
	
	public static RainforestAPINotification lookupByName(String name) {
		return nameIndex.get(name);
	}
}
