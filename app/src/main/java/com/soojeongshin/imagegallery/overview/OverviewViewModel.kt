package com.soojeongshin.imagegallery.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.soojeongshin.imagegallery.network.Hit
import com.soojeongshin.imagegallery.network.PixabayApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * The [ViewModel] that is attached to the [OverviewFragment].
 */
class OverviewViewModel : ViewModel() {

    private val _status = MutableLiveData<String>()

    val status: LiveData<String>
        get() = _status

    // Internally, we use a MutableLiveData, because we will be updating the List of Hit
    // with new values
    private val _hits = MutableLiveData<List<Hit>>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    val hits: LiveData<List<Hit>>
        get() = _hits

    // Internally, we use a MutableLiveData to handle navigation to the selected hit
    private val _navigateToSelectedHit = MutableLiveData<Hit>()

    // The external immutable LiveData for the navigation hit
    val navigateToSelectedHit: LiveData<Hit>
        get() =_navigateToSelectedHit

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    /**
     * Call getPixabayImages() on init so we can display status immediately.
     */
    init {
        getPixabayImages()
    }

    private fun getPixabayImages() {
        coroutineScope.launch {
            val getImagesSuspended  =
                PixabayApi.retrofitService.getImageResponse(
                    "10961674-bf47eb00b05f514cdd08f6e11",
                    1)
            try {
                val listResult = getImagesSuspended.hits
                if (listResult!!.isNotEmpty()) {
                    _hits.value = listResult
                }
            } catch (e:Exception) {
                _status.value = "Failure: ${e.message}"
            }
        }
    }

    /**
     * When the [ViewModel] is finished, we cancel our coroutine [viewModelJob], which tells the
     * Retrofit service to stop.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * When the hit is clicked, set the [_navigateToSelectedHit] [MutableLiveData]
     * @param hit The [Hit] that was clicked on
     */
    fun displayHitDetails(hit: Hit) {
        _navigateToSelectedHit.value = hit
    }

    /**
     * After the navigation has taken place, make sure navigateToSelectedHit is set to null
     */
    fun displayHitDetailsComplete() {
        _navigateToSelectedHit.value = null
    }
}