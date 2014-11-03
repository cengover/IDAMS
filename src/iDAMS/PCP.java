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
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class PCP implements Provider {
	
	public int id; // Identifier of provider
	double intervention_budget; // Cumulative intervention cost
	int total_patients; // Cumulative number of patients served
	int distinct_patients; // Distinct number of patients who are served
	// List of patients in the line
	public LinkedList<Bene> cList;
	public ACO aco;
	public int countMoratality;

	
	public PCP(int id, ACO a){
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		this.id = id;
		this.intervention_budget = 0;
		this.total_patients = 0;
		this.distinct_patients = 0;
		this.cList = new LinkedList<Bene>();
		this.aco = a;
		this.countMoratality = 0;
	}
	
	// Step function - at every time tick agents run it after they are shuffled - You can add priority to the agent types.
	@ScheduledMethod(start = 1, interval = 1, shuffle = true, priority = 0)
	public void step(){
		
		// Get Scheduler
		Schedule schedule= (Schedule) RunEnvironment.getInstance().getCurrentSchedule();
		// Get Context
		Context context = ContextUtils.getContext(this);
	    Grid grid = (Grid) context.getProjection("grid");
	    // Get parameters
		Parameters p = RunEnvironment.getInstance().getParameters();
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
		
		// Dynamic Network?
		if ((Integer)p.getInteger("controlledGroup") == 1){
			// Fixed or mixed?
			if ((Integer)p.getInteger("mixed") == 1){
			// Random?
				if ((Integer)p.getInteger("mixingStyle") == 1){
					
					
				}
				
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
		
		return this.countMoratality;
	}
	// Group formation
	public void randomReciprocalNetwork(){
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		Context context = ContextUtils.getContext(this);
	    Network gNetwork = (Network) context.getProjection("gNetwork");
	    Grid grid = (Grid) context.getProjection("grid");
		Bene source = new Bene(1);
		Bene target = new Bene(1);
		gNetwork.removeEdges();	
		int t = 0;
		int size = (Integer)p.getInteger("groupSize");
	    LinkedList gList = new LinkedList<Bene>();
	    // Get 10 members
		while (t < size){
			
			Bene o = (Bene) grid.getRandomObjectAt(1,0);
			if (gNetwork.getDegree(o) == 0){
				
				gList.add(o);
				t++;
			}
		}
		for (int i = 0; i < size; i++) {

			for (int j = i; j < size; j++){
				
				gNetwork.addEdge(gList.get(i),gList.get(j));
			}		
		}
		// Do it for the number of groups						
	}
}
