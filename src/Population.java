import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

class Population {
  private Genome[] genomes;
  private Map<ConnectionGene, Integer> innovations;

  private final int POPULATION_COUNT;

  public Population(int populationCount, int inputCount, int outputCount) {
    this.POPULATION_COUNT = populationCount;
    this.genomes = new Genome[POPULATION_COUNT];
    this.innovations = new Hashtable<>();

    for (int i = 0; i < POPULATION_COUNT; i++) {
      genomes[i] = new Genome(inputCount, outputCount, innovations);
    }
  }

  // TODO: Implement end-of-generational functions.

  private void crossover(Genome parent1, Genome parent2) {
    List<Connection>[] partitionedConnections = parent1.partitionConnections(parent2, innovations);

    List<Connection> matchingConnections = partitionedConnections[0];
    List<Connection> disjointConnections = partitionedConnections[1];
    List<Connection> excessConnections = partitionedConnections[2];

    // TODO: Need to re-implement node ids since children genomes can have nodes
    //  that are missing (e.g. 1,2,3,4,6,7,8).
  }

  public static void addInnovation(ConnectionGene gene, Map<ConnectionGene, Integer> innovations) {
    if (!innovations.containsKey(gene)) {
      innovations.put(gene, innovations.size());
    }
  }
}