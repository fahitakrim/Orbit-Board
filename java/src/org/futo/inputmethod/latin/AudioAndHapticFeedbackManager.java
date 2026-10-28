/*
 * Copyright (C) 2012 The Android Open Source Project
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

package org.futo.inputmethod.latin;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;

import org.futo.inputmethod.latin.common.Constants;
import org.futo.inputmethod.latin.settings.Settings;
import org.futo.inputmethod.latin.settings.SettingsValues;

/**
 * This class gathers audio feedback and haptic feedback functions.
 *
 * It offers a consistent and simple interface that allows LatinIME to forget about the
 * complexity of settings and the like.
 */
public final class AudioAndHapticFeedbackManager {
    private static final String TAG = AudioAndHapticFeedbackManager.class.getSimpleName();
    private AudioManager mAudioManager;
    private Vibrator mVibrator;
    private SoundPool mSoundPool;
    private int mSoundStandard = 0;
    private int mSoundSpacebar = 0;
    private int mSoundDelete = 0;
    private int mSoundReturn = 0;

    private SettingsValues mSettingsValues;
    private boolean mSoundOn;

    private static final AudioAndHapticFeedbackManager sInstance =
            new AudioAndHapticFeedbackManager();

    public static AudioAndHapticFeedbackManager getInstance() {
        return sInstance;
    }

    private AudioAndHapticFeedbackManager() {
        // Intentional empty constructor for singleton.
    }

    public static void init(final Context context) {
        sInstance.initInternal(context);
    }

    private void initInternal(final Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        try {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mSoundPool = new SoundPool.Builder()
                    .setMaxStreams(8)
                    .setAudioAttributes(audioAttributes)
                    .build();
            mSoundStandard = mSoundPool.load(context, R.raw.keypress_standard, 1);
            mSoundSpacebar = mSoundPool.load(context, R.raw.keypress_spacebar, 1);
            mSoundDelete = mSoundPool.load(context, R.raw.keypress_delete, 1);
            mSoundReturn = mSoundPool.load(context, R.raw.keypress_return, 1);
        } catch (final Exception e) {
            Log.e("AudioFeedback", "Failed to init SoundPool", e);
        }

        try {
            Settings settings = Settings.getInstance();
            if (settings != null) {
                if (settings.getCurrent() != null) {
                    onSettingsChanged(settings.getCurrent());
                }
                settings.settingsChangedListeners.add((old, newSettings) -> {
                    onSettingsChanged(newSettings);
                });
            }
        } catch (final Exception e) {
            Log.e(TAG, "Failed to register settings listener", e);
        }
    }

    public void performHapticAndAudioFeedback(final int code,
            final View viewToPerformHapticFeedbackOn) {
        performHapticFeedback(viewToPerformHapticFeedbackOn, false);
        performAudioFeedback(code);
    }

    public boolean hasVibrator() {
        return mVibrator != null && mVibrator.hasVibrator();
    }

    public void vibrate(final long milliseconds) {
        if (mVibrator == null || milliseconds <= 0) {
            return;
        }
        try {
            mVibrator.vibrate(milliseconds);
        } catch (final Throwable t) {
            Log.e(TAG, "Vibration failed", t);
        }
    }

    private boolean reevaluateIfSoundIsOn() {
        if (mSettingsValues == null || !mSettingsValues.mSoundOn) {
            return false;
        }
        if (mAudioManager != null && mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            return false;
        }
        return true;
    }

    public void performAudioFeedback(final int code) {
        if (!mSoundOn) {
            return;
        }
        final float vol = (mSettingsValues != null && mSettingsValues.mKeypressSoundVolume >= 0.0f)
                ? mSettingsValues.mKeypressSoundVolume : 0.8f;
        if (mSoundPool != null) {
            final int soundId;
            switch (code) {
            case Constants.CODE_DELETE:
                soundId = mSoundDelete != 0 ? mSoundDelete : mSoundStandard;
                break;
            case Constants.CODE_ENTER:
                soundId = mSoundReturn != 0 ? mSoundReturn : mSoundStandard;
                break;
            case Constants.CODE_SPACE:
                soundId = mSoundSpacebar != 0 ? mSoundSpacebar : mSoundStandard;
                break;
            default:
                soundId = mSoundStandard;
                break;
            }
            if (soundId != 0) {
                mSoundPool.play(soundId, vol, vol, 1, 0, 1.0f);
                return;
            }
        }
        if (mAudioManager != null) {
            final int sound;
            switch (code) {
            case Constants.CODE_DELETE:
                sound = AudioManager.FX_KEYPRESS_DELETE;
                break;
            case Constants.CODE_ENTER:
                sound = AudioManager.FX_KEYPRESS_RETURN;
                break;
            case Constants.CODE_SPACE:
                sound = AudioManager.FX_KEYPRESS_SPACEBAR;
                break;
            default:
                sound = AudioManager.FX_KEYPRESS_STANDARD;
                break;
            }
            mAudioManager.playSoundEffect(sound, vol);
        }
    }

    public void performHapticFeedback(final View viewToPerformHapticFeedbackOn, final boolean repeatKey) {
        if (mSettingsValues == null || !mSettingsValues.mVibrateOn) {
            return;
        }
        if (mSettingsValues.mKeypressVibrationDuration >= 0) {
            vibrate(mSettingsValues.mKeypressVibrationDuration / (repeatKey ? 2 : 1));
            return;
        }
        // Go ahead with the system default
        if (viewToPerformHapticFeedbackOn != null) {
            viewToPerformHapticFeedbackOn.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP);
        }
    }

    public void onSettingsChanged(final SettingsValues settingsValues) {
        mSettingsValues = settingsValues;
        mSoundOn = reevaluateIfSoundIsOn();
    }

    public void onRingerModeChanged() {
        mSoundOn = reevaluateIfSoundIsOn();
    }
}
