package com.atlauncher.exceptions;

public class CommandException extends RuntimeException
{
    public CommandException()
    {
        super();
    }

    public CommandException(String message)
    {
        super(message);
    }

    public CommandException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public CommandException(Throwable cause)
    {
        super(cause);
    }

    protected CommandException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
