package com.externalblesniffer.ui.selected.viewmodels

import androidx.lifecycle.ViewModel
import com.externalblesniffer.repo.CurrentConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SelectedViewModel @Inject constructor(
    currentConnection: CurrentConnection,
): ViewModel() {

    val latestReceivedData = currentConnection.latestReceivedData
}