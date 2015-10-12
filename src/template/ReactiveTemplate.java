package template;

import java.util.ArrayList;

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

	private final double EPSILON = 10000;
	private double costPerKm = 50;

	private ArrayList<State> states = new ArrayList<State>();
	
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);
		System.out.println("hello!");
		
		//We create a list of all possible states. A state is a specific city with a task to an other specific city
		//or a city with no task. 
		for(City cityFrom: topology.cities()){
			for(City cityTo: topology.cities()){
				if(cityFrom.name != cityTo.name){
					states.add(new State(cityFrom, cityTo, true));
				}
			}
			states.add(new State(cityFrom, null, false));
		}
		
		boolean goodEnough = false;
		double vChange = 0;
		
		while(!goodEnough){
			vChange = 0;
			for(State state: states){
				// the actions are either to pick the packet or to move to an other city (an action for each city)
				double rewardPickup = 0;
				if(state.availableTask){
					rewardPickup = reward(state.getFrom(), state.getTo(), td);
				}
				
				double rewardMove = 0;
				City bestCity = null;
				for(City neighbour: state.from.neighbors()){
					// R(s, a)
					double tempRewardWithPacket = - costPerKm * state.from.distanceTo(neighbour);
					double tempRewardWithoutPacket = tempRewardWithPacket;
					for(City nextCity: topology.cities()) {
						if(nextCity.name != neighbour.name) {
							// for an available packet in neighbour to nextCity
							tempRewardWithPacket += td.probability(neighbour, nextCity) * V(neighbour, nextCity, true);
							// for no packet in neighbour to nextCity
							//tempRewardWithoutPacket += (1-td.probability(neighbour, nextCity)) * V(neighbour, null, false);
						}
					}
					tempRewardWithPacket *= discount;
					//tempRewardWithoutPacket *= discount;

					if(tempRewardWithPacket > rewardMove) {
						rewardMove = tempRewardWithPacket;
						bestCity = neighbour;
					}
					else if(tempRewardWithoutPacket > rewardMove) {
						rewardMove = tempRewardWithoutPacket;
						bestCity = neighbour;
					}
				}
				
				if(rewardMove < rewardPickup) {
					double change = Math.abs(rewardPickup-state.getV());
					if(change > vChange){
						vChange = change;
					}
					changeAction(null, state);
					updateV(state, rewardPickup);
				}
				else {
					double change = Math.abs(rewardMove-state.getV());
					if(change > vChange){
						vChange = change;
					}
					changeAction(bestCity, state);
					updateV(state, rewardMove);
				}
			}
			System.out.println(vChange);
			if(vChange < EPSILON){
				goodEnough = true;
			}
		}
	}
	
	private double reward(City from, City to, TaskDistribution td){
		return td.reward(from, to) - costPerKm*from.distanceTo(to);
	}
	
	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null) {
			City currentCity = vehicle.getCurrentCity();
			State currentState = states.get(states.indexOf(new State(currentCity, null, false)));
			action = new Move(currentState.getAction());
		} else {
			action = new Pickup(availableTask);
		}
		return action;
	}
	
	
	
	private class State {
		private City from;
		private City to;
		private boolean availableTask;
		private double V;
		private City bestAction;
		
		public State(City f, City t, boolean task){
			from = f;
			to = t;
			availableTask = task;
			V = 0;
			bestAction = null;
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
		
		public double getV(){
			return V;
		}
		
		public void setV(double newV){
			V = newV;
		}
		
		public City getAction() {
			return bestAction;
		}
		
		public void setAction(City a) {
			bestAction = a;
		}
		
		@Override
		public boolean equals(Object b){
			if (this.getTo() == null && ((State)b).getTo() == null) 
				return (this.getFrom().name == ((State) b).getFrom().name); 
			
			else if (this.getTo() == null ^ ((State)b).getTo() == null) 
				return false;
			  
			else 
				return (this.getFrom().name == ((State) b).getFrom().name) && (this.getTo().name == ((State) b).getTo().name) && (this.getTask() == ((State) b).getTask());
		}
	}
	
	private double V(City from, City to, boolean pack) {
		return states.get(states.indexOf(new State(from, to, pack))).getV();
	}
	
	private void updateV(State state, double newV) {
		states.get(states.indexOf(state)).setV(newV);
	}
	
	private void changeAction(City a, State state) {
		states.get(states.indexOf(state)).setAction(a);
	}
}
