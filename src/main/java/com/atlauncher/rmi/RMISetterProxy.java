package com.atlauncher.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Used to set a value remotely
 *
 * @param <T>
 *     The single type parameter to pass along data
 */
public interface RMISetterProxy<T> extends Remote {
    public void set(T t) throws RemoteException;
}