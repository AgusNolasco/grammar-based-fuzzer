package fuzzer;

import org.json.simple.JSONObject;

import java.util.List;

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
  public abstract List<String> generate(int n);
}
