package io.github.cjcool06.safetrade.commands;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class CommandArgs {

    private final Multimap<String, Object> parsedArgs = ArrayListMultimap.create();

    public void put(String key, Object value) {
        parsedArgs.put(key, value);
    }

    public <T> Collection<T> getAll(String key) {
        return (Collection<T>) this.parsedArgs.get(key);
    }

    public <T> Optional<T> getOne(String key) {
        Collection<Object> values = this.parsedArgs.get(key);
        if (values.size() != 1) {
            return Optional.empty();
        }
        return Optional.ofNullable((T) values.iterator().next());
    }

    public void putArg(String key, Object value) {
        checkNotNull(value, "value");
        this.parsedArgs.put(key, value);
    }
}
