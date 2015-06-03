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
package com.atlauncher;

import com.atlauncher.anno.ExecutionOrder;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public final class OrderedRunner extends BlockJUnit4ClassRunner {
    public OrderedRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> methods = super.computeTestMethods();
        List<FrameworkMethod> copy = new LinkedList<>(methods);
        Collections.sort(copy, AnnotatedSortingStrategy.INSTANCE);
        return copy;
    }

    private enum AnnotatedSortingStrategy implements Comparator<FrameworkMethod> {
        INSTANCE;

        @Override
        public int compare(FrameworkMethod frameworkMethod, FrameworkMethod t1) {
            ExecutionOrder exco1 = frameworkMethod.getAnnotation(ExecutionOrder.class);
            ExecutionOrder exco2 = t1.getAnnotation(ExecutionOrder.class);

            if (exco1 == null || exco2 == null) {
                return -1;
            }

            return exco1.value() - exco2.value();
        }
    }
}