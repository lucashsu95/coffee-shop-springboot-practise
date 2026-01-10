package tw.edu.ntub.imd.birc.practice.util.function;


import tw.edu.ntub.imd.birc.practice.util.json.object.ObjectData;

public interface AddObjectDataMapConsumer<K, V> extends FourConsumer<ObjectData, Integer, K, V> {
    @Override
    default void accept(ObjectData objectData, Integer integer, K k, V v) {
        addObject(objectData, integer, k, v);
    }

    void addObject(ObjectData objectData, int index, K k, V v);
}
