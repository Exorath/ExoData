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

import com.exorath.exodata.impl.IExoDocument;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import rx.Observable;

/**
 * TODO: Observable with external changes (fe the site)
 * An ExoDocument is an interface to fetch and update a specific document. Note that the cached document will not keep track of changes!
 * All Observable methods are cold observables. This means that the method will not execute unless subscribed to! If the method involves IO, it's most likely done on the {@link rx.schedulers.Schedulers#io} threads.
 * Created by toonsev on 8/21/2016.
 */
public interface ExoDocument {
    /**
     * Gets the namespace where this document is located in (the collection and database).
     *
     * @return the namespace where this document is located in
     */
    MongoCollection getCollection();

    /**
     * Gets the unique id of this document (MongoDB's '_id' field).
     *
     * @return the unique id of this document
     */
    Object getId();

    /**
     * Gets the cached version of the database document, if it was fetched already otherwise the document will be fetched from the database.
     * See the {@link #fetch()} method for the fetch behavior.
     *
     * @return an observable that will emit the document and complete, or throw an error
     */
    Observable<Document> getCachedOrFetch();

    /**
     * Fetches the latest version of the document from the database (note that even this may already be outdated when it arrives back).
     * <p>
     * The fetched document will also be cached for the {@link #getCachedOrFetch()} method.
     * <p>
     * If the document does not exist yet, this operation will create it atomically (with the findAndModify MongoDB operator).
     *
     * @return an observable that will emit the document and complete, or throw an error
     */
    Observable<Document> fetch();

    /**
     * Fetches the latest version of the document from the database (note that even this may already be outdated when it arrives back).
     * This method differs from {@link #fetch()} in the projection parameter. This allows you to specify which fields to include and exclude in the response document.
     * <p>
     * <p>
     * The fetched document will also be cached for the {@link #getCachedOrFetch()} method.
     * <p>
     * If the document does not exist yet, this operation will create it atomically (with the findAndModify MongoDB operator).
     *
     * @param projection the fields to include in the returned document
     * @return an observable that will emit the document and complete, or throw an error
     * @see <a http://mongodb.github.io/mongo-java-driver/3.0/builders/projections/">http://mongodb.github.io/mongo-java-driver/3.0/builders/projections/</a>
     */
    Observable<Document> fetch(Bson projection);


    /**
     * The set operator replaces the value of a field with the specified value. If the field does not exist, $set will add a new field with the specified value.
     * If you specify a dotted path for a non-existent field, $set will create the embedded documents as needed to fulfill the dotted path to the field.
     * <p>
     * This operation will create the document in the database if it does not exist yet.
     *
     * @param key   field identifier
     * @param value new value to assign
     * @return an observable that will emit the UpdateResult and complete, or throw an error
     * @see <a href="https://docs.mongodb.com/manual/reference/operator/update/set/">https://docs.mongodb.com/manual/reference/operator/update/set/</a>
     */
    Observable<UpdateResult> set(String key, Object value);

    /**
     * The inc operator increments a field by a specified value.
     * If the field does not exist, inc creates the field and sets the field to the specified value.
     * <p>
     * This operation will create the document in the database if it does not exist yet.
     *
     * @param key    field identifier
     * @param amount amount to increment (negative amount will decrement the field)
     * @return an observable that will emit the UpdateResult and complete, or throw an error
     * @see <a href="https://docs.mongodb.com/manual/reference/operator/update/inc/">https://docs.mongodb.com/manual/reference/operator/update/inc/</a>
     * <p>
     * The $inc operator accepts positive and negative values. Respectively incrementing and decrementing the field.
     */
    Observable<UpdateResult> inc(String key, Number amount);

    /**
     * Decrements a field if it has a sufficient value (greater or equal to the amount).
     * You can check whether or not this was successful by checking if the {@link UpdateResult#getModifiedCount()} is larger then zero.
     * <p>
     * Note that amount can be positive (decrements) and negative (increments), though I do not see why you would want to increment if there is enough balance ;).
     * <p>
     * This operation will NOT create the document in the database if it does not exist yet.
     *
     * @param key    field identifier
     * @param amount amount to decrement if sufficient
     * @return an observable that will emit the UpdateResult and complete, or throw an error. Check {@link UpdateResult#getModifiedCount()} to verify the decrement was successful.
     */
    Observable<UpdateResult> decIfSufficient(String key, int amount);

    /**
     * Decrements a field if it has a sufficient value (greater or equal to the amount).
     * You can check whether or not this was successful by checking if the {@link UpdateResult#getModifiedCount()} is larger then zero.
     * <p>
     * Note that amount can be positive (decrements) and negative (increments), though I do not see why you would want to increment if there is enough balance ;).
     * <p>
     * This operation will NOT create the document in the database if it does not exist yet.
     *
     * @param key    field identifier
     * @param amount amount to decrement if sufficient
     * @return an observable that will emit the UpdateResult and complete, or throw an error. Check {@link UpdateResult#getModifiedCount()} to verify the decrement was successful.
     */
    Observable<UpdateResult> decIfSufficient(String key, long amount);

    /**
     * Decrements a field if it has a sufficient value (greater or equal to the amount).
     * You can check whether or not this was successful by checking if the {@link UpdateResult#getModifiedCount()} is larger then zero.
     * <p>
     * Note that amount can be positive (decrements) and negative (increments), though I do not see why you would want to increment if there is enough balance ;).
     * <p>
     * This operation will NOT create the document in the database if it does not exist yet.
     *
     * @param key    field identifier
     * @param amount amount to decrement if sufficient
     * @return an observable that will emit the UpdateResult and complete, or throw an error. Check {@link UpdateResult#getModifiedCount()} to verify the decrement was successful.
     */
    Observable<UpdateResult> decIfSufficient(String key, double amount);

    /**
     * The push operator appends a specified value to an array.
     * If the field is absent in the document to update, push adds the array field with the value as its element.
     * If the value is an array, push appends the whole array as a single element.
     * <p>
     * This operation will create the document in the database if it does not exist yet.
     *
     * @param key   field identifier
     * @param value the element to append to the array
     * @return an observable that will emit the UpdateResult and complete, or throw an error.
     * @see <a href="https://docs.mongodb.com/manual/reference/operator/update/push/">https://docs.mongodb.com/manual/reference/operator/update/push/</a>
     */
    Observable<UpdateResult> push(String key, Object value);

    /**
     * The pop operator removes the first or last element of an array.
     * If the pop operator removes the last item in the field, the field will then hold an empty array.
     * <p>
     * This operation will NOT create the document in the database if it does not exist yet.
     *
     * @param key   field key of the array
     * @param first if true this operation pops the first element, if false it pops the last element
     * @return an observable that will emit the UpdateResult and complete, or throw an error.
     * @see <a href="https://docs.mongodb.com/manual/reference/operator/update/pop/">https://docs.mongodb.com/manual/reference/operator/update/pop/</a>
     */
    Observable<UpdateResult> pop(String key, boolean first);

    /**
     * The remove operator deletes a particular field.
     * You can check the {@link UpdateResult#getModifiedCount()} to see whether or not this element was removed (if the count == 0 it means the element was already removed).
     * To specify a field in an embedded (aka nested) document or in an array, use dot notation.
     * If the field does not exist, then remove does nothing (i.e. no operation).
     * <p>
     * This operation will NOT create the document in the database if it does not exist yet.
     *
     * @param key field identifier
     * @return an observable that will emit the UpdateResult and complete, or throw an error.
     * @see <a href="https://docs.mongodb.com/manual/reference/operator/update/unset/">https://docs.mongodb.com/manual/reference/operator/update/unset/</a>
     */
    Observable<UpdateResult> remove(String key);

    /**
     * Sends an update to the database on this data in MongoDB style (the query just contains the unique id of this document).
     *
     * @param update the update document
     * @param upsert whether or not the document should be created if no document matched the query
     * @return an observable that will emit the UpdateResult and complete, or throw an error.
     * @see <a href="https://api.mongodb.com/java/3.3/com/mongodb/client/model/Updates.html">https://api.mongodb.com/java/3.3/com/mongodb/client/model/Updates.html</a> for static methods to ease the writing of these updates.
     * @see <a href="https://docs.mongodb.com/manual/reference/method/db.collection.update/">https://docs.mongodb.com/manual/reference/method/db.collection.update/</a>
     */
    Observable<UpdateResult> update(Bson update, boolean upsert);

    /**
     * Sends an update to the database in MongoDB style.
     * Note that YOU MUST ADD THE "_id" QUERY TO THE QUERY yourself to specifiy this document (with the {@link #getId()} as value)! You can quickly receive this query with {@link #getIdQuery()}.
     *
     * @param query  the query document
     * @param update the update document
     * @param upsert whether or not the document should be created if no document matched the query
     * @return an observable that will emit the UpdateResult and complete, or throw an error.
     * @see <a href="https://api.mongodb.com/java/3.3/com/mongodb/client/model/Filters.html">https://api.mongodb.com/java/3.3/com/mongodb/client/model/Filters.html</a> for static methods to ease the writing of these queries.
     * @see <a href="https://api.mongodb.com/java/3.3/com/mongodb/client/model/Updates.html">https://api.mongodb.com/java/3.3/com/mongodb/client/model/Updates.html</a> for static methods to ease the writing of these updates.
     * @see <a href="https://docs.mongodb.com/manual/reference/method/db.collection.update/">https://docs.mongodb.com/manual/reference/method/db.collection.update/</a>
     */
    Observable<UpdateResult> update(Bson query, Bson update, boolean upsert);

    /**
     * Gets a query that identifies this document.
     * It would look like this: {'_id': {@link #getId()}} (the {@link #getId()} would contain the actual value of that method);
     *
     * @return a query that identifies this document
     */
    Document getIdQuery();

    static ExoDocument create(MongoCollection collection, Object id) {
        return new IExoDocument(collection, id);
    }
}
