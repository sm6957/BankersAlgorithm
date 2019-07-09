package mytest;


import java.util.ArrayList;

public class Task {
	
	ArrayList<CommandInfo> activities;
	CommandInfo activity;
	int activityPos;
	
	int[] resourcesOwned;
	int[] resourcesNeeded;
	
	int id;
	int curDelay;
	int timeTaken;
	int waitTime;
	double waitPercent;
	
	boolean terminate;
	boolean aborted;
	boolean delayed;
	boolean complete;
		
	// Empty constructor
	public Task() {
		
		activities = new ArrayList<CommandInfo>();
		activity = new CommandInfo();
		activityPos = 0;
		
		resourcesOwned = new int[0];
		resourcesNeeded = new int[0];
		
		id = -1;
		curDelay = 0;
		timeTaken = 0;
		waitTime = 0;
		waitPercent = 0;
		
		terminate = false;
		aborted = false;
		delayed = false;
		complete = false;
				
	}
	
	// Constructor to initialize the activities array list and resources owned/needed arrays
	public Task(int id, int numResourceTypes) {
		activities = new ArrayList<CommandInfo>();
		activity = new CommandInfo();
		activityPos = 0;
		curDelay = 0;
		
		resourcesOwned = new int[numResourceTypes];
		resourcesNeeded = new int[numResourceTypes];
		
		for (int i = 0; i < numResourceTypes; i++) {
			resourcesOwned[i] = 0;
			resourcesNeeded[i] = 0;
		}
		
		this.id = id;
		
	}
	
	// Add an activity to the task
	public void addActivity(String type, int delay, int resource, int value) {
		activities.add(new CommandInfo(type,delay,resource,value));
	}
	
	// Move on to the next activity
	public void nextActivity() {
		activityPos += 1;
		activity = activities.get(activityPos);
	}
	
	// Update the task's current possession of a resource type
	public void receiveUnits(int resourceType, int units) {
		resourcesOwned[resourceType] += units;
		resourcesNeeded[resourceType] -= units;
	}
	
	// Update the task's current possession of a resource type
	public void releaseUnits(int resourceType, int units) {
		resourcesOwned[resourceType] -= units;
		resourcesNeeded[resourceType] += units;
	}
	
	// Update the task's claim for a resource type
	public void updateClaim(int resource, int value) {
		resourcesNeeded[resource] = value;
	}
	
	// Abort the task
	public void abort() {
		aborted = true;
	}
	
	// Compute stats for the task
	public void terminate(int endCycle) {
		timeTaken = endCycle;
		terminate = true;
	}
	
	// Display task stats
	public void showStats() {
				
		waitPercent = ((double)waitTime / (double)timeTaken) * 100;
				
		if (aborted == false) {
			System.out.printf("\t%-8s: ", ("Task #" + id));
			System.out.printf("Took %d cycle(s); Waited %d cycle(s) (%.0f%%)\n", timeTaken, waitTime, waitPercent);
		} else {
			System.out.printf("\t%-8s: ", ("Task #" + id));
			System.out.println("Aborted");
		}
		
	}
	
	// Display the task's remaining resource needs
	public void getCurNeeds() {
		System.out.printf("Task #%d currently needs: ", id);
		
		for (int i = 0; i < resourcesNeeded.length; i++) {
			System.out.println(resourcesNeeded[i] + " R" + (i+1) + ", ");
		}
	}

}