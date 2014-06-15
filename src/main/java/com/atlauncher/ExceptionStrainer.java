package com.atlauncher;

import com.atlauncher.exceptions.ChunkyException;
import com.atlauncher.reporter.GithubIssueReporter;
import com.atlauncher.utils.Utils;

public final class ExceptionStrainer implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if(e instanceof ChunkyException){
            try {
                GithubIssueReporter.submit("Strained Exception", Utils.error(e));
            } catch (Exception e1) {
                e1.printStackTrace(System.err);
            }
        }
    }
}