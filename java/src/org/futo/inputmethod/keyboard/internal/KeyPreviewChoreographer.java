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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import org.futo.inputmethod.keyboard.Key;
import org.futo.inputmethod.latin.common.CoordinateUtils;
import org.futo.inputmethod.latin.utils.ViewLayoutUtils;

import java.util.ArrayDeque;
import java.util.HashMap;

/**
 * This class controls pop up key previews. This class decides:
 * - what kind of key previews should be shown.
 * - where key previews should be placed.
 * - how key previews should be shown and dismissed.
 */
public final class KeyPreviewChoreographer {
    // Free {@link KeyPreviewView} pool that can be used for key preview.
    private final ArrayDeque<KeyPreviewView> mFreeKeyPreviewViews = new ArrayDeque<>();
    // Map from {@link Key} to {@link KeyPreviewView} that is currently being displayed as key
    // preview.
    private final HashMap<Key,KeyPreviewView> mShowingKeyPreviewViews = new HashMap<>();

    private final KeyPreviewDrawParams mParams;

    public KeyPreviewChoreographer(final KeyPreviewDrawParams params) {
        mParams = params;
    }

    public KeyPreviewView getKeyPreviewView(final Key key, final ViewGroup placerView) {
        KeyPreviewView keyPreviewView = mShowingKeyPreviewViews.remove(key);
        if (keyPreviewView != null) {
            return keyPreviewView;
        }
        keyPreviewView = mFreeKeyPreviewViews.poll();
        if (keyPreviewView != null) {
            return keyPreviewView;
        }
        final Context context = placerView.getContext();
        keyPreviewView = new KeyPreviewView(context, null /* attrs */);
        keyPreviewView.setBackground(mParams.mPreviewBackground);
        placerView.addView(keyPreviewView, ViewLayoutUtils.newLayoutParam(placerView, 0, 0));
        return keyPreviewView;
    }

    public boolean isShowingKeyPreview(final Key key) {
        return mShowingKeyPreviewViews.containsKey(key);
    }

    public void dismissKeyPreview(final Key key, final boolean withAnimation) {
        if (key == null) {
            return;
        }
        final KeyPreviewView keyPreviewView = mShowingKeyPreviewViews.get(key);
        if (keyPreviewView == null) {
            return;
        }
        final Object tag = keyPreviewView.getTag();
        if (withAnimation) {
            if (tag instanceof KeyPreviewAnimators) {
                final KeyPreviewAnimators animators = (KeyPreviewAnimators)tag;
                animators.startDismiss();
                return;
            }
        }
        // Dismiss preview without animation.
        mShowingKeyPreviewViews.remove(key);
        if (tag instanceof KeyPreviewAnimators) {
            ((KeyPreviewAnimators)tag).cancel();
        } else if (tag instanceof Animator) {
            ((Animator)tag).cancel();
        }
        keyPreviewView.setTag(null);
        keyPreviewView.setVisibility(View.INVISIBLE);
        mFreeKeyPreviewViews.add(keyPreviewView);
    }

    public void cancelAllOngoingEvents() {
        for (final Key key : new java.util.ArrayList<>(mShowingKeyPreviewViews.keySet())) {
            dismissKeyPreview(key, false /* withAnimation */);
        }
    }

    public void placeAndShowKeyPreview(final Key key, final KeyboardIconsSet iconsSet,
            final KeyDrawParams drawParams, final int keyboardViewWidth, final int[] keyboardOrigin,
            final ViewGroup placerView, final boolean withAnimation) {
        if (mParams.mProvider != null && ("off".equalsIgnoreCase(mParams.mProvider.getKeyPopupStyle())
                || "none".equalsIgnoreCase(mParams.mProvider.getKeyPopupStyle()))) {
            return;
        }
        final KeyPreviewView keyPreviewView = getKeyPreviewView(key, placerView);
        placeKeyPreview(
                key, keyPreviewView, iconsSet, drawParams, keyboardViewWidth, keyboardOrigin, placerView);
        showKeyPreview(key, keyPreviewView, withAnimation);
    }

    private void placeKeyPreview(final Key key, final KeyPreviewView keyPreviewView,
            final KeyboardIconsSet iconsSet, final KeyDrawParams drawParams,
            final int keyboardViewWidth, final int[] originCoords, final ViewGroup placerView) {
        if (mParams.mProvider != null && ("off".equalsIgnoreCase(mParams.mProvider.getKeyPopupStyle())
                || "none".equalsIgnoreCase(mParams.mProvider.getKeyPopupStyle()))) {
            keyPreviewView.setBackground(null);
            keyPreviewView.setVisibility(View.INVISIBLE);
            return;
        }
        if (mParams.mProvider != null) {
            final Drawable currentBg = keyPreviewView.getBackground();
            final Drawable customFeedback;
            if (currentBg instanceof KeyPopupDrawable) {
                customFeedback = mParams.mProvider.getKeyPreviewBackground(key, (KeyPopupDrawable) currentBg);
            } else {
                customFeedback = mParams.mProvider.getKeyPreviewBackground(key);
            }
            if (customFeedback != null) {
                if (customFeedback != currentBg) {
                    keyPreviewView.setBackground(customFeedback);
                } else {
                    keyPreviewView.syncPaddingWithBackground();
                    keyPreviewView.invalidate();
                }
            } else {
                keyPreviewView.setBackground(mParams.mPreviewBackground);
            }
            Integer customTextColor = mParams.mProvider.getKeyPreviewTextColor(key);
            if (customTextColor == null) {
                customTextColor = mParams.mProvider.getKeyTextColor(key);
            }
            keyPreviewView.setPreviewVisual(key, iconsSet, drawParams, customTextColor);
        } else {
            keyPreviewView.setBackground(mParams.mPreviewBackground);
            keyPreviewView.setPreviewVisual(key, iconsSet, drawParams, null);
        }
        final int keyDrawWidth = key.getDrawWidth();
        final int previewWidth = keyDrawWidth;
        final int previewHeight = key.getHeight() * 2;
        keyPreviewView.measure(
                View.MeasureSpec.makeMeasureSpec(previewWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(previewHeight, View.MeasureSpec.EXACTLY));
        mParams.setGeometry(keyPreviewView);

        final int[] placerOrigin = CoordinateUtils.newInstance();
        if (placerView != null) {
            placerView.getLocationInWindow(placerOrigin);
        }
        final int originX = CoordinateUtils.x(originCoords) - CoordinateUtils.x(placerOrigin);
        final int originY = CoordinateUtils.y(originCoords) - CoordinateUtils.y(placerOrigin);

        // The key preview is horizontally aligned with the center of the visible part of the
        // parent key within the KeyboardView. If it doesn't fit in this {@link KeyboardView},
        // it is clamped to the KeyboardView bounds and left/right background is used.
        final int keyPreviewPosition;
        int previewX = key.getDrawX() - (previewWidth - keyDrawWidth) / 2;
        if (previewX < 0) {
            previewX = 0;
            keyPreviewPosition = KeyPreviewView.POSITION_LEFT;
        } else if (previewX > keyboardViewWidth - previewWidth) {
            previewX = keyboardViewWidth - previewWidth;
            keyPreviewPosition = KeyPreviewView.POSITION_RIGHT;
        } else {
            keyPreviewPosition = KeyPreviewView.POSITION_MIDDLE;
        }
        final boolean hasMoreKeys = (key.getMoreKeys() != null);
        keyPreviewView.setPreviewBackground(hasMoreKeys, keyPreviewPosition);

        // Place relative to placerView
        previewX += originX;
        final int previewY = key.getY() - previewHeight + mParams.mPreviewOffset
                + originY + key.getHeight();

        ViewLayoutUtils.placeViewAt(
                keyPreviewView, previewX, previewY, previewWidth, previewHeight);
        keyPreviewView.syncPaddingWithBackground();
        keyPreviewView.setPivotX(previewWidth / 2.0f);
        keyPreviewView.setPivotY(previewHeight);
    }

    void showKeyPreview(final Key key, final KeyPreviewView keyPreviewView,
            final boolean withAnimation) {
        if (!withAnimation) {
            keyPreviewView.setVisibility(View.VISIBLE);
            mShowingKeyPreviewViews.put(key, keyPreviewView);
            return;
        }

        // Show preview with animation.
        final Animator showUpAnimator = createShowUpAnimator(key, keyPreviewView);
        final Animator dismissAnimator = createDismissAnimator(key, keyPreviewView);
        final KeyPreviewAnimators animators = new KeyPreviewAnimators(
                showUpAnimator, dismissAnimator);
        keyPreviewView.setTag(animators);
        animators.startShowUp();
    }

    public Animator createShowUpAnimator(final Key key, final KeyPreviewView keyPreviewView) {
        final Animator showUpAnimator = mParams.createShowUpAnimator(keyPreviewView);
        showUpAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(final Animator animator) {
                showKeyPreview(key, keyPreviewView, false /* withAnimation */);
            }
        });
        return showUpAnimator;
    }

    private Animator createDismissAnimator(final Key key, final KeyPreviewView keyPreviewView) {
        final Animator dismissAnimator = mParams.createDismissAnimator(keyPreviewView);
        dismissAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animator) {
                dismissKeyPreview(key, false /* withAnimation */);
            }
        });
        return dismissAnimator;
    }

    private static class KeyPreviewAnimators extends AnimatorListenerAdapter {
        private final Animator mShowUpAnimator;
        private final Animator mDismissAnimator;

        public KeyPreviewAnimators(final Animator showUpAnimator, final Animator dismissAnimator) {
            mShowUpAnimator = showUpAnimator;
            mDismissAnimator = dismissAnimator;
        }

        public void startShowUp() {
            mShowUpAnimator.start();
        }

        public void startDismiss() {
            if (mShowUpAnimator.isRunning()) {
                mShowUpAnimator.addListener(this);
                return;
            }
            mDismissAnimator.start();
        }

        public void cancel() {
            mShowUpAnimator.cancel();
            mDismissAnimator.cancel();
        }

        @Override
        public void onAnimationEnd(final Animator animator) {
            mDismissAnimator.start();
        }
    }
}
