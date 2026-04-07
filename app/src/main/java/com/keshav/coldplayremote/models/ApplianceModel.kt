package com.keshav.coldplayremote.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Remote(
    val name: String = "",
    val buttons: List<RemoteButton> = emptyList()
) : Parcelable

@Parcelize
data class RemoteButton(
    val name: String = "",
    val code: String = "",
) : Parcelable
