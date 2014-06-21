import com.atlauncher.rmi.RMIInvokerProxy;
import com.atlauncher.rmi.RMIRegistry;

import java.rmi.registry.Registry;

public final class Test{
    public static void main(String... args)
    throws Exception{
        Registry registry = RMIRegistry.local();
        RMIInvokerProxy relauncher = RMIRegistry.lookup(registry, "atl-relauncher");
        relauncher.invoke();
    }
}