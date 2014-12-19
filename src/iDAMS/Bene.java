package iDAMS;

import iDAMS.PCP.Intervention;
import iDAMS.PCP.interventionType;
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
	public double weightDecay;
	public double socialInfluence;
	public double socialSupport;
	
	public Bene(int id){
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		this.id = id;
		this.health = RandomHelper.nextIntFromTo(0, 1);
		this.behavior = RandomHelper.nextDoubleFromTo(0, 1); 
		/*int dist = (Integer)p.getInteger("IniHealthBehDist");
		// Initial Health Behavior Assignment
		if (dist == 0){ // Random Uniform for Health Behavior
			
			this.behavior = RandomHelper.nextDoubleFromTo(0, 1); 
		}
		else if (dist == 1){ // Beta Distribution for Health Behavior
			
			double alpha = (Double)p.getDouble("betaAlpha");
			double beta = (Double)p.getDouble("betaBeta");
			this.behavior = RandomHelper.createBeta(alpha, beta).nextDouble();
		}*/
		
		this.duration = 0;
		this.visits = 0;
		this.threshold = RandomHelper.nextDoubleFromTo((Double)p.getDouble("min_threshold"),(Double)p.getDouble("max_threshold")); //Threshold for seeking treatment
		this.sList = new LinkedList<Bene>(); //Social Network
		this.pList = new LinkedList<PCP>(); //Provider List
		this.interventionList = new LinkedList<Intervention>();
		// Parameterize this Min/Max of rate
		this.susceptibility = RandomHelper.nextDoubleFromTo(0.1, 0.3);
		this.selfEfficacy = RandomHelper.nextDoubleFromTo(0.1, 0.3);
		this.weightDecay = RandomHelper.nextDoubleFromTo(0.90, 0.99);
		// Social Influences
		this.socialInfluence = 0;
		this.socialSupport = 0;
	}
	
	//Step function - at every time tick agents run it after they are shuffled - You can add priority to the agent types.
	@ScheduledMethod(start = 1, interval = 1, shuffle = true, priority = 2)
	public void step(){
		// Get Context 
		Context context = ContextUtils.getContext(this);
	    Network<Bene> gNetwork = (Network<Bene>) context.getProjection("groupNetwork");
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
		if (this.sList.size()>0){
			for (Iterator<Bene> iterator = this.sList.iterator(); iterator.hasNext();) {
							
				// Interact here
				Bene bene = (Bene)iterator.next();
				this.socialInfluence = this.socialInfluence  + (bene.behavior-this.behavior);
			}
			this.socialInfluence = this.socialInfluence/this.sList.size();	
		}
		// Social Support
		if (gNetwork.getDegree(this) > 0){
			for(Iterator<RepastEdge<Bene>> iterator =  gNetwork.getEdges(this).iterator(); iterator.hasNext();) {
				
				RepastEdge<Bene> b = iterator.next();
				double weight = b.getWeight();
				double behavior = b.getTarget().behavior;
				this.socialSupport = this.socialSupport + weight*behavior;	
				// Revise strength after using it
				b.setWeight(weight*this.weightDecay);
			}
			this.socialSupport = this.socialSupport/gNetwork.getDegree(this);
		}

		// Planned Behavior simple implementation
		this.behavior = this.behavior + 0.5*(1-this.behavior)*(this.selfEfficacy*this.socialSupport+this.socialInfluence*this.susceptibility);	
		// Seek treatment
		int s = (Integer)p.getInteger("stateSympthom");
		int d = (Integer)p.getInteger("stateDeath");
		if (this.behavior>this.threshold&&this.health != d){
			
			if(this.health>=s && pList.getFirst().aco.stateTransitions[this.health][this.health+1] < RandomHelper.nextDouble()){
				
				this.health++;
				this.pList.getFirst().cList.add(this);	
				this.visits++;
			}
		}
	}
	public double getBehavior(){
		
		return this.behavior;	
	}
	public double getHealth(){
		
		return this.health;	
	}
	public double getVisits(){
		
		return this.visits;	
	}
	public double getId(){
		
		return this.id;	
	}
}
