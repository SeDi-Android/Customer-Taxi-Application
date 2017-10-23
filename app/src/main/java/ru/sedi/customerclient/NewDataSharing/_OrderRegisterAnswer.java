package ru.sedi.customerclient.NewDataSharing;


public class _OrderRegisterAnswer {
    private int ObjectId;
    private boolean Success;
    private String Message;

    public _OrderRegisterAnswer(int objectId, boolean success, String message) {
        ObjectId = objectId;
        Success = success;
        Message = message;
    }

    public _OrderRegisterAnswer() {
    }

    public int getObjectId() {
        return ObjectId;
    }

    public boolean isSuccess() {
        return Success;
    }

    public String getMessage() {
        return Message;
    }

}
