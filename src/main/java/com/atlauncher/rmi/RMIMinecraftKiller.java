package com.atlauncher.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@RMIProxy("atl-mckiller")
public final class RMIMinecraftKiller extends UnicastRemoteObject implements RMIInvokerProxy{
    protected RMIMinecraftKiller()
    throws RemoteException {
        super();
    }

    @Override
    public void invoke()
    throws RemoteException {
        // TODO: Write MC Launch & Land Events
    }
}