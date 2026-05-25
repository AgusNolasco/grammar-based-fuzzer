package fuzzer;

import java.util.*;

public class GrammarCoverageFuzzer extends SimpleGrammarCoverageFuzzer {

    public GrammarCoverageFuzzer(FuzzerConfig config) {
        super(config);
    }

    @Override
    public int chooseNodeExpansion(DerivationTree node, List<List<DerivationTree>> childrenAlternatives) {
        String symbol = node.getSymbolName();

        List<Set<String>> newCoverages = newCoverages(node, childrenAlternatives);

        if (newCoverages == null) {
            return super.chooseNodeExpansion(node, childrenAlternatives);
        }

        int maxNewCoverage = newCoverages.stream().map(Set::size).max(Comparator.comparingInt(i -> i)).get();

        List<List<DerivationTree>> childrenWithMaxNewCoverage = new ArrayList<>();
        int idx = 0;
        for (List<DerivationTree> c : childrenAlternatives) {
            if (newCoverages.get(idx).size() == maxNewCoverage) {
                childrenWithMaxNewCoverage.add(c);
            }
            idx++;
        }

        List<Integer> indexMap = new ArrayList<>();
        int i = 0;
        for (List<DerivationTree> c : childrenAlternatives) {
            if (newCoverages.get(i).size() == maxNewCoverage) {
                indexMap.add(i);
            }
            i++;
        }

        int newChildrenIndex = super.chooseNodeExpansion(node, childrenWithMaxNewCoverage);
        List<DerivationTree> newChildren = childrenWithMaxNewCoverage.get(newChildrenIndex);
        String key = expansionKey(symbol, newChildren);
        coveredExpansions.add(key);
        return indexMap.get(newChildrenIndex);
    }

    private List<Set<String>> newCoverages(DerivationTree node, List<List<DerivationTree>> childrenAlternatives) {
        String symbol = node.getSymbolName();
        for (int maxDepth = 0; maxDepth < grammar.size(); maxDepth++) {
            List<Set<String>> newCoverages = new ArrayList<>();
            for (List<DerivationTree> c : childrenAlternatives) {
                newCoverages.add(newChildCoverage(symbol, c, (double) maxDepth));
            }
            int maxNewCoverage = newCoverages.stream().map(Set::size).max(Comparator.comparingInt(i -> i)).get();
            if (maxNewCoverage > 0) {
                return newCoverages;
            }
        }

        return null;
    }

    private Set<String> newChildCoverage(String symbol, List<DerivationTree> children, Double maxDepth) {
        Set<String> newCov = newChildCoverage0(children, maxDepth);
        newCov.add(expansionKey(symbol, children));
        newCov.removeAll(expansionCoverage());
        return newCov;
    }

    private Set<String> newChildCoverage0(List<DerivationTree> children, Double maxDepth) {
        Set<String> newCov = new HashSet<>();
        for (DerivationTree c : children) {
            String symbol = c.getSymbolName();
            if (grammar.containsKey(symbol)) {
                newCov.addAll(maxExpansionCoverage(symbol, maxDepth));
            }
        }
        return newCov;
    }


}
