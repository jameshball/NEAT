import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Population {
  private List<Genome> genomes;
  private Map<ConnectionGene, Integer> innovations;

  private final int POPULATION_COUNT;

  public Population(int populationCount, int inputCount, int outputCount) {
    this.POPULATION_COUNT = populationCount;
    this.genomes = new ArrayList<>();
    this.innovations = new HashMap<>();

    for (int i = 0; i < POPULATION_COUNT; i++) {
      genomes.add(new Genome(inputCount, outputCount, innovations));
    }
  }

  public static void addInnovation(ConnectionGene gene, Map<ConnectionGene, Integer> innovations) {
    innovations.put(gene, innovations.size());
  }
}