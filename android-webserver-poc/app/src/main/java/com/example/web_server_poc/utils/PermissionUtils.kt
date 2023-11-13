package com.example.web_server_poc.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.tbruyelle.rxpermissions3.RxPermissions
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable

object PermissionUtils {

    private val requiredPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
    )

    fun hasPermissionsGranted(context: Context): Boolean {
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        return true
    }
    fun requestPermission(activity: FragmentActivity): Single<List<Boolean>> {
        return RxPermissions(activity).request(*requiredPermissions)
            .toSortedList()
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