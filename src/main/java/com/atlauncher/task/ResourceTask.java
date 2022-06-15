package com.atlauncher.task;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

public abstract class ResourceTask extends AbstractTask{
    private final Path file;
    private final Gson gson;

    protected ResourceTask(@Nonnull final Path file,
                           @Nonnull final Gson gson,
                           @Nullable final CountDownLatch latch) {
        super(latch);
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(gson);
        this.file = file;
        this.gson = gson;
    }

    public final Path getFile(){
        return this.file;
    }

    public final Gson getGson(){
        return this.gson;
    }
}