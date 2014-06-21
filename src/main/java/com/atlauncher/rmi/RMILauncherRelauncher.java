package com.atlauncher.rmi;

import com.atlauncher.App;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@RMIProxy("atl-relauncher")
public final class RMILauncherRelauncher extends UnicastRemoteObject implements RMIInvokerProxy{
    protected RMILauncherRelauncher()
    throws RemoteException {
        super();
    }

    @Override
    public void invoke()
    throws RemoteException {
        App.settings.restartLauncher();
    }
}