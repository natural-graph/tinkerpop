package com.tinkerpop.gremlin.process.oltp.filter;

import com.tinkerpop.gremlin.process.Traversal;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class DedupStep<S> extends FilterStep<S> {

    public boolean hasUniqueFunction;

    public DedupStep(final Traversal traversal, final Function<S, ?> uniqueFunction) {
        super(traversal);
        final Set<Object> set = new LinkedHashSet<>();
        if (null == uniqueFunction) {
            this.hasUniqueFunction = false;
            this.setPredicate(holder -> set.add(holder.get()));
        } else {
            this.hasUniqueFunction = true;
            this.setPredicate(holder -> set.add(uniqueFunction.apply(holder.get())));
        }
    }

    public DedupStep(final Traversal traversal) {
        this(traversal, null);
    }
}
