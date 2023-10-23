package com.example.web_server_poc.utils

import android.Manifest
import com.tbruyelle.rxpermissions3.RxPermissions
import io.reactivex.rxjava3.disposables.Disposable

object PermissionUtils {

    val cameraPermission = Manifest.permission.CAMERA
    val audioPermission = Manifest.permission.RECORD_AUDIO
    val phoneStatePermission = Manifest.permission.READ_PHONE_STATE

    fun requestPermission(rxPermission: RxPermissions, vararg permission: String, block: () -> Unit): Disposable {
        return rxPermission.requestEach(*permission)
            .subscribe { perm ->  // will emit 2 Permission objects
                if (perm.granted) {
                    // permissions granted
                    block()
                } else if (perm.shouldShowRequestPermissionRationale) {
                    // Denied
                } else {
                    // Denied permission with ask never again,  need to go to the settings
                    // ignored
                }
            }
    }

//    fun checkPermissionsAndProceed(activity: FragmentActivity, permision: String, action: () -> Unit) {
//        if (!RxPermissions(activity).isGranted(permision)) {
//            requestPermission(permision, RxPermissions(activity)) { action() }
//        } else action()
//    }
//
//    fun checkPermissionsAndProceed(fragment: Fragment, permision: String, action: () -> Unit) {
//        if (!RxPermissions(fragment).isGranted(permision)) {
//            requestPermission(permision, RxPermissions(fragment)) { action() }
//        } else action()
//    }
//
//    fun requestPermissions(fragment: Fragment, vararg permissions: String): Observable<Boolean> {
//        return RxPermissions(fragment).request(*permissions)
//    }
//
//
//    fun isGranted(fragment: Fragment, vararg permissions: String): Boolean {
//        return permissions.find { !RxPermissions(fragment).isGranted(it) } == null
//    }

//    fun requestPermission(rxPermission: RxPermissions, block: () -> Unit, permissions: Array<String>): Disposable {
//        return rxPermission.request(*permissions)
//            .subscribe { result ->
//                if (result) block()
//            }
//    }
//
//    fun requestLocationPermission(rxPermission: RxPermissions, block: () -> Unit): Disposable {
//        return rxPermission.request(
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        )
//            .subscribe { result ->
//                if (result) block()
//            }
//    }
//
//    fun isLocationPermissionsGrated(context: Context): Boolean {
//        return ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//    }
}