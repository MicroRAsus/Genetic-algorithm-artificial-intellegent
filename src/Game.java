class Game
{
	public static void main(String[] args) throws Exception
	{
		//Use evolveWeights for evolve from random; Use continueEvolveWeights AND provide a generation ARFF file to continue evolving; Use calculateFitnessToFile to calculate fitness of Generation ARFF files in the current working directory
		//Use runTopFighterFromFile to run top fit fighter of the provided generation ARFF file against the reflex agent.
		//Genetic.evolveWeights("fitness.txt");// run loops to evolve best weight. go to genetic class to set evolution stop points. once it returns, it passes each stop point's top fighter weights
		//Genetic.continueEvolveWeights("Gen5.txt", 5, new int[] {10, 15, 25, 50, 75, 100, 150, 200, 250, 300, 400, 500, 600, 700, 800, 1000, 1200, 1400, 1600, 1800, 2000, 2200, 2400, 2600, 2800, 3000}, "fitness.txt");
		//Genetic.continueEvolveWeights("./Gen8500.txt", 8500, 100, 20000, "fitness.txt");
		//Genetic.calculateFitnessToFile("./", "fitness.txt");
		Genetic.runTopFighterFromFile("./Gen900.txt");
	}
}
