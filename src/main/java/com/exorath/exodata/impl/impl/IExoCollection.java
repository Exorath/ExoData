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
import com.exorath.exodata.impl.api.ExoDocument;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by toonsev on 8/22/2016.
 */
public class IExoCollection implements ExoCollection{
    private MongoCollection collection;

    public IExoCollection(MongoCollection collection){
        this.collection = collection;
    }

    @Override
    public Observable<String> createIndex(Bson bson) {
        return Observable.create(s -> {
            s.onNext(collection.createIndex(bson));
            s.onCompleted();
        }).subscribeOn(Schedulers.io()).cast(String.class);
    }

    @Override
    public ExoDocument getDocument(Object id) {
        return ExoDocument.create(collection, id);
    }

    @Override
    public MongoCollection getMongoCollection() {
        return collection;
    }
}
