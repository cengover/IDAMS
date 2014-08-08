package iDAMS;

import java.util.LinkedList;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class PCP {
	
	public String id; // Identifier of provider
	double busy_time; // Total number of busy time serving
	double service_cost; // Total service cost
	double intervention_budget; // Cumulative intervention cost
	int total_patients; // Cumulative number of patients served
	int distinct_patients; // Distinct number of patients who are served
	// List of patients in the line
	public LinkedList<Bene> cList;
	
	public PCP(String id){
		
		//Parameters p = RunEnvironment.getInstance().getParameters();
		this.id = id;
		this.busy_time = 0;
		this.service_cost = 0;
		this.intervention_budget = 0;
		this.total_patients = 0;
		this.distinct_patients = 0;
		this.cList = new LinkedList<Bene>();
	}
	
	// Step function - at every time tick agents run it after they are shuffled - You can add priority to the agent classes.
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
		if (this.cList.size() > 0){
			// Serve the first patient in the queue
			Bene patient = this.cList.getFirst();
			discharge(patient);
		}
		
		// Print state variables at termination of the simulation.
		if (schedule.getTickCount() == (Integer)p.getValue("endOfSim")){
			
			// Print out and end Run 
			Bene bene = new Bene(" ");
			PCP pcp = new PCP(" ");
			for (Object o: grid.getObjects()){
				
				if(o instanceof Bene){
					bene = (Bene)o;
					System.out.println(bene.id+" "+bene.health+" "+bene.lifestyle+" "+bene.t_queue+" "+bene.t_cum+" "+bene.duration);
				}	
			}
			for (Object o: grid.getObjects()){
				
				if(o instanceof PCP){
					pcp = (PCP)o;
					System.out.println(pcp.id+" "+pcp.total_patients+" "+pcp.distinct_patients+" "+pcp.busy_time+" "+pcp.service_cost+" "+pcp.intervention_budget
							+" "+pcp.cList.size());
				}	
			}	
		}
		RunEnvironment.getInstance().endAt((Integer)p.getValue("endOfSim"));	
	}
	// Discharge Function
	void discharge(Bene b){
		
		Schedule schedule= (Schedule) RunEnvironment.getInstance().getCurrentSchedule();
		if(b.diagnosed == 0){
			
			this.distinct_patients += 1;
		}
		this.total_patients+=1;
		this.service_cost = this.service_cost + b.health*(b.insurance+1.0);
		this.busy_time=this.busy_time+1.0;
		// Changes to the patient
		b.t_queue = b.t_queue + (schedule.getTickCount() - b.t_entry);
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		if (r < 0.2){
			
			b.intervention = 1;
			this.intervention_budget = this.intervention_budget + 0.1;
		}
		b.diagnosed = 1;
		b.hospitalized = 0;
		b.lifestyle = 0;
		if (b.health == 1){
			
			b.health = 0;
		}
		this.cList.removeFirst();
	}
}
