package fuzzer;

import java.util.Set;

public abstract class Fuzzer {

  /**
   * Returns an expression from the grammar.
   */
  public abstract String fuzz();

  /**
   * Returns a list of fuzzed expressions from the grammar
   */
  public abstract Set<String> fuzz(int n);
}
