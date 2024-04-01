package Application;

import framework.*;
import java.util.HashMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
//import org.apache.commons.lang3.ObjectUtils.Null;


@ToString
@EqualsAndHashCode
public class KVStore implements Application {

    public interface KVStoreCommand extends Command {
    }

    public interface SingleKeyCommand extends KVStoreCommand {
        String key();
    }

    @Data
    public static final class Get implements SingleKeyCommand {
        @NonNull private final String key;

        @Override
        public boolean readOnly() {
            return true;
        }

        @Override
        public String key() {
            return key;
        }
    }

    @Data
    public static final class Put implements SingleKeyCommand {
        @NonNull private final String key, value;

        @Override
        public String key() {
            return key;
        }
    }

    @Data
    public static final class Append implements SingleKeyCommand {
        @NonNull private final String key, value;

        @Override
        public String key() {
            return key;
        }
    }

    public interface KVStoreResult extends Result {
    }

    @Data
    public static final class GetResult implements KVStoreResult {
        @NonNull private final String value;
    }

    @Data
    public static final class KeyNotFound implements KVStoreResult {
    }

    @Data
    public static final class PutOk implements KVStoreResult {
    }

    @Data
    public static final class AppendResult implements KVStoreResult {
        @NonNull private final String value;
    }

    // Your code here...
    private HashMap<String, String> datastore = new HashMap<>();


    @Override
    public KVStoreResult execute(Command command) {
        if (command instanceof Get) {
            Get g = (Get) command;
            // Your code here...
            if (datastore.containsKey(g.key)) {
                return new GetResult(datastore.get(g.key));
            }

            return new KeyNotFound();
        }

        if (command instanceof Put) {
            Put p = (Put) command;
            // Your code here...
            datastore.put(p.key, p.value);

            return new PutOk();
        }

        if (command instanceof Append) {
            Append a = (Append) command;
            // Your code here...

            String currValue = datastore.get(a.key);

            if (currValue != null) {
                currValue = currValue + a.value;
            }
            else {
                currValue = a.value;
            }

            datastore.put(a.key, currValue);

            return new AppendResult(currValue);
        }

        throw new IllegalArgumentException();
    }
}
