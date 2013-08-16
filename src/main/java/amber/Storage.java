package amber;

import amber.data.state.Scope;
import amber.data.state.State;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tudor
 */
public class Storage {

    @State(Scope.GLOBAL)
    public static Map<String, String> recentProjects = new HashMap<String, String>();

    public static void init() {
        // Calls static
    }

    static {
        Amber.getStateManager().registerStateOwner(Storage.class);
    }
}
