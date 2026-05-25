package fuzzer;

import org.json.simple.JSONArray;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TrackingGrammarCoverageFuzzer extends EfficientGrammarFuzzer {

    protected Set<String> coveredExpansions;
    private Set<String> symbolsSeen;

    public TrackingGrammarCoverageFuzzer(FuzzerConfig config) {
        super(config);
    }

    @Override
    public List<String> generate(int n) {
        resetCoverage();
        List<String> fuzzedTerms = super.generate(n);

        System.out.println("Covered expansions: " + expansionCoverage().size());
        System.out.println("Uncovered expansions: " + missingExpansionCoverage().size());

        return fuzzedTerms;
    }

    public void resetCoverage() {
        coveredExpansions = new HashSet<>();
    }

    public Set<String> maxExpansionCoverage(String symbol, Double maxDepth) {
        symbolsSeen = new HashSet<>();
        Set<String> cov = maxExpansionCoverage0(symbol, maxDepth);
        if (Objects.equals(symbol, INITIAL_SYMBOL)) {
            assert symbolsSeen.size() == grammar.size();
        }
        return cov;
    }

    @SuppressWarnings("unchecked")
    private Set<String> maxExpansionCoverage0(String symbol, Double maxDepth) {
        if (maxDepth <= 0) {
            return new HashSet<>();
        }

        symbolsSeen.add(symbol);

        Set<String> expansions = new HashSet<>();
        for (Object e : (List<Object>) grammar.get(symbol)) {
            String expansion = (String) e;
            expansions.add(expansionKey(symbol, expansion));
            for (String nonTerminal : nonTerminals(expansion)) {
                if (!symbolsSeen.contains(nonTerminal)) {
                    expansions.addAll(maxExpansionCoverage0(nonTerminal, maxDepth-1));
                }
            }
        }

        return expansions;
    }

    public String expansionKey(String symbol, String expansion) {
        return symbol + " -> " + expansion;
    }

    public String expansionKey(String symbol, List<DerivationTree> node) {
        return symbol + " -> " + new DerivationTree(null, node);
    }

    @Override
    public int chooseNodeExpansion(DerivationTree node, List<List<DerivationTree>> childrenAlternatives) {
        int index = super.chooseNodeExpansion(node, childrenAlternatives);
        addCoverage(node.getSymbolName(), childrenAlternatives.get(index));
        return index;
    }

    public void addCoverage(String symbol, List<DerivationTree> newChild) {
        coveredExpansions.add(expansionKey(symbol, newChild));
    }

    public Set<String> expansionCoverage() {
        return coveredExpansions;
    }

    public Set<String> missingExpansionCoverage() {
        Set<String> maxExpansionCoverage = maxExpansionCoverage(INITIAL_SYMBOL, Double.MAX_VALUE);
        maxExpansionCoverage.removeAll(expansionCoverage());
        return maxExpansionCoverage;
    }

}
