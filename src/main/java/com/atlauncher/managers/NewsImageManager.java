/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2026 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.managers;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;

import com.atlauncher.FileSystem;
import com.atlauncher.Network;
import com.google.common.hash.Hashing;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/** Loads small, cached news thumbnails without blocking Swing. */
public final class NewsImageManager {
    static final int MAX_WIDTH = 304;
    static final int MAX_HEIGHT = 132;
    static final String MINECRAFT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36";

    private final Path directory;
    private final ImageFetcher fetcher;
    private final Scheduler scheduler;
    private final int memoryLimit;
    private final long retryDelayMillis;
    private final Semaphore downloads = new Semaphore(4);
    private final Map<String, BufferedImage> thumbnails = new LinkedHashMap<>(16, 0.75f, true);
    private final Map<String, Single<BufferedImage>> pending = new HashMap<>();

    @FunctionalInterface
    interface ImageFetcher {
        byte[] fetch(String url) throws IOException;
    }

    private static final class Holder {
        static final NewsImageManager INSTANCE = new NewsImageManager(
                FileSystem.REMOTE_IMAGE_CACHE.resolve("news"), NewsImageManager::download,
                Schedulers.io(), 96, 250);
    }

    NewsImageManager(Path directory, ImageFetcher fetcher, Scheduler scheduler, int memoryLimit,
            long retryDelayMillis) {
        if (memoryLimit < 1 || retryDelayMillis < 0) {
            throw new IllegalArgumentException("Invalid thumbnail cache configuration");
        }
        this.directory = directory.toAbsolutePath().normalize();
        this.fetcher = Objects.requireNonNull(fetcher);
        this.scheduler = Objects.requireNonNull(scheduler);
        this.memoryLimit = memoryLimit;
        this.retryDelayMillis = retryDelayMillis;
    }

    /** Failures are not cached, so requesting the same URL again retries it. */
    public static Single<BufferedImage> getThumbnail(String url) {
        return Single.defer(() -> url == null || url.trim().isEmpty()
                ? Single.error(new IllegalArgumentException("A thumbnail URL is required"))
                : Holder.INSTANCE.load(url));
    }

    Single<BufferedImage> load(String url) {
        return Single.defer(() -> {
            if (url == null || url.trim().isEmpty()) {
                return Single.error(new IllegalArgumentException("A thumbnail URL is required"));
            }
            synchronized (this) {
                BufferedImage image = thumbnails.get(url);
                if (image != null) {
                    return Single.just(image);
                }
                Single<BufferedImage> existing = pending.get(url);
                if (existing != null) {
                    return existing;
                }
                // cache() shares one finite request even when a source tab is hidden mid-download.
                Single<BufferedImage> request = Single.fromCallable(() -> loadImage(url))
                        .subscribeOn(scheduler)
                        .doOnSuccess(result -> remember(url, result))
                        .doOnError(error -> forgetRequest(url))
                        .cache();
                pending.put(url, request);
                return request;
            }
        });
    }

    private synchronized void remember(String url, BufferedImage image) {
        thumbnails.put(url, image);
        while (thumbnails.size() > memoryLimit) {
            String oldest = thumbnails.keySet().iterator().next();
            thumbnails.remove(oldest);
        }
        pending.remove(url);
    }

    private synchronized void forgetRequest(String url) {
        pending.remove(url);
    }

    Path cachePath(String url) {
        String hash = Hashing.sha256().hashString(url, StandardCharsets.UTF_8).toString();
        return directory.resolve(hash + ".png");
    }

    private BufferedImage loadImage(String url) throws IOException {
        Path path = cachePath(url);
        if (Files.isRegularFile(path)) {
            try (InputStream input = Files.newInputStream(path)) {
                BufferedImage cached = ImageIO.read(input);
                if (cached != null) {
                    return thumbnail(cached);
                }
            } catch (IOException ignored) {
                // An incomplete or corrupt cache entry must not permanently hide its image.
            }
        }

        IOException lastFailure = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return fetchThumbnail(url, path);
            } catch (IOException error) {
                lastFailure = error;
                if (Thread.currentThread().isInterrupted()) {
                    throw error;
                }
                if (attempt < 3 && retryDelayMillis > 0) {
                    try {
                        Thread.sleep(retryDelayMillis * attempt);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new IOException("News image loading interrupted", interrupted);
                    }
                }
            }
        }
        throw Objects.requireNonNull(lastFailure);
    }

    private BufferedImage fetchThumbnail(String url, Path path) throws IOException {
        try {
            downloads.acquire();
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IOException("News image loading interrupted", error);
        }
        try {
            byte[] bytes = fetcher.fetch(url);
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(bytes));
            if (source == null) {
                throw new IOException("News image response is not a supported image");
            }
            BufferedImage image = thumbnail(source);
            try {
                writeCache(path, image);
            } catch (IOException error) {
                LogManager.debug("Unable to cache news thumbnail: " + error.getMessage());
            }
            return image;
        } finally {
            downloads.release();
        }
    }

    private void writeCache(Path path, BufferedImage image) throws IOException {
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "news-", ".png");
        try {
            if (!ImageIO.write(image, "png", temporary.toFile())) {
                throw new IOException("Could not encode news thumbnail");
            }
            try {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static BufferedImage thumbnail(BufferedImage source) {
        double scale = Math.min(1.0,
                Math.min((double) MAX_WIDTH / source.getWidth(), (double) MAX_HEIGHT / source.getHeight()));
        if (scale == 1.0) {
            return source;
        }
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage scaled = new BufferedImage(width, height,
                source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = scaled.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
            source.flush();
        }
        return scaled;
    }

    /** Preserve launcher proxy, cache, trust and timeout configuration. */
    static OkHttpClient imageClient(OkHttpClient base) {
        return base.newBuilder().addNetworkInterceptor(chain -> {
            Request request = chain.request();
            String host = request.url().host();
            if (host.equals("minecraft.net") || host.endsWith(".minecraft.net")) {
                // This must run after UserAgentInterceptor: the image CDN rejects the app UA.
                request = request.newBuilder().header("User-Agent", MINECRAFT_USER_AGENT).build();
            }
            return chain.proceed(request);
        }).build();
    }

    private static byte[] download(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = imageClient(Network.CACHED_CLIENT).newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("News image request failed: HTTP " + response.code());
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("News image response is empty");
            }
            return body.bytes();
        }
    }
}
