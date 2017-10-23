package ru.sedi.customerclient.common.AsyncAction;


import ru.sedi.customerclient.common.LogUtil;

public class CompliteAction<T> implements Runnable {
    IActionFeedback<T> complete;
    Exception exception;
    T result;

    public CompliteAction(IActionFeedback<T> complete, Exception exception, T result) {
        this.complete = complete;
        this.exception = exception;
        this.result = result;
    }

    public void run() {
        try {
            if (exception == null && result != null)
                complete.onResponse(result);
            else {
                if(result == null && exception == null)
                    exception = new Exception("Empty server response");
                complete.onFailure(exception);
            }
        } catch (Exception ex) {
            LogUtil.log(ex);
        }
    }
}
