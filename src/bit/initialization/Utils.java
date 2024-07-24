package bit.initialization;

public class Utils {
    private static long scopeId = 0;

    public static long newScopeId() {
        return scopeId++;
    }
}
