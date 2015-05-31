package com.atlauncher.benchmark;

import com.atlauncher.annot.Subscribe;
import com.atlauncher.evnt.EventBus;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.LinkedList;
import java.util.List;

@State(Scope.Thread)
public class EventBusBenchmark{
    public static final int HANDLERS = 10;
    public static final int EVENTS = 10;

    private static final EventBus eventbus = new EventBus();
    private static final List<Handler> handlers = new LinkedList<>();

    public static void main(String... args)
    throws RunnerException {
        Options opts = new OptionsBuilder()
                .addProfiler(StackProfiler.class)
                .forks(1)
                .include(EventBusBenchmark.class.getSimpleName())
                .build();
        new Runner(opts).run();
    }

    @Benchmark
    public void aTestSubscribe(){
        for(int i = 0; i < HANDLERS; i++){
            Handler handler = new Handler();
            handlers.add(handler);
            eventbus.subscribe(handler);
        }
    }

    @Benchmark
    public void bTestPublish(){
        for(int i = 0; i < EVENTS; i++){
            eventbus.publish(new TestEvent("Hello, " + i));
        }
    }

    @Benchmark
    public void cTestUnsubscribe(){
        for(Handler handler : handlers){
            eventbus.unsubscribe(handler);
        }
    }

    private static final class Handler{
        @Subscribe
        public void onTest(TestEvent e){
            System.out.println(e.message);
        }
    }

    private static final class TestEvent{
        public final String message;

        private TestEvent(String message){
            this.message = message;
        }
    }
}