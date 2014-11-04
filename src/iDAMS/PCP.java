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
	public int countMoratality;

	
	public PCP(int id, ACO a){
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		this.id = id;
		this.intervention_budget = 0;
		this.total_patients = 0;
		this.distinct_patients = 0;
		this.cList = new LinkedList<Bene>();
		this.patientList = new LinkedList<Bene>();
		this.aco = a;
		this.countMoratality = 0;
	}
	
	// Step function - at every time tick agents run it after they are shuffled - You can add priority to the agent types.
	@ScheduledMethod(start = 1, interval = 1, shuffle = true, priority = 1)
	public void step(){
		
		// Get Scheduler
		Schedule schedule= (Schedule) RunEnvironment.getInstance().getCurrentSchedule();
		// Get Context
		Context context = ContextUtils.getContext(this);
	    Grid grid = (Grid) context.getProjection("grid");
	    // Get parameters
		Parameters p = RunEnvironment.getInstance().getParameters();
		
		// Dynamic Network?
		if ((Integer)p.getInteger("controlledGroup") == 1){
			// Fixed or mixed?
			if ((Integer)p.getInteger("mixed") == 0){
				randomGroupNetwork();
			}
			else if((Integer)p.getInteger("mixed") == 1 && (Integer)p.getInteger("mixingStyle") == 1){
				randomGroupNetwork();
			}
			else {
				// Write a method to mix benes based on their attributes
			}
		}
		// If queue is not empty
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
				this.countMoratality++;	
			}
			this.cList.removeFirst();
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
		
		return this.countMoratality;
	}
	// Random Group Network
	public void randomGroupNetwork(){
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		Context context = ContextUtils.getContext(this);
	    Network<Object> network = (Network) context.getProjection("groupNetwork");
	    Grid grid = (Grid) context.getProjection("grid");
		int t = 0;
		int size = (Integer)p.getInteger("groupSize");
		// Iterate over this tempList to connect benes - for computational benefits
	    LinkedList<Bene>tempList = this.patientList;
	    int numOfGroups = (int) Math.round((double)(this.patientList.size()/size-0.5));
	    for (int t1 = 0; t1 < numOfGroups; t1++){
		    LinkedList<Bene> gList = new LinkedList<Bene>();
	    	while (t < size){
	    		int source = RandomHelper.nextIntFromTo(0, tempList.size()-1);
				Bene o = (Bene) tempList.get(source);
				if (network.getDegree(o) == 0){
					
					gList.add(o);
					tempList.remove(source);
					t++;
				}
			}
			for (int i = 0; i < size; i++) {

				for (int j = i+1; j < size; j++){
					
					network.addEdge(gList.get(i),gList.get(j));
					System.out.println("Connect");
				}		
			}
			t = 0;
	    }					
	}
}
