package com.atlauncher.backup;

import com.atlauncher.workers.InstanceInstaller;

public interface BackupMethod{
    public void backup(InstanceInstaller installer);
    public void restore(InstanceInstaller installer);
}