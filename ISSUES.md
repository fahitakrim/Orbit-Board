# Orbit Board — Active Issues & Feature Tracking

## Active & Resolved High-Priority Issues

### ISSUE-001: Symbol Page 2 and Page 3 Two-Way Toggle Connection (User Marked)
- **Status**: RESOLVED & VERIFIED (Build 1.0.2)
- **Reported By**: User (with visual screenshots: `media_1793135938970_8b3cd142.png` & `media_1793135938804_745667f6.png`)
- **Description**:
  - **2nd Page (`kbd_symbols_bengali.xml`)**: Contains Bengali numbers `১ ২ ৩ ৪ ৫ ৬ ৭ ৮ ৯ ০`, currency `৳`, and basic punctuation. The red-marked key above `অকখ` is labeled `= \<`.
  - **3rd Page (`kbd_symbols_shift_bengali.xml`)**: Contains symbols `~ ` | ₹ √ π ÷ × { }`, `⇥ £ $ % ^ ° _ = [ ]`, etc. The red-marked key above `অকখ` is labeled `?১২৩`.
  - **Requirement**: The red-marked key on these two pages MUST act as a direct two-way bridge connecting them:
    - Pressing `= \<` on 2nd Page $\rightarrow$ Immediately switches to 3rd Page (`kbd_symbols_shift_bengali`).
    - Pressing `?১২৩` on 3rd Page $\rightarrow$ Immediately switches back to 2nd Page (`kbd_symbols_bengali`).
    - The bottom-left functional key `অকখ` (or `ABC` in English) remains the dedicated key to return to the 1st page (Alphabet layout).
- **Root Cause & Architectural Resolution**:
  - `PointerTracker.java`: When a modifier key triggers a layout switch on `ACTION_DOWN` (`mLayoutChangedOnDown`), `onUpEventInternal` releases key graphics without sending `detectAndSendKey`, suppressing unintentional double-toggling of the newly revealed opposite key on finger lift.
  - `KeyboardState.java`: Symbol paging switch state is set to `SWITCH_STATE_SYMBOL_BEGIN` (latched/persistent) rather than momentary, preventing sliding input gestures from accidentally reverting symbol pages on release.

---

### ISSUE-002: Ridmik-Parity Long-Press Cut, Copy, and Paste Shortcuts
- **Status**: RESOLVED & VERIFIED (Build 1.0.2)
- **Reported By**: User ("copy by c, cut by x and not working like ridmic.. note them i will give you some more feedback")
- **Description**:
  - Ridmik Keyboard signature feature:
    - Long-press on `C` $\rightarrow$ Copy selected text, or fallback to all text buffer (`beforeCursor + afterCursor`).
    - Long-press on `X` $\rightarrow$ Cut selected text, or fallback to all text buffer (`deleteSurroundingText`).
    - Long-press on `V` $\rightarrow$ Paste from clipboard.
- **Root Cause & Architectural Resolution**:
  - `Key.java`: Added `isCutKey()`, `isCopyKey()`, and `isPasteKey()` to recognize physical X, C, V positions across English QWERTY (`x, c, v`), Bengali Avro (`x, c, v`), Bengali Probhat (`শ/ঢ়, চ/ছ, আ/ঋ`), and Bengali Jatiyo (`ো/ৌ, ে/ৈ, র`), enabling long-press timers for all of them.
  - `PointerTracker.java`: Dispatches `CUSTOM_CODE_CUT`, `CUSTOM_CODE_COPY`, and `CUSTOM_CODE_PASTE` on long-press before any more-keys panels.
  - `LatinIMELegacy.java`: Implemented full buffer fallback logic via `getTextBeforeCursor` + `getTextAfterCursor` and `deleteSurroundingText(lenBefore, lenAfter)` with haptic feedback and Toast notifications ("Copied", "Cut", "Pasted").

---

### ISSUE-005: Caps Lock Synchronization and Glitch Resolution
- **Status**: RESOLVED & VERIFIED (Build 1.0.2)
- **Reported By**: User ("উল্টা কেপস লক করার নিতে গ্লিচ চলে আসছে")
- **Description**: Rapid layout flickering and failure to unshift when triggering Caps Lock via Shift double-tap or long-press.
- **Root Cause & Architectural Resolution**:
  - In `PointerTracker.java`, when `moreKeyCode == Constants.CODE_CAPSLOCK` triggers via long-press auto-more-key, `sListener.onReleaseKey(key.getCode(), false)` is explicitly invoked for the original `CODE_SHIFT` before switching to Caps Lock, preventing `mShiftKeyState` from staying permanently trapped in `STATE_PRESSING`.
  - In `KeyboardState.java`, `mShiftKeyState.onRelease()` is called when setting `CODE_CAPSLOCK`.
  - In `onPressShift()`, if `mAlphabetShiftState.isShiftLocked()` is active, a single tap on Shift cleanly unlocks Caps Lock directly back to unshifted lowercase.

---

### ISSUE-003: Bengali Composing & Backspace Word Deletion Safeguard
- **Status**: IN-REVIEW
- **Description**: Ensure backspacing after space deletes only the space character without erasing the entire preceding Bengali word.

---

### ISSUE-004: Configurable Long-Press Delay Setting
- **Status**: PLANNED
- **Description**: Add a setting in Settings $\rightarrow$ Typing to adjust long-press duration (e.g. 150ms - 400ms).
