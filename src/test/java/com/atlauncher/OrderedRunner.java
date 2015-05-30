package com.atlauncher;

import com.atlauncher.anno.ExecutionOrder;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public final class OrderedRunner
extends BlockJUnit4ClassRunner{
    public OrderedRunner(Class<?> klass)
    throws InitializationError {
        super(klass);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> methods = super.computeTestMethods();
        List<FrameworkMethod> copy = new LinkedList<>(methods);
        Collections.sort(copy, AnnotatedSortingStrategy.INSTANCE);
        return copy;
    }

    private enum AnnotatedSortingStrategy
    implements Comparator<FrameworkMethod>{
        INSTANCE;

        @Override
        public int compare(FrameworkMethod frameworkMethod, FrameworkMethod t1) {
            ExecutionOrder exco1 = frameworkMethod.getAnnotation(ExecutionOrder.class);
            ExecutionOrder exco2 = t1.getAnnotation(ExecutionOrder.class);

            if(exco1 == null || exco2 == null){
                return -1;
            }

            return exco1.value() - exco2.value();
        }
    }
}