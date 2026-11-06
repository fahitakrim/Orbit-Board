/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.futo.inputmethod.keyboard.internal;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import org.futo.inputmethod.keyboard.Key;
import org.futo.inputmethod.keyboard.KeyboardView;
import org.futo.inputmethod.latin.R;

import java.util.HashSet;
import java.util.Objects;

/**
 * The pop up key preview view.
 */
public class KeyPreviewView extends androidx.appcompat.widget.AppCompatTextView {
    public static final int POSITION_MIDDLE = 0;
    public static final int POSITION_LEFT = 1;
    public static final int POSITION_RIGHT = 2;

    private final Rect mBackgroundPadding = new Rect();
    private static final HashSet<String> sNoScaleXTextSet = new HashSet<>();

    public KeyPreviewView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyPreviewView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(Gravity.CENTER);
        setIncludeFontPadding(false);
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        syncPaddingWithBackground();
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        syncPaddingWithBackground();
    }

    @Override
    public void setBackground(final Drawable background) {
        super.setBackground(background);
        syncPaddingWithBackground();
    }

    public void syncPaddingWithBackground() {
        final Drawable bg = getBackground();
        if (bg != null) {
            final Rect pad = new Rect();
            if (bg.getPadding(pad)) {
                if (getPaddingLeft() != pad.left || getPaddingTop() != pad.top
                        || getPaddingRight() != pad.right || getPaddingBottom() != pad.bottom) {
                    setPadding(pad.left, pad.top, pad.right, pad.bottom);
                }
            }
        }
    }

    @Override
    protected void onDraw(final android.graphics.Canvas canvas) {
        final Drawable bg = getBackground();
        final CharSequence text = getText();
        if (bg instanceof KeyPopupDrawable && !android.text.TextUtils.isEmpty(text)) {
            final KeyPopupDrawable popupDrawable = (KeyPopupDrawable) bg;
            final android.graphics.RectF bubbleBounds = popupDrawable.getContentRect();
            if (bubbleBounds != null && !bubbleBounds.isEmpty()) {
                final TextPaint paint = getPaint();
                paint.setColor(getCurrentTextColor());
                paint.setTextAlign(android.graphics.Paint.Align.CENTER);
                final android.graphics.Paint.FontMetrics fm = paint.getFontMetrics();
                final float textY = bubbleBounds.centerY() - (fm.ascent + fm.descent) / 2.0f;
                final float textX = bubbleBounds.centerX();

                final float textWidth = paint.measureText(text, 0, text.length());
                final float maxW = bubbleBounds.width() - 8.0f;
                final float origScaleX = paint.getTextScaleX();
                if (textWidth > maxW && maxW > 0.0f) {
                    paint.setTextScaleX(origScaleX * (maxW / textWidth));
                }
                canvas.drawText(text, 0, text.length(), textX, textY, paint);
                paint.setTextScaleX(origScaleX);
                return;
            }
        }

        final Drawable[] drawables = getCompoundDrawables();
        Drawable icon = null;
        for (Drawable d : drawables) {
            if (d != null) {
                icon = d;
                break;
            }
        }
        if (bg instanceof KeyPopupDrawable && icon != null) {
            final KeyPopupDrawable popupDrawable = (KeyPopupDrawable) bg;
            final android.graphics.RectF bubbleBounds = popupDrawable.getContentRect();
            if (bubbleBounds != null && !bubbleBounds.isEmpty()) {
                final int iconW = icon.getIntrinsicWidth();
                final int iconH = icon.getIntrinsicHeight();
                final int l = (int) (bubbleBounds.centerX() - iconW / 2.0f);
                final int t = (int) (bubbleBounds.centerY() - iconH / 2.0f);
                icon.setBounds(l, t, l + iconW, t + iconH);
                icon.draw(canvas);
                return;
            }
        }

        super.onDraw(canvas);
    }

    public void setPreviewVisual(final Key key, final KeyboardIconsSet iconsSet,
            final KeyDrawParams drawParams) {
        setPreviewVisual(key, iconsSet, drawParams, null);
    }

    public void setPreviewVisual(final Key key, final KeyboardIconsSet iconsSet,
            final KeyDrawParams drawParams, final Integer customTextColor) {
        // What we show as preview should match what we show on a key top in onDraw().
        final String iconId = key.getIconId();
        if (!Objects.equals(iconId, KeyboardIconsSet.ICON_UNDEFINED)) {
            final Drawable icon = key.getPreviewIcon(iconsSet);
            if (icon != null) {
                final int tint = customTextColor != null ? customTextColor : drawParams.mPreviewTextColor;
                if (tint != 0) {
                    icon.setTint(tint);
                }
            }
            setCompoundDrawables(null, null, null, icon);
            setText(null);
            return;
        }

        setCompoundDrawables(null, null, null, null);
        if (customTextColor != null) {
            setTextColor(customTextColor);
        } else {
            setTextColor(drawParams.mPreviewTextColor);
        }

        final Drawable bg = getBackground();
        final boolean isGboardBubble = (bg instanceof KeyPopupDrawable)
                && (((KeyPopupDrawable) bg).getStyle() == KeyPopupDrawable.STYLE_GBOARD_BUBBLE);
        final float fontScale = isGboardBubble ? 0.65f : 0.70f;
        setTextSize(TypedValue.COMPLEX_UNIT_PX, key.selectPreviewTextSize(drawParams) * fontScale);

        final String previewLabel = key.getPreviewLabel();
        boolean hasBengali = false;
        if (previewLabel != null) {
            for (int i = 0; i < previewLabel.length(); i++) {
                final char c = previewLabel.charAt(i);
                if (c >= 0x0980 && c <= 0x09FF) {
                    hasBengali = true;
                    break;
                }
            }
        }
        if (hasBengali) {
            setTypeface(android.graphics.Typeface.DEFAULT);
        } else {
            setTypeface(key.selectPreviewTypeface(drawParams));
        }
        // TODO Should take care of temporaryShiftLabel here.
        setTextAndScaleX(previewLabel);
    }

    private void setTextAndScaleX(final String text) {
        final String cleanText = KeyboardView.cleanCombiningMarkForDisplay(text);
        setTextScaleX(1.0f);
        setText(cleanText);
        if (sNoScaleXTextSet.contains(text)) {
            return;
        }
        // TODO: Override {@link #setBackground(Drawable)} that is supported from API 16 and
        // calculate maximum text width.
        final Drawable background = getBackground();
        if (background == null) {
            return;
        }
        background.getPadding(mBackgroundPadding);
        final int maxWidth = background.getIntrinsicWidth() - mBackgroundPadding.left
                - mBackgroundPadding.right;
        if (maxWidth <= 0) {
            return;
        }
        final float width = getTextWidth(text, getPaint());
        if (width <= 0.0f || Float.isNaN(width)) {
            return;
        }
        if (width <= maxWidth) {
            sNoScaleXTextSet.add(text);
            return;
        }
        final float scaleX = maxWidth / width;
        if (Float.isFinite(scaleX) && scaleX > 0.0f) {
            setTextScaleX(Math.min(1.0f, scaleX));
        }
    }

    public static void clearTextCache() {
        sNoScaleXTextSet.clear();
    }

    private static float getTextWidth(final String text, final TextPaint paint) {
        if (TextUtils.isEmpty(text)) {
            return 0.0f;
        }
        final int len = text.length();
        final float[] widths = new float[len];
        final int count = paint.getTextWidths(text, 0, len, widths);
        float width = 0;
        for (int i = 0; i < count; i++) {
            width += widths[i];
        }
        return width;
    }

    // Background state set
    private static final int[][][] KEY_PREVIEW_BACKGROUND_STATE_TABLE = {
        { // POSITION_MIDDLE
            {},
            { R.attr.state_has_morekeys }
        },
        { // POSITION_LEFT
            { R.attr.state_left_edge },
            { R.attr.state_left_edge, R.attr.state_has_morekeys }
        },
        { // POSITION_RIGHT
            { R.attr.state_right_edge },
            { R.attr.state_right_edge, R.attr.state_has_morekeys }
        }
    };
    private static final int STATE_NORMAL = 0;
    private static final int STATE_HAS_MOREKEYS = 1;

    public void setPreviewBackground(final boolean hasMoreKeys, final int position) {
        final Drawable background = getBackground();
        if (background == null) {
            return;
        }
        final int hasMoreKeysState = hasMoreKeys ? STATE_HAS_MOREKEYS : STATE_NORMAL;
        background.setState(KEY_PREVIEW_BACKGROUND_STATE_TABLE[position][hasMoreKeysState]);
    }
}
