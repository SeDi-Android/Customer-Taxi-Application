package ru.sedi.customerclient.NewDataSharing;

public class _Rating {
    private String Comment;
    private int Rate;

    public _Rating(String comment, int rate) {
        Comment = comment == null ? "" : comment;
        Rate = rate;
    }

    public _Rating() {
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public int getRate() {
        return Rate;
    }

    public void setRate(int rate) {
        Rate = rate;
    }
}
