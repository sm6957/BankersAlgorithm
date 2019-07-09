package mytest;
public class CommandInfo {
	
	String type;
	int delay;
	int resource;
	int value;
	
	//Empty constructor
	public CommandInfo() {
		type = "";
		delay = 0;
		resource = 0;
		value = 0;
	}
	
	// Constructor to set activity variables to provided information
	public CommandInfo(String activity, int delay, int resource, int value) {
		this.type = activity;
		this.delay = delay;
		this.resource = resource;
		this.value = value;
	}
	
	
	

}