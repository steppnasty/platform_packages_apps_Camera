/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Copyright (C) 2013, The Linux Foundation. All rights reserved.
 * Not a contribution. Apache license notifcation and license are
 * retained for attribution purposes only.
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

package com.android.camera;

import android.content.Context;
import android.hardware.Camera.Parameters;
import android.view.LayoutInflater;

import com.android.camera.ui.AbstractSettingPopup;
import com.android.camera.ui.ListPrefSettingPopup;
import com.android.camera.ui.MoreSettingPopup;
import com.android.camera.ui.PieItem;
import com.android.camera.ui.PieItem.OnClickListener;
import com.android.camera.ui.PieRenderer;

public class PhotoController extends PieController
        implements MoreSettingPopup.Listener,
        ListPrefSettingPopup.Listener {
    private static String TAG = "CAM_photocontrol";
    private static float FLOAT_PI_DIVIDED_BY_TWO = (float) Math.PI / 2;
    private final String mSettingOff;
    private int popupNum;

    private PhotoModule mModule;
    private String[] mOtherKeys1;
    private String[] mOtherKeys2;
    private String[] mOtherKeys3;
   // First level popups
    private MoreSettingPopup mPopup1;
    private MoreSettingPopup mPopup2;
    private MoreSettingPopup mPopup3;
    private MoreSettingPopup currPopup;
    // Second level popup
    private AbstractSettingPopup mSecondPopup;

    public PhotoController(CameraActivity activity, PhotoModule module, PieRenderer pie) {
        super(activity, pie);
        mModule = module;
        mSettingOff = activity.getString(R.string.setting_off_value);
    }

    public void initialize(PreferenceGroup group) {
        super.initialize(group);
        mPopup1 = null;
        mPopup2 = null;
        mPopup3 = null;
        currPopup = null;
        mSecondPopup = null;
        popupNum = 0;

        float sweep = FLOAT_PI_DIVIDED_BY_TWO / 2;
        addItem(CameraSettings.KEY_FLASH_MODE, FLOAT_PI_DIVIDED_BY_TWO - sweep, sweep);
        addItem(CameraSettings.KEY_EXPOSURE, 3 * FLOAT_PI_DIVIDED_BY_TWO - sweep, sweep);
        addItem(CameraSettings.KEY_WHITE_BALANCE, 3 * FLOAT_PI_DIVIDED_BY_TWO + sweep, sweep);
        if (group.findPreference(CameraSettings.KEY_CAMERA_ID) != null) {
            PieItem item = makeItem(R.drawable.ic_switch_photo_facing_holo_light);
            item.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO + sweep, sweep);
            item.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(PieItem item) {
                    // Find the index of next camera.
                    ListPreference camPref = mPreferenceGroup
                            .findPreference(CameraSettings.KEY_CAMERA_ID);
                    if (camPref != null) {
                        int index = camPref.findIndexOfValue(camPref.getValue());
                        CharSequence[] values = camPref.getEntryValues();
                        index = (index + 1) % values.length;
                        int newCameraId = Integer
                                .parseInt((String) values[index]);
                        mListener.onCameraPickerClicked(newCameraId);
                    }
                }
            });
            mRenderer.addItem(item);
        }
        if (group.findPreference(CameraSettings.KEY_CAMERA_HDR) != null) {
            PieItem hdr = makeItem(R.drawable.ic_hdr);
            hdr.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO, sweep);
            hdr.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(PieItem item) {
                    // Find the index of next camera.
                    ListPreference pref = mPreferenceGroup
                            .findPreference(CameraSettings.KEY_CAMERA_HDR);
                    if (pref != null) {
                        // toggle hdr value
                        int index = (pref.findIndexOfValue(pref.getValue()) + 1) % 2;
                        pref.setValueIndex(index);
                        onSettingChanged(pref);
                    }
                }
            });
            mRenderer.addItem(hdr);
        }

       // Dividing the set of other keys into two parts, to accommodate them on the screen
        mOtherKeys1 = new String[] {
                CameraSettings.KEY_SCENE_MODE,
                CameraSettings.KEY_RECORD_LOCATION,
                CameraSettings.KEY_PICTURE_SIZE,
                CameraSettings.KEY_HISTOGRAM,
                CameraSettings.KEY_FOCUS_MODE,
                CameraSettings.KEY_PICTURE_FORMAT,
                CameraSettings.KEY_JPEG_QUALITY,
                CameraSettings.KEY_ZSL};

        mOtherKeys2 = new String[] {
                CameraSettings.KEY_COLOR_EFFECT,
                CameraSettings.KEY_FACE_DETECTION,
                CameraSettings.KEY_FACE_RECOGNITION,
                CameraSettings.KEY_TOUCH_AF_AEC,
                CameraSettings.KEY_SELECTABLE_ZONE_AF,
                CameraSettings.KEY_SATURATION,
                CameraSettings.KEY_CONTRAST,
                CameraSettings.KEY_SHARPNESS,
                CameraSettings.KEY_AUTOEXPOSURE};

        mOtherKeys3 = new String[] {
                CameraSettings.KEY_ANTIBANDING,
                CameraSettings.KEY_ISO,
                CameraSettings.KEY_DENOISE,
                CameraSettings.KEY_REDEYE_REDUCTION,
                CameraSettings.KEY_AE_BRACKET_HDR};

        PieItem item1 = makeItem(R.drawable.ic_settings_holo_light);
        item1.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO * 3, sweep);
        item1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(PieItem item) {
                if (mPopup1 == null) {
                    initializePopup();
                }
                mModule.showPopup(mPopup1);
                currPopup = mPopup1;
                popupNum = 1;
            }
        });
        mRenderer.addItem(item1);

        PieItem item2 = makeItem(R.drawable.ic_settings_holo_light);
        item2.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO * 3 - (2*sweep), sweep);
        item2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(PieItem item) {
                if (mPopup2 == null) {
                    initializePopup();
                }
                mModule.showPopup(mPopup2);
                currPopup = mPopup2;
                popupNum = 2;
            }
        });
        mRenderer.addItem(item2);

        PieItem item3 = makeItem(R.drawable.ic_settings_holo_light);
        item3.setFixedSlice(FLOAT_PI_DIVIDED_BY_TWO * 3 + (2*sweep), sweep);
        item3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(PieItem item) {
                if (mPopup3 == null) {
                    initializePopup();
                }
                mModule.showPopup(mPopup3);
                currPopup = mPopup3;
                popupNum = 3;
            }
        });
        mRenderer.addItem(item3);

    }

    protected void setCameraId(int cameraId) {
        ListPreference pref = mPreferenceGroup.findPreference(CameraSettings.KEY_CAMERA_ID);
        pref.setValue("" + cameraId);
    }

    @Override
    public void reloadPreferences() {
        super.reloadPreferences();
        if (mPopup1 != null) {
            mPopup1.reloadPreference();
        }
        if (mPopup2 != null) {
            mPopup2.reloadPreference();
        }
        if (mPopup3 != null) {
            mPopup3.reloadPreference();
        }
    }

    @Override
    // Hit when an item in the second-level popup gets selected
    public void onListPrefChanged(ListPreference pref) {
        if (mPopup1 != null && mSecondPopup != null && (mPopup2 != null) && (mPopup3 != null)) {
                mModule.dismissPopup(true);
                mPopup1.reloadPreference();
                mPopup2.reloadPreference();
                mPopup3.reloadPreference();
        }
        onSettingChanged(pref);
    }

    @Override
    public void overrideSettings(final String ... keyvalues) {
        super.overrideSettings(keyvalues);
        if ((mPopup1 == null) &&  (mPopup2 == null)  &&  (mPopup3 == null)) initializePopup();
        mPopup1.overrideSettings(keyvalues);
        mPopup2.overrideSettings(keyvalues);
        mPopup3.overrideSettings(keyvalues);
    }

    protected void initializePopup() {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        MoreSettingPopup popup1 = (MoreSettingPopup) inflater.inflate(
                R.layout.more_setting_popup, null, false);
        popup1.setSettingChangedListener(this);
        popup1.initialize(mPreferenceGroup, mOtherKeys1);
        if (mActivity.isSecureCamera()) {
            // Prevent location preference from getting changed in secure camera mode
            popup1.setPreferenceEnabled(CameraSettings.KEY_RECORD_LOCATION, false);
        }		
        mPopup1 = popup1;


        MoreSettingPopup popup2 = (MoreSettingPopup) inflater.inflate(
                R.layout.more_setting_popup, null, false);
        popup2.setSettingChangedListener(this);
        popup2.initialize(mPreferenceGroup, mOtherKeys2);
        mPopup2 = popup2;

        MoreSettingPopup popup3 = (MoreSettingPopup) inflater.inflate(
                R.layout.more_setting_popup, null, false);
        popup3.setSettingChangedListener(this);
        popup3.initialize(mPreferenceGroup, mOtherKeys3);
        mPopup3 = popup3;

    }

    public void popupDismissed(boolean topPopupOnly) {
        // if the 2nd level popup gets dismissed
        if (mSecondPopup != null) {
            mSecondPopup = null;

            initializePopup();

            if (topPopupOnly) {
                if(popupNum == 1) mModule.showPopup(mPopup1);
                else if(popupNum == 2) mModule.showPopup(mPopup2);
                else if(popupNum == 3) mModule.showPopup(mPopup3);
            }
        }
        else{
            initializePopup();
        }
    }

    // Return true if the preference has the specified key but not the value.
    private static boolean notSame(ListPreference pref, String key, String value) {
        return (key.equals(pref.getKey()) && !value.equals(pref.getValue()));
    }

    private void setPreference(String key, String value) {
        ListPreference pref = mPreferenceGroup.findPreference(key);
        if (pref != null && !value.equals(pref.getValue())) {
            pref.setValue(value);
            reloadPreferences();
        }
    }

    @Override
    public void onSettingChanged(ListPreference pref) {
        // Reset the scene mode if HDR is set to on. Reset HDR if scene mode is
        // set to non-auto.
        if (notSame(pref, CameraSettings.KEY_CAMERA_HDR, mSettingOff)) {
            setPreference(CameraSettings.KEY_SCENE_MODE, Parameters.SCENE_MODE_AUTO);
        } else if (notSame(pref, CameraSettings.KEY_SCENE_MODE, Parameters.SCENE_MODE_AUTO)) {
            setPreference(CameraSettings.KEY_CAMERA_HDR, mSettingOff);
        }
        super.onSettingChanged(pref);
    }

    @Override
    // Hit when an item in the first-level popup gets selected, then bring up
    // the second-level popup
    public void onPreferenceClicked(ListPreference pref) {
        if (mSecondPopup != null) return;

        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        ListPrefSettingPopup basic = (ListPrefSettingPopup) inflater.inflate(
                R.layout.list_pref_setting_popup, null, false);
        basic.initialize(pref);
        basic.setSettingChangedListener(this);
        mModule.dismissPopup(true);
        mSecondPopup = basic;
        mModule.showPopup(mSecondPopup);
    }
}
