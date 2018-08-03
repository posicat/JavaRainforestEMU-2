package org.cattech.rainforestEMU2.xmlCommunications;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public enum RainforestAPIDataElement {

	Protocol("text"),
	MacId("text"),
	DeviceMacId("text"),
	InstallCode("text"),
	LinkKeyHigh("text"),
	LinkKeyLow("text"),
	FWVersion("text"),
	HWVersion("text"),
	Manufacturer("text"),
	ModelId("text"),
	DateCode("text"),
	ImageType("text"),
	Status("text"),
	CoordMacId("text"),
	Description("text"),
	StatusCode("text"),
	ExtPanId("text"),
	ShortAddr("text"),
	Channel("text"),
	Tier("text"),
	RateLabel("text"),
	Id("text"),
	Priority("text"),
	Text("text"),
	ConfirmationRequired("text"),
	Confirmed("text"),
	Read("text"),
	Queue("text"),
	MeterMacId("text"),
	Event("text"),
	Enabled("text"),

	LinkStrength("strength"), // 0-64 -> 0-100%

	Frequency("hex"),

	TimeStamp("timestamp"),
	StartTime("timestamp"),
	EndTime("timestamp"),

	SummationDelivered("piecemealNumber"),
	Demand("piecemealNumber"),
	Multiplier("text"),
	Divisor("null"),
	DigitsRight("null"),
	DigitsLeft("null"),
	SuppressLeadingZero("null"),

	Price("price"),
	Currency("null"), // Defined in ISO 4217
	TrailingDigits("null"),;

	static Logger log = Logger.getLogger(RainforestAPIDataElement.class.getName());
	
	private String format;
	private static final Map<String, RainforestAPIDataElement> nameIndex = new HashMap<String, RainforestAPIDataElement>(RainforestAPIDataElement.values().length);
	private static final Long jan2000 = calcJan2000();

	static {
		for (RainforestAPIDataElement elem : RainforestAPIDataElement.values()) {
			nameIndex.put(elem.name(), elem);
		}

	}

	public static RainforestAPIDataElement lookupByName(String name) {
		return nameIndex.get(name);
	}

	private static Long calcJan2000() {
		Calendar j2k = Calendar.getInstance();
		j2k.set(Calendar.YEAR, 2000);
		j2k.set(Calendar.MONTH, Calendar.JANUARY);
		j2k.set(Calendar.DATE, 1);
		j2k.set(Calendar.HOUR, 0);
		j2k.set(Calendar.MINUTE, 0);
		j2k.set(Calendar.SECOND, 0);
		j2k.set(Calendar.MILLISECOND, 0);
		j2k.setTimeZone(TimeZone.getTimeZone("UTC"));
		return j2k.getTime().getTime();
	}

	RainforestAPIDataElement(String format) {
		this.format = format;
	}

	public static void formatElement(String name, JSONObject json) {
		if (json.has(name)) {
			String format = RainforestAPIDataElement.lookupByName(name).getFormat();

			switch (format) {
			case "null":
				break;
			case "text":
				break;
			case "piecemealNumber": {
				String mult = ifExistsErase(json, Multiplier.name());
				String div = ifExistsErase(json, Divisor.name());
				String dr = ifExistsErase(json, DigitsRight.name());
				String dl = ifExistsErase(json, DigitsLeft.name());
				String slz = ifExistsErase(json, SuppressLeadingZero.name());

				String val = ifExistsErase(json, name);

				Float value = (float) Long.parseLong(val.replaceAll("^0x", ""), 16);

				if (null != mult) {
					value = value * (float) Long.parseLong(mult.replaceAll("^0x", ""), 16);
				}
				if (null != div) {
					value = value / (float) Long.parseLong(div.replaceAll("^0x", ""), 16);
				}

				log.debug("Formatted '" + val + "[" + mult + "," + div + "," + dr + "," + dl + "," + slz + "]' to : " + value);

				json.put(name, value);
			}
				break;
			case "price": {
				String cur = ifExistsErase(json, Currency.name());
				String tra = ifExistsErase(json, TrailingDigits.name());

				String val = ifExistsErase(json, name);

				double value = 0;

				if (val != null) {
					value = Long.parseLong(val.replaceAll("^0x", ""), 16);

					if (null != tra) {
						Integer d = Integer.parseInt(tra.replaceAll("^0x", ""), 16);
						value = value / (Math.pow(10, d));
					}
				}

				log.debug("Formatted '" + val + "[" + cur + "," + tra + "]' to : " + value);

				json.put(name, value);
			}
				break;
			case "timestamp": {
				String val = ifExistsErase(json, name);
				Calendar value = Calendar.getInstance();
				value.setTimeInMillis(Long.parseLong(val.replaceAll("^0x", ""), 16) * 1000 + jan2000);
				value.setTimeZone(TimeZone.getTimeZone("UTC"));
				json.put(name, new Timestamp(value.getTimeInMillis()));
			}
				break;
			case "hex": {
				String val = ifExistsErase(json, name);
				Integer value = Integer.parseInt(val.replaceAll("^0x", ""), 16);
				json.put(name, value);
			}
				break;
			default:
				log.error("Could not find format : " + format);
			}
		}
		// If we didn't find it, that's ok, it was consumed by one of the numeric
		// formatting options above.
	}

	private static String ifExistsErase(JSONObject json, String name) {
		String ret = null;
		if (json.has(name)) {
			ret = (String) json.remove(name);
		}
		return ret;
	}

	private String getFormat() {
		return this.format;
	}

}
