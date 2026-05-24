package fuzzer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicGrammarBasedFuzzer extends GrammarBasedFuzzer {

  private static final int MAX_NONTERMINALS = 6;
  private static final int MAX_EXPANSION_TRIALS = 100;
  protected final String INITIAL_SYMBOL = "<S>";
  private static final String RE_NONTERMINAL = "(<[^<> ]*>)";

  protected Random rand;

  public BasicGrammarBasedFuzzer(String grammar_file_name) {
    try {
      grammar = read_grammar(grammar_file_name);
      rand = new Random();
    } catch (Exception e) {
      throw new IllegalArgumentException("Unable to read grammar: " + grammar_file_name);
    }
  }

  public BasicGrammarBasedFuzzer(JSONObject grammar, int seed) {
    rand = new Random(seed);
    JSONParser parser = new JSONParser();
    try {
      this.grammar = (JSONObject) parser.parse(grammar.toJSONString());
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Fuzz the grammar and returns a valid expression
   */
  public String fuzz() {
    String term = INITIAL_SYMBOL;
    int expansion_trials = 0;
    List<String> non_terminals = nonterminals(term);
    while (non_terminals.size() > 0) {
      String symbol_to_expand = non_terminals.get(rand.nextInt(non_terminals.size()));
      JSONArray expansions = (JSONArray) grammar.get(symbol_to_expand);
      String expansion = (String) expansions.get(rand.nextInt(expansions.size()));
      String new_term = term.replaceFirst(symbol_to_expand, expansion);
      non_terminals = nonterminals(new_term);
      if (non_terminals.size() < MAX_NONTERMINALS) {
        term = new_term;
        expansion_trials = 0;
      } else {
        expansion_trials += 1;
        if (expansion_trials >= MAX_EXPANSION_TRIALS)
          throw new IllegalStateException("Can't expand " + term);
      }
    }
    return term;
  }


  /**
   * Fuzz the grammar the given number of times
   */
  public Set<String> fuzz(int n) {
    if (n <= 0) throw new IllegalArgumentException("The amount of expressions to fuzz must be a positive number");
    Set<String> fuzzed = new HashSet<>();
    for (int i = 0; i < n; i++) {
      fuzzed.add(fuzz());
    }
    return fuzzed;
  }

  /**
   * Get the amount of non terminals symbols of the given symbol
   */
  protected List<String> nonterminals(String symbol) {
    List<String> matches = new LinkedList<String>();
    Matcher m = Pattern.compile("(?=(" + RE_NONTERMINAL + "))").matcher(symbol);
    while (m.find()) {
      matches.add(m.group(1));
    }
    return matches;
  }

  /**
   * Load the grammar
   * 
   * @throws ParseException
   */
  private JSONObject read_grammar(String grammar_file_name) throws IOException, ParseException {
    BufferedReader reader = new BufferedReader(new FileReader(grammar_file_name));
    StringBuilder stringBuilder = new StringBuilder();
    String line = null;
    while ((line = reader.readLine()) != null) {
      stringBuilder.append(line);
    }
    reader.close();

    String content = stringBuilder.toString();
    JSONParser parser = new JSONParser();
    return (JSONObject) parser.parse(content);
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      throw new IllegalArgumentException("Only the fully grammar file name is expected");
    }
    String grammar_file = args[0];
    BasicGrammarBasedFuzzer bf = new BasicGrammarBasedFuzzer(grammar_file);
    System.out.println(bf.fuzz());
  }

}
