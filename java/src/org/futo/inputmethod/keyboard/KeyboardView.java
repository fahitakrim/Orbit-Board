/*
 * Copyright (C) 2010 The Android Open Source Project
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

package org.futo.inputmethod.keyboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import org.futo.inputmethod.keyboard.internal.KeyDrawParams;
import org.futo.inputmethod.keyboard.internal.KeyVisualAttributes;
import org.futo.inputmethod.keyboard.internal.OrbitDotBangla;
import org.futo.inputmethod.latin.uix.DynamicThemeProvider;
import org.futo.inputmethod.latin.uix.DynamicThemeProviderOwner;
import org.futo.inputmethod.latin.R;
import org.futo.inputmethod.latin.common.Constants;
import org.futo.inputmethod.latin.utils.TypefaceUtils;

import java.util.HashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A view that renders a virtual {@link Keyboard}.
 *
 * @attr ref R.styleable#KeyboardView_keyBackground
 * @attr ref R.styleable#KeyboardView_functionalKeyBackground
 * @attr ref R.styleable#KeyboardView_spacebarBackground
 * @attr ref R.styleable#KeyboardView_spacebarIconWidthRatio
 * @attr ref R.styleable#Keyboard_Key_keyLabelFlags
 * @attr ref R.styleable#KeyboardView_keyHintLetterPadding
 * @attr ref R.styleable#KeyboardView_keyPopupHintLetter
 * @attr ref R.styleable#KeyboardView_keyPopupHintLetterPadding
 * @attr ref R.styleable#KeyboardView_keyShiftedLetterHintPadding
 * @attr ref R.styleable#KeyboardView_keyTextShadowRadius
 * @attr ref R.styleable#KeyboardView_verticalCorrection
 * @attr ref R.styleable#Keyboard_Key_keyTypeface
 * @attr ref R.styleable#Keyboard_Key_keyLetterSize
 * @attr ref R.styleable#Keyboard_Key_keyLabelSize
 * @attr ref R.styleable#Keyboard_Key_keyLargeLetterRatio
 * @attr ref R.styleable#Keyboard_Key_keyLargeLabelRatio
 * @attr ref R.styleable#Keyboard_Key_keyHintLetterRatio
 * @attr ref R.styleable#Keyboard_Key_keyShiftedLetterHintRatio
 * @attr ref R.styleable#Keyboard_Key_keyHintLabelRatio
 * @attr ref R.styleable#Keyboard_Key_keyLabelOffCenterRatio
 * @attr ref R.styleable#Keyboard_Key_keyHintLabelOffCenterRatio
 * @attr ref R.styleable#Keyboard_Key_keyPreviewTextRatio
 * @attr ref R.styleable#Keyboard_Key_keyTextColor
 * @attr ref R.styleable#Keyboard_Key_keyTextColorDisabled
 * @attr ref R.styleable#Keyboard_Key_keyTextShadowColor
 * @attr ref R.styleable#Keyboard_Key_keyHintLetterColor
 * @attr ref R.styleable#Keyboard_Key_keyHintLabelColor
 * @attr ref R.styleable#Keyboard_Key_keyShiftedLetterHintInactivatedColor
 * @attr ref R.styleable#Keyboard_Key_keyShiftedLetterHintActivatedColor
 * @attr ref R.styleable#Keyboard_Key_keyPreviewTextColor
 */
public class KeyboardView extends View {
    // XML attributes
    private final KeyVisualAttributes mKeyVisualAttributes;
    // Default keyLabelFlags from {@link KeyboardTheme}.
    // Currently only "alignHintLabelToBottom" is supported.
    private final int mDefaultKeyLabelFlags;
    private final float mKeyHintLetterPadding;
    private final String mKeyPopupHintLetter;
    private final float mKeyPopupHintLetterPadding;
    private final float mKeyShiftedLetterHintPadding;
    private final float mKeyTextShadowRadius;
    private final float mVerticalCorrection;
    private Drawable mKeyboardBackground;
    private Drawable mKeyBackground;
    private Drawable mFunctionalKeyBackground;
    private Drawable mSpacebarBackground;
    protected boolean mIsMoreKeys;
    protected boolean mIsMoreKeysAction;
    protected DynamicThemeProvider mDrawableProvider;
    private final float mSpacebarIconWidthRatio;
    private final Rect mKeyBackgroundPadding = new Rect();
    private static final float KET_TEXT_SHADOW_RADIUS_DISABLED = -1.0f;

    // The maximum key label width in the proportion to the key width.
    private static final float MAX_LABEL_RATIO = 0.90f;

    // Main keyboard
    // TODO: Consider having a base keyboard object to make this @Nonnull
    @Nullable
    private Keyboard mKeyboard;
    private int mLockedOccupiedHeight = 0;
    private int mLastOccupiedWidth = 0;

    public void resetKeyboardHeightLock() {
        mLockedOccupiedHeight = 0;
        mLastOccupiedWidth = 0;
    }

    private static boolean isEligibleForHeightLock(@Nullable final KeyboardId id) {
        if (id == null) return false;
        final int elementId = id.mElementId;
        return id.isAlphabetKeyboard()
                || elementId == KeyboardId.ELEMENT_SYMBOLS
                || elementId == KeyboardId.ELEMENT_SYMBOLS_SHIFTED
                || elementId == KeyboardId.ELEMENT_SYMBOLS_3;
    }
    @Nonnull
    private final KeyDrawParams mKeyDrawParams = new KeyDrawParams();

    // Drawing
    /** True if all keys should be drawn */
    private boolean mInvalidateAllKeys;
    /** The keys that should be drawn */
    private final HashSet<Key> mInvalidatedKeys = new HashSet<>();
    /** The working rectangle for clipping */
    private final Rect mClipRect = new Rect();
    /** The keyboard bitmap buffer for faster updates */
    private Bitmap mOffscreenBuffer;
    /** The canvas for the above mutable keyboard bitmap */
    @Nonnull
    private final Canvas mOffscreenCanvas = new Canvas();
    @Nonnull
    private final Paint mPaint = new Paint();
    private final Paint.FontMetrics mFontMetrics = new Paint.FontMetrics();

    public KeyboardView(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.keyboardViewStyle);
    }

    public KeyboardView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray keyboardViewAttr = context.obtainStyledAttributes(attrs,
                R.styleable.KeyboardView, defStyle, R.style.KeyboardView);

        final TypedArray keyAttr = context.obtainStyledAttributes(attrs,
                R.styleable.Keyboard_Key, defStyle, R.style.KeyboardView);

        assert(context instanceof ContextThemeWrapper);

        if(((ContextThemeWrapper) context).getBaseContext() instanceof DynamicThemeProviderOwner) {
            mDrawableProvider = ((DynamicThemeProviderOwner) ((ContextThemeWrapper) context).getBaseContext()).getDrawableProvider();
        } else if(context instanceof DynamicThemeProviderOwner) {
            mDrawableProvider = ((DynamicThemeProviderOwner) context).getDrawableProvider();
        } else {
            throw new IllegalStateException("Failed to obtain DynamicThemeProvider");
        }


        mIsMoreKeys = keyAttr.getBoolean(R.styleable.Keyboard_Key_isMoreKey, false);
        mIsMoreKeysAction = keyAttr.getBoolean(R.styleable.Keyboard_Key_isAction, false);

        mKeyboardBackground = mIsMoreKeysAction ? null :
                mIsMoreKeys ?  mDrawableProvider.getMoreKeysKeyboardBackground() :
                        mDrawableProvider.getKeyboardBackground();
        setBackground(mKeyboardBackground);

        mKeyBackground = mIsMoreKeysAction ? mDrawableProvider.getActionPopupKey() :
                mIsMoreKeys ? mDrawableProvider.getPopupKey() :
                        mDrawableProvider.getKeyBackground();
        mKeyBackground.getPadding(mKeyBackgroundPadding);

        mFunctionalKeyBackground = mKeyBackground;

        mSpacebarBackground = mDrawableProvider.getSpaceBarBackground();

        mSpacebarIconWidthRatio = keyboardViewAttr.getFloat(
                R.styleable.KeyboardView_spacebarIconWidthRatio, 1.0f);
        mKeyHintLetterPadding = keyboardViewAttr.getDimension(
                R.styleable.KeyboardView_keyHintLetterPadding, 0.0f);
        mKeyPopupHintLetter = keyboardViewAttr.getString(
                R.styleable.KeyboardView_keyPopupHintLetter);
        mKeyPopupHintLetterPadding = keyboardViewAttr.getDimension(
                R.styleable.KeyboardView_keyPopupHintLetterPadding, 0.0f);
        mKeyShiftedLetterHintPadding = keyboardViewAttr.getDimension(
                R.styleable.KeyboardView_keyShiftedLetterHintPadding, 0.0f);
        mKeyTextShadowRadius = keyboardViewAttr.getFloat(
                R.styleable.KeyboardView_keyTextShadowRadius, KET_TEXT_SHADOW_RADIUS_DISABLED);
        mVerticalCorrection = keyboardViewAttr.getDimension(
                R.styleable.KeyboardView_verticalCorrection, 0.0f);
        keyboardViewAttr.recycle();

        mDefaultKeyLabelFlags = keyAttr.getInt(R.styleable.Keyboard_Key_keyLabelFlags, 0);
        Typeface typeface = (mDrawableProvider != null && mDrawableProvider.getKeyboardTypeface() != null)
                ? mDrawableProvider.getKeyboardTypeface()
                : ResourcesCompat.getFont(context, R.font.ndot_family);

        mKeyVisualAttributes = KeyVisualAttributes.newInstance(keyAttr, mDrawableProvider, typeface);

        if((mIsMoreKeys || mIsMoreKeysAction) && mKeyVisualAttributes != null) {
            mKeyVisualAttributes.mTextColor = mDrawableProvider.getMoreKeysTextColor();
        }

        keyAttr.recycle();

        mPaint.setAntiAlias(true);
    }

    @Nullable
    public DynamicThemeProvider getDrawableProvider() {
        return mDrawableProvider;
    }

    public void setDrawableProvider(@Nullable final DynamicThemeProvider provider) {
        if (provider != null) {
            mDrawableProvider = provider;
            mKeyboardBackground = mIsMoreKeysAction ? null :
                    mIsMoreKeys ? provider.getMoreKeysKeyboardBackground() :
                            provider.getKeyboardBackground();
            setBackground(mKeyboardBackground);
            mKeyBackground = mIsMoreKeysAction ? provider.getActionPopupKey() :
                    mIsMoreKeys ? provider.getPopupKey() :
                            provider.getKeyBackground();
            if (mKeyBackground != null) {
                mKeyBackground.getPadding(mKeyBackgroundPadding);
            }
            mFunctionalKeyBackground = mKeyBackground;
            mSpacebarBackground = provider.getSpaceBarBackground();
            if ((mIsMoreKeys || mIsMoreKeysAction) && mKeyVisualAttributes != null) {
                mKeyVisualAttributes.mTextColor = provider.getMoreKeysTextColor();
            }
            final Typeface typeface = (provider.getKeyboardTypeface() != null)
                    ? provider.getKeyboardTypeface()
                    : ResourcesCompat.getFont(getContext(), R.font.ndot_family);
            if (mKeyVisualAttributes != null) {
                mKeyVisualAttributes.mTypeface = typeface;
            }
            if (mKeyboard != null && mKeyboard.mKeyVisualAttributes != null) {
                mKeyboard.mKeyVisualAttributes.mTypeface = typeface;
            }
            if (mKeyboard != null) {
                final int keyHeight = mKeyboard.mMostCommonKeyHeight - mKeyboard.mVerticalGap;
                final int keyWidth = mKeyboard.mMostCommonKeyWidth;
                mKeyDrawParams.updateParams(keyWidth, Math.min(keyWidth, keyHeight), mKeyVisualAttributes);
                mKeyDrawParams.updateParams(keyWidth, Math.min(keyWidth, keyHeight), mKeyboard.mKeyVisualAttributes);
            }
            invalidateAllKeys();
        }
    }

    @Nullable
    public KeyVisualAttributes getKeyVisualAttribute() {
        return mKeyVisualAttributes;
    }

    private static void blendAlpha(@Nonnull final Paint paint, final int alpha) {
        final int color = paint.getColor();
        paint.setARGB((paint.getAlpha() * alpha) / Constants.Color.ALPHA_OPAQUE,
                Color.red(color), Color.green(color), Color.blue(color));
    }

    public void setHardwareAcceleratedDrawingEnabled(final boolean enabled) {
        if (!enabled) return;
        // TODO: Should use LAYER_TYPE_SOFTWARE when hardware acceleration is off?
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    /**
     * Attaches a keyboard to this view. The keyboard can be switched at any time and the
     * view will re-layout itself to accommodate the keyboard.
     * @see Keyboard
     * @see #getKeyboard()
     * @param keyboard the keyboard to display in this view
     */
    public void setKeyboard(@Nonnull final Keyboard keyboard) {
        mKeyboard = keyboard;
        if (keyboard.mOccupiedWidth != mLastOccupiedWidth) {
            mLastOccupiedWidth = keyboard.mOccupiedWidth;
            mLockedOccupiedHeight = keyboard.mOccupiedHeight;
        } else if (keyboard.mId != null && keyboard.mId.isAlphabetKeyboard()) {
            mLockedOccupiedHeight = keyboard.mOccupiedHeight;
        } else if (mLockedOccupiedHeight > 0 && isEligibleForHeightLock(keyboard.mId)) {
            keyboard.setOccupiedHeight(mLockedOccupiedHeight);
        }
        final int keyHeight = keyboard.mMostCommonKeyHeight - keyboard.mVerticalGap;
        final int keyWidth = keyboard.mMostCommonKeyWidth;

        mKeyDrawParams.updateParams(keyWidth, Math.min(keyWidth, keyHeight), mKeyVisualAttributes);
        mKeyDrawParams.updateParams(keyWidth, Math.min(keyWidth, keyHeight), keyboard.mKeyVisualAttributes);
        invalidateAllKeys();
        requestLayout();
    }

    /**
     * Returns the current keyboard being displayed by this view.
     * @return the currently attached keyboard
     * @see #setKeyboard(Keyboard)
     */
    @Nullable
    public Keyboard getKeyboard() {
        return mKeyboard;
    }

    protected float getVerticalCorrection() {
        return mVerticalCorrection;
    }

    @Nonnull
    protected KeyDrawParams getKeyDrawParams() {
        return mKeyDrawParams;
    }

    protected void updateKeyDrawParams(final int keyHeight) {
        final int keyWidth = mKeyboard.mMostCommonKeyWidth;
        mKeyDrawParams.updateParams(keyWidth, Math.min(keyWidth, keyHeight), mKeyVisualAttributes);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final Keyboard keyboard = getKeyboard();
        if (keyboard == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        final int occupiedHeight = (mLockedOccupiedHeight > 0 && isEligibleForHeightLock(keyboard.mId))
                ? mLockedOccupiedHeight : keyboard.mOccupiedHeight;
        // The main keyboard expands to the entire this {@link KeyboardView}.
        final int width = keyboard.mOccupiedWidth + getPaddingLeft() + getPaddingRight();
        final int height = occupiedHeight + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, height);
    }

    protected void onDrawUnderKeys(final Canvas canvas) {
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        if (canvas.isHardwareAccelerated()) {
            onDrawUnderKeys(canvas);
            onDrawKeyboard(canvas);
            return;
        }

        final boolean bufferNeedsUpdates = mInvalidateAllKeys || !mInvalidatedKeys.isEmpty();
        if (bufferNeedsUpdates || mOffscreenBuffer == null) {
            if (maybeAllocateOffscreenBuffer()) {
                mInvalidateAllKeys = true;
                // TODO: Stop using the offscreen canvas even when in software rendering
                mOffscreenCanvas.setBitmap(mOffscreenBuffer);
            }
            final Drawable background = getBackground();
            if (background != null) {
                mOffscreenCanvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
                background.draw(mOffscreenCanvas);
            }
            onDrawUnderKeys(mOffscreenCanvas);
            onDrawKeyboard(mOffscreenCanvas);
        }
        canvas.drawBitmap(mOffscreenBuffer, 0.0f, 0.0f, null);
    }

    private boolean maybeAllocateOffscreenBuffer() {
        final int width = getWidth();
        final int height = getHeight();
        if (width == 0 || height == 0) {
            return false;
        }
        if (mOffscreenBuffer != null && mOffscreenBuffer.getWidth() == width
                && mOffscreenBuffer.getHeight() == height) {
            return false;
        }
        freeOffscreenBuffer();
        mOffscreenBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        return true;
    }

    private void freeOffscreenBuffer() {
        setLayerType(LAYER_TYPE_NONE, null);

        mOffscreenCanvas.setBitmap(null);
        mOffscreenCanvas.setMatrix(null);
        if (mOffscreenBuffer != null) {
            mOffscreenBuffer.recycle();
            mOffscreenBuffer = null;
        }
    }

    private void onDrawKeyboard(@Nonnull final Canvas canvas) {
        final Keyboard keyboard = getKeyboard();
        if (keyboard == null) {
            return;
        }

        final Paint paint = mPaint;
        final Drawable background = getBackground();
        // Calculate clip region and set.
        final boolean drawAllKeys = mInvalidateAllKeys || mInvalidatedKeys.isEmpty();
        final boolean isHardwareAccelerated = canvas.isHardwareAccelerated();
        // TODO: Confirm if it's really required to draw all keys when hardware acceleration is on.
        if (drawAllKeys || isHardwareAccelerated) {
            // Draw all keys.
            for (final Key key : keyboard.getSortedKeys()) {
                onDrawKey(key, canvas, paint);
            }
        } else {
            for (final Key key : mInvalidatedKeys) {
                if (!keyboard.hasKey(key)) {
                    continue;
                }
                if (background != null) {
                    // Need to redraw key's background on {@link #mOffscreenBuffer}.
                    final int x = key.getX() + getPaddingLeft();
                    final int y = key.getY() + getPaddingTop();
                    mClipRect.set(x, y, x + key.getWidth(), y + key.getHeight());
                    canvas.save();
                    canvas.clipRect(mClipRect);
                    canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
                    background.draw(canvas);
                    canvas.restore();
                }
                onDrawKey(key, canvas, paint);
            }
        }

        mInvalidatedKeys.clear();
        mInvalidateAllKeys = false;
    }

    private void onDrawKey(@Nonnull final Key key, @Nonnull final Canvas canvas,
            @Nonnull final Paint paint) {
        final int keyDrawX = key.getDrawX() + getPaddingLeft();
        final int keyDrawY = key.getY() + getPaddingTop();
        canvas.translate(keyDrawX, keyDrawY);

        final KeyVisualAttributes attr = key.getVisualAttributes();
        final KeyDrawParams params = mKeyDrawParams.mayCloneAndUpdateParams(key.getWidth(),
                Math.min(key.getHeight(), key.getWidth()), attr);
        params.mAnimAlpha = Constants.Color.ALPHA_OPAQUE;

        if (!key.isSpacer()) {
            final Drawable background = key.selectBackgroundDrawable(
                    mKeyBackground, mFunctionalKeyBackground, mSpacebarBackground);
            if (background != null) {
                onDrawKeyBackground(key, canvas, background);
            }
        }
        onDrawKeyTopVisuals(key, canvas, paint, params);

        canvas.translate(-keyDrawX, -keyDrawY);
    }

    // Draw key background.
    public static boolean isNumberRowKey(@Nullable final KeyboardId keyboardId, @Nullable final Key key) {
        if (keyboardId == null || key == null) return false;
        if (!keyboardId.isAlphabetKeyboard() || !keyboardId.mNumberRow) {
            return false;
        }
        final String label = key.getLabel();
        if (label == null || label.isEmpty()) return false;
        final char c = label.charAt(0);
        final boolean isDigit = (c >= '0' && c <= '9') || (c >= '\u09E6' && c <= '\u09EF');
        if (isDigit) {
            return key.getY() <= (key.getHeight() / 2);
        }
        if (key.getY() <= (key.getHeight() / 2) && "!@#$%^&*()".indexOf(c) >= 0) {
            return true;
        }
        return false;
    }

    /* package */ boolean isNumberRowKey(@Nonnull final Key key) {
        return isNumberRowKey(mKeyboard != null ? mKeyboard.mId : null, key);
    }

    protected void onDrawKeyBackground(@Nonnull final Key key, @Nonnull final Canvas canvas,
            @Nonnull final Drawable background) {
        final Drawable customBg = (mDrawableProvider != null) ? mDrawableProvider.getKeyBackground(key, key.isPressed()) : null;
        final Drawable bgToDraw = (customBg != null) ? customBg : background;
        final int keyWidth = key.getDrawWidth();
        final int keyHeight = key.getHeight();
        final int bgWidth, bgHeight, bgX, bgY;
        final boolean isNumberRow = isNumberRowKey(key);
        if (isNumberRow || (key.needsToKeepBackgroundAspectRatio(mDefaultKeyLabelFlags)
                // HACK: To disable expanding normal/functional key background.
                && !key.hasCustomActionLabel())) {
            final int size = Math.min(keyWidth, keyHeight);
            bgWidth = size;
            bgHeight = size;
            bgX = (keyWidth - size) / 2;
            bgY = (keyHeight - size) / 2;
        } else {
            final Rect padding = mKeyBackgroundPadding;
            bgWidth = keyWidth + padding.left + padding.right;
            bgHeight = keyHeight + padding.top + padding.bottom;
            bgX = -padding.left;
            bgY = -padding.top;
        }
        final Rect bounds = bgToDraw.getBounds();
        if (bgWidth != bounds.right || bgHeight != bounds.bottom) {
            bgToDraw.setBounds(0, 0, bgWidth, bgHeight);
        }
        canvas.translate(bgX, bgY);
        bgToDraw.draw(canvas);
        canvas.translate(-bgX, -bgY);
    }

    // Draw key top visuals.
    protected void onDrawKeyTopVisuals(@Nonnull final Key key, @Nonnull final Canvas canvas,
            @Nonnull final Paint paint, @Nonnull final KeyDrawParams params) {
        final int keyWidth = key.getDrawWidth();
        final int keyHeight = key.getHeight();
        final float centerX = keyWidth * 0.5f;
        final float centerY = keyHeight * 0.5f;

        if (mDrawableProvider != null && mDrawableProvider.onDrawCustomKeyTopVisuals(
                key, canvas, paint, keyWidth, keyHeight, params.mAnimAlpha)) {
            if (key.hasPopupHint() && key.getMoreKeys() != null) {
                drawKeyPopupHint(key, canvas, paint, params);
            }
            return;
        }

        // Draw key label.
        final Keyboard keyboard = getKeyboard();
        final Drawable icon = (keyboard == null) ? null
                : key.getIcon(keyboard.mIconsSet, params.mAnimAlpha);
        final Drawable hintIcon = (keyboard == null) ? null
                : key.getHintIcon(keyboard.mIconsSet, params.mAnimAlpha);
        float labelX = centerX;
        float labelBaseline = centerY;
        final String rawLabel = cleanCombiningMarkForDisplay(key.getLabel());
        final String override = (mDrawableProvider != null)
                ? mDrawableProvider.getKeyLabelOverride(key, rawLabel != null ? rawLabel : "")
                : null;
        final String label = (override != null)
                ? override
                : rawLabel;
        if (label != null && !label.isEmpty()) {
            final boolean isBengaliNumberPad = (keyboard != null && keyboard.mId != null &&
                    keyboard.mId.mElementId == KeyboardId.ELEMENT_NUMBER_BENGALI);
            final boolean isQurova = (mDrawableProvider != null && mDrawableProvider.isQurovaFont());
            if (shouldFallbackToDefaultTypeface(isQurova, isBengaliNumberPad, label)) {
                paint.setTypeface(Typeface.DEFAULT);
            } else {
                paint.setTypeface(key.selectTypeface(params));
            }
            paint.setTextSize(key.selectTextSize(params));
            final float labelCharHeight = TypefaceUtils.getReferenceCharHeight(paint);
            final float labelCharWidth = TypefaceUtils.getReferenceCharWidth(paint);

            // Vertical label text alignment.
            labelBaseline = centerY + labelCharHeight / 2.0f;

            // Horizontal label text alignment
            if (key.isAlignLabelOffCenter()) {
                // The label is placed off center of the key. Used mainly on "phone number" layout.
                labelX = centerX + params.mLabelOffCenterRatio * labelCharWidth;
                paint.setTextAlign(Align.LEFT);
            } else {
                labelX = centerX;
                paint.setTextAlign(Align.CENTER);
            }
            final float stringWidth = TypefaceUtils.getStringWidth(label, paint);
            final float maxLabelWidth = keyWidth * MAX_LABEL_RATIO;
            if (key.needsAutoXScale() || stringWidth > maxLabelWidth) {
                final float ratio = Math.min(1.0f, maxLabelWidth / Math.max(1.0f, stringWidth));
                if (key.needsAutoScale() || stringWidth > maxLabelWidth) {
                    final float autoSize = paint.getTextSize() * ratio;
                    paint.setTextSize(autoSize);
                } else {
                    paint.setTextScaleX(ratio);
                }
            }

            if (key.isEnabled()) {
                final Integer customTextColor = (mDrawableProvider != null) ? mDrawableProvider.getKeyTextColor(key) : null;
                if (customTextColor != null) {
                    paint.setColor(customTextColor);
                } else {
                    paint.setColor(key.selectTextColor(params));
                }
                // Set a drop shadow for the text if the shadow radius is positive value.
                if (mKeyTextShadowRadius > 0.0f) {
                    paint.setShadowLayer(mKeyTextShadowRadius, 0.0f, 0.0f, params.mTextShadowColor);
                } else {
                    paint.clearShadowLayer();
                }
            } else {
                // Make label invisible
                paint.setColor(Color.TRANSPARENT);
                paint.clearShadowLayer();
            }
            blendAlpha(paint, params.mAnimAlpha);
            final float rotation = (mDrawableProvider != null) ? mDrawableProvider.getKeyRotation(key) : 0.0f;
            if (rotation != 0.0f) {
                canvas.save();
                canvas.rotate(rotation, centerX, centerY);
                canvas.drawText(label, 0, label.length(), labelX, labelBaseline, paint);
                canvas.restore();
            } else {
                canvas.drawText(label, 0, label.length(), labelX, labelBaseline, paint);
            }
            // Turn off drop shadow and reset x-scale.
            paint.clearShadowLayer();
            paint.setTextScaleX(1.0f);
        }

        // Draw hint label.
        final String hintLabel = cleanCombiningMarkForDisplay(key.getHintLabel());
        if (hintLabel != null) {
            final Integer customHintColor = (mDrawableProvider != null) ? mDrawableProvider.getKeyHintColor(key) : null;
            final int hintColor = (customHintColor != null) ? customHintColor : key.selectHintTextColor(params);
            if (Color.alpha(hintColor) > 0) {
                paint.setTextSize(key.selectHintTextSize(params));
                paint.setColor(hintColor);
                final boolean isQurova = (mDrawableProvider != null && mDrawableProvider.isQurovaFont());
                if (shouldFallbackToDefaultTypeface(isQurova, false, hintLabel)) {
                    paint.setTypeface(Typeface.DEFAULT);
                } else {
                    paint.setTypeface(key.selectTypeface(params));
                }
                blendAlpha(paint, params.mAnimAlpha);
                final float labelCharHeight = TypefaceUtils.getReferenceCharHeight(paint);
                final float labelCharWidth = TypefaceUtils.getReferenceCharWidth(paint);
                final float hintX, hintBaseline;
                final float[] customCoords = (mDrawableProvider != null)
                        ? mDrawableProvider.getKeyHintCoordinates(key, keyWidth, keyHeight, mKeyHintLetterPadding, paint)
                        : null;
                if (customCoords != null && customCoords.length >= 2) {
                    hintX = customCoords[0];
                    hintBaseline = customCoords[1];
                    paint.setTextAlign(Align.CENTER);
                } else if (key.hasHintLabel()) {
                    // The hint label is placed just right of the key label. Used mainly on
                    // "phone number" layout.
                    hintX = labelX + params.mHintLabelOffCenterRatio * labelCharWidth;
                    if (key.isAlignHintLabelToBottom(mDefaultKeyLabelFlags)) {
                        hintBaseline = labelBaseline;
                    } else {
                        hintBaseline = centerY + labelCharHeight / 2.0f;
                    }
                    paint.setTextAlign(Align.LEFT);
                } else if (key.hasShiftedLetterHint()) {
                    // The hint label is placed at top-right corner of the key. Used mainly on tablet.
                    final float padX = Math.max(mKeyShiftedLetterHintPadding, keyWidth * 0.08f);
                    final float padY = Math.max(mKeyShiftedLetterHintPadding, keyHeight * 0.08f);
                    hintX = keyWidth - padX - labelCharWidth / 2.0f;
                    paint.getFontMetrics(mFontMetrics);
                    hintBaseline = -mFontMetrics.top + padY;
                    paint.setTextAlign(Align.CENTER);
                } else { // key.hasHintLetter()
                    // The hint letter is placed at top-right corner of the key. Used mainly on phone.
                    final float hintDigitWidth = TypefaceUtils.getReferenceDigitWidth(paint);
                    final float hintLabelWidth = TypefaceUtils.getStringWidth(hintLabel, paint);
                    final float padX = Math.max(mKeyHintLetterPadding, keyWidth * 0.08f);
                    final float padY = Math.max(mKeyHintLetterPadding, keyHeight * 0.08f);
                    hintX = keyWidth - padX
                            - Math.max(hintDigitWidth, hintLabelWidth) / 2.0f;
                    hintBaseline = -paint.ascent() + padY;
                    paint.setTextAlign(Align.CENTER);
                }
                final float adjustmentY = (customCoords != null) ? 0.0f : (params.mHintLabelVerticalAdjustment * labelCharHeight);
                canvas.drawText(
                        hintLabel, 0, hintLabel.length(), hintX, hintBaseline + adjustmentY, paint);
            }
        } else if(hintIcon != null) {
            final float size = key.selectHintTextSize(params);

            int iconWidth = (int)size;
            int iconHeight = (int)size;

            int hintX = keyWidth - iconWidth - (int)mKeyHintLetterPadding;
            int hintY = (int)mKeyHintLetterPadding;

            final float[] customCoords = (mDrawableProvider != null)
                    ? mDrawableProvider.getKeyHintCoordinates(key, keyWidth, keyHeight, mKeyHintLetterPadding)
                    : null;
            if (customCoords != null && customCoords.length >= 2) {
                hintX = (int)Math.max(0, Math.min(keyWidth - iconWidth, customCoords[0] - iconWidth / 2.0f));
                hintY = (int)Math.max(0, Math.min(keyHeight - iconHeight, customCoords[1] - iconHeight));
            }

            hintIcon.setTint(key.selectHintTextColor(params));
            drawIcon(canvas, hintIcon, hintX, hintY, iconWidth, iconHeight);
        }

        // Draw key icon.
        if ((label == null || label.isEmpty()) && icon != null) {
            final float size = key.selectTextSize(params) * 1.75f;

            int iconWidth;
            if (key.getCode() == Constants.CODE_SPACE && icon instanceof NinePatchDrawable) {
                iconWidth = (int)(keyWidth * mSpacebarIconWidthRatio);
            } else {
                iconWidth = Math.min(icon.getIntrinsicWidth(), keyWidth);
            }
            int iconHeight = icon.getIntrinsicHeight();

            if(iconWidth > size) {
                iconHeight = (int)((float)iconHeight / (float)iconWidth * (float)size);
                iconWidth = (int)size;
            }

            final int iconY;
            if (key.isAlignIconToBottom()) {
                iconY = keyHeight - iconHeight;
            } else {
                iconY = (keyHeight - iconHeight) / 2; // Align vertically center.
            }
            final int iconX = (keyWidth - iconWidth) / 2; // Align horizontally center.

            final Integer customIconColor = (mDrawableProvider != null) ? mDrawableProvider.getKeyTextColor(key) : null;
            if (customIconColor != null) {
                icon.setTint(customIconColor);
            } else {
                icon.setTint(key.selectTextColor(params));
            }
            drawIcon(canvas, icon, iconX, iconY, iconWidth, iconHeight);
        }

        if (key.hasPopupHint() && key.getMoreKeys() != null) {
            drawKeyPopupHint(key, canvas, paint, params);
        }

        if (mDrawableProvider != null) {
            mDrawableProvider.onPostDrawKeyVisuals(key, canvas, paint, keyWidth, keyHeight, labelX, labelBaseline);
        }
    }

    // Draw popup hint "..." at the bottom right corner of the key.
    protected void drawKeyPopupHint(@Nonnull final Key key, @Nonnull final Canvas canvas,
            @Nonnull final Paint paint, @Nonnull final KeyDrawParams params) {
        if (TextUtils.isEmpty(mKeyPopupHintLetter)) {
            return;
        }
        final int keyWidth = key.getDrawWidth();
        final int keyHeight = key.getHeight();

        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(params.mHintLetterSize);
        paint.setColor(params.mHintLabelColor);
        paint.setTextAlign(Align.CENTER);
        final float hintX = keyWidth - mKeyHintLetterPadding
                - TypefaceUtils.getReferenceCharWidth(paint) / 2.0f;
        final float hintY = keyHeight - mKeyPopupHintLetterPadding;
        canvas.drawText(mKeyPopupHintLetter, hintX, hintY, paint);
    }

    protected static void drawIcon(@Nonnull final Canvas canvas,@Nonnull final Drawable icon,
            final int x, final int y, final int width, final int height) {
        canvas.translate(x, y);
        icon.setBounds(0, 0, width, height);
        icon.draw(canvas);
        canvas.translate(-x, -y);
    }

    public Paint newLabelPaint(@Nullable final Key key) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        if (key == null) {
            paint.setTypeface(mKeyDrawParams.mTypeface);
            paint.setTextSize(mKeyDrawParams.mLabelSize);
        } else {
            paint.setColor(key.selectTextColor(mKeyDrawParams));
            final String kLabel = key.getLabel();
            final boolean isBengaliNumberPad = (mKeyboard != null && mKeyboard.mId != null &&
                    mKeyboard.mId.mElementId == KeyboardId.ELEMENT_NUMBER_BENGALI);
            final boolean isQurova = (mDrawableProvider != null && mDrawableProvider.isQurovaFont());
            if (shouldFallbackToDefaultTypeface(isQurova, isBengaliNumberPad, kLabel)) {
                paint.setTypeface(Typeface.DEFAULT);
            } else {
                paint.setTypeface(key.selectTypeface(mKeyDrawParams));
            }
            paint.setTextSize(key.selectTextSize(mKeyDrawParams));
        }
        return paint;
    }

    /**
     * Requests a redraw of the entire keyboard. Calling {@link #invalidate} is not sufficient
     * because the keyboard renders the keys to an off-screen buffer and an invalidate() only
     * draws the cached buffer.
     * @see #invalidateKey(Key)
     */
    public void invalidateAllKeys() {
        mInvalidatedKeys.clear();
        mInvalidateAllKeys = true;
        invalidate();
    }

    /**
     * Invalidates a key so that it will be redrawn on the next repaint. Use this method if only
     * one key is changing it's content. Any changes that affect the position or size of the key
     * may not be honored.
     * @param key key in the attached {@link Keyboard}.
     * @see #invalidateAllKeys
     */
    public void invalidateKey(@Nullable final Key key) {
        if (mInvalidateAllKeys || key == null) {
            return;
        }
        mInvalidatedKeys.add(key);
        final int x = key.getX() + getPaddingLeft();
        final int y = key.getY() + getPaddingTop();
        invalidate(x, y, x + key.getWidth(), y + key.getHeight());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        freeOffscreenBuffer();
    }

    public void deallocateMemory() {
        freeOffscreenBuffer();
    }

    public static boolean isCombiningMark(final int codePoint) {
        if ((codePoint >= 0x0981 && codePoint <= 0x0983)
                || codePoint == 0x09BC
                || (codePoint >= 0x09BE && codePoint <= 0x09C4)
                || (codePoint >= 0x09C7 && codePoint <= 0x09C8)
                || (codePoint >= 0x09CB && codePoint <= 0x09CD)
                || codePoint == 0x09D7) {
            return true;
        }
        final int type = Character.getType(codePoint);
        return type == Character.NON_SPACING_MARK || type == Character.COMBINING_SPACING_MARK;
    }

    @Nullable
    public static String cleanCombiningMarkForDisplay(@Nullable final String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String cleaned = text;
        if (cleaned.indexOf('\u25CC') >= 0) {
            cleaned = cleaned.replace("\u25CC", "\u00A0");
        }
        if (cleaned.length() > 0 && isCombiningMark(cleaned.codePointAt(0))) {
            cleaned = "\u00A0" + cleaned;
        }
        return cleaned;
    }

    public static boolean shouldFallbackToDefaultTypeface(
            final boolean isQurova,
            final boolean isBengaliNumberPad,
            @Nullable final String text
    ) {
        if (isBengaliNumberPad) {
            return true;
        }
        if (text == null) {
            return false;
        }
        boolean hasBengali = false;
        boolean hasDigit = false;
        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);
            if (c >= 0x0980 && c <= 0x09FF) {
                hasBengali = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (c > 0x024F) {
                // Symbols, arrows (↵, ⇑), emoji (🎀, 🙂), and Unicode art fallback safely to system default font
                return true;
            }
        }
        return hasBengali || (isQurova && hasDigit);
    }
}
