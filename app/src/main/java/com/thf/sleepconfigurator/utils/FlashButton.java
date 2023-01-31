package com.thf.sleepconfigurator.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;
import com.thf.sleepconfigurator.R;

/* loaded from: classes.dex */
public class FlashButton extends ImageButton {
    private boolean clickable;
    private FlashListener mFlashListener;
    private FlashEnum mState;

    /* loaded from: classes.dex */
    public enum FlashEnum {
        OFF,
        WHITE,
        YELLOW
    }

    /* loaded from: classes.dex */
    public interface FlashListener {
        void onState(FlashEnum flashEnum);
    }

    public FlashButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.clickable = true;
        setState(FlashEnum.OFF);
    }

    @Override // android.view.View
    public boolean performClick() {
        super.performClick();
        if (this.clickable) {
            setState(FlashEnum.values()[(this.mState.ordinal() + 1) % FlashEnum.values().length]);
            performFlashClick();
            return true;
        }
        return false;
    }

    private void performFlashClick() {
        FlashListener flashListener = this.mFlashListener;
        if (flashListener == null) {
            return;
        }
        flashListener.onState(this.mState);
    }

    private void createDrawableState() {
        int i = this.mState.ordinal();
        if (i == 0) {
            setImageResource(R.drawable.off);
        } else if (i == 1) {
            setImageResource(R.drawable.white);
        } else if (i != 2) {
        } else {
            setImageResource(R.drawable.yellow);
        }
    }

    public FlashEnum getState() {
        return this.mState;
    }

    public void setState(FlashEnum flashEnum) {
        if (flashEnum == null) {
            return;
        }
        this.mState = flashEnum;
        createDrawableState();
    }

    public FlashListener getFlashListener() {
        return this.mFlashListener;
    }

    public void setFlashListener(FlashListener flashListener) {
        this.mFlashListener = flashListener;
    }

    @Override // android.widget.ImageView, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, measuredWidth);
    }

    @Override // android.view.View
    public void setEnabled(boolean z) {
        if (z) {
            setImageAlpha(255);
            this.clickable = true;
            return;
        }
        setImageAlpha(100);
        this.clickable = false;
    }
}
