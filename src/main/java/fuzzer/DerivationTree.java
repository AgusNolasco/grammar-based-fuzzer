package fuzzer;

import java.util.List;
import java.util.stream.Collectors;

public class DerivationTree {

    private final String symbol_name;
    private List<DerivationTree> children;

    public DerivationTree(String symbol_name, List<DerivationTree> children) {
        this.symbol_name = symbol_name;
        this.children = children;
    }

    public String get_symbol_name() {
        return symbol_name;
    }

    public List<DerivationTree> get_children() {
        return children;
    }

    public String toString() {
        if (children != null) {
            if (children.isEmpty()) {
                return symbol_name;
            } else {
                return children.stream().map(DerivationTree::toString).collect(Collectors.joining(" "));
            }
        } else {
            return symbol_name;
        }
    }

}
