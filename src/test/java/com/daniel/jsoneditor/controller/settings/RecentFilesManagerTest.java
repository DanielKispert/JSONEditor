package com.daniel.jsoneditor.controller.settings;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecentFilesManagerTest
{
    @Test
    void testConcurrentAddRecentFile() throws InterruptedException
    {
        final RecentFilesManager manager = new RecentFilesManager();
        final int threadCount = 10;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicReference<Throwable> caught = new AtomicReference<>();

        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++)
        {
            final int index = i;
            final Thread t = new Thread(() ->
            {
                try
                {
                    manager.addRecentFile(
                            new File("/tmp/test" + index + ".json"),
                            new File("/tmp/schema" + index + ".json")
                    );
                }
                catch (Throwable e)
                {
                    caught.compareAndSet(null, e);
                }
                finally
                {
                    latch.countDown();
                }
            });
            threads.add(t);
        }
        threads.forEach(Thread::start);
        latch.await();

        assertNull(caught.get(), "Concurrent addRecentFile threw: " + caught.get());
        assertNotNull(manager.getRecentFiles());
    }

    @Test
    void testConcurrentReadWhileWrite() throws InterruptedException
    {
        final RecentFilesManager manager = new RecentFilesManager();
        final AtomicReference<Throwable> caught = new AtomicReference<>();
        final CountDownLatch writerDone = new CountDownLatch(1);
        final CountDownLatch readerDone = new CountDownLatch(1);

        final Thread writer = new Thread(() ->
        {
            try
            {
                for (int i = 0; i < 50; i++)
                {
                    manager.addRecentFile(
                            new File("/tmp/write" + i + ".json"),
                            new File("/tmp/schema" + i + ".json")
                    );
                }
            }
            catch (Throwable e)
            {
                caught.compareAndSet(null, e);
            }
            finally
            {
                writerDone.countDown();
            }
        }, "writer");

        final Thread reader = new Thread(() ->
        {
            try
            {
                for (int i = 0; i < 200; i++)
                {
                    final List<RecentFilesManager.RecentFile> list = manager.getRecentFiles();
                    assertNotNull(list);
                    for (final RecentFilesManager.RecentFile ignored : list)
                    {
                        // iterate to trigger potential ConcurrentModificationException
                    }
                }
            }
            catch (Throwable e)
            {
                caught.compareAndSet(null, e);
            }
            finally
            {
                readerDone.countDown();
            }
        }, "reader");

        writer.start();
        reader.start();
        writerDone.await();
        readerDone.await();

        assertNull(caught.get(), "Concurrent read/write threw: " + caught.get());
    }
}
