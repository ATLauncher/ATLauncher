package com.atlauncher.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Used to invoke a method remotely
 */
public interface RMIInvokerProxy extends Remote {
    public void invoke() throws RemoteException;
}