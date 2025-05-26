package com.keshav.coldplayremote.components

import androidx.lifecycle.ViewModel
import com.keshav.coldplayremote.models.Remote
import com.keshav.coldplayremote.models.RemoteButton
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(): ViewModel() {

    fun getRemotes(): List<Remote>{
        return listOf(Remote("TV", listOf(RemoteButton("On", 4564), RemoteButton("Off", 4564))), Remote("SoundBar", listOf(RemoteButton("On", 4564), RemoteButton("Off", 4564))))
    }
}