package ru.sedi.customerclient.Otto;

/**
 * Created by RAM on 20.10.2015.
 */
public class LocaleChangeEvent {
    private boolean isChange = false;

    public LocaleChangeEvent(boolean isChange) {
        this.isChange = isChange;
    }

    public LocaleChangeEvent() {
    }

    public boolean isChange() {
        return isChange;
    }

    public void setIsChange(boolean isChange) {
        this.isChange = isChange;
    }
}
