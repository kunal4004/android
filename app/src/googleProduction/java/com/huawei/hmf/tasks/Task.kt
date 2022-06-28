package com.huawei.hmf.tasks

/*
 * Dummy class to prevent build errors for "google" product flavor
 * since Huawei dependencies are restricted for "huawei"" flavor only
 */
class Task<TResult> {
    fun addOnSuccessListener(var1: (TResult) -> Unit): Task<TResult> {
        return this
    }

    fun addOnFailureListener(var1: (Exception) -> Unit): Task<TResult> {
        return this
    }
}