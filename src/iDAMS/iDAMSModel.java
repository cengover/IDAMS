package iDAMS;

import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkFactoryFinder;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class iDAMSModel implements ContextBuilder<Object>{

	
	public Context<Object> build(Context<Object> context) {	
	
		// Here assign the parameter values
		Parameters p = RunEnvironment.getInstance().getParameters();
		int width = (Integer)p.getValue("width");
		int height = (Integer)p.getValue("height");
		int benes = (Integer)p.getValue("numOfBenes");
		int links = (Integer)p.getValue("numOfLinks");
		int PCPs = (Integer)p.getValue("numOfPCPs");
			
		// Create a new 2D grid on which the agents will move.Multi-occupancy 
		Grid<Object> grid = GridFactoryFinder.createGridFactory(null).createGrid("grid", context, 	
				new GridBuilderParameters<Object>(new WrapAroundBorders(), 
					new RandomGridAdder<Object>(), true, width, height));
		
		// Create the social Network
		Network<Object> sNetwork = NetworkFactoryFinder.createNetworkFactory(null).createNetwork(
				"socialNetwork", context, false);
		
		// Create the social Network
		Network<Object> b2pNetwork = NetworkFactoryFinder.createNetworkFactory(null).createNetwork(
				"bene2PCPNetwork", context, false);
			
		// Create the initial benes and add to the context. 
		for(int i = 0; i < benes; i++){
			
			Bene bene = new Bene("Bene-"+ i);
			context.add(bene);
			grid.moveTo(bene, 0,0);
		}
		// Create the initial PCPs and add to the context. 
		for(int i = 0; i < PCPs; i++){
			
			PCP pcp = new PCP("PCP-"+ i);
			context.add(pcp);
			grid.moveTo(pcp, 0,1);
		}
		// Random Directed Graph among bene population
		Bene source = new Bene(" ");
		Bene target = new Bene(" ");
		for (Object o: grid.getObjectsAt(0,0)){
			
			if (o instanceof Bene){
				source = (Bene)o;
				// Loop until the number of connections is satisfied
				int con = 0;
				while (con < links){
					// Get a bene object
					Object t = grid.getRandomObjectAt(0,0);
					if (t instanceof Bene){
						target = (Bene)t;
						//System.out.println(source.id+" "+target.id);
						// If selected agent is not a neighbor and itself
						if (((Bene) o).sList.contains(t) == false && source!=target){
							
							//System.out.println(source.id+" "+target.id);
							sNetwork.addEdge(source, target);
							((Bene) source).sList.add((Bene) target);
							con++;
						}		
					}			
				}						
			}
		}
		// Connect benes and providers
		for (Object o: grid.getObjects()){
			
			if (o instanceof Bene){
				
				for (Object pro: grid.getObjects()){
					
					if (pro instanceof PCP){
						
						b2pNetwork.addEdge(o, pro);
						((Bene) o).pList.add((PCP) pro);
					}
				}
			}
		}
		return context;			
	}
}
