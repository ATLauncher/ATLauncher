package com.atlauncher.evnt;

import com.atlauncher.annot.Subscribe;
import com.atlauncher.managers.LogManager;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public final class EventBus{
    private static final AtomicLong count = new AtomicLong(0);

    private final ThreadGroup group;
    private final List<EBSubscriptionHandler> handlers = new CopyOnWriteArrayList<>();
    private final BlockingQueue<EBSubscriptionHandler> killQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newCachedThreadPool(new EBThreadFactory());

    public EventBus(){
        this.group = new ThreadGroup("ATLauncher-EventBus-" + count.getAndIncrement());

        Thread killQueueThread = new Thread(this.group, new EBKillQueueRunner(), "KillQueue-Thread");
        killQueueThread.setDaemon(true);
        killQueueThread.start();
    }

    public void publish(Object e){
        final List<EBSubscriptionCallable> list = new LinkedList<>();
        for(EBSubscriptionHandler handler : handlers){
            if(!handler.matches(e)){
                continue;
            }

            list.add(new EBSubscriptionCallable(handler, e));
        }

        try{
            executor.invokeAll(list);
        } catch(Exception ex){
            LogManager.logStackTrace(ex);
            ex.printStackTrace(System.err);
        }
    }

    public void unsubscribe(Object obj){
        List<EBSubscriptionHandler> kills = new LinkedList<>();
        for(EBSubscriptionHandler handler : this.handlers){
            Object sub = handler.getSubscriber();
            if(sub == null || obj == sub){
                kills.add(handler);
            }
        }

        for(EBSubscriptionHandler kill : kills){
            handlers.remove(kill);
        }
    }

    public void subscribe(final Object obj){
        try{
            executor.execute(new EBAnnotatedMethodCollector(obj));
        } catch(Exception e){
            LogManager.logStackTrace(e);
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    private final class EBAnnotatedMethodCollector
    implements Runnable{
        private final Object obj;

        private EBAnnotatedMethodCollector(Object obj){
            this.obj = obj;
        }

        @Override
        public void run() {
            try{
                boolean subbed = false;
                for(EBSubscriptionHandler handler : handlers){
                    Object sub = handler.getSubscriber();
                    if(sub == null){
                        try{
                            killQueue.add(handler);
                        } catch(Exception e){
                            LogManager.logStackTrace(e);
                        }

                        continue;
                    }

                    if(obj == sub){
                        subbed = true;
                    }
                }

                if(subbed){
                    return;
                }

                for(Method m : obj.getClass().getDeclaredMethods()){
                    Subscribe sub = m.getAnnotation(Subscribe.class);
                    if(sub == null){
                        continue;
                    }

                    Class<?>[] params = m.getParameterTypes();
                    if(params.length != 1){
                        throw new EBSubscriptionException(obj.getClass(), m);
                    }

                    EBSubscriptionHandler handler = new EBSubscriptionHandler(params[0], m, obj);
                    handlers.add(handler);
                }
            } catch(Exception e){
                e.printStackTrace(System.err);
                LogManager.logStackTrace(e);
                throw new RuntimeException(e);
            }
        }
    }

    private final class EBKillQueueRunner
    implements Runnable{
        @Override
        public void run() {
            try{
                while(true){
                    EBSubscriptionHandler handler = killQueue.take();
                    if(handler.getSubscriber() == null){
                        handlers.remove(handler);
                    }
                }
            } catch(Exception e){
                LogManager.logStackTrace(e);
                throw new RuntimeException(e);
            }
        }
    }

    private final class EBSubscriptionCallable
    implements Callable<Void> {
        private final EBSubscriptionHandler handler;
        private final Object event;

        private EBSubscriptionCallable(EBSubscriptionHandler handler, Object event) {
            this.handler = handler;
            this.event = event;
        }

        @Override
        public Void call()
        throws Exception {
            try{
                Object sub = this.handler.getSubscriber();
                if(sub == null){
                    killQueue.add(this.handler);
                    return null;
                }

                this.handler.invoke.invoke(sub, this.event);
            } catch(Exception e){
                Throwable cause = e;
                while(cause.getCause() != null){
                    cause = cause.getCause();
                }

                LogManager.logStackTrace(cause);
                cause.printStackTrace(System.err);
            }

            return null;
        }
    }

    private final class EBSubscriptionException
    extends Exception{
        public EBSubscriptionException(Class<?> holder, Method invokeable){
            super("Method " + holder.getSimpleName() + "#" + invokeable.getName() + " requires 1 Parameter");
        }
    }

    private final class EBSubscriptionHandler{
        private final Class<?> eClass;
        private final Method invoke;
        private final WeakReference<Object> subscriber;

        private EBSubscriptionHandler(Class<?> eClass, Method invoke, Object subscriber) {
            this.eClass = eClass;
            this.invoke = invoke;
            this.invoke.setAccessible(true);
            this.subscriber = new WeakReference<>(subscriber);
        }

        public boolean matches(Object obj){
            return obj.getClass().equals(this.eClass);
        }

        public Object getSubscriber(){
            return this.subscriber.get();
        }

        @Override
        public String toString(){
            return this.getSubscriber().getClass().getSimpleName() + "#" + this.invoke.getName();
        }
    }

    private final class EBThreadFactory
    implements ThreadFactory {
        private final AtomicLong count = new AtomicLong(0);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread t = new Thread(group, runnable);
            t.setName("Worker-Thread-" + this.count.getAndIncrement());
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}