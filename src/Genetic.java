import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class Genetic {
	public static final int SIZE_OF_POPULATION = 100;
	private static final int SIZE_OF_META_PARAMETER = 4;
	private static final int SIZE_OF_WEIGHTS = 291;
	public static final int SIZE_OF_CHROMOSOME = Genetic.SIZE_OF_WEIGHTS + Genetic.SIZE_OF_META_PARAMETER;
	private static final int WINNER_SURVIVAL_CHANCE_POS = SIZE_OF_CHROMOSOME - 4;
	private static final int MUTATION_CHANCE_POS = SIZE_OF_CHROMOSOME - 3;
	private static final int MUTATION_DEVIATION_POS = SIZE_OF_CHROMOSOME - 2;
	private static final int FIGHTS_PER_GEN_POS = SIZE_OF_CHROMOSOME - 1;
	private static final double DISASTER_CHANCE = 0.02;
	
	private static final float WINNER_SURVIVAL_CHANCE = 0.9f; //90% chance
	private static final float MUTATION_CHANCE = 0.8f; //80% chance
	private static final double MUTATION_DEVIATION = 1.5;
	private static final double FIGHTS_PER_GEN = 10.0;
	private static final int[] STOP_POINT = new int[] {1, 5, 10, 15, 25, 50, 75, 100, 150, 200, 250, 300};
	
	private static final Random r = new Random();
	
	public static void evolveWeights(String fitnessFile) throws Exception {
		// Create a random initial population
		Matrix population = new Matrix(Genetic.SIZE_OF_POPULATION, Genetic.SIZE_OF_CHROMOSOME); //100 populations (rows of weights), each row has 291 of weights + 5 meta parameter (chromosomes data)
		for(int i = 0; i < Genetic.SIZE_OF_POPULATION; i++)
		{
			double[] chromosome = population.row(i);
			for(int j = 0; j < chromosome.length - Genetic.SIZE_OF_META_PARAMETER; j++) {
				chromosome[j] = 0.03 * r.nextGaussian();
			}
			chromosome[FIGHTS_PER_GEN_POS] = Genetic.FIGHTS_PER_GEN; //294
			chromosome[MUTATION_DEVIATION_POS] = Genetic.MUTATION_DEVIATION; //293
			chromosome[MUTATION_CHANCE_POS] = Genetic.MUTATION_CHANCE; //292
			chromosome[WINNER_SURVIVAL_CHANCE_POS] = Genetic.WINNER_SURVIVAL_CHANCE; //291
		}

		PrintWriter out = new PrintWriter(new FileWriter(fitnessFile));
		int stopPointIndex = 0;
		for(int i = 0; i < Genetic.STOP_POINT[Genetic.STOP_POINT.length - 1]; i++) { //evolve
			//generate next generation
			naturalSelection(population);
			disaster(population);
			replenish(population);
			diversify(population);
			System.out.println("Gen " + (i + 1) + " done.");
			if(i == Genetic.STOP_POINT[stopPointIndex] - 1) { //save chromosomes of each generation in the stop point array
				population.saveARFF("Gen" + (i + 1) + ".txt");
				System.out.println("Gen " + (i + 1) + ":");
				double[] fitness = new double[population.rows()];
				int top = neuralBattleReflex(population, fitness);
				out.println("Gen " + (i + 1) + " top fitness: " + fitness[top]);
				//Controller.doBattle(new ReflexAgent(), new NeuralAgent(Genetic.getProperWeights(population.row(top))));
				stopPointIndex++;
			}
		}
		out.close();
	}
	
	public static void continueEvolveWeights(String fileName, int start, int[] stopPoints, String fitnessFile) throws Exception {
		Matrix population = new Matrix(Genetic.SIZE_OF_POPULATION, Genetic.SIZE_OF_CHROMOSOME); //100 populations (rows of weights), each row has 291 of weights + 4 meta parameter (chromosomes data)
		population.loadARFF(fileName);
		
		PrintWriter out = new PrintWriter(new FileWriter(fitnessFile, true));
		int stopPointIndex = 0;
		for(int i = start; i < stopPoints[stopPoints.length - 1]; i++) { //evolve
			//generate next generation
			naturalSelection(population);
			disaster(population);
			replenish(population);
			diversify(population);
			System.out.println("Gen " + (i + 1) + " done.");
			if(i == stopPoints[stopPointIndex] - 1) { //save chromosomes of each generation in the stop point array
				population.saveARFF("Gen" + (i + 1) + ".txt");
				System.out.println("Gen " + (i + 1) + ":");
				double[] fitness = new double[population.rows()];
				int top = neuralBattleReflex(population, fitness);
				out.println("Gen " + (i + 1) + " top fitness: " + fitness[top]);
				//Controller.doBattle(new ReflexAgent(), new NeuralAgent(Genetic.getProperWeights(population.row(top))));
				stopPointIndex++;
			}
		}
		out.close();
	}
	
	public static void continueEvolveWeights(String fileName, int start, int forEvery, int end, String fitnessFile) throws Exception {
		Matrix population = new Matrix(Genetic.SIZE_OF_POPULATION, Genetic.SIZE_OF_CHROMOSOME); //100 populations (rows of weights), each row has 291 of weights + 4 meta parameter (chromosomes data)
		population.loadARFF(fileName);
		
		PrintWriter out = new PrintWriter(new FileWriter(fitnessFile, true));
		int genCounter = 0;
		for(int i = start; i < end; i++) { //evolve
			//generate next generation
			naturalSelection(population);
			disaster(population);
			replenish(population);
			diversify(population);
			System.out.println("Gen " + (i + 1) + " done.");
			genCounter++;
			if(genCounter == forEvery) { //save chromosomes of each generation in the stop point array
				population.saveARFF("Gen" + (i + 1) + ".txt");
				System.out.println("Gen " + (i + 1) + ":");
				double[] fitness = new double[population.rows()];
				int top = neuralBattleReflex(population, fitness);
				out.println("Gen " + (i + 1) + " top fitness: " + fitness[top]);
				//Controller.doBattle(new ReflexAgent(), new NeuralAgent(Genetic.getProperWeights(population.row(top))));
				genCounter = 0;
			}
		}
		out.close();
	}
	
	public static void calculateFitnessToFile(String directory, String fitnessFile) throws Exception {
		File root = new File(directory);
		FilenameFilter beginsWith = new FilenameFilter() {
			public boolean accept(File directory, String filename) {
				return filename.startsWith("Gen");
			}
		};

		PrintWriter out = new PrintWriter(new FileWriter(fitnessFile));
        File[] files = root.listFiles(beginsWith);
        for (File f: files) {
        	Matrix population = new Matrix(Genetic.SIZE_OF_POPULATION, Genetic.SIZE_OF_CHROMOSOME);
        	String fileName = f.getName();
    		population.loadARFF(fileName);
    		double[] fitness = new double[population.rows()];
    		int top = neuralBattleReflex(population, fitness);
    		out.println(fileName.substring(0, fileName.length() - 4) + " top fitness: " + fitness[top]);
        }
        out.close();
	}
	
	public static void runTopFighterFromFile(String ARFF) throws Exception {
		Matrix population = new Matrix(Genetic.SIZE_OF_POPULATION, Genetic.SIZE_OF_CHROMOSOME);
		population.loadARFF(ARFF);
		double[] fitness = new double[population.rows()];
		int top = neuralBattleReflex(population, fitness);
		Controller.doBattle(new ReflexAgent(), new NeuralAgent(Genetic.getProperWeights(population.row(top))));
	}
	
	private static double getWinnerSurvivalChance(Matrix population, int index) {
		return population.row(index)[Genetic.WINNER_SURVIVAL_CHANCE_POS];
	}
	
	private static double getMutationChance(Matrix population, int index) {
		return population.row(index)[Genetic.MUTATION_CHANCE_POS];
	}
	
	private static double getMutationDeviation(Matrix population, int index) {
		return population.row(index)[Genetic.MUTATION_DEVIATION_POS];
	}
	
	private static double[] getProperWeights(double[] chromosome) {
		double[] weights = new double[Genetic.SIZE_OF_WEIGHTS];
		System.arraycopy(chromosome, 0, weights, 0, Genetic.SIZE_OF_WEIGHTS); //set proper weights
		return weights;
	}
	
	private static int neuralBattleReflex(Matrix population, double[] fitness) throws Exception {
		ArrayList<IAgent> agents = new ArrayList<IAgent>();
		for(int j = 0; j < population.rows(); j++) {
			agents.add(new NeuralAgent(Genetic.getProperWeights(population.row(j))));
		}
		return Controller.doTournamentAgainst(new ReflexAgent(), agents, fitness, true); //return strongest fighter index
	}
	
	private static void naturalSelection(Matrix population) throws Exception {
		double[] fighter1 = null;
		double[] fighter2 = null;
		int fighter1Index = 0;
		int fighter2Index = 0;
		double winnerSurvivalChance = 0.0;
		int fightPerGen = (int)Math.round(population.columnMean(Genetic.FIGHTS_PER_GEN_POS)); //get average fight per gen from the population
		for(int i = 0; i < fightPerGen; i++) {
			fighter1Index = r.nextInt(population.rows());
			fighter1 = Genetic.getProperWeights(population.row(fighter1Index)); //set fighter1 weights
			while(fighter1Index == (fighter2Index = r.nextInt(population.rows()))) { //prevent fight itself
			}
			fighter2 = Genetic.getProperWeights(population.row(fighter2Index)); //set fighter2 weights
			int result = Controller.doBattleNoGui(new NeuralAgent(fighter1), new NeuralAgent(fighter2)); //fight
			if(result != 0) {
				boolean winnerSurvive = true;
				if(result < 0) {
					winnerSurvivalChance = Math.min(Genetic.getWinnerSurvivalChance(population, fighter2Index), 0.96); //always certain chance to die
				} else {
					winnerSurvivalChance = Math.min(Genetic.getWinnerSurvivalChance(population, fighter1Index), 0.96);
				}
				
				if(r.nextDouble() > winnerSurvivalChance) { //chance of winner dies
					winnerSurvive = false;
				}
				if(result < 0 && winnerSurvive || result > 0 && !winnerSurvive) { //fighter1 lose and die or fighter2 lose but killed fighter1
					population.removeRow(fighter1Index); //kill fighter 1;
				} else if(result > 0 && winnerSurvive || result < 0 && !winnerSurvive) { //fighter2 lose and die or fighter1 lose but killed fighter2
					population.removeRow(fighter2Index); //kill fighter 2;
				}
			} else { //tie, they are very close to each other, we calculate fitness and kill lowest one; this prevents the tendency of agent evolve into trying to tie the match.
				double fighter1Fitness = Controller.agentFitnessBattle(new ReflexAgent(), new NeuralAgent(Genetic.getProperWeights(fighter1)));
				double fighter2Fitness = Controller.agentFitnessBattle(new ReflexAgent(), new NeuralAgent(Genetic.getProperWeights(fighter2)));
				if(fighter1Fitness < fighter2Fitness) {
					population.removeRow(fighter1Index); //kill fighter 1;
				} else {
					population.removeRow(fighter2Index); //kill fighter 2;
				}
			}
		}
	}
	
	private static void disaster(Matrix population) {
		double disasterChance = Genetic.DISASTER_CHANCE + (r.nextInt(3) - 1) / 100.0; //-.01 0 .01
		if(r.nextDouble() < disasterChance) { //disaster
			int mutantCount = population.rows() / 2; // randomly kill half the population
			for(int i = 0; i < mutantCount; i++) {
				int mutationCount = (int) Math.round(r.nextDouble() * 2 + Genetic.SIZE_OF_CHROMOSOME / 2); //~ half of the gene letter will be mutated
				for(int j = 0; j < mutationCount; j++) {
					int mutationIndex = r.nextInt(Genetic.SIZE_OF_CHROMOSOME);
					population.row(i)[mutationIndex] += r.nextGaussian() * Genetic.getMutationDeviation(population, i);
				}
			}
		}
	}
	
	private static void replenish(Matrix population) throws Exception { //implementing interpolation
		int pairsNeeded = Genetic.SIZE_OF_POPULATION - population.rows();
		double[] parent1 = null;
		double[] parent2 = null;
		int parent1Index = 0;
		int parent2Index = 0;
		ArrayList<double[]> children = new ArrayList<double[]>();
		for(int i = 0; i < pairsNeeded; i++) { //mate and add the children to the arraylist till all pairs found
			parent1Index = r.nextInt(population.rows());
			parent1 = population.row(parent1Index);
			while(parent1Index == (parent2Index = r.nextInt(population.rows()))) { //prevent mate with itself
			}
			parent2 = population.row(parent2Index);
			
			double parent1Chance= r.nextDouble(); //parent 2 chance is complement of parent1
			double[] child = new double[Genetic.SIZE_OF_CHROMOSOME];
			for(int j = 0; j < Genetic.SIZE_OF_CHROMOSOME; j++) {
				if(r.nextDouble() < parent1Chance) { //parent 1 gene letter chosen
					child[j] = parent1[j];
				} else { //parent 2 gene chosen
					child[j] = parent2[j];
				}
			}
			children.add(child);
		}
		
		for(double[] child : children) { //add the children to the population
			population.takeRow(child);
		}
	}
	
	private static void diversify(Matrix population) {
		for(int i = 0; i < population.rows(); i++) {
			if(r.nextDouble() < Math.min(Genetic.getMutationChance(population, i), 0.99)) { //mutation chance of that chromosome
				int mutationCount = r.nextInt(3) + 1; //1 to 3 gene letter may be mutated
				for(int j = 0; j < mutationCount; j++) {
					int mutationIndex = r.nextInt(Genetic.SIZE_OF_CHROMOSOME);
					population.row(i)[mutationIndex] += r.nextGaussian() * Genetic.getMutationDeviation(population, i);
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private static int neuralBattleNeural(Matrix population) throws Exception { //this method is extremely slow, because it's running n^2 battles and each of these battle may vary in time.
		ArrayList<IAgent> agents = new ArrayList<IAgent>();
		for(int j = 0; j < population.rows(); j++) {
			agents.add(new NeuralAgent(population.row(j)));
		}
		return Controller.doTournament(agents, false); //return strongest fighter index
	}
}
