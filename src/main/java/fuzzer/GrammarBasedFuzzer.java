package fuzzer;

import org.json.simple.JSONObject;

public abstract class GrammarBasedFuzzer extends Fuzzer {

  protected JSONObject grammar;

  protected String INITIAL_SYMBOL = "<S>";

  protected void setInitialSymbol(String initialSymbol) {
    INITIAL_SYMBOL = initialSymbol;
  }

}
