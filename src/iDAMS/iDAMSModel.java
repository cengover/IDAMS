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
		
		// Create the social Network = Un-directed
		Network<Object> sNetwork = NetworkFactoryFinder.createNetworkFactory(null).createNetwork(
				"socialNetwork", context, false);
		
		// Create the Bene-Provider Network = Un-directed
		Network<Object> b2pNetwork = NetworkFactoryFinder.createNetworkFactory(null).createNetwork(
				"bene2PCPNetwork", context, false);
		
		// Create the initial ACO and add to the context.
		ACO aco = new ACO(1);
		context.add(aco);
		grid.moveTo(aco,0,0);
			
		// Create the initial benes and add to the context. 
		for(int i = 0; i < benes; i++){
			
			Bene bene = new Bene(i);
			context.add(bene);
			grid.moveTo(bene,1,0);
		}
		
		// Create the initial PCPs and add to the context. 
		for(int i = 0; i < PCPs; i++){
			
			PCP pcp = new PCP(i,aco);
			aco.pcpList.add(pcp);
			context.add(pcp);
			grid.moveTo(pcp,2,0);
		}
		// Random Directed Graph among bene population
		randomStaticSocialNetwork(grid, sNetwork, links);
		// Connect benes and providers
		randomStaticBeneProviderNetwork(grid, b2pNetwork);
		return context;			
	}
	// Random Directed Graph among bene population
	public void randomStaticSocialNetwork(Grid grid,Network sNetwork, int links){
		
		Bene source = new Bene(1);
		Bene target = new Bene(1);
		for (Object o: grid.getObjectsAt(1,0)){
			
			if (o instanceof Bene){
				source = (Bene)o;
				// Loop until the number of connections is satisfied
				int con = source.sList.size();
				while (con < links){
					// Get a bene object
					Object t = grid.getRandomObjectAt(1,0);
					if (t instanceof Bene){
						target = (Bene)t;
						// If selected agent is not a neighbor and itself
						if (((Bene) o).sList.contains(t) == false && source!=target && target.sList.size()<5){
							
							sNetwork.addEdge(source, target);
							((Bene) source).sList.add((Bene) target);
							((Bene) target).sList.add((Bene) source);
							con++;
						}		
					}			
				}						
			}
		}
	}
	// Random Graph among benes and providers
	public void randomStaticBeneProviderNetwork(Grid grid,Network b2pNetwork){
		
		for (Object o: grid.getObjects()){
			
			if (o instanceof Bene){
				
				Provider pro  =  (Provider)grid.getRandomObjectAt(2,0);
				b2pNetwork.addEdge(o, pro);
				((Bene) o).pList.add((PCP) pro);
			}
		}
	}
}
