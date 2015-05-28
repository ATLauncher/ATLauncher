package com.atlauncher.collection;

import com.atlauncher.LogManager;
import com.atlauncher.data.Account;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.utils.MojangAPIUtils;

import java.util.LinkedList;

public final class Accounts
extends LinkedList<Account> {
    public Account getByName(String name){
        for(Account acc : this){
            if(acc.getUsername().equalsIgnoreCase(name)){
                return acc;
            }
        }

        return null;
    }

    public void checkUUIDs(){
        LogManager.debug("Checking account UUIDs");
        for(Account acc : this){
            if(acc.isUUIDNull()){
                acc.setUUID(MojangAPIUtils.getUUID(acc.getMinecraftUsername()));
            }
        }
        AccountManager.saveAccounts();
        LogManager.debug("Done checking account UUIDs");
    }

    public void checkForNameChanges(){
        LogManager.info("Checking for username changes");
        boolean changed = false;
        for(Account acc : this){
            if(acc.checkForUsernameChange()){
                changed = true;
                break;
            }
        }

        if(changed){
            AccountManager.saveAccounts();
        }

        LogManager.info("Checking for username changes complete");
    }
}