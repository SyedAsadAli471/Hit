package com.app.hit.util.images;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class RxBus {

    private static final RxBus ourInstance = new RxBus();

    private PublishSubject<Object> bus = PublishSubject.create();

    public static RxBus getInstance() {
        return ourInstance;
    }

    private RxBus() {
    }

    public void send(Object o) {
        bus.onNext(o);
    }

    public Observable<Object> toObservable() {
        return bus;
    }
}
