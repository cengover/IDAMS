package iDAMS;

import iDAMS.ACO.BillType;
import iDAMS.PCP.Intervention;
import iDAMS.PCP.interventionType;

import java.util.ArrayList;
import java.util.Collection;
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

public class Bene {
	
	public int id;
	public int health;
	public double behavior;
	public double duration;
	public int visits;
	double threshold;
	public LinkedList<Bene> sList;
	public LinkedList<PCP> pList;
	public LinkedList<Intervention> interventionList; 
	public double susceptibility;
	public double selfEfficacy;
	//public State progress;
	public double socialInfluence;
	public double socialSupport;
	
	public Bene(int id){
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		this.id = id;
		this.health = RandomHelper.nextIntFromTo(0, 1);
		this.behavior = RandomHelper.nextDoubleFromTo(0, 1); // Health Behavior;
		this.duration = 0;
		this.visits = 0;
		this.threshold = RandomHelper.nextDoubleFromTo((Double)p.getDouble("min_threshold"),(Double)p.getDouble("max_threshold")); //Threshold for seeking treatment
		this.sList = new LinkedList<Bene>(); //Social Network
		this.pList = new LinkedList<PCP>(); //Provider List
		this.interventionList = new LinkedList<Intervention>();
		// Parameterize this Min/Max susceptibility rates
		this.susceptibility = RandomHelper.nextDoubleFromTo(0.1, 0.3);
		//this.progress = new State();
		//this.progress.index = this.health;
		this.socialInfluence = 0;
		this.socialSupport = 0;
		this.selfEfficacy = 0;
	}
	
	//Step function - at every time tick agents run it after they are shuffled - You can add priority to the agent types.
	@ScheduledMethod(start = 1, interval = 1, shuffle = true, priority = 2)
	public void step(){
		
		// Get Context 
		Context context = ContextUtils.getContext(this);
	    Network<Bene> gNetwork = (Network) context.getProjection("groupNetwork");
		// Get Scheduler
		Schedule schedule= (Schedule) RunEnvironment.getInstance().getCurrentSchedule();
	    // Get parameters
		Parameters p = RunEnvironment.getInstance().getParameters();	
		// Interventions
		if ((Integer)p.getInteger("controlledGroup") == 0){
			
			while (this.interventionList.isEmpty()==false){
				
				//Iterator<Intervention> iterator = this.interventionList.iterator();
				// Interact here
				Intervention inter = interventionList.getFirst();
				if (inter.intType==interventionType.offline){
					
					this.behavior=this.behavior*p.getDouble("impact_Offline");
				}			
				else if (inter.intType==interventionType.online){
					
					this.behavior=this.behavior*p.getDouble("impact_Online");
				}
				else if (inter.intType==interventionType.onsite){
					
					this.behavior=this.behavior*p.getDouble("impact_Onsite");;
				}
				interventionList.removeFirst();
			}
		}
		// Social Influence Mechanism
		double total = 0;
		if (this.sList.size()>0){
			for (Iterator<Bene> iterator = this.sList.iterator(); iterator.hasNext();) {
							
				// Interact here
				Bene bene = (Bene)iterator.next();
				this.socialInfluence = this.socialInfluence  + (bene.behavior-this.behavior);
			}
			this.socialInfluence = this.socialInfluence/this.sList.size();		
		}
		// Social Support
		ArrayList<RepastEdge<Bene>> myNodeList = new ArrayList<RepastEdge<Bene>>((Collection<? extends RepastEdge<Bene>>) gNetwork.getEdges(this));
		for(Iterator<RepastEdge<Bene>> iterator = myNodeList.iterator(); iterator.hasNext();) {
			
			this.socialSupport = this.socialSupport + iterator.next().getWeight()*iterator.next().getTarget().behavior;
			System.out.println(this.socialSupport);
		}
		// Planned Behavior simple implementation
		this.behavior = this.behavior + 0.5*(1-this.behavior)*(this.selfEfficacy*this.socialSupport+this.socialInfluence*this.susceptibility);		
		// Seek treatment
		/*if (this.behavior>this.threshold){
			
			int s = (Integer)p.getInteger("stateSympthom");
			
			if(this.progress.index>=s && this.progress.rate < RandomHelper.nextDouble()){
				
				this.progress.index++;
				this.pList.getFirst().cList.add(this);	
				this.visits++;
			}
		} */
	}
	
	public double getBehavior(){
		
		return this.behavior;	
	}
	public double getHealth(){
		
		//return this.progress.index;	
		return this.health;	
	}
	public double getVisits(){
		
		return this.visits;	
	}
	
	public class State {
		
		public String state;
		public int index;
		public double rate;
		
		public State() {
			
			this.state = "State";
			this.index = 0;
			this.rate = RandomHelper.nextDoubleFromTo(0, 1);
		}
	}
}
