package com.kglabs.wristdj.components

import androidx.lifecycle.ViewModel
import com.kglabs.wristdj.RemoteSignalConstants
import com.kglabs.wristdj.models.Remote
import com.kglabs.wristdj.models.RemoteButton
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