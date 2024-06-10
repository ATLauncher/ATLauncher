package com.atlauncher.data;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Defines information of any given worker.
 *
 * @since 2024 / 06 / 08
 */
public class WorkerInfo {
    public final UUID id;
    public final String name;
    public final String icon;
    public final boolean isComplete;

    /**
     * If negative, is indefinite.
     */
    public final double progress;

    public WorkerInfo(UUID id, String name, String icon, boolean isComplete, double progress) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.isComplete = isComplete;
        this.progress = progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkerInfo that = (WorkerInfo) o;
        return isComplete == that.isComplete && Double.compare(progress, that.progress) == 0 && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(icon, that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, icon, isComplete, progress);
    }

    @Nonnull
    public WorkerInfo copy(@Nullable Boolean isComplete, @Nullable Double progress) {
        boolean newComplete;

        if (isComplete != null) {
            newComplete = isComplete;
        } else {
            newComplete = this.isComplete;
        }

        double newProgress;

        if (progress != null) {
            newProgress = progress;
        } else {
            newProgress = this.progress;
        }

        return new WorkerInfo(id, name, icon, newComplete, newProgress);
    }
}
