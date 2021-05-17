#!/bin/bash
ln -sn ~/.var/app/com.atlauncher.ATLauncher/cache/ ~/.var/app/com.atlauncher.ATLauncher/data/cache
ln -sn ~/.var/app/com.atlauncher.ATLauncher/config ~/.var/app/com.atlauncher.ATLauncher/data/configs
/app/jre/bin/java -jar /app/bin/ATLauncher.jar --working-dir=.var/app/com.atlauncher.ATLauncher/data/ --no-launcher-update
