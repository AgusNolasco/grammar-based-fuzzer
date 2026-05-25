package fuzzer;

import org.json.simple.JSONObject;

import java.util.Set;

public abstract class GrammarBasedFuzzer {

  protected JSONObject grammar;

  protected String INITIAL_SYMBOL = "<S>";

  public void setInitialSymbol(String initialSymbol) {
    INITIAL_SYMBOL = initialSymbol;
  }

  /**
   * Returns an expression from the grammar.
   */
  public abstract String generate();

  /**
   * Returns a list of fuzzed expressions from the grammar
   */
  public abstract Set<String> generate(int n);
}
