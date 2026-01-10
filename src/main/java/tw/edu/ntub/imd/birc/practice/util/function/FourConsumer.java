package tw.edu.ntub.imd.birc.practice.util.function;

@FunctionalInterface
public interface FourConsumer<F, S, T, FT> {
    void accept(F f, S s, T t, FT ft);
}
