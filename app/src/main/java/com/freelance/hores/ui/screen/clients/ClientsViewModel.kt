package com.freelance.hores.ui.screen.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.domain.model.Client
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientsViewModel @Inject constructor(
    private val repository: RegistreRepository
) : ViewModel() {

    val clients: StateFlow<List<Client>> = repository.getClients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveClient(client: Client) {
        viewModelScope.launch {
            repository.saveClient(client)
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            repository.deleteClient(client)
        }
    }
}
