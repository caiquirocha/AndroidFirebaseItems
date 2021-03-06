package com.tstudioz.androidfirebaseitems.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tstudioz.androidfirebaseitems.data.DataItem;
import com.tstudioz.androidfirebaseitems.data.IFirebaseDatabaseItemRepository;
import com.tstudioz.androidfirebaseitems.data.Resource;
import com.tstudioz.essentialuilibrary.viewmodel.LiveDataEvent;
import com.tstudioz.essentialuilibrary.viewmodel.LiveDataEventWithTaggedObservers;

import java.util.List;

import javax.inject.Inject;

public class ItemsViewModel extends ViewModel {

    private IFirebaseDatabaseItemRepository<DataItem> repo;

    private MutableLiveData<List<DataItem>> items;
    private IFirebaseDatabaseItemRepository.FirebaseDatabaseRepositoryCallback<DataItem> callback;
    private MediatorLiveData<LiveDataEvent<Resource<DataItem>>> decreaseCountEvent = new MediatorLiveData<>();
    private MediatorLiveData<LiveDataEvent<Resource<DataItem>>> increaseCountEvent = new MediatorLiveData<>();

    @Inject
    public ItemsViewModel(final IFirebaseDatabaseItemRepository<DataItem> repo) {
        this.repo = repo;
        this.callback = new IFirebaseDatabaseItemRepository.FirebaseDatabaseRepositoryCallback<DataItem>() {
            @Override
            public void onSuccess(List<DataItem> result) {
                items.setValue(result);
            }

            @Override
            public void onError(Exception e) {
                items.setValue(null);
            }
        };
    }

    @MainThread
    public LiveData<List<DataItem>> getItems() {
        if (items == null) {
            items = new MutableLiveData<>();
            loadItems();
        }
        return items;
    }

    @MainThread
    private void loadItems() {
        repo.addItemListListener(callback);
    }

    @MainThread
    public void saveItem(@Nullable DataItem oldItem, @NonNull DataItem newItem) {
        repo.save(oldItem, newItem);
    }

    @MainThread
    public void deleteItem(@NonNull DataItem item) {
        repo.delete(item);
    }

    private LiveData<Resource<DataItem>> decreaseCountLiveData = new MutableLiveData<>();
    @MainThread
    public void decreaseCount(@NonNull DataItem item) {
        decreaseCountEvent.removeSource(decreaseCountLiveData);
        decreaseCountLiveData = repo.decreaseCount(item);
        decreaseCountEvent.addSource(decreaseCountLiveData, res -> {
            decreaseCountEvent.setValue(new LiveDataEvent<>(res));
        });
    }

    private LiveData<Resource<DataItem>> increaseCountLiveData = new MutableLiveData<>();
    @MainThread
    public void increaseCount(@NonNull DataItem item) {
        increaseCountEvent.removeSource(increaseCountLiveData);
        increaseCountLiveData = repo.increaseCount(item);
        increaseCountEvent.addSource(increaseCountLiveData, res -> {
            increaseCountEvent.setValue(new LiveDataEvent<>(res));
        });
    }

    @MainThread
    public LiveData<LiveDataEventWithTaggedObservers<Resource<DataItem>>> getSaveItemEvent() {
        return repo.getSaveModelEvent();
    }

    @MainThread
    public LiveData<LiveDataEventWithTaggedObservers<Resource<DataItem>>> getUpdateItemEvent() {
        return repo.getUpdateModelEvent();
    }

    @MainThread
    public LiveData<LiveDataEventWithTaggedObservers<Resource<DataItem>>> getDeleteItemEvent() {
        return repo.getDeleteModelEvent();
    }

    @MainThread
    public LiveData<LiveDataEvent<Resource<DataItem>>> getDecreaseCountEvent() {
        return decreaseCountEvent;
    }

    @MainThread
    public LiveData<LiveDataEvent<Resource<DataItem>>> getIncreaseCountEvent() {
        return increaseCountEvent;
    }

    @MainThread
    public LiveData<Resource<Boolean>> isConnected() {
        return repo.isConnected();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repo.removeItemListener(callback);
    }
}
