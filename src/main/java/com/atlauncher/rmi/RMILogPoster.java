package com.atlauncher.rmi;

import com.atlauncher.LogManager;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@RMIProxy("atl-logger")
public final class RMILogPoster extends UnicastRemoteObject implements RMISetterProxy<String>{
    protected RMILogPoster()
    throws RemoteException {
        super();
    }

    @Override
    public void set(String s)
    throws RemoteException {
        LogManager.info(s);
    }
}