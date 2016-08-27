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

package com.exorath.exodata;

import com.exorath.exodata.api.ExoDocument;
import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static org.junit.Assert.*;

/**
 * TODO: Test dotted keys
 * Created by toonsev on 8/22/2016.
 */
public class ExoDocumentTest {
    private static final String DB_NAME = "testdb";
    private static final String COLL_NAME = "testcoll";
    private Fongo fongo;
    private MongoClient client;
    private UUID id;
    private MongoCollection collection;
    private ExoDocument document;

    @Before
    public void setup() {
        fongo = new Fongo("mongo server 1");
        client = fongo.getMongo();
        collection = client.getDatabase(DB_NAME).getCollection(COLL_NAME);
        this.id = UUID.randomUUID();
        document = ExoDocument.create(collection, id.toString());
    }

    @Test(timeout = 1000)
    public void getCollectionNotNullTest() {
        assertNotNull(document.getCollection());
    }

    @Test(timeout = 1000)
    public void getCollectionNamespaceEqualsCollectionNamespaceTest() {
        assertEquals(collection.getNamespace(), document.getCollection().getNamespace());
    }

    @Test(timeout = 1000)
    public void fetchObservableNotNullTest() {
        assertNotNull(document.fetch());
    }

    @Test(timeout = 1000)
    public void fetchNotNullTest() {
        assertNotNull(document.fetch().toBlocking().first());
    }

    @Test(timeout = 1000)
    public void fetchCompletesTest() {
        AtomicBoolean called = new AtomicBoolean(false);
        document.fetch().toBlocking().subscribe(d -> {
        }, e -> {
        }, () -> called.set(true));
        assertTrue(called.get());
    }

    @Test(timeout = 1000)
    public void fetchEqualsDocumentTest() {
        Document doc = new Document("_id", id.toString()).append("testkey", "testvalue").append("testnested", new Document("value1", "impl").append("value2", "def"));
        collection.insertOne(doc);
        assertEquals(doc, document.fetch().toBlocking().first());
    }

    @Test(timeout = 1000)
    public void fetchDoesNotEqualsDocumentTest() {
        Document doc = new Document("_id", id.toString()).append("testkey", "testvalue").append("testnested", new Document("value1", "impl").append("value2", "def"));
        assertNotEquals(doc, document.fetch().toBlocking().first());
    }

    @Test(timeout = 1000)
    public void fetchWithProjectionObservableNotNullTest() {
       assertNotNull(document.fetch());
    }

    @Test(timeout = 1000)
    public void fetchWithProjectionNotNullTest() {
        assertNotNull(document.fetch().toBlocking().first());
    }

    @Test(timeout = 1000)
    public void fetchWithProjectionIncludesFieldContainsTest() {
        document.set("key1", "value1").toBlocking().subscribe();
        assertTrue(document.fetch(include("key1")).toBlocking().first().containsKey("key1"));
    }

    @Test(timeout = 1000)
    public void fetchWithProjectionExcludesFieldDoesNotContainsTest() {
        document.set("key1", "value1").toBlocking().subscribe();
        assertFalse(document.fetch(exclude("key1")).toBlocking().first().containsKey("key1"));
    }
    @Test(timeout = 1000)
    public void fetchWithProjectionIncludeFieldDoesNotContainsTest() {
        document.set("key1", "value1").toBlocking().subscribe();
        document.set("key2", "value2").toBlocking().subscribe();
        assertFalse(document.fetch(include("key1")).toBlocking().first().containsKey("key2"));
    }

    @Test(timeout = 1000)
    public void getCachedOrFetchObservableNotNullTest() {
        assertNotNull(document.getCachedOrFetch());
    }

    @Test(timeout = 1000)
    public void getCachedOrFetchNotNullTest() {
        assertNotNull(document.getCachedOrFetch().toBlocking().first());
    }

    @Test(timeout = 1000)
    public void getCachedOrFetchReturnsFetchTest() {
        Document doc = new Document("_id", id.toString()).append("testkey", "testvalue").append("testnested", new Document("value1", "impl").append("value2", "def"));
        collection.insertOne(doc);
        assertTrue(document.getCachedOrFetch().toBlocking().first().equals(doc));
    }

    @Test(timeout = 1000)
    public void getCachedOrFetchReturnsCacheTest() {
        Bson beforeUpdate = document.fetch().toBlocking().first();
        collection.updateOne(new Document("_id", id.toString()), new Document("$set", new Document("testkey", "testvalue")));
        assertTrue(document.getCachedOrFetch().toBlocking().first().equals(beforeUpdate));
    }

    @Test(timeout = 1000)
    public void setObservableNotNullTest() {
        assertNotNull(document.set("testkey", "testvalue"));
    }

    @Test(timeout = 1000)
    public void setNotNullTest() {
        assertNotNull(document.set("testkey", "testvalue").toBlocking().first());
    }

    @Test(timeout = 1000)
    public void setCompletesTest() {
        AtomicBoolean called = new AtomicBoolean(false);
        document.set("testkey", "testvalue").toBlocking().subscribe(d -> {
        }, e -> {
        }, () -> called.set(true));
        assertTrue(called.get());
    }

    @Test(timeout = 1000)
    public void setReturnsUpsertedUpdateResultIfUpsertedTest() {
        assertNotNull(document.set("testkey", "testvalue").toBlocking().first().getUpsertedId());
    }

    @Test(timeout = 1000)
    public void setReturnsModifiedUpdateResultIfDocumentExistsTest() {
        document.fetch().toBlocking().first();
        assertEquals(1, document.set("testkey", "testvalue").toBlocking().first().getModifiedCount());
    }

    @Test(timeout = 1000)
    public void setUpdatesDocumentFetchTest() {
        document.set("testkey", "testvalue").toBlocking().subscribe();
        assertEquals("testvalue", document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void setUpdatesDatabaseTest() {
        document.set("testkey", "testvalue").toBlocking().subscribe();
        assertEquals("testvalue", ((Document) collection.find(new Document("_id", id.toString())).first()).get("testkey"));
    }

    @Test(timeout = 1000)
    public void setDoesNotRemoveExistingFieldsTest() {
        collection.insertOne(new Document("_id", id.toString()).append("persist", "yup"));
        document.set("testkey", "testvalue").toBlocking().subscribe();
        assertTrue(document.fetch().toBlocking().first().containsKey("persist"));
    }

    @Test(timeout = 1000)
    public void incObservableNotNullTest() {
        assertNotNull(document.inc("testkey", 123));
    }

    @Test(timeout = 1000)
    public void incIntNotNullTest() {
        assertNotNull(document.inc("testkey", 123).toBlocking().first());
    }

    @Test(timeout = 1000)
    public void incDoubleNotNullTest() {
        assertNotNull(document.inc("testkey", 123.0d).toBlocking().first());
    }

    @Test(timeout = 1000)
    public void incLongNotNullTest() {
        assertNotNull(document.inc("testkey", Long.MAX_VALUE).toBlocking().first());
    }

    @Test(timeout = 1000)
    public void incReturnsUpsertedResultSetIfUpsertedTest() {
        assertNotNull(document.inc("testkey", 123).toBlocking().first().getUpsertedId());
    }

    @Test(timeout = 1000)
    public void incReturnsModifiedResultSetIfDocumentExistsTest() {
        document.fetch().toBlocking().first();
        assertEquals(1, document.inc("testkey", 123).toBlocking().first().getModifiedCount());
    }

    @Test(timeout = 1000)
    public void incUpdatesDocumentFetchToValueTest() {
        document.inc("testkey", 10).toBlocking().subscribe();
        assertEquals(10, document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void twoIncsUpdatesDocumentFetchToTotalTest() {
        document.inc("testkey", 10).toBlocking().subscribe();
        document.inc("testkey", 10).toBlocking().subscribe();
        assertEquals(20, document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void negativeIncDeductsTest() {
        document.inc("testkey", -10).toBlocking().subscribe();
        assertEquals(-10, document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void incUpdatesDatabaseTest() {
        document.inc("testkey", 10).toBlocking().subscribe();
        assertEquals(10, ((Document) collection.find(new Document("_id", id.toString())).first()).get("testkey"));
    }

    @Test(timeout = 1000)
    public void incDoesNotRemoveExistingFieldsTest() {
        collection.insertOne(new Document("_id", id.toString()).append("persist", "yup"));
        document.inc("testkey", 10).toBlocking().subscribe();
        assertTrue(document.fetch().toBlocking().first().containsKey("persist"));
    }

    @Test(timeout = 1000)
    public void decIfSufficientIntObservableNotNullTest() {
        assertNotNull(document.decIfSufficient("testkey", 123));
    }

    @Test(timeout = 1000)
    public void decIfSufficientIntNotNullTest() {
        assertNotNull(document.decIfSufficient("testkey", 123).toBlocking().first());
    }

    @Test(timeout = 1000)
    public void decIfSufficientIntDoesNotReturnUpsertedResultSetIfUpsertedTest() {
        assertNull(document.decIfSufficient("testkey", 123).toBlocking().first().getUpsertedId());
    }

    @Test(timeout = 1000)
    public void decIfSufficientIntReturnsZeroModifiedResultSetIfDocumentExistsTestAndNotEnough() {
        document.fetch().toBlocking().first();
        assertEquals(0, document.decIfSufficient("testkey", 123).toBlocking().first().getModifiedCount());
    }

    @Test(timeout = 1000)
    public void decIfSufficientIntReturnsOneModifiedResultSetIfDocumentExistsTestAndJustEnough() {
        document.inc("testkey", 123).toBlocking().subscribe();
        assertEquals(1, document.decIfSufficient("testkey", 123).toBlocking().first().getModifiedCount());
    }

    @Test(timeout = 1000)
    public void decIfSufficientIntReturnsOneModifiedResultSetIfDocumentExistsTestAndEnough() {
        document.inc("testkey", 1000).toBlocking().subscribe();
        assertEquals(1, document.decIfSufficient("testkey", 123).toBlocking().first().getModifiedCount());
    }

    @Test(timeout = 1000)
    public void decIfSufficientIntUpdatesDocumentFetchToValueTest() {
        document.inc("testkey", 30).toBlocking().subscribe();
        document.decIfSufficient("testkey", 10).toBlocking().subscribe();
        assertEquals(20, document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void twoDecsIfSufficientIntUpdatesDocumentFetchToTotalTest() {
        document.inc("testkey", 50).toBlocking().subscribe();
        document.decIfSufficient("testkey", 10).toBlocking().subscribe();
        document.decIfSufficient("testkey", 10).toBlocking().subscribe();
        assertEquals(30, document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void negativeDecIfSufficientIntIncrementsTest() {
        document.inc("testkey", 50).toBlocking().subscribe();
        document.decIfSufficient("testkey", -10).toBlocking().subscribe();
        assertEquals(60, document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void negativeDecIfSufficientIntDoesNotIncrementIfInsufficientTest() {
        document.decIfSufficient("testkey", -10).toBlocking().subscribe();
        assertNull(document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void decIfSufficientIntUpdatesDatabaseTest() {
        document.inc("testkey", 50).toBlocking().subscribe();
        document.decIfSufficient("testkey", 10).toBlocking().subscribe();
        assertEquals(40, ((Document) collection.find(new Document("_id", id.toString())).first()).get("testkey"));
    }

    @Test(timeout = 1000)
    public void decIfSufficientIntDoesNotRemoveExistingFieldsTest() {
        collection.insertOne(new Document("_id", id.toString()).append("persist", "yup"));
        document.inc("testkey", 50).toBlocking().subscribe();
        document.decIfSufficient("testkey", 10).toBlocking().subscribe();
        assertTrue(document.fetch().toBlocking().first().containsKey("persist"));
    }

    @Test(timeout = 1000)
    public void decIfSufficientDoubleObservableNotNullTest() {
        assertNotNull(document.decIfSufficient("testkey", 123.4d));
    }

    @Test(timeout = 1000)
    public void decIfSufficientDoubleNotNullTest() {
        assertNotNull(document.decIfSufficient("testkey", 123.4d).toBlocking().first());
    }

    @Test(timeout = 1000)
    public void decIfSufficientDoubleDoesNotReturnUpsertedResultSetIfUpsertedTest() {
        assertNull(document.decIfSufficient("testkey", 123.4d).toBlocking().first().getUpsertedId());
    }

    @Test(timeout = 1000)
    public void decIfSufficientDoubleReturnsZeroModifiedResultSetIfDocumentExistsTestAndNotEnough() {
        document.fetch().toBlocking().first();
        assertEquals(0, document.decIfSufficient("testkey", 123.4d).toBlocking().first().getModifiedCount());
    }

    @Test(timeout = 1000)
    public void decIfSufficientDoubleReturnsOneModifiedResultSetIfDocumentExistsTestAndJustEnough() {
        document.inc("testkey", 123.4d).toBlocking().subscribe();
        assertEquals(1, document.decIfSufficient("testkey", 123.4d).toBlocking().first().getModifiedCount());
    }

    @Test(timeout = 1000)
    public void decIfSufficientDoubleReturnsOneModifiedResultSetIfDocumentExistsTestAndEnough() {
        document.inc("testkey", 1000.4d).toBlocking().subscribe();
        assertEquals(1, document.decIfSufficient("testkey", 123.4d).toBlocking().first().getModifiedCount());
    }

    @Test(timeout = 1000)
    public void decIfSufficientDoubleUpdatesDocumentFetchToValueTest() {
        document.inc("testkey", 23.4d).toBlocking().subscribe();
        document.decIfSufficient("testkey", 23.2d).toBlocking().subscribe();
        assertEquals(0.2d, document.fetch().toBlocking().first().getDouble("testkey"), 0.01d);
    }

    @Test(timeout = 1000)
    public void twoDecsIfSufficientDoubleUpdatesDocumentFetchToTotalTest() {
        document.inc("testkey", 20.4d).toBlocking().subscribe();
        document.decIfSufficient("testkey", 10.2d).toBlocking().subscribe();
        document.decIfSufficient("testkey", 5.1d).toBlocking().subscribe();
        assertEquals(5.1d, document.fetch().toBlocking().first().getDouble("testkey"), 0.01d);
    }

    @Test(timeout = 1000)
    public void negativeDecIfSufficientDoubleIncrementsTest() {
        document.inc("testkey", 15.2d).toBlocking().subscribe();
        document.decIfSufficient("testkey", -10.1d).toBlocking().subscribe();
        assertEquals(25.3d, document.fetch().toBlocking().first().getDouble("testkey"), 0.01d);
    }

    @Test(timeout = 1000)
    public void negativeDecIfSufficientDoubleDoesNotIncrementIfInsufficientTest() {
        document.decIfSufficient("testkey", -5.3d).toBlocking().subscribe();
        assertNull(document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void decIfSufficientDoubleUpdatesDatabaseTest() {
        document.inc("testkey", 15.2d).toBlocking().subscribe();
        document.decIfSufficient("testkey", 10.1d).toBlocking().subscribe();
        assertEquals(5.1d, ((Document) collection.find(new Document("_id", id.toString())).first()).getDouble("testkey"), 0.01d);
    }

    @Test(timeout = 1000)
    public void decIfSufficientDoubleDoesNotRemoveExistingFieldsTest() {
        collection.insertOne(new Document("_id", id.toString()).append("persist", "yup"));
        document.inc("testkey", 12.4d).toBlocking().subscribe();
        document.decIfSufficient("testkey", 7.2d).toBlocking().subscribe();
        assertTrue(document.fetch().toBlocking().first().containsKey("persist"));
    }

    //Long
    @Test(timeout = 1000)
    public void decIfSufficientLongObservableNotNullTest() {
        assertNotNull(document.decIfSufficient("testkey", 123l));
    }

    @Test(timeout = 1000)
    public void decIfSufficientLongNotNullTest() {
        assertNotNull(document.decIfSufficient("testkey", Long.MAX_VALUE).toBlocking().first());
    }

    @Test(timeout = 1000)
    public void decIfSufficientLongDoesNotReturnUpsertedResultSetIfUpsertedTest() {
        assertNull(document.decIfSufficient("testkey", 123l).toBlocking().first().getUpsertedId());
    }

    @Test(timeout = 1000)
    public void decIfSufficientLongReturnsZeroModifiedResultSetIfDocumentExistsTestAndNotEnough() {
        document.fetch().toBlocking().first();
        assertEquals(0, document.decIfSufficient("testkey", 123l).toBlocking().first().getModifiedCount());
    }

    @Test(timeout = 1000)
    public void decIfSufficientLongReturnsOneModifiedResultSetIfDocumentExistsTestAndJustEnough() {
        document.inc("testkey", 123l).toBlocking().subscribe();
        assertEquals(1, document.decIfSufficient("testkey", 123l).toBlocking().first().getModifiedCount());
    }

    @Test(timeout = 1000)
    public void decIfSufficientLongReturnsOneModifiedResultSetIfDocumentExistsTestAndEnough() {
        document.inc("testkey", 1000l).toBlocking().subscribe();
        assertEquals(1, document.decIfSufficient("testkey", 123l).toBlocking().first().getModifiedCount());
    }

    @Test(timeout = 1000)
    public void decIfSufficientLongReturnsOneModifiedResultSetIfDocumentExistsTestAndHasMaxValueAndDecrementsMaxValueMinusOneTest() {
        document.inc("testkey", Long.MAX_VALUE).toBlocking().subscribe();
        assertEquals(1, document.decIfSufficient("testkey", Long.MAX_VALUE - 1).toBlocking().first().getModifiedCount());
    }

    @Test(timeout = 1000)
    public void decIfSufficientLongUpdatesDocumentFetchToValueTest() {
        document.inc("testkey", 30l).toBlocking().subscribe();
        document.decIfSufficient("testkey", 10l).toBlocking().subscribe();
        assertEquals(20l, document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void twoDecsIfSufficientLongUpdatesDocumentFetchToTotalTest() {
        document.inc("testkey", 50l).toBlocking().subscribe();
        document.decIfSufficient("testkey", 10l).toBlocking().subscribe();
        document.decIfSufficient("testkey", 10l).toBlocking().subscribe();
        assertEquals(30l, document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void negativeDecIfSufficientLongIncrementsTest() {
        document.inc("testkey", 50l).toBlocking().subscribe();
        document.decIfSufficient("testkey", -10l).toBlocking().subscribe();
        assertEquals(60l, document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void negativeDecIfSufficientLongDoesNotIncrementIfInsufficientTest() {
        document.decIfSufficient("testkey", -10l).toBlocking().subscribe();
        assertNull(document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void decIfSufficientLongUpdatesDatabaseTest() {
        document.inc("testkey", 50l).toBlocking().subscribe();
        document.decIfSufficient("testkey", 10l).toBlocking().subscribe();
        assertEquals(40l, ((Document) collection.find(new Document("_id", id.toString())).first()).get("testkey"));
    }

    @Test(timeout = 1000)
    public void decIfSufficientLongDoesNotRemoveExistingFieldsTest() {
        collection.insertOne(new Document("_id", id.toString()).append("persist", "yup"));
        document.inc("testkey", 50l).toBlocking().subscribe();
        document.decIfSufficient("testkey", 10l).toBlocking().subscribe();
        assertTrue(document.fetch().toBlocking().first().containsKey("persist"));
    }

    @Test(timeout = 1000)
    public void pushObservableNotNullTest() {
        assertNotNull(document.push("testkey", "testvalue"));
    }

    @Test(timeout = 1000)
    public void pushNotNullTest() {
        assertNotNull(document.push("testkey", "testvalue").toBlocking().first());
    }

    @Test(timeout = 1000)
    public void pushCompletesTest() {
        AtomicBoolean called = new AtomicBoolean(false);
        document.push("testkey", "testvalue").toBlocking().subscribe(d -> {
        }, e -> {
        }, () -> called.set(true));
        assertTrue(called.get());
    }

    @Test(timeout = 1000)
    public void pushReturnsUpsertedUpdateResultIfUpsertedTest() {
        assertNotNull(document.push("testkey", "testvalue").toBlocking().first().getUpsertedId());
    }

    @Test(timeout = 1000)
    public void pushReturnsModifiedUpdateResultIfDocumentExistsTest() {
        document.fetch().toBlocking().first();
        assertEquals(1, document.push("testkey", "testvalue").toBlocking().first().getModifiedCount());
    }

    @Test(timeout = 1000)
    public void pushUpdatesDocumentFetchTest() {
        document.push("testkey", "testvalue").toBlocking().subscribe();
        List<String> values = new ArrayList<>();
        values.add("testvalue");
        assertEquals(values, document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void twoPushesAppendsToDocumentFetchTest() {
        document.push("testkey", "testvalue").toBlocking().subscribe();
        document.push("testkey", "testvalue2").toBlocking().subscribe();
        List<String> values = new ArrayList<>();
        values.add("testvalue");
        values.add("testvalue2");
        assertEquals(values, document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void pushUpdatesDatabaseTest() {
        document.push("testkey", "testvalue").toBlocking().subscribe();
        List<String> values = new ArrayList<>();
        values.add("testvalue");
        assertEquals(values, ((Document) collection.find(new Document("_id", id.toString())).first()).get("testkey"));
    }

    @Test(timeout = 1000)
    public void pushDoesNotRemoveExistingFieldsTest() {
        collection.insertOne(new Document("_id", id.toString()).append("persist", "yup"));
        document.push("testkey", "testvalue").toBlocking().subscribe();
        assertTrue(document.fetch().toBlocking().first().containsKey("persist"));
    }

    @Test(timeout = 1000)
    public void popObservableNotNullTest() {
        assertNotNull(document.pop("testkey", true));
    }

    @Test(timeout = 1000)
    public void popNotNullTest() {
        assertNotNull(document.pop("testkey", true).toBlocking().first());
    }

    @Test(timeout = 1000)
    public void popCompletesTest() {
        AtomicBoolean called = new AtomicBoolean(false);
        document.pop("testkey", true).toBlocking().subscribe(d -> {
        }, e -> {
        }, () -> called.set(true));
        assertTrue(called.get());
    }

    @Test(timeout = 1000)
    public void popDoesNotUpsertTest() {
        assertNull(document.pop("testkey", true).toBlocking().first().getUpsertedId());
    }

    //TODO: Check if Fongo is no longer bugged in this section
    /*@Test(timeout = 1000)
    public void popReturnsNoModifiedUpdateResultIfDocumentExistsButNothingPoppedTest() {
        document.fetch().toBlocking().subscribe();
        assertEquals(0, document.pop("testkey", true).toBlocking().first().getModifiedCount());
    }*/
    @Test(timeout = 1000)
    public void popReturnsModifiedUpdateResultIfDocumentExistsAndPoppedTest() {
        document.push("testkey", "test").toBlocking().subscribe();
        assertEquals(1, document.pop("testkey", true).toBlocking().first().getModifiedCount());
    }

    @Test(timeout = 1000)
    public void popFirstUpdatesDocumentFetchTest() {
        document.push("testkey", "testvalue1").toBlocking().subscribe();
        document.push("testkey", "testvalue2").toBlocking().subscribe();
        document.pop("testkey", true).toBlocking().subscribe();
        List<String> values = new ArrayList<>();
        values.add("testvalue2");
        assertEquals(values, document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void popLastUpdatesDocumentFetchTest() {
        document.push("testkey", "testvalue1").toBlocking().subscribe();
        document.push("testkey", "testvalue2").toBlocking().subscribe();
        document.pop("testkey", false).toBlocking().subscribe();
        List<String> values = new ArrayList<>();
        values.add("testvalue1");
        assertEquals(values, document.fetch().toBlocking().first().get("testkey"));
    }

    @Test(timeout = 1000)
    public void popUpdatesDatabaseTest() {
        document.push("testkey", "testvalue1").toBlocking().subscribe();
        document.push("testkey", "testvalue2").toBlocking().subscribe();
        document.pop("testkey", true).toBlocking().subscribe();
        List<String> values = new ArrayList<>();
        values.add("testvalue2");
        assertEquals(values, ((Document) collection.find(new Document("_id", id.toString())).first()).get("testkey"));
    }

    @Test(timeout = 1000)
    public void popDoesNotRemoveExistingFieldsTest() {
        collection.insertOne(new Document("_id", id.toString()).append("persist", "yup"));
        document.push("testkey", "testvalue").toBlocking().subscribe();
        document.pop("testkey", true).toBlocking().subscribe();
        assertTrue(document.fetch().toBlocking().first().containsKey("persist"));
    }

    //remove
    @Test(timeout = 1000)
    public void removeObservableNotNullTest() {
        assertNotNull(document.remove("testkey"));
    }

    @Test(timeout = 1000)
    public void removeNotNullIfExistingTest() {
        document.set("testkey", "testvalue");
        assertNotNull(document.remove("testkey").toBlocking().first());
    }

    @Test(timeout = 1000)
    public void removeNotNullIfNonExistingTest() {
        assertNotNull(document.remove("testkey").toBlocking().first());
    }

    @Test(timeout = 1000)
    public void removeCompletesIfExistingTest() {
        AtomicBoolean called = new AtomicBoolean(false);
        document.set("testkey", "testvalue");
        document.remove("testkey").toBlocking().subscribe(d -> {
        }, e -> {
        }, () -> called.set(true));
        assertTrue(called.get());
    }

    @Test(timeout = 1000)
    public void removeCompletesIfNonExistingTest() {
        AtomicBoolean called = new AtomicBoolean(false);
        document.remove("testkey").toBlocking().subscribe(d -> {
        }, e -> {
        }, () -> called.set(true));
        assertTrue(called.get());
    }

    @Test(timeout = 1000)
    public void removeDoesNotUpsertTest() {
        assertNull(document.remove("testkey").toBlocking().first().getUpsertedId());
    }

    //TODO: Check if Fongo fixed this test
    /*  @Test(timeout = 1000)
    public void removeReturnsNoModifiedUpdateResultIfDocumentExistsButNothingremovepedTest() {
        document.fetch().toBlocking().subscribe();
        assertEquals(0, document.remove("testkey").toBlocking().first().getModifiedCount());
    }*/
    @Test(timeout = 1000)
    public void removeReturnsModifiedUpdateResultIfDocumentExistsAndRemovedTest() {
        document.set("testkey", "test").toBlocking().subscribe();
        assertEquals(1, document.remove("testkey").toBlocking().first().getModifiedCount());
    }

    @Test(timeout = 1000)
    public void removeUpdatesDocumentFetchTest() {
        document.set("testkey", "testvalue1").toBlocking().subscribe();
        document.remove("testkey").toBlocking().subscribe();
        assertFalse(document.fetch().toBlocking().first().containsKey("testkey"));
    }

    @Test(timeout = 1000)
    public void removeUpdatesDatabaseTest() {
        document.set("testkey", "testvalue1").toBlocking().subscribe();
        document.remove("testkey").toBlocking().subscribe();
        assertFalse(((Document) collection.find(new Document("_id", id.toString())).first()).containsKey("testkey"));
    }

    @Test(timeout = 1000)
    public void removeDoesNotRemoveExistingFieldsTest() {
        collection.insertOne(new Document("_id", id.toString()).append("persist", "yup"));
        document.set("testkey", "testvalue").toBlocking().subscribe();
        document.remove("testkey").toBlocking().subscribe();
        assertTrue(document.fetch().toBlocking().first().containsKey("persist"));
    }
    @Test(timeout = 1000)
    public void removeRemovesArrayTest() {
        document.push("array", "value1").toBlocking().subscribe();
        document.push("array", "value2").toBlocking().subscribe();
        document.remove("array").toBlocking().subscribe();
        assertFalse(document.fetch().toBlocking().first().containsKey("array"));
    }


    //TODO: Update tests (Not really necessary as all above tests use update indirectly)
}
