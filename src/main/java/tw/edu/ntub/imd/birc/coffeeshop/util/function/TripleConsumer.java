package tw.edu.ntub.imd.birc.coffeeshop.util.function;

@FunctionalInterface
public interface TripleConsumer<F, S, T> {
    void accept(F f, S s, T t);
}

