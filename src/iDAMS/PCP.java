package iDAMS;

import iDAMS.ACO.Bill;
import iDAMS.ACO.BillType;

import java.util.Iterator;
import java.util.LinkedList;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class PCP implements Provider {
	
	public int id; // Identifier of provider
	double intervention_budget; // Cumulative intervention cost
	int total_patients; // Cumulative number of patients served
	int distinct_patients; // Distinct number of patients who are served
	// List of patients in the line
	public LinkedList<Bene> cList;
	public LinkedList<Bene> patientList;
	public ACO aco;
	public int countMortality;
	public int tickCount;

	
	public PCP(int id, ACO a){
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		this.id = id;
		this.intervention_budget = 0;
		this.total_patients = 0;
		this.distinct_patients = 0;
		this.cList = new LinkedList<Bene>();
		this.patientList = new LinkedList<Bene>();
		this.aco = a;
		this.countMortality = 0;
		this.tickCount = 0;
	}
	
	// Step function - at every time tick agents run it after they are shuffled - You can add priority to the agent types.
	@ScheduledMethod(start = 1, interval = 1, shuffle = true, priority = 1)
	public void step(){
		
		// Get Scheduler
		Schedule schedule= (Schedule) RunEnvironment.getInstance().getCurrentSchedule();
		// Get Context
		Context context = ContextUtils.getContext(this);
	    // Get parameters
		Parameters p = RunEnvironment.getInstance().getParameters();
		// Increment Tick Count
		tickCount++;
		// Dynamic Group Network?
		if ((Integer)p.getInteger("controlledGroup") == 1){
			
			// Fixed or mixed?
			int i = Math.abs((Integer)p.getInteger("endOfSim")/(Integer)p.getInteger("meetingFrequency"));
			if ((Integer)p.getInteger("mixed") == 0 && schedule.getTickCount() == 1){
				
				randomGroupNetwork();
			}
			if (tickCount == i){
				
				tickCount = 0;
				if((Integer)p.getInteger("mixed") == 1 && (Integer)p.getInteger("mixingStyle") == 1){
					
					randomGroupNetwork();
				}
				else if ((Integer)p.getInteger("mixed") == 1 && (Integer)p.getInteger("mixingStyle") == 0){
					
					preferedGroupNetwork();
				}		
			}
		}
		
		if ((Integer)p.getInteger("controlledGroup") == 0){
		
			// If queue is not empty - Meaning that Provider received patients
			while (this.cList.size() > 0){
				
				// Serve the first patient in the queue
				Bene patient = this.cList.getFirst();
				patient.health = 0;
				aco.bill(patient, this, aco.visit_cost, BillType.visit);
				// Assign interventions
				if (RandomHelper.nextDoubleFromTo(0, 1)<aco.offline_rate){
					
					aco.bill(patient, this, aco.offline_cost, BillType.offline);
					patient.interventionList.add(new Intervention(this,interventionType.offline));
				}
				if (RandomHelper.nextDoubleFromTo(0, 1)<this.aco.online_rate){
					
					aco.bill(patient, this, aco.online_cost, BillType.online);
					if(RandomHelper.nextDoubleFromTo(0, 1)<p.getDouble("reachability")){
						
						patient.interventionList.add(new Intervention(this,interventionType.online));
					}
				}
				if (RandomHelper.nextDoubleFromTo(0, 1)<this.aco.onsite_rate){
					
					aco.bill(patient, this, aco.onsite_cost, BillType.onsite);
					if (RandomHelper.nextDoubleFromTo(0, 1)<p.getDouble("attendance_rate")){
						
						patient.interventionList.add(new Intervention(this,interventionType.onsite));
					}			
				}
				
				// Kill Bene
				if (p.getInteger("mortality")==1&&patient.health==p.getInteger("stateDeath")){
					
					context.remove(patient);
					this.countMortality++;	
				}
				this.cList.removeFirst();
			}
		}
		
		RunEnvironment.getInstance().endAt((Integer)p.getValue("endOfSim"));	
	}
	
	public class Intervention {
		
		public PCP provider;
		public interventionType intType;
		
		public Intervention(PCP provider, interventionType cType) {
			this.provider = provider;
			this.intType = cType;
		}
	}
	public enum interventionType {
		
		offline,
		online,
		onsite
	}
	public int getMortalityCount() {
		
		return this.countMortality;
	}
	// Random Group Network
	public void randomGroupNetwork(){
		
		// Get Context
		Parameters p = RunEnvironment.getInstance().getParameters();
		Context context = ContextUtils.getContext(this);
	    Network<Bene> network = (Network) context.getProjection("groupNetwork");
	    // Remove Edges
	    network.removeEdges();
		// Get group size
		int size = (Integer)p.getInteger("groupSize");
		// Iterate over this tempList to connect benes - for computational benefits
		LinkedList<Bene> tempList = new LinkedList<Bene>();
		for (Iterator<Bene> iterator = this.patientList.iterator(); iterator.hasNext();) {
			
			Bene bene = (Bene)iterator.next();
			tempList.add(bene);
		}
	    
	    // Assign number of groups
	    int numOfGroups = (int) Math.round((double)(this.patientList.size()/size-0.5));
		// Count how many patients are put in a group
	    int t = 0;
	    for (int t1 = 0; t1 < numOfGroups; t1++){
		    
	    	LinkedList<Bene> gList = new LinkedList<Bene>();
		    // Select patients until count is equal to group size
	    	while (t < size){
	    		int source = RandomHelper.nextIntFromTo(0, tempList.size()-1);
				Bene o = (Bene) tempList.get(source);
				if (network.getDegree(o) == 0){
					
					gList.add(o);
					tempList.remove(source);
					t++;
				}
			}
	    	Bene source = new Bene(1);
	    	Bene target = new Bene(1);
			for (int i = 0; i < size; i++) {

				for (int j = i+1; j < size; j++){
					source = (Bene)gList.get(i);
					target = (Bene)gList.get(j);
					network.addEdge(source, target);
				}		
			}
			t = 0;
	    }	
	}
	
	// Preferred Group Network
	public void preferedGroupNetwork(){
		
		// Get Context
		Parameters p = RunEnvironment.getInstance().getParameters();
		Context context = ContextUtils.getContext(this);
	    Network<Bene> network = (Network) context.getProjection("groupNetwork");
	    // Remove Edges
	    network.removeEdges();
		// Get group size
		int size = (Integer)p.getInteger("groupSize");
	    // Assign number of groups
	    int numOfGroups = (int) Math.round((double)(this.patientList.size()/size-0.5));
		// Find best Benes
		LinkedList<Bene> tempList = new LinkedList<Bene>();
		LinkedList<Bene> bestList = new LinkedList<Bene>();
		double attribute = 1.0; 
		int index = 0;
		for (Iterator<Bene> iterator = this.patientList.iterator(); iterator.hasNext();) {
			
			Bene bene = (Bene)iterator.next();
			if(bestList.size()<numOfGroups){

				bestList.add(bene);
			}
			else{
				
				Bene b = new Bene(1);
				for (Iterator<Bene> it = bestList.iterator(); it.hasNext();) {
					
					b = (Bene)it.next();	
					if (b.behavior < attribute){

						attribute = b.behavior;	
						index = bestList.indexOf(b);
					}
				}
				
				if (bene.behavior > attribute){
					
					attribute = 1;
					bestList.remove(index);
					bestList.add(bene);
				}
			}
		}
		
		for (Iterator<Bene> iterator = bestList.iterator(); iterator.hasNext();) {
			
			Bene bene = (Bene)iterator.next();
			System.out.println("IDBEST = "+bene.id+"Behavior"+bene.behavior);
		}
		
		// Iterate over this tempList to connect benes - for computational benefits
		for (Iterator<Bene> iterator = this.patientList.iterator(); iterator.hasNext();) {
			
			Bene bene = (Bene)iterator.next();
			tempList.add(bene);
			System.out.println("ID = "+bene.id+"Behavior"+bene.behavior);
		}
		// Count how many patients are put in a group
	    int t = 0;
	    for (int t1 = 0; t1 < numOfGroups; t1++){
		    LinkedList<Bene> gList = new LinkedList<Bene>();
		    gList.add(bestList.get(t1));
		    t++;
		    tempList.remove(bestList.get(t1));
		    // Select patients until count is equal to group size
	    	while (t < size){
	    		int source = RandomHelper.nextIntFromTo(0, tempList.size()-1);
				Bene o = (Bene) tempList.get(source);
				if (network.getDegree(o) == 0){
					
					gList.add(o);
					tempList.remove(source);
					t++;
				}
			}
	    	Bene source = new Bene(1);
	    	Bene target = new Bene(1);
			for (int i = 0; i < size; i++) {

				for (int j = i+1; j < size; j++){
					source = (Bene)gList.get(i);
					target = (Bene)gList.get(j);
					network.addEdge(source, target);
				}		
			}
			t = 0;
	    }	
	}
}
