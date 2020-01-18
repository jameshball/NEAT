import java.util.*;

class Population {
  private Genome[] genomes;
  private Map<ConnectionGene, Integer> innovations;
  private Random rng;

  private final int POPULATION_COUNT;

  private static final float INHERITED_GENE_DISABLED_RATE = 0.75f;
  private static final float CROSSOVER_RATE = 0.75f;

  public Population(int populationCount, int inputCount, int outputCount) {
    this.POPULATION_COUNT = populationCount;
    this.genomes = new Genome[POPULATION_COUNT];
    this.innovations = new Hashtable<>();
    this.rng = new Random();

    for (int i = 0; i < POPULATION_COUNT; i++) {
      genomes[i] = new Genome(inputCount, outputCount, innovations);
    }
  }

  private Genome crossover(Genome parent1, Genome parent2, State state) {
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
          if (rng.nextFloat() < 0.5) {
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
    if (parent1.evaluateFitness(state) > parent2.evaluateFitness(state)) {
      childConns.addAll(parent1Conns);
      return new Genome(inputCount, outputCount, childConns, parent1.getNodes());
    } else {
      childConns.addAll(parent2Conns);
      return new Genome(inputCount, outputCount, childConns, parent2.getNodes());
    }
  }

  private float adjustedFitness(Genome genome, State state) {
    return genome.evaluateFitness(state) / speciesSize(genome);
  }

  private int speciesSize(Genome genome1) {
    int speciesSize = 0;

    for (Genome genome2 : genomes) {
      if (genome1.getSpecies() == genome2.getSpecies()) {
        speciesSize++;
      }
    }

    return speciesSize;
  }

  public static void addInnovation(ConnectionGene gene, Map<ConnectionGene, Integer> innovations) {
    if (!innovations.containsKey(gene)) {
      innovations.put(gene, innovations.size());
    }
  }
}