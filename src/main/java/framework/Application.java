package framework;

import java.io.Serializable;

/**
 * <p>Applications are simple data structures used by your nodes.
 *
 * <p>Applications need not support concurrent access. However, to work with
 * the distributed systems you create, they must be deterministic.
 */
public interface Application extends Serializable {
    Result execute(Command command);
}
