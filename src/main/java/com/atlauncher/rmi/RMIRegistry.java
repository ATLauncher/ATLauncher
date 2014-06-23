package com.atlauncher.rmi;

import java.lang.reflect.Constructor;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.atlauncher.exceptions.ChunkyException;

public final class RMIRegistry {
    /**
     * The port of the RMI server shouldn't be changed unless absolutely needed
     */
    public static final int PORT = 1337;

    private static RMIRegistry instance;

    private final Registry registry;

    private RMIRegistry() {
        try {
            this.registry = LocateRegistry.createRegistry(PORT);
        } catch (RemoteException e) {
            throw new ChunkyException(e);
        }
    }

    /**
     * Only called on a client adapter
     * 
     * @return
     */
    public static Registry local() {
        try {
            return LocateRegistry.getRegistry("localhost", PORT);
        } catch (RemoteException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Remote> T lookup(Registry r, String tag) throws RemoteException,
            NotBoundException {
        return (T) r.lookup(tag);
    }

    /**
     * Since the RMIRegistry is a singleton this is how you retrieve the instance
     * 
     * @return
     */
    public static RMIRegistry instance() {
        return (instance == null ? instance = new RMIRegistry() : instance);
    }

    /**
     * Used to register an RMI object
     * 
     * @param tClass
     * @param <T>
     */
    public <T extends Remote> void register(Class<T> tClass) {
        try {
            if (!tClass.isAnnotationPresent(RMIProxy.class)) {
                throw new IllegalStateException("Cannot register remote object " + tClass.getName()
                        + " because there is no @RMIProxy");
            }
            Constructor<T> tConstructor = tClass.getDeclaredConstructor();
            tConstructor.setAccessible(true);
            T instance = tConstructor.newInstance();
            String tag = tClass.getAnnotation(RMIProxy.class).value();
            this.registry.bind(tag, instance);
        } catch (Exception ex) {
            throw new ChunkyException(ex);
        }
    }
}