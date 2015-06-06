/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
package com.atlauncher.benchmark;

import com.atlauncher.injector.Injector;
import com.atlauncher.injector.InjectorFactory;
import com.atlauncher.injector.Module;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.inject.Inject;

public class InjectionBenchmark {
    private static Injector injector = InjectorFactory.createInjector(new BenchmarkModule());

    public static void main(String... args) throws RunnerException {
        Options opts = new OptionsBuilder().addProfiler(StackProfiler.class).forks(1).include(InjectionBenchmark
                .class.getSimpleName()).build();
        new Runner(opts).run();
    }

    @Benchmark
    public void singleton() {
        injector.getInstance(InjectionSingletonDummy.class);
    }

    @Benchmark
    public void creation() {
        injector.getInstance(InjectionDummy.class);
    }

    @Benchmark
    public void instance() {
        injector.getInstance(InjectionInstanceDummy.class);
    }

    private static interface Injection {
        public String get();
    }

    private static final class InjectionImpl implements Injection {
        @Override
        public String get() {
            return "Hello World";
        }
    }

    private static final class InjectionDummy {
        @Inject
        private InjectionDummy() {
        }
    }

    private static final class InjectionInstanceDummy {
        private InjectionInstanceDummy() {
        }
    }

    private static final class InjectionSingletonDummy {
        @Inject
        private InjectionSingletonDummy() {
        }
    }

    private static final class BenchmarkModule extends Module {
        @Override
        protected void configure() {
            this.bind(InjectionInstanceDummy.class).toInstance(new InjectionInstanceDummy());
            this.bind(InjectionSingletonDummy.class).asSingleton();
            this.bind(Injection.class).to(InjectionImpl.class);
        }
    }
}