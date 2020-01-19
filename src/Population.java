import java.util.*;

class Population {
  private Genome[] genomes;
  private Map<ConnectionGene, Integer> innovations;
  private List<Species> species;
  private Random rng;

  public static int GENERATION_NUMBER;

  private final int POPULATION_COUNT;

  private static final float INHERITED_GENE_DISABLED_RATE = 0.75f;
  private static final float CROSSOVER_RATE = 0.75f;
  private static final float INTERSPECIES_MATING_RATE = 0.001f;
  private static final float COMPATIBILITY_DISTANCE_THRESHOLD = 3.0f;

  public Population(int populationCount, int inputCount, int outputCount, State state) {
    this.GENERATION_NUMBER = 0;
    this.POPULATION_COUNT = populationCount;
    this.genomes = new Genome[POPULATION_COUNT];
    this.innovations = new Hashtable<>();
    this.species = new ArrayList<>();
    this.rng = new Random();

    species.add(new Species(GENERATION_NUMBER));
    species.get(0).setSize(POPULATION_COUNT);

    for (int i = 0; i < POPULATION_COUNT; i++) {
      genomes[i] = new Genome(inputCount, outputCount, state, innovations);
    }
  }

  // TODO: Ensure this class is idiomatic and efficient.

  public void update() {
    Arrays.stream(genomes).parallel().forEach(Genome::updateState);

    if (allEnded()) {
      System.out.println(fitnessSum() / POPULATION_COUNT);
      nextGeneration();
    }
  }

  public boolean allEnded() {
    for (Genome genome : genomes) {
      if (!genome.hasEnded()) {
        return false;
      }
    }

    return true;
  }

  private void nextGeneration() {
    Genome[] newGenomes = new Genome[POPULATION_COUNT];

    evaluateFitness();

    for (int i = 0; i < POPULATION_COUNT; i++) {
      if (rng.nextFloat() < CROSSOVER_RATE) {
        Genome parent1 = getParent(genomes[i]);
        Genome parent2 = getParent(genomes[i]);

        assert parent1 != null && parent2 != null;

        newGenomes[i] = crossover(parent1, parent2);
      } else {
        newGenomes[i] = genomes[i];
      }

      newGenomes[i].mutate(innovations);
      placeInSpecies(newGenomes[i]);
    }

    genomes = newGenomes;
    GENERATION_NUMBER++;
  }

  // This never explicitly chooses a parent from another species, there is just
  // a chance that a parent is selected from the whole population, instead of
  // one species.
  // TODO: Make more efficient (repeated calls to fitnessSum()).
  private Genome getParent(Genome genome) {
    boolean interspeciesMating = rng.nextFloat() < INTERSPECIES_MATING_RATE;

    float fitnessSum = interspeciesMating ? fitnessSum() : fitnessSum(genome.getSpecies());

    float randomFitnessTotal = rng.nextFloat() * fitnessSum;
    float total = 0;

    for (int i = 0; i < POPULATION_COUNT; i++) {
      if (interspeciesMating || genomes[i].getSpecies() == genome.getSpecies()) {
        total += genomes[i].getFitness();
      }

      if (total >= randomFitnessTotal) {
        return genomes[i];
      }
    }

    return null;
  }

  private void evaluateFitness() {
    float[] bestFitness = new float[species.size()];

    for (Genome genome : genomes) {
      if (species.size() > 1 && species.get(genome.getSpecies()).isStagnant(GENERATION_NUMBER)) {
        genome.setFitness(0);
      } else {
        genome.setFitness(genome.evaluateFitness());
        //genome.setFitness(genome.evaluateFitness() / species.get(genome.getSpecies()).size());

        if (genome.getFitness() > bestFitness[genome.getSpecies()]) {
          bestFitness[genome.getSpecies()] = genome.getFitness();
        }
      }
    }

    for (int i = 0; i < species.size(); i++) {
      species.get(i).bestFitnessInSpecies(bestFitness[i], GENERATION_NUMBER);
    }
  }

  public float fitnessSum() {
    float total = 0;

    for (Genome genome : genomes) {
      total += genome.getFitness();
    }

    return total;
  }

  public float fitnessSum(int species) {
    float total = 0;

    for (Genome genome : genomes) {
      if (genome.getSpecies() == species) {
        total += genome.getFitness();
      }
    }

    return total;
  }

  private Genome crossover(Genome parent1, Genome parent2) {
    assert parent1.INPUT_COUNT == parent2.INPUT_COUNT;
    assert parent1.OUTPUT_COUNT == parent2.OUTPUT_COUNT;

    int inputCount = parent1.INPUT_COUNT;
    int outputCount = parent1.OUTPUT_COUNT;

    List<Connection> childConns = new ArrayList<>();

    int maxNode = 0;

    List<Connection> parent1Conns = parent1.getConnections();
    List<Connection> parent2Conns = parent2.getConnections();

    for (int i = 0; i < parent1Conns.size(); i++) {
      Connection connection1 = parent1Conns.get(i);

      for (int j = 0; j < parent2Conns.size(); j++) {
        Connection connection2 = parent2Conns.get(j);

        if (connection1.getGene().equals(connection2.getGene())) {
          if (connection1.isDisabled() || connection2.isDisabled()) {
            if (rng.nextFloat() < INHERITED_GENE_DISABLED_RATE) {
              connection1.disable();
              connection2.disable();
            } else {
              connection1.enable();
              connection2.enable();
            }
          }

          // Randomly choose a parent
          if (rng.nextBoolean()) {
            childConns.add(connection1);
          } else {
            childConns.add(connection2);
          }

          if (connection1.getOut() > maxNode) {
            maxNode = connection1.getOut();
          }

          parent1Conns.remove(i);
          parent2Conns.remove(j);
        }
      }
    }

    // Since we have removed all matching connections, this adds the disjoint
    // and excess connections.
    if (parent1.evaluateFitness() > parent2.evaluateFitness()) {
      childConns.addAll(parent1Conns);
      return new Genome(inputCount, outputCount, childConns, parent1);
    } else {
      childConns.addAll(parent2Conns);
      return new Genome(inputCount, outputCount, childConns, parent2);
    }
  }

  private void placeInSpecies(Genome genome) {
    Set<Integer> seenSpecies = new HashSet<>();

    int maxSpecies = 0;

    for (Genome rep : genomes) {
      int currentSpecies = rep.getSpecies();

      if (!seenSpecies.contains(currentSpecies)) {
        if (genome.compatibilityDistance(rep, innovations) < COMPATIBILITY_DISTANCE_THRESHOLD) {
          setSpecies(genome, currentSpecies);
          return;
        } else {
          seenSpecies.add(currentSpecies);
        }
      }

      if (currentSpecies > maxSpecies) {
        maxSpecies = currentSpecies;
      }
    }

    setSpecies(genome, maxSpecies + 1);
  }

  private void setSpecies(Genome genome, int newSpecies) {
    if (newSpecies >= species.size()) {
      species.add(new Species(GENERATION_NUMBER));
    }

    species.get(genome.getSpecies()).remove();
    genome.setSpecies(newSpecies);
    species.get(newSpecies).add();
  }

  public static void addInnovation(ConnectionGene gene, Map<ConnectionGene, Integer> innovations) {
    if (!innovations.containsKey(gene)) {
      innovations.put(gene, innovations.size());
    }
  }
}