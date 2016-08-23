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

import com.exorath.exodata.impl.api.ExoCollection;
import com.exorath.exodata.impl.api.ExoDatabase;
import com.mongodb.client.MongoDatabase;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by toonsev on 8/22/2016.
 */
public class IExoDatabase implements ExoDatabase {
    private MongoDatabase database;
    public IExoDatabase(MongoDatabase database){
        this.database = database;
    }
    @Override
    public Observable<ExoCollection> getCollection(String name) {
        return Observable.create(s -> s.onNext(ExoCollection.create(database.getCollection(name))))
                .subscribeOn(Schedulers.io()).cast(ExoCollection.class);
    }

    @Override
    public MongoDatabase getMongoDatabase() {
        return database;
    }
}
