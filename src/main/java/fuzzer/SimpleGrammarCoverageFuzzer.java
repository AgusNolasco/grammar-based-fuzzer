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
        List<List<DerivationTree>> uncovered_children = childrenAlternatives.stream()
                .filter(c -> !coveredExpansions.contains(expansion_key(symbol, c)))
                .collect(Collectors.toList());

        List<Integer> indexMap = new ArrayList<>();
        int i = 0;
        for (List<DerivationTree> c : uncovered_children) {
            if (uncovered_children.contains(c)) {
                indexMap.add(i);
            }
            i++;
        }

        if (uncovered_children.isEmpty()) {
            return super.chooseNodeExpansion(node, childrenAlternatives);
        }
        int index = super.chooseNodeExpansion(node, uncovered_children);
        return indexMap.get(index);
    }

}
