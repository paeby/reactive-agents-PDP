package template;

import java.util.ArrayList;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private double pPickup;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);
		
		//We create a list of all possible states. A state is a specific city with a task to an other specific city
		//or a city with no task. 
		ArrayList<State> states = new ArrayList();
		for(City cityFrom: topology.cities()){
			for(City cityTo: topology.cities()){
				if(cityFrom.name != cityTo.name){
					states.add(new State(cityFrom, cityTo, true));
					states.add(new State(cityFrom, cityTo, false));
				}
			}
		}
		
		int costPerKm = 5;
		for(State state: states){
			// the actions are either to pick the packet or to move to an other city (an action for each city)
			int rewardPickup = 0;
			if(state.availableTask){
				rewardPickup = (int) (td.reward(state.from, state.to) - costPerKm*state.from.distanceTo(state.to));
			}
			
			int rewardMove = 0;
			City bestCity = null;
			for(State state2: states){
				int tempReward = 0;
				if(state.from.name != state2.from.name){
					// the probability to arrive to a state with a task is given by td.probability
					// so the probability that there is no task is given by 1-tdprobability
					
					//problem: probability to arrive to a state with a specific task....
					tempReward = 
					
					
				}
			}
			
		}
		

		
		
		
	}
	
	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}
		return action;
	}
	
	
	
	private class State {
		private City from;
		private City to;
		private boolean availableTask;
		
		public State(City f, City t, boolean task){
			from = f;
			to = t;
			availableTask = task;
		}
		
		public City getFrom(){
			return from;
		}
		
		public City getTo(){
			return to;
		}
		
		public boolean getTask(){
			return availableTask;
		}
	}
}
