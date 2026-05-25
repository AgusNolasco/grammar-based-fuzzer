package fuzzer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleGrammarCoverageFuzzer extends TrackingGrammarCoverageFuzzer {

    public SimpleGrammarCoverageFuzzer(FuzzerConfig config) {
        super(config);
    }

    @Override
    public int chooseNodeExpansion(DerivationTree node, List<List<DerivationTree>> childrenAlternatives) {
        String symbol = node.getSymbolName();
        List<List<DerivationTree>> uncoveredChildren = childrenAlternatives.stream()
                .filter(c -> !coveredExpansions.contains(expansionKey(symbol, c)))
                .collect(Collectors.toList());

        List<Integer> indexMap = new ArrayList<>();
        int i = 0;
        for (List<DerivationTree> c : uncoveredChildren) {
            if (uncoveredChildren.contains(c)) {
                indexMap.add(i);
            }
            i++;
        }

        if (uncoveredChildren.isEmpty()) {
            return super.chooseNodeExpansion(node, childrenAlternatives);
        }
        int index = super.chooseNodeExpansion(node, uncoveredChildren);
        return indexMap.get(index);
    }

}
