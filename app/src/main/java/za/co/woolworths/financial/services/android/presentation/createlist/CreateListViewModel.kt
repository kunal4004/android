package za.co.woolworths.financial.services.android.presentation.createlist

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.domain.usecase.CreateNewListUC
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.presentation.addtolist.components.CreateNewListState
import za.co.woolworths.financial.services.android.presentation.createlist.components.CreateListScreenEvent
import javax.inject.Inject

@HiltViewModel
class CreateListViewModel @Inject constructor(
    private val createNewListUC: CreateNewListUC
) : ViewModel() {

    private val createNewListState = mutableStateOf(CreateNewListState())

    private val _isListCreated = MutableStateFlow<Boolean?>(null)
    val isListCreated: StateFlow<Boolean?> = _isListCreated.stateIn(
        scope = viewModelScope,
        initialValue = null,
        started = SharingStarted.WhileSubscribed(5000L)
    )

    fun onEvent(event: CreateListScreenEvent) {
        when (event) {
            is CreateListScreenEvent.CreateList -> createList(event.listName)
            else -> {}
        }
    }

    private fun createList(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (name.isEmpty()) {
                return@launch
            }

            createNewListUC(name.trim()).collect {
                viewModelScope.launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.SUCCESS -> {
                            createNewListState.value = createNewListState.value.copy(
                                isLoading = false,
                                isSuccess = true,
                                isError = false
                            )
                            _isListCreated.value = true
                        }

                        Status.ERROR -> {
                            createNewListState.value = createNewListState.value.copy(
                                isLoading = false,
                                isError = true,
                                isSuccess = false,
                                errorMessage = it.data?.response?.let {
                                    return@let it.desc ?: it.message ?: ""
                                } ?: ""
                            )
                        }

                        Status.LOADING -> {
                            createNewListState.value = createNewListState.value.copy(
                                isLoading = true,
                                isError = false,
                                isSuccess = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun getCreateListState(): CreateNewListState = createNewListState.value

}