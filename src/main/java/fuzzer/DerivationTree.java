package fuzzer;

import java.util.List;
import java.util.stream.Collectors;

public class DerivationTree {

    private final String symbolName;
    private List<DerivationTree> children;

    public DerivationTree(String symbol_name, List<DerivationTree> children) {
        this.symbolName = symbol_name;
        this.children = children;
    }

    public String getSymbolName() {
        return symbolName;
    }

    public List<DerivationTree> getChildren() {
        return children;
    }

    public String toString() {
        if (children != null) {
            if (children.isEmpty()) {
                return symbolName;
            } else {
                return children.stream().map(DerivationTree::toString).collect(Collectors.joining(""));
            }
        } else {
            return symbolName;
        }
    }

}
