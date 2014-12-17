package iDAMS;

import java.util.Iterator;
import java.util.LinkedList;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.graph.Network;
import repast.simphony.util.ContextUtils;

public class ACO {
	
	int id;
	public LinkedList<PCP> pcpList;
	public LinkedList<Bill> outstandingBills; 
	// Interventions
	public double offline_rate; // E-mail, FB
	public double online_rate; // Tele-monitoring
	public double onsite_rate; // Education by community workers
	public double offline_cost; // Cost of offline messaging
	public double online_cost; // Cost of tele-monitoring
	public double onsite_cost;// Cost of education by community workers
	public double visit_cost; // Cost of care to patient
	
	public ACO(int id){
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		this.id = id;
		this.pcpList = new LinkedList<PCP>();
		this.outstandingBills= new LinkedList<Bill>();
		this.offline_rate = (Double)p.getDouble("offline_rate");
		this.online_rate = (Double)p.getDouble("online_rate");
		this.onsite_rate = (Double)p.getDouble("onsite_rate");
		this.offline_cost = (Double)p.getDouble("offline_cost");
		this.online_cost = (Double)p.getDouble("online_cost");
		this.onsite_cost = (Double)p.getDouble("onsite_cost");
		this.visit_cost = (Double)p.getDouble("visit_cost");	
	}
	public class Bill {
		public PCP pcp;
		public Bene bene;
		public double cost;
		public BillType costType;
		
		public Bill(Bene patient,PCP provider, double cost, BillType cType) {
			this.pcp = provider;
			this.cost = cost;
			this.costType = cType;
			this.bene = patient;		
		}
	}
	public enum BillType {
		offline,
		online,
		onsite,
		visit
	}
	public void bill(Bene patient, PCP provider, double cost, BillType costType) {
		
		outstandingBills.add(new Bill(patient,provider, cost, costType));
	}
	@ScheduledMethod(start = 1, interval = 1, shuffle = true, priority = 10)
	public void step(){
		
	}
	
	public double getCost(){
		double cost = 0;
		for (Iterator<Bill> iterator = this.outstandingBills.iterator(); iterator.hasNext();) {	
			
			Bill b = iterator.next();
			if (b.costType!=BillType.visit){
				
				cost = cost + b.cost;
			}
		}
		return cost;
	}
	
	public double getOfflineCost(){
		double cost = 0;
		for (Iterator<Bill> iterator = this.outstandingBills.iterator(); iterator.hasNext();) {	
			
			Bill b = iterator.next();
			if (b.costType==BillType.offline){
				
				cost = cost + b.cost;
			}
			
		}
		return cost;
	}
	
	public double getOnlineCost(){
		double cost = 0;
		for (Iterator<Bill> iterator = this.outstandingBills.iterator(); iterator.hasNext();) {	
			
			Bill b = iterator.next();
			if (b.costType==BillType.online){
				
				cost = cost + b.cost;
			}
			
		}
		return cost;
	}
	public double getOnsiteCost(){
		double cost = 0;
		for (Iterator<Bill> iterator = this.outstandingBills.iterator(); iterator.hasNext();) {	
			
			Bill b = iterator.next();
			if (b.costType==BillType.onsite){
				
				cost = cost + b.cost;
			}
			
		}
		return cost;
	}
	public double getVisitCost(){
		double cost = 0;
		for (Iterator<Bill> iterator = this.outstandingBills.iterator(); iterator.hasNext();) {	
			
			Bill b = iterator.next();
			if (b.costType==BillType.visit){
				
				cost = cost + b.cost;
			}		
		}
		return cost;
	}
}
