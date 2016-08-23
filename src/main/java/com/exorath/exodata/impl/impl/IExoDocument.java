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

import com.exorath.exodata.impl.api.ExoDocument;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import rx.Observable;
import rx.schedulers.Schedulers;

import static com.mongodb.client.model.Filters.and;

/**
 * Created by toonsev on 8/19/2016.
 */
public class IExoDocument implements ExoDocument{
    private Object id;
    private Document document;

    private MongoCollection collection;

    public IExoDocument(MongoCollection collection, Object id) {
        this.id = id;
        this.collection = collection;
    }

    public Object getId() {
        return id;
    }

    @Override
    public MongoCollection getCollection() {
        return collection;
    }

    @Override
    public Observable<Document> getCachedOrFetch() {
        return document == null ? fetch() : Observable.create(s -> {
            s.onNext(document);
            s.onCompleted();
        });
    }

    @Override
    public Observable<Document> fetch() {
        return Observable.create((subscriber -> {
            Document fetched = (Document) collection.findOneAndUpdate(getIdQuery(), Updates.setOnInsert("_id", getId().toString()),
                    new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
            document = fetched;
            subscriber.onNext(fetched);
            subscriber.onCompleted();
        })).subscribeOn(Schedulers.io()).cast(Document.class);
    }
    @Override
    public Observable<UpdateResult> pop(String key, boolean first) {
        return first ? update(Updates.popFirst(key), false) : update(Updates.popLast(key), false);
    }
    @Override
    public Observable<UpdateResult> set(String key, Object value) {
        return update(Updates.set(key, value), true);
    }
    @Override
    public Observable<UpdateResult> inc(String key, Number amount) {
        return update(Updates.inc(key, amount), true);
    }

    @Override
    public Observable<UpdateResult> decIfSufficient(String key, int amount) {
        return incIfHas(key, Math.abs(amount), -amount);
    }

    @Override
    public Observable<UpdateResult> decIfSufficient(String key, long amount) {
        return incIfHas(key, Math.abs(amount), -amount);
    }

    @Override
    public Observable<UpdateResult> decIfSufficient(String key, double amount) {
        return incIfHas(key, Math.abs(amount), -amount);
    }

    private Observable<UpdateResult> incIfHas(String key, Number has, Number increment) {
        return update(and(getIdQuery(), Filters.gte(key, has)), Updates.inc(key, increment), false);
    }

    @Override
    public Observable<UpdateResult> push(String key, Object value) {
        return update(Updates.push(key, value), true);
    }

    @Override
    public Observable<UpdateResult> remove(String key) {
        return update(Updates.unset(key), false);
    }

    @Override
    public Observable<UpdateResult> update(Bson update, boolean upsert) {
        return update(getIdQuery(), update, upsert);
    }

    @Override
    public Observable<UpdateResult> update(Bson query, Bson update, boolean upsert) {
        return Observable.create((subscriber -> {
            subscriber.onNext(collection.updateOne(query, update, new UpdateOptions().upsert(upsert)));
            subscriber.onCompleted();
        })).subscribeOn(Schedulers.io()).cast(UpdateResult.class);
    }

    public Document getIdQuery() {
        return new Document("_id", id.toString());
    }
}
