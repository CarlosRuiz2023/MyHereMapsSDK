/*
 * Copyright (C) 2019-2024 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package com.hackaprende.myheremapssdk.permisos;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

/**
 * Convenience class to request the Android permissions as defined by manifest.
 */
public class PermissionsRequestor {

    // The request code must be < 256
    private static final int PERMISSIONS_REQUEST_CODE = 42;
    // Create a new ResultListener
    private ResultListener resultListener;
    // Create an activity
    private final Activity activity;
    // Create a constructor
    public PermissionsRequestor(Activity activity) {
        this.activity = activity;
    }
    // Create an interface
    public interface ResultListener {
        void permissionsGranted();
        void permissionsDenied();
    }
    // Create a method to request the permissions
    public void request(ResultListener resultListener) {
        // Set the result listener
        this.resultListener = resultListener;
        // Get the missing permissions
        String[] missingPermissions = getPermissionsToRequest();
        // Request the permissions
        if (missingPermissions.length == 0) {
            // All permissions are already granted
            resultListener.permissionsGranted();
        } else {
            // Request the permissions
            ActivityCompat.requestPermissions(activity, missingPermissions, PERMISSIONS_REQUEST_CODE);
        }
    }
    // Get the permissions to request
    @SuppressWarnings("deprecation")
    private String[] getPermissionsToRequest() {
        // Create a list of permissions to request
        ArrayList<String> permissionList = new ArrayList<>();
        try {
            // Get the package name
            String packageName = activity.getPackageName();
            // Generate a package info
            PackageInfo packageInfo;
            // Verification for Android 11
            if (Build.VERSION.SDK_INT >= 33) {
                // Android 11
                packageInfo = activity.getPackageManager().getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS));
            } else {
                // Android <11
                packageInfo = activity.getPackageManager().getPackageInfo(
                        packageName,
                        PackageManager.GET_PERMISSIONS);
            }
            // Get the requested permissions
            if (packageInfo.requestedPermissions != null) {
                // Loop through the requested permissions
                for (String permission : packageInfo.requestedPermissions) {
                    // Check if the permission is already granted
                    if (ContextCompat.checkSelfPermission(
                            activity, permission) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M &&
                                permission.equals(Manifest.permission.CHANGE_NETWORK_STATE)) {
                            // Exclude CHANGE_NETWORK_STATE as it does not require explicit user approval.
                            // This workaround is needed for devices running Android 6.0.0,
                            // see https://issuetracker.google.com/issues/37067994
                            continue;
                        }
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                            permission.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                            continue;
                        }
                        // Add the permission
                        permissionList.add(permission);
                    }
                }
            }
        } catch (Exception e) {
            // Do nothing
            e.printStackTrace();
        }
        // Return the permissions to request
        return permissionList.toArray(new String[0]);
    }
    // Handle the permissions result
    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        // Check if the request code is the same
        if (resultListener == null) {
            return;
        }
        // Check if the request was cancelled
        if (grantResults.length == 0) {
            // Request was cancelled.
            return;
        }
        // Check if the request code is the same
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // Check if all permissions are granted
            boolean allGranted = true;
            // Loop through the grant results
            for (int result : grantResults) {
                // Check if the result is not granted
                allGranted &= result == PackageManager.PERMISSION_GRANTED;
            }
            // Check if all permissions are granted
            if (allGranted) {
                // All permissions are granted
                resultListener.permissionsGranted();
            } else {
                // At least one permission is denied
                resultListener.permissionsDenied();
            }
        }
    }
    // Check if all permissions are granted
    public boolean areAllPermissionsGranted() {
        // Get the missing permissions
        String[] missingPermissions = getPermissionsToRequest();
        // Check if there are any missing permissions
        return missingPermissions.length == 0;
    }
}
