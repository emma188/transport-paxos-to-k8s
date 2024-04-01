package framework;

import java.io.Serializable;

/**
 * An operation on an {@link Application}.
 */
public interface Command extends Serializable {

    /**
     * @return whether or not the command changes the application's state
     */
    default boolean readOnly() {
        return false;
    }
}
