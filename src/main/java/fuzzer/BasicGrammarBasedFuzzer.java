package fuzzer;

import org.json.simple.JSONArray;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicGrammarBasedFuzzer extends GrammarBasedFuzzer {

    private static final int MAX_NONTERMINALS = 6;
    private static final int MAX_EXPANSION_TRIALS = 100;
    private static final String RE_NONTERMINAL = "(<[^<> ]*>)";

    protected Random rand;

    public BasicGrammarBasedFuzzer(FuzzerConfig config) {
        grammar = config.getGrammar();
        rand = new Random(config.getSeed());
        verbose = config.isVerbose();
    }

    /**
     * Fuzz the grammar and returns a valid expression
     */
    @SuppressWarnings("unchecked")
    public String generate() {
        String term = INITIAL_SYMBOL;
        int expansion_trials = 0;
        List<String> non_terminals = nonTerminals(term);
        while (!non_terminals.isEmpty()) {
            String symbol_to_expand = non_terminals.get(rand.nextInt(non_terminals.size()));
            List<Object> expansions = (List<Object>) grammar.get(symbol_to_expand);
            String expansion = (String) expansions.get(rand.nextInt(expansions.size()));
            String new_term = term.replaceFirst(symbol_to_expand, expansion);
            non_terminals = nonTerminals(new_term);
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
    public List<String> generate(int n) {
        if (n <= 0) throw new IllegalArgumentException("The amount of expressions to fuzz must be a positive number");
        List<String> fuzzed = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            fuzzed.add(generate());
        }
        return fuzzed;
    }

    /**
     * Get the amount of non-terminals symbols of the given symbol
     */
    protected List<String> nonTerminals(String symbol) {
        List<String> matches = new LinkedList<String>();
        Matcher m = Pattern.compile("(?=(" + RE_NONTERMINAL + "))").matcher(symbol);
        while (m.find()) {
            matches.add(m.group(1));
        }
        return matches;
    }

}
