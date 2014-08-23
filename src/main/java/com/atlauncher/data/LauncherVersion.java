/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import com.atlauncher.annot.Json;

@Json
public class LauncherVersion{
    private int reserved;
    private int major;
    private int minor;
    private int revision;

    public LauncherVersion(int reserved, int major, int minor, int revision){
        this.reserved = reserved;
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    public int getReserved(){
        return this.reserved;
    }

    public int getMajor(){
        return this.major;
    }

    public int getMinor(){
        return this.minor;
    }

    public int getRevision(){
        return this.revision;
    }

    public boolean needsUpdate(LauncherVersion toThis){
        if(this.reserved > toThis.getReserved()){
            return false;
        } else if(this.reserved < toThis.getReserved()){
            return true;
        } else{
            if(this.major > toThis.getMajor()){
                return false;
            } else if(this.major < toThis.getMajor()){
                return true;
            } else{
                if(this.minor > toThis.getMinor()){
                    return false;
                } else if(this.minor < toThis.getMinor()){
                    return true;
                } else{
                    if(this.revision > toThis.getRevision()){
                        return false;
                    } else if(this.revision < toThis.getRevision()){
                        return true;
                    } else{
                        return false; // Same version so doesn't need to update
                    }
                }
            }
        }
    }

    @Override
    public String toString(){
        return String.format("%d.%d.%d.%d", this.reserved, this.major, this.minor, this.revision);
    }
}
