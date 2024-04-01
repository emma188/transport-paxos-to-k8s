package Application;

import framework.*;
import java.util.HashMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public final class AMOApplication<T extends Application>
        implements Application {
    @Getter @NonNull private final T application;

    // Your code here...
    private HashMap<String, AMOResult> buffer = new HashMap<>();

    @Override
    public AMOResult execute(Command command) {
        if (!(command instanceof AMOCommand)) {
            throw new IllegalArgumentException();
        }

        AMOCommand amoCommand = (AMOCommand) command;

        // Your code here...
        if (alreadyExecuted(amoCommand)) {
            return buffer.get(amoCommand.getAddress());
        }

        Result res = application.execute(amoCommand.getCommand());
        AMOResult amoResult = new AMOResult(res, amoCommand.getSequenceNum());
        buffer.put(amoCommand.getAddress(), amoResult);

        return amoResult;
    }

//    public HashMap<Address, AMOResult> getBuffer() {
//        return buffer;
//    }
//    public HashMap<String, String> getStore() {
//        return application.getStore();
//    }
//
//    public void setBuffer(HashMap<Address, AMOResult> buffer) {
//        this.buffer = new HashMap<>(buffer);
//
//    }

    public Result executeReadOnly(Command command) {
        if (!command.readOnly()) {
            throw new IllegalArgumentException();
        }

        if (command instanceof AMOCommand) {
            return execute(command);
        }

        return application.execute(command);
    }

    public boolean alreadyExecuted(AMOCommand amoCommand) {
        // Your code here...

        if (!buffer.containsKey(amoCommand.getAddress())) {
            return false;
        }

        AMOResult lastResult = buffer.get(amoCommand.getAddress());

        if (lastResult.getSequenceNum() >= amoCommand.getSequenceNum())
        {
            return true;
        }

        return false;
    }
}
