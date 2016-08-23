/*
 * Copyright 2016 Exorath
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.exorath.exodata.impl.impl;

import com.exorath.exodata.impl.api.DataAPI;
import com.exorath.exodata.impl.api.ExoDatabase;
import com.mongodb.MongoClient;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by toonsev on 8/22/2016.
 */
public class IDataAPI implements DataAPI {
    private MongoClient client;

    public IDataAPI(){
        this.client = new MongoClient();
    }
    public IDataAPI(String host){
        this.client = new MongoClient(host);
    }

    public IDataAPI(String host, int port){
        this.client = new MongoClient(host, port);
    }
    public IDataAPI(MongoClient client){
        this.client = client;
    }
    @Override
    public Observable<ExoDatabase> getDatabase(String name) {
        return Observable.create(subscriber -> {
            subscriber.onNext(ExoDatabase.create(client.getDatabase(name)));
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io()).cast(ExoDatabase.class);
    }
}
