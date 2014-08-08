package iDAMS;

import java.util.Iterator;
import java.util.LinkedList;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;

public class Bene {
	
	public String id;
	public int health;
	public int lifestyle;
	public int insurance ;
	public int gene;
	public int hospitalized;
	public int intervention;
	public int diagnosed;
	public double progression;
	public int memory; // How many realizations (internal transition function calls) a bene will keep track of influences
	public int memory_count; // The count of the number of realizations.
	double memory_factor; // Importance given to recent influence
	double tendency;
	double threshold;
	double risk_aversion;
	double duration;
	int influence;
	int total;
	double t_cum;
	double t_queue; // Time spent in the queue at provider
	double t_entry;
	public LinkedList<Bene> sList;
	public LinkedList<PCP> pList;
	
	public Bene(String id){
		
		//Parameters p = RunEnvironment.getInstance().getParameters();
		this.id = id;
		this.health = RandomHelper.nextIntFromTo(0, 1); // Health Status
		this.lifestyle = RandomHelper.nextIntFromTo(0, 1); // Lifestyle
		this.insurance = RandomHelper.nextIntFromTo(0, 1); // Insurance
		this.gene = RandomHelper.nextIntFromTo(0, 1); // Gene
		this.hospitalized = 0;
		this.intervention = 0;
		this.diagnosed = 0;
		this.progression = 0;
		this.memory = 5; 
		this.memory_count = 0;
		this.memory_factor = RandomHelper.nextDoubleFromTo(0, 0.2);
		this.tendency = 0;
		this.threshold = RandomHelper.nextDoubleFromTo(0, 0.2);	
		this.risk_aversion = RandomHelper.nextDoubleFromTo(0, 1);		
		this.influence = 0;
		this.duration = 0;
		this.total = 1;
		this.t_cum = 0;
		this.t_queue = 0;
		this.t_entry = 0;
		this.sList = new LinkedList<Bene>(); // Social network
		this.pList = new LinkedList<PCP>(); // Provider List
	}
	
	// Step function - at every time tick agents run it - You can add priority to the agents after shuffle if you wish.
	@ScheduledMethod(start = 1, interval = 1, shuffle = true, priority = 1)
	public void step(){
		
		// Get Scheduler
		Schedule schedule= (Schedule) RunEnvironment.getInstance().getCurrentSchedule();
		
		// If a patient is not hospitalized
		if (this.hospitalized == 0){	

			if(this.lifestyle == 1){
				// Update Duration
				update_duration();
			}
			
			// Evaluate
			double r = RandomHelper.nextDoubleFromTo(0, 1);
			this.memory_count++;
			tendency = memory_factor*(influence/total)+(1-memory_factor)*tendency;
			if (memory_count == memory){

				memory_count = 0;
				influence = 1;
				total = 1;
			}
			if (r<=tendency){
				
				lifestyle = 1;
			}
			else if (r>tendency){
					
				lifestyle = 0;
			}
			// Signal
			Bene bene = new Bene("");
			for (Iterator<Bene> iterator = this.sList.iterator(); iterator.hasNext();) {
				
				// Interact here
				bene = (Bene)iterator.next();
				bene.influence = bene.influence + this.lifestyle;
				bene.total = bene.total + 1;		
			}
			update_progression();
			// Update Duration
			double a = schedule.getTickCount();
			this.duration = (double)t_cum/(a-this.t_queue);
			// Here we assign health status transitions
			if (progression > (threshold + 0.2*health)){
	
				intervention = 0;
				// If health is not the last stage
				if (health != 3){
	
					health = health + 1;
				}
				if (insurance == 1){
	
					hospitalized = 1;
				}
				// After we add self-efficacy, we will change the condition
				else if (insurance == 0 && RandomHelper.nextDoubleFromTo(0.0,1.0)>risk_aversion){
	
					hospitalized = 1;
				}
				// Visit
				if (this.hospitalized == 1){
					
					this.pList.getFirst().cList.add(this);
					this.t_entry =  schedule.getTickCount();
				}
			}	
		}	
	}

	/// Update duration
	void update_duration(){
		
		Schedule schedule= (Schedule) RunEnvironment.getInstance().getCurrentSchedule();	
		t_cum++;
		// Update Duration
		double a = schedule.getTickCount();
		this.duration = (double)t_cum/(a-this.t_queue);
	}
	
	/// Update progression
	void update_progression(){

		if (t_cum > 0){

			progression = 0.2*gene+0.2*lifestyle-0.2*intervention+0.2*health/3+0.2*duration;
		}
		else{

			progression = 0.2*gene+0.2*lifestyle-0.2*intervention+0.2*health/3;
		}
		if (progression < 0){

			progression = 0;
		}
	}	
}
