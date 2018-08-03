package org.cattech.rainforestEMU2.xmlCommunications;

public enum RainforestAPICommand {

	// List of available requests, and required parameters.
	get_network_info("Protocol","MacId"),
	list_network(),
	get_network_status("Protocol","MacId"),
	get_instantaneous_demand("MacId"),
	get_price("MacId"),
	get_message("MacId"),
	confirm_message("MacId","ID"),
	get_current_summation("MacId"),
	get_history_data("MacId","StartTime","EndTime","Frequency"),
	set_schedule("MacId","Event","Frequency","Enabled"),
	get_schedule("MacId","Event"),
	reboot("MacId","Target"),
	get_demand_peaks();

//	static final String[] schedualable = {"time","message","price","summation","demand","scheduled_prices","profiled_data","billing_period","block_period"};
	static final String[] schedualable = {"demand","price"};

	private String[] params;

	RainforestAPICommand(String... params) {
		this.params = params;
	}
	
	public  String[] getParamsForcommand() {
		return this.params;
	}

	public static String[] getSchedualable() {
		return schedualable;
	}

}
