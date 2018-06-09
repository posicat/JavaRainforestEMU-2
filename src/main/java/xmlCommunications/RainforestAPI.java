package xmlCommunications;

public enum RainforestAPI {

	
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


	private String[] params;

	RainforestAPI(String... params) {
		this.params = params;
	}
	
	public  String[] getParamsForcommand() {
		return this.params;
	}

}
