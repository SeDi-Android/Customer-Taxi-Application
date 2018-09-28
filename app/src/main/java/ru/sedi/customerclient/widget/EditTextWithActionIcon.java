package ru.sedi.customerclient.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import ru.sedi.customer.R;

public class EditTextWithActionIcon extends AppCompatEditText implements View.OnTouchListener, View.OnFocusChangeListener {

    public static enum Location {
        LEFT(0), RIGHT(2);

        final int idx;

        private Location(int idx) {
            this.idx = idx;
        }
    }

    public interface Listener {
        void didAction();
    }

    public EditTextWithActionIcon(Context context) {
        super(context);
        init(null);
    }

    public EditTextWithActionIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EditTextWithActionIcon(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * null disables the icon
     */
    public void setIconLocation(Location loc) {
        this.loc = loc;
        initIcon(null);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        this.l = l;
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener f) {
        this.f = f;
    }

    private Location loc = Location.RIGHT;

    private Drawable xD;
    private Listener listener;

    private OnTouchListener l;
    private OnFocusChangeListener f;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (getDisplayedDrawable() != null) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            int left = (loc == Location.LEFT) ? 0 : getWidth() - getPaddingRight() - xD.getIntrinsicWidth();
            int right = (loc == Location.LEFT) ? getPaddingLeft() + xD.getIntrinsicWidth() : getWidth();
            boolean tappedX = x >= left && x <= right && y >= 0 && y <= (getBottom() - getTop());
            if (tappedX) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (listener != null) {
                        listener.didAction();
                    }
                }
                return true;
            }
        }
        if (l != null) {
            return l.onTouch(v, event);
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (f != null) {
            f.onFocusChange(v, hasFocus);
        }
    }

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        super.setCompoundDrawables(left, top, right, bottom);
        initIcon(null);
    }

    private void init(AttributeSet attrs) {
        Drawable icon = null;
        if (attrs != null) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs,
                    R.styleable.EditTextWithActionIcon, 0, 0);

            try {
                icon = typedArray.getDrawable(R.styleable.EditTextWithActionIcon_action_icon);
            } finally {
                typedArray.recycle();
            }
        }

        super.setOnTouchListener(this);
        super.setOnFocusChangeListener(this);
        initIcon(icon);
        setActionIconVisible(true);
    }

    private void initIcon(Drawable icon) {
        xD = null;
        if (icon == null) {
            if (loc != null) {
                xD = getCompoundDrawables()[loc.idx];
            }
            if (xD == null) {
                xD = getResources().getDrawable(R.drawable.ic_close_circle_dark);
            }
        } else
            xD = icon;
        xD.setBounds(0, 0, xD.getIntrinsicWidth(), xD.getIntrinsicHeight());
        int min = getPaddingTop() + xD.getIntrinsicHeight() + getPaddingBottom();
        if (getSuggestedMinimumHeight() < min) {
            setMinimumHeight(min);
        }
    }

    public void setIconRes(@DrawableRes int icon) {
        xD = ContextCompat.getDrawable(getContext(), icon);
    }

    private Drawable getDisplayedDrawable() {
        return (loc != null) ? getCompoundDrawables()[loc.idx] : null;
    }

    protected void setActionIconVisible(boolean visible) {
        Drawable[] cd = getCompoundDrawables();
        Drawable displayed = getDisplayedDrawable();
        boolean wasVisible = (displayed != null);
        if (visible != wasVisible) {
            Drawable x = visible ? xD : null;
            super.setCompoundDrawables((loc == Location.LEFT) ? x : cd[0], cd[1], (loc == Location.RIGHT) ? x : cd[2],
                    cd[3]);
        }
    }
}
