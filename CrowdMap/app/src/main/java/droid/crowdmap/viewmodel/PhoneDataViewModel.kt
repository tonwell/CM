package droid.crowdmap.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import droid.crowdmap.basededados.PhoneData
import droid.crowdmap.repository.PhoneDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class PhoneDataViewModel(context: Context) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + viewModelJob
    private val viewModelJob = Job()

    private val phoneDataRepo by lazy {
        PhoneDataRepository(context)
    }

    private val _phoneData: MutableLiveData<List<PhoneData>> = MutableLiveData()
    val phoneDataLiveData: LiveData<List<PhoneData>> get() = _phoneData

    fun getVisiblePhoneData(firstCoord: LatLng, lastCoord: LatLng, networkOperator: String): LiveData<List<PhoneData>> {
        return phoneDataRepo.getVisiblePhoneDatas(firstCoord, lastCoord, networkOperator)
    }
}

class PhoneDataViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhoneDataViewModel::class.java)) {
            return PhoneDataViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
