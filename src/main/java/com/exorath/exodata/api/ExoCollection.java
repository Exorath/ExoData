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

package com.exorath.exodata.api;

import com.exorath.exodata.impl.IExoCollection;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;
import rx.Observable;

/**
 * Created by toonsev on 8/22/2016.
 */
public interface ExoCollection {

    ExoDocument getDocument(Object id);

    /**
     * Create an index with the given keys.
     *
     * @param bson an object describing the index key(s), which may not be null
     * @return the index name
     * @see <a href="https://docs.mongodb.com/manual/reference/method/db.collection.createIndex/">https://docs.mongodb.com/manual/reference/method/db.collection.createIndex/</a>
     */
    Observable<String> createIndex(Bson bson);

    /**
     * Gets the actual {@link MongoCollection} encapsulated by this class
     * @return Gets the actual {@link MongoCollection}
     */
    MongoCollection getMongoCollection();

    /**
     * Creates an ExoCollection from an actual {@link MongoCollection}.
     * @param collection the actual {@link MongoCollection}
     * @return an ExoCollection that encapsulates the actual {@link MongoCollection}
     */
    static ExoCollection create(MongoCollection collection) {
        return new IExoCollection(collection);
    }
}
