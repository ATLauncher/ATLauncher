package com.atlauncher.events;

import com.atlauncher.data.Instance;
import com.atlauncher.data.minecraft.loaders.LoaderType;

public abstract class PackEvent extends AnalyticsEvent.AppEvent{
    private static String getLabel(final String pack, final String launcherVersion){
        return String.format("%s - %s", pack, launcherVersion);
    }

    private static String getPack(final Instance instance){
        return instance.launcher.pack;
    }

    private static String getVersion(final Instance instance){
        return instance.launcher.version;
    }

    protected PackEvent(final String pack, final String launcherVersion, final String action, final String category){
        super(getLabel(pack, launcherVersion), action, category);
    }

    protected PackEvent(final String pack, final String launcherVersion, final String action, final String category, final int value){
        super(getLabel(pack, launcherVersion), action, category, value);
    }

    protected PackEvent(final String action, final Instance instance){
        this(getPack(instance), getVersion(instance), action, instance.getAnalyticsCategory());
    }

    protected PackEvent(final String action, final Instance instance, final int value){
        this(getPack(instance), getVersion(instance), action, instance.getAnalyticsCategory(), value);
    }

    public static final class PackPlayEvent extends PackEvent{
        public static final String ACTION = "Play";

        public PackPlayEvent(final Instance instance){
            super(ACTION, instance);
        }
    }

    public static final class PackPlayOfflineEvent extends PackEvent{
        public static final String ACTION = "PlayOffline";

        public PackPlayOfflineEvent(final Instance instance){
            super(ACTION, instance);
        }
    }

    public static final class PackReinstallEvent extends PackEvent{
        public static final String ACTION = "Reinstall";

        public PackReinstallEvent(final Instance instance){
            super(ACTION, instance);
        }
    }

    public static final class PackRenameEvent extends PackEvent{
        public static final String ACTION = "Rename";

        public PackRenameEvent(final Instance instance){
            super(ACTION, instance);
        }
    }

    public static final class PackCloneEvent extends PackEvent{
        public static final String ACTION = "Clone";

        public PackCloneEvent(final Instance instance){
            super(ACTION, instance);
        }
    }

    public static final class PackImageChangedEvent extends PackEvent{
        public static final String ACTION = "ChangeImage";

        public PackImageChangedEvent(final Instance instance){
            super(ACTION, instance);
        }
    }

    public static final class PackLoaderVersionChangedEvent extends PackEvent{
        public static final String ACTION = "ChangeLoaderVersion";

        public PackLoaderVersionChangedEvent(final Instance instance){
            super(ACTION, instance);
        }
    }

    public static final class PackLoaderAddedEvent extends PackEvent{
        public static final String ACTION = "AddLoader";

        public PackLoaderAddedEvent(final Instance instance, final LoaderType type){
            super(ACTION, instance, type.getAnalyticsValue());
        }
    }

    public static final class PackLoaderRemovedEvent extends PackEvent{
        public static final String ACTION = "RemoveLoader";

        public PackLoaderRemovedEvent(final Instance instance, final LoaderType type){
            super(ACTION, instance, type.getAnalyticsValue());
        }
    }

    public static final class PackBackupEvent extends PackEvent{
        public static final String ACTION = "Backup";

        public PackBackupEvent(final Instance instance){
            super(ACTION, instance);
        }
    }
}