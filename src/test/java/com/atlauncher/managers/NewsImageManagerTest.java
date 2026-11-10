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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class NewsImageManagerTest {
    @TempDir
    Path cache;

    private byte[] imageBytes(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    @Test
    void downloadsAndDownsamplesAnUncachedImageThenReusesMemoryAndDisk() throws Exception {
        byte[] original = imageBytes(1170, 500);
        AtomicInteger requests = new AtomicInteger();
        NewsImageManager manager = new NewsImageManager(cache, url -> {
            requests.incrementAndGet();
            return original;
        }, Schedulers.trampoline(), 2, 0);
        String url = "https://www.minecraft.net/page-two.jpg";

        BufferedImage thumbnail = manager.load(url).blockingGet();
        assertTrue(thumbnail.getWidth() <= NewsImageManager.MAX_WIDTH);
        assertTrue(thumbnail.getHeight() <= NewsImageManager.MAX_HEIGHT);
        assertTrue(Math.abs((double) thumbnail.getWidth() / thumbnail.getHeight() - 1170.0 / 500) < 0.02);
        assertSame(thumbnail, manager.load(url).blockingGet());
        assertEquals(1, requests.get());
        assertNotNull(ImageIO.read(manager.cachePath(url).toFile()));

        NewsImageManager laterSession = new NewsImageManager(cache, ignored -> {
            throw new IOException("A valid disk cache should avoid the network");
        }, Schedulers.trampoline(), 2, 0);
        BufferedImage disk = laterSession.load(url).blockingGet();
        assertEquals(thumbnail.getWidth(), disk.getWidth());
        assertEquals(thumbnail.getHeight(), disk.getHeight());
    }

    @Test
    void displaysDownloadedImagesWhenTheDiskCacheCannotBeWritten() throws Exception {
        byte[] original = imageBytes(500, 200);
        Path blockedCache = Files.createFile(cache.resolve("blocked-cache"));
        AtomicInteger requests = new AtomicInteger();
        NewsImageManager manager = new NewsImageManager(blockedCache, url -> {
            requests.incrementAndGet();
            return original;
        }, Schedulers.trampoline(), 2, 0);
        String url = "https://www.minecraft.net/uncacheable.jpg";

        BufferedImage thumbnail = manager.load(url).blockingGet();
        assertNotNull(thumbnail);
        assertSame(thumbnail, manager.load(url).blockingGet());
        assertEquals(1, requests.get());
    }

    @Test
    void recoversFromCorruptCachedImages() throws Exception {
        byte[] original = imageBytes(700, 300);
        AtomicInteger requests = new AtomicInteger();
        NewsImageManager manager = new NewsImageManager(cache, url -> {
            requests.incrementAndGet();
            return original;
        }, Schedulers.trampoline(), 2, 0);
        String url = "https://www.minecraft.net/corrupt.jpg";
        Files.write(manager.cachePath(url), "not an image".getBytes(StandardCharsets.UTF_8));

        manager.load(url).test().assertComplete().assertNoErrors();
        assertEquals(1, requests.get());
        assertNotNull(ImageIO.read(manager.cachePath(url).toFile()));
    }

    @Test
    void retriesTransientFailuresWithoutCachingAnError() throws Exception {
        byte[] original = imageBytes(500, 200);
        AtomicInteger requests = new AtomicInteger();
        NewsImageManager manager = new NewsImageManager(cache, url -> {
            if (requests.incrementAndGet() < 3) {
                throw new IOException("Temporary failure");
            }
            return original;
        }, Schedulers.trampoline(), 2, 0);

        manager.load("https://www.minecraft.net/transient.jpg").test().assertComplete().assertNoErrors();
        assertEquals(3, requests.get());
    }

    @Test
    void invalidResponsesAreNotPersistedAndCanBeRetriedManually() throws Exception {
        byte[] original = imageBytes(500, 200);
        AtomicInteger requests = new AtomicInteger();
        NewsImageManager manager = new NewsImageManager(cache, url -> requests.incrementAndGet() <= 3
                ? "<html>temporary error</html>".getBytes(StandardCharsets.UTF_8)
                : original, Schedulers.trampoline(), 2, 0);
        String url = "https://www.minecraft.net/invalid.jpg";

        manager.load(url).test().assertError(IOException.class);
        assertFalse(Files.exists(manager.cachePath(url)));
        manager.load(url).test().assertComplete().assertNoErrors();
        assertEquals(4, requests.get());
    }

    @Test
    void concurrentSubscribersShareOneDownload() throws Exception {
        byte[] original = imageBytes(500, 200);
        AtomicInteger requests = new AtomicInteger();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        NewsImageManager manager = new NewsImageManager(cache, url -> {
            requests.incrementAndGet();
            started.countDown();
            try {
                if (!release.await(2, TimeUnit.SECONDS)) {
                    throw new IOException("Test did not release download");
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                throw new IOException(error);
            }
            return original;
        }, Schedulers.io(), 2, 0);

        TestObserver<BufferedImage> first = manager.load("https://www.minecraft.net/shared.jpg").test();
        assertTrue(started.await(2, TimeUnit.SECONDS));
        TestObserver<BufferedImage> second = manager.load("https://www.minecraft.net/shared.jpg").test();
        release.countDown();
        first.awaitDone(3, TimeUnit.SECONDS).assertComplete().assertNoErrors();
        second.awaitDone(3, TimeUnit.SECONDS).assertComplete().assertNoErrors();
        assertEquals(1, requests.get());
        assertSame(first.values().get(0), second.values().get(0));
    }

    @Test
    void limitsDecodedImagesInMemoryWithoutDiscardingDiskCache() throws Exception {
        byte[] original = imageBytes(500, 200);
        AtomicInteger requests = new AtomicInteger();
        NewsImageManager manager = new NewsImageManager(cache, url -> {
            requests.incrementAndGet();
            return original;
        }, Schedulers.trampoline(), 2, 0);
        BufferedImage first = manager.load("https://www.minecraft.net/first.jpg").blockingGet();
        manager.load("https://www.minecraft.net/second.jpg").blockingGet();
        manager.load("https://www.minecraft.net/third.jpg").blockingGet();

        assertNotSame(first, manager.load("https://www.minecraft.net/first.jpg").blockingGet());
        assertEquals(3, requests.get());
    }

    @Test
    void setsBrowserCompatibleUserAgentAfterExistingNetworkInterceptors() throws Exception {
        Interceptor prior = chain -> chain.proceed(chain.request());
        OkHttpClient base = new OkHttpClient.Builder().addNetworkInterceptor(prior).build();
        OkHttpClient client = NewsImageManager.imageClient(base);
        assertSame(prior, client.networkInterceptors().get(0));
        assertSame(base.connectionPool(), client.connectionPool());
        assertSame(base.proxySelector(), client.proxySelector());
        Interceptor override = client.networkInterceptors().get(client.networkInterceptors().size() - 1);
        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        Request original = new Request.Builder().url("https://www.minecraft.net/another image.jpg")
                .header("User-Agent", "ATLauncher/normal").build();
        when(chain.request()).thenReturn(original);
        when(chain.proceed(any(Request.class))).thenReturn(mock(Response.class));

        override.intercept(chain);

        ArgumentCaptor<Request> request = ArgumentCaptor.forClass(Request.class);
        verify(chain).proceed(request.capture());
        assertEquals(NewsImageManager.MINECRAFT_USER_AGENT, request.getValue().header("User-Agent"));
        assertEquals("/another%20image.jpg", request.getValue().url().encodedPath());
    }

    @Test
    void leavesNonMinecraftUserAgentsAlone() throws Exception {
        OkHttpClient client = NewsImageManager.imageClient(new OkHttpClient());
        Interceptor override = client.networkInterceptors().get(0);
        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        Request original = new Request.Builder().url("https://example.com/image.jpg")
                .header("User-Agent", "ATLauncher/normal").build();
        when(chain.request()).thenReturn(original);
        when(chain.proceed(any(Request.class))).thenReturn(mock(Response.class));

        override.intercept(chain);

        ArgumentCaptor<Request> request = ArgumentCaptor.forClass(Request.class);
        verify(chain).proceed(request.capture());
        assertEquals("ATLauncher/normal", request.getValue().header("User-Agent"));
    }
}
