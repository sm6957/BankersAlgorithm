package mytest;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class BankerAlgo {
	
	static boolean detailedOutput; 
	static int numJobs;
	public static int numResourceTypes;
	static int[] resources;
	static Task[] Jobs;
	static ArrayList<Task> activeJobs = new ArrayList<Task>();
	static ArrayList<Task> blockedJobs = new ArrayList<Task>();
	static ArrayList<Task> nonblockedJobs = new ArrayList<Task>();
	
	static int[] resourcesGained = new int[numResourceTypes];
	
	// Perform initiation procedure
	public static void initiate(Task task, String managerType) {
		
		int resource = task.activity.resource;
		int claim = task.activity.value;
										
		// Check if delay is present
		if (task.curDelay > 0) {
		
			task.delayed = true;
			task.curDelay -= 1;
			
			if (task.curDelay == 0) { task.delayed = false; }
			if (detailedOutput) { System.out.printf("%-37s", "Task #" + task.id + " delayed (" + task.curDelay + " cycle(s) left)"); }
			nonblockedJobs.add(task);
			
		// Run initiate procedure if no delay is present
		} else {
			
			// Update the task's resource claims
			task.updateClaim(resource, claim);
			
			// Check if claim is higher than resources available; abort if so
			if (managerType.equals("banker")) {
				for (int i = 0; i < numResourceTypes; i++) {
					if (task.resourcesNeeded[i] > resources[i]) {
						task.aborted = true;
					}
				}
			}
			
			// Finish initiate procedure if abortion is unnecessary
			if (task.aborted == false) {
				task.nextActivity();
				task.curDelay = task.activity.delay;
				nonblockedJobs.add(task);
				if (detailedOutput) { System.out.printf("%-37s", "Task #" + task.id + " initiated."); }
			} else {
				if (detailedOutput) { System.out.printf("%-37s", "Task #" + task.id + " aborted (claim > # units)."); }
			}
			
		}
				
	}
	
	// (can only be called by the banker request procedure)
	// Simulates the current status of the simulation to see if completion is possible (safe state).
	// Returns true if safe state preserved, false if unsafe state found
	public static boolean checkSafe(int idRequesting, int resourceRequested, int resourceAmountRequested) {
		
		Task taskRequesting = new Task();
		
		// Create copy of active Jobs (ensure no accidental modificiation of original data structure)
			ArrayList<Task> Jobs = new ArrayList<Task>();
			for (int i = 0; i < activeJobs.size(); i++) {
				Jobs.add(new Task());
				Jobs.get(i).id = activeJobs.get(i).id;
				Jobs.get(i).resourcesNeeded = activeJobs.get(i).resourcesNeeded;
				Jobs.get(i).resourcesOwned = activeJobs.get(i).resourcesOwned;
			}
			
		// Tag the active task that is requesting the resource
			for (int i = 0; i < activeJobs.size(); i++) {
				if (activeJobs.get(i).id == idRequesting) {
					taskRequesting.id = activeJobs.get(i).id;
					taskRequesting.resourcesNeeded = activeJobs.get(i).resourcesNeeded;
					taskRequesting.resourcesOwned = activeJobs.get(i).resourcesOwned;
				}
			}
			
		// Create copy of resources (ensure no modification of original data structure)
			int[] availableResources = new int[numResourceTypes];
			for (int i = 0; i < numResourceTypes; i++) {
				availableResources[i] = resources[i];
			}
			
		// "Grant" resource request (does not actually grant, but will be used to check if granting the request can maintain a safe state
			availableResources[resourceRequested] -= resourceAmountRequested;
			taskRequesting.resourcesNeeded[resourceRequested] -= resourceAmountRequested;
			taskRequesting.resourcesOwned[resourceRequested] += resourceAmountRequested;
			
		// Check if the resource request leads to a safe (return true) or unsafe state (return false)
			boolean safeStatePossible = true; //evaluates if a safe state is possible
			boolean completable = false; //evaluates if a certain task is completable
			
			// If the while loop is completed and Jobs.size() > 0, then we have an unsafe state
			while (safeStatePossible == true) {
				safeStatePossible = false; //Unsafe state until proven otherwise
				
				for (int i = 0; i < Jobs.size(); i++) {
					completable = true; // Task is completable until proven otherwise
					
					//Check if the current task is completable
					for (int j = 0; j < numResourceTypes; j++) {
						if (Jobs.get(i).resourcesNeeded[j] > availableResources[j]) {
							completable = false;
						}
					}
					
					//If at least one task is completable, the simulation could be in a safe state
					if (completable == true) {
						
						//Report potential safe state
						safeStatePossible = true;
						
						//Give resources back (simulate resource return)
						for (int j = 0; j < numResourceTypes; j++) {
							availableResources[j] += Jobs.get(i).resourcesOwned[j];
						}
												
						//Remove "finished" task from the arraylist
						Jobs.remove(Jobs.get(i));
						
					}
					
				}
				
				//Check is the Jobs arraylist is empty; if so, the simulation state is safe
				if (Jobs.size() == 0) { return(true); }
			}
			
			// If this line is reached, then safeStatePossible=false and Jobs.size() > 0. UNSAFE!
			return(false);
	}
	
	// Perform request procedure (optimistic manager)
	public static void optimisticRequest(Task task) {
		
		int resource = task.activity.resource;
		int unitsRequested = task.activity.value;
		int unitsAvailable = resources[resource];
						
		// Check if delay is present
		if (task.curDelay > 0) {
		
		task.delayed = true;
		task.curDelay -= 1;
		
		if (task.curDelay == 0) { task.delayed = false; }
		if (detailedOutput) { System.out.printf("%-37s", "Task #" + task.id + " delayed (" + task.curDelay + " cycle(s) left)"); }
		nonblockedJobs.add(task);
		
		// Run request procedure if no delay is present
		} else {
			
			// Request units
				// Grant request
				if (unitsAvailable >= unitsRequested) {
					resources[resource] -= unitsRequested;
					task.receiveUnits(resource, unitsRequested);
					task.nextActivity();
					task.curDelay = task.activity.delay;
					if (detailedOutput) { System.out.printf("%-37s", "Task #" + task.id + " granted " + unitsRequested + " R" + (resource+1) + "."); }
					nonblockedJobs.add(task);
				// Reject request
				} else {
					if (detailedOutput) { System.out.printf("%-37s", "Task #" + task.id + " not granted " + unitsRequested + " R" + (resource+1) + "."); }
					task.waitTime += 1;
					blockedJobs.add(task);
				}
			
		}		
		
	}
	
	// Perform request procedure (banker manager)
	public static void bankerRequest(Task task) {
		
		int resource = task.activity.resource;
		int unitsRequested = task.activity.value;
		int unitsAvailable = resources[resource];
		int maxSafeUnits = -1;
						
		// Check if delay is present
		if (task.curDelay > 0) {
		
		task.delayed = true;
		task.curDelay -= 1;
		
		if (task.curDelay == 0) { task.delayed = false; }
		if (detailedOutput) { System.out.printf("%-37s", "Task #" + task.id + " delayed (" + task.curDelay + " cycle(s) left)"); }
		nonblockedJobs.add(task);
		
		// Run request procedure if no delay is present
		} else {
			
			// Check if the request is illegal (would make units owned higher than units claimed)
			// If request is illegal, task should be aborted
			if (unitsRequested > task.resourcesNeeded[resource]) {
				task.aborted = true;
			}
			
			// Check if the request is unsafe
			int unitsNeeded = task.resourcesNeeded[resource];
			int unitsOwned = task.resourcesOwned[resource];
			boolean isSafeRequest = checkSafe(task.id, resource, unitsRequested);
			task.resourcesNeeded[resource] = unitsNeeded;
			task.resourcesOwned[resource] = unitsOwned;
								
			// Request units
				if (task.aborted == false) {
					// Grant request
					if ((unitsAvailable >= unitsRequested) && (isSafeRequest == true)) {
						resources[resource] -= unitsRequested;
						task.receiveUnits(resource, unitsRequested);
						task.nextActivity();
						task.curDelay = task.activity.delay;
						if (detailedOutput) { System.out.printf("%-37s", "Task #" + task.id + " granted " + unitsRequested + " R" + (resource+1) + "."); }
						nonblockedJobs.add(task);
					// Reject request (unsafe)
					} else if ((unitsAvailable >= unitsRequested) && (isSafeRequest == false)) {
						if (detailedOutput) { System.out.printf("%-37s", "Task #" + task.id + " not granted " + unitsRequested + " R" + (resource+1) + " (unsafe)."); }
						task.waitTime += 1;
						blockedJobs.add(task);
					// Reject request (unavailable)
					} else {
						if (detailedOutput) { System.out.printf("%-37s", "Task #" + task.id + " not granted " + unitsRequested + " R" + (resource+1) + "."); }
						task.waitTime += 1;
						blockedJobs.add(task);
					}
				} else {
					task.aborted = true;
					task.terminate = true;
					blockedJobs.add(task);
					if (detailedOutput) { System.out.printf("%-37s", "Task #" + task.id + " aborted (request > claim)."); }
				}
			
		}		
		
	}
	
	// Perform release procedure
	public static void release(Task task) {
		
		int delay = task.activity.delay;
		int resource = task.activity.resource;
		int unitsToSend = task.activity.value;
		int unitsOwned = task.resourcesOwned[resource];
						
		// Check if delay is present
		if (task.curDelay > 0) {
			
			task.delayed = true;
			task.curDelay -= 1;
			
			if (task.curDelay == 0) { task.delayed = false; }
			if (detailedOutput) { System.out.printf("%-37s", "Task #" + task.id + " delayed (" + task.curDelay + " cycle(s) left)"); }
			nonblockedJobs.add(task);
			
		// Run release procedure if no delay is present
		} else {
			
			// Request units
				// Accept release
				if (unitsOwned >= unitsToSend) {
					resourcesGained[resource] += unitsToSend;
					task.releaseUnits(resource, unitsToSend);
					task.nextActivity();
					task.curDelay = task.activity.delay;
					if (detailedOutput) { System.out.printf("%-37s", "Task #" + task.id + " releases " + unitsToSend + " R" + (resource+1) + "."); }
					nonblockedJobs.add(task);
				// Reject release
				} else {
					if (detailedOutput) { System.out.printf("%-37s", "Task #" + task.id + " cannot release " + unitsToSend + " R" + (resource+1) + " (more than owned)."); }
					task.waitTime += 1;
					blockedJobs.add(task);
				}
			
		}
		
	}

	// Perform terminate procedure
	public static void terminate(Task task, int endCycle) {
		
		
		// Check if delay is present
		if (task.curDelay > 0) {
			
			task.delayed = true;
			task.curDelay -= 1;
			
			if (task.curDelay == 0) { task.delayed = false; }
			if (detailedOutput) { System.out.printf("%-37s", "Task #" + task.id + " delayed (" + task.curDelay + " cycle(s) left)"); }
			nonblockedJobs.add(task);
		// Run terminate procedure if no delay is present
		} else {
			
			task.terminate = true;
			task.timeTaken = endCycle;
			if (detailedOutput) { System.out.printf("%-37s", "Task #" + task.id + " has been terminated."); }
			
		}
	}
	
	// Resolve discovered deadlock
	public static void resolveDeadlock() {
		
		int numAbortions = blockedJobs.size() - 1;
		int lowestID = 100000; //Arbitrary number to guarantee all found ID's are lower
		
		// Abort all Jobs but the highest (highest ID)
		for (int i = 0; i < numAbortions; i++) {
			
			lowestID = 100000;
			
			// Determine the lowest ID within the blocked Jobs
			for (int j = 0; j < blockedJobs.size(); j++) {
				if (blockedJobs.get(j).id < lowestID) {
					lowestID = blockedJobs.get(j).id;
				}
			}
			
			// Abort the task that possesses the lowest ID
			for (int j = 0; j < blockedJobs.size(); j++) {
				if (blockedJobs.get(j).id == lowestID) {
					if (detailedOutput) { System.out.println("\tDEADLOCK! Task #" + blockedJobs.get(j).id + " has been aborted."); }
					
					blockedJobs.get(j).aborted = true;
					blockedJobs.get(j).complete = true;
					blockedJobs.get(j).terminate = true;
					
					//Release all resources
					for (int k = 0; k < numResourceTypes; k++) {
						resourcesGained[k] += blockedJobs.get(j).resourcesOwned[k];
						blockedJobs.get(j).resourcesOwned[k] = 0;
					}
					
					blockedJobs.remove(j);
					break;
				}
			}
		}
		
		
	}
	
	// Run the banker simulation
	public static void bankerManager() {
			
		boolean simulationComplete = false;
		Task curTask = new Task();
		
		// Initialize all Jobs as active
		for (int i = 0; i < Jobs.length; i++) {
			activeJobs.add(Jobs[i]);
		}
		
		// Perform simulation
		int curCycle = 0;
		
		if (detailedOutput) { System.out.println("================================BANKER RESOURCE MANAGER SIMULATION================================\n"); }
		
		while (activeJobs.size() > 0) {
			
			// Show resource availability
			if (detailedOutput) {
				System.out.print("  AVAILABLE: ");
				for (int j = 0; j < numResourceTypes; j++) {
					System.out.print("[R" + (j+1) + ": ");
					System.out.print(resources[j] + "]  ");
				}
				System.out.println("\n  -----------------------------------------");
			}
			
			// Perform a cycle
				for (int i = 0; i < activeJobs.size(); i++) {
					
					curTask = activeJobs.get(i);
					
					// Display current cycle
						if (detailedOutput) { System.out.printf(" %2d:  ", curCycle); }
						
					// Perform appropriate activity procedure
						switch (curTask.activity.type) {
							case "initiate": initiate(curTask, "banker"); break;
							case "request": bankerRequest(curTask); break;
							case "release": release(curTask); break;
							case "terminate": terminate(curTask, curCycle); break;
							default: break;
						}
					
					// Show task needs
						if (detailedOutput) {
							for (int j = 0; j < activeJobs.size(); j++) {
								if (activeJobs.get(j).terminate == false) {
									System.out.print("#" + activeJobs.get(j).id + " needs [");
									
									for (int k = 0; k < numResourceTypes; k++) {
										System.out.print(activeJobs.get(j).resourcesNeeded[k] + " ");
									}
									System.out.print("]\t");
								}
							}
							if (detailedOutput) { System.out.println(); }
						}
				}
				
				// Resolve deadlock if present
				if ((blockedJobs.size() > 0) && (nonblockedJobs.size() == 0)) {
					
					// Check if a task has already been aborted first
					boolean taskAlreadyAborted = false;
					for (int j = 0; j < blockedJobs.size(); j++) {
						if (blockedJobs.get(j).aborted == true) {
							taskAlreadyAborted = true;
							
							//Return resources
							for (int k = 0; k < numResourceTypes; k++) {
								resources[k] += blockedJobs.get(j).resourcesOwned[k];
							}
							
							blockedJobs.remove(blockedJobs.get(j));
							break;
						}
					}
					
					// Resolve deadlock if no task has been aborted
					if (taskAlreadyAborted == false) {
						if (detailedOutput) { System.out.println(); }
						resolveDeadlock();
					}
				}
				
				// Process pending resource gains
				for (int i = 0; i < numResourceTypes; i++) {
					resources[i] += resourcesGained[i];
					resourcesGained[i] = 0;
				}
				
				// Arrange activeJobs order for next cycle
				activeJobs.clear();
				activeJobs.addAll(blockedJobs);
				activeJobs.addAll(nonblockedJobs);
				blockedJobs.clear();
				nonblockedJobs.clear();
				
				// Increment the cycle
				curCycle += 1;
				if (detailedOutput) { System.out.println(); }
							
		}
		
		if (detailedOutput) { System.out.println("==================================================================================================\n"); }
															
	}

	// Run the optimistic manager simulation
	public static void FIFO() {
		
		boolean simulationComplete = false;
		Task curTask = new Task();
		
		// Initialize all Jobs as active
		for (int i = 0; i < Jobs.length; i++) {
			activeJobs.add(Jobs[i]);
		}
		
		// Perform simulation
		int curCycle = 0;
		
		if (detailedOutput) { System.out.println("==============================FIFO RESOURCE MANAGER SIMULATION==============================\n"); }
		
		while (activeJobs.size() > 0) {
			
			// Show resource availability
				if (detailedOutput) {
					System.out.print("  AVAILABLE: ");
					for (int j = 0; j < numResourceTypes; j++) {
						System.out.print("[R" + (j+1) + ": ");
						System.out.print(resources[j] + "]  ");
					}
					System.out.println("\n  -----------------------------------------");
				}
			
			// Perform a cycle
				for (int i = 0; i < activeJobs.size(); i++) {
					
					curTask = activeJobs.get(i);
					
					// Display current cycle
						if (detailedOutput) { System.out.printf(" %2d:  ", curCycle); }
						
					// Perform appropriate activity procedure
						switch (curTask.activity.type) {
							case "initiate": initiate(curTask, "optimistic"); break;
							case "request": optimisticRequest(curTask); break;
							case "release": release(curTask); break;
							case "terminate": terminate(curTask, curCycle); break;
							default: break;
						}
						
					if (detailedOutput) { System.out.println(); }
				}
				
				// Resolve deadlock if present
				if ((blockedJobs.size() > 0) && (nonblockedJobs.size() == 0)) {
					if (detailedOutput) { System.out.println(); }
					resolveDeadlock();
				}
				
				// Process pending resource gains
				for (int i = 0; i < numResourceTypes; i++) {
					resources[i] += resourcesGained[i];
					resourcesGained[i] = 0;
				}
				
				// Arrange activeJobs order for next cycle
				activeJobs.clear();
				activeJobs.addAll(blockedJobs);
				activeJobs.addAll(nonblockedJobs);
				blockedJobs.clear();
				nonblockedJobs.clear();
				
				// Increment the cycle
				curCycle += 1;
				if (detailedOutput) { System.out.println(); }
			
		}
		
		if (detailedOutput) { System.out.println("==================================================================================================\n"); }
														
	}
	
	
	// Read the input file, evaluating values for appropriate variables/objects.
	private static void readInput(String fileName) {
		
		File file = new File(fileName);
				
		String activityType = "";
		int taskNum = 0;
		int delay = 0;
		int resourceType = 0;
		int value = 0;
		
		try {
			
			Scanner fileInput = new Scanner(file);
			
			// Evaluate the number of Jobs/resource types in the simulation
			numJobs = fileInput.nextInt();
			numResourceTypes = fileInput.nextInt();
			
			//Create an integer array of resource types
			resources = new int[numResourceTypes];
			resourcesGained = new int[numResourceTypes];
			
			//Evaluate the # of units each resource type has based on the input.
			//Initialize pending resource gain array as array of zeroes
			for (int i = 0; i < resources.length; i++) {
				resources[i] = fileInput.nextInt();
				resourcesGained[i] = 0;
			}
			
			//Initialize the array of Jobs.
				Jobs = new Task[numJobs];
				
				for (int i = 0; i < Jobs.length; i++) {
					Jobs[i] = new Task((i+1),numResourceTypes);
				}
			
			
			// Fill the Jobs with its activities from the input				
			while (fileInput.hasNext()) {
				
				// Retrieve activity information from input
					activityType = fileInput.next();
					taskNum = fileInput.nextInt();
					delay = fileInput.nextInt();
					resourceType = fileInput.nextInt();
					value = fileInput.nextInt();
				
				// Add activity to the appropriate task
					Jobs[taskNum-1].addActivity(activityType,delay,(resourceType-1),value);
			}
			
			//Set current activity (start activity) for the Jobs
			for (int i = 0; i < Jobs.length; i++) {
				Jobs[i].activity = Jobs[i].activities.get(0);
				Jobs[i].curDelay = Jobs[i].activity.delay;
			}
			
			fileInput.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}


	// Main method
	public static void main(String[] args) {
		
		String fileName = "";
		
		// Obtain file name from command line argument; report errors if necessary.
			if (args.length == 0) {
				System.out.println("ERROR: Filename not given. No input file can be read.");
			} else {
				fileName = args[0];
			}
			
		// Run the optimistic manager simulation
			if (!fileName.equals("")) { readInput(fileName); }
			FIFO();
			
		// Compute optimistic manager stats
			int totalTime = 0;
			int totalWaitTime = 0;
			double avgWaitPercent = 0.0;
			int completedJobs = 0;
			
			System.out.println("=========================FIFO RESULTS==============================");
			
			for (int i = 0; i < numJobs; i++) {
				
				Jobs[i].showStats();
				
				if (Jobs[i].aborted == false) {
					completedJobs += 1;
					totalTime += Jobs[i].timeTaken;
					totalWaitTime += Jobs[i].waitTime;
				}
				
				
			}
			avgWaitPercent = ((double)totalWaitTime / (double)totalTime) * 100;
			
		// Display optimistic manager stats
			System.out.printf("\t%-8s: ", "Total");
			System.out.printf("Took %d cycle(s); Waited %d cycle(s) (%.0f%%)\n", totalTime, totalWaitTime, avgWaitPercent);
			System.out.println("\n");
						
		// Run the banker manager simulation
			if (!fileName.equals("")) { readInput(fileName); }
			bankerManager();

		// Compute banker manager stats
			totalTime = 0;
			totalWaitTime = 0;
			avgWaitPercent = 0.0;
			completedJobs = 0;
			
			System.out.println("=========================BANKER RESULTS============================");
			
			for (int i = 0; i < numJobs; i++) {
				
				Jobs[i].showStats();
				
				if (Jobs[i].aborted == false) {
					completedJobs += 1;
					totalTime += Jobs[i].timeTaken;
					totalWaitTime += Jobs[i].waitTime;
				}
				
				
			}
			avgWaitPercent = ((double)totalWaitTime / (double)totalTime) * 100;
			
		// Display banker manager stats
			System.out.printf("\t%-8s: ", "Total");
			System.out.printf("Took %d cycle(s); Waited %d cycle(s) (%.0f%%)\n", totalTime, totalWaitTime, avgWaitPercent);

	}

}