package com.keshav.coldplayremote.components

import androidx.lifecycle.ViewModel
import com.keshav.coldplayremote.RemoteSignalConstants
import com.keshav.coldplayremote.models.Remote
import com.keshav.coldplayremote.models.RemoteButton
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    fun getRemotes(): List<Remote> {
        return listOf(
            Remote("Sound Bar", RemoteSignalConstants.SoundBar.buttons.map { 
                RemoteButton(it.first, it.second) 
            }),
            Remote("ColdPlay", RemoteSignalConstants.ColdPlayBand.buttons.map { 
                RemoteButton(it.first, it.second) 
            })
        )
    }
}