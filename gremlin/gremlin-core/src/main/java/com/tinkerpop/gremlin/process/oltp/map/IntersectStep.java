package com.tinkerpop.gremlin.process.oltp.map;

import com.tinkerpop.gremlin.process.Holder;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.oltp.AbstractStep;
import com.tinkerpop.gremlin.process.oltp.util.PipelineRing;
import com.tinkerpop.gremlin.process.oltp.util.SingleIterator;
import com.tinkerpop.gremlin.process.util.GremlinHelper;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class IntersectStep<S, E> extends AbstractStep<S, E> {

    public final PipelineRing<S, E> traversalRing;
    private boolean drainState = false; // TODO: Make an AtomicBoolean?

    @SafeVarargs
    public IntersectStep(final Traversal traversal, final Traversal<S, E>... traversals) {
        super(traversal);
        this.traversalRing = new PipelineRing<>(traversals);
    }

    protected Holder<E> processNextStart() {
        while (true) {
            if (this.drainState) {
                int counter = 0;
                while (counter++ < this.traversalRing.size()) {
                    final Traversal<S, E> traversal = this.traversalRing.next();
                    if (traversal.hasNext()) {
                        return GremlinHelper.getEnd(traversal).next();
                    }
                }
                this.drainState = false;
                this.traversalRing.reset();
            } else {
                final Holder<S> start = this.starts.next();
                this.traversalRing.forEach(p -> p.addStarts(new SingleIterator<>(start.makeSibling())));
                if (this.traversalRing.stream().map(p -> p.hasNext()).reduce(true, (a, b) -> a && b))
                    this.drainState = true;
                else
                    this.traversalRing.stream().forEach(GremlinHelper::iterate);
            }
        }
    }
}
