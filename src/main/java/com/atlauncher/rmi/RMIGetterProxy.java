package com.atlauncher.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This is a proxy for setting up a remote getter
 *
 * @param <T>
 *     The type of object being returned
 */
public interface RMIGetterProxy<T> extends Remote {
    public T get() throws RemoteException;
}