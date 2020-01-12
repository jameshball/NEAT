import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Population {
  private List<Species> species;
  private Map<ConnectionGene, Integer> innovations;

  private final int POPULATION_COUNT;

  public Population(int populationCount, int inputCount, int outputCount) {
    this.POPULATION_COUNT = populationCount;
    this.species = new ArrayList<>();
    this.innovations = new HashMap<>();


  }

  public static void addInnovation(ConnectionGene gene, HashMap<ConnectionGene, Integer> innovations) {
    innovations.put(gene, innovations.size());
  }
}