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

import com.exorath.exodata.impl.api.ExoCollection;
import com.exorath.exodata.impl.api.ExoDatabase;
import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by toonsev on 8/22/2016.
 */
public class ExoCollectionTest {
    private static final String DB_NAME = "testdb";
    private static final String COLL_NAME = "testcoll";
    private Fongo fongo;
    private MongoClient client;
    private ExoCollection collection;

    @Before
    public void setup() {
        fongo = new Fongo("mongo server 1");
        client = fongo.getMongo();
        collection = ExoCollection.create(client.getDatabase(DB_NAME).getCollection(COLL_NAME));
    }
    @Test
    public void getMongoCollectionNotNullTest(){
        assertNotNull(collection.getMongoCollection());
    }
    @Test
    public void getMongoCollectionNameEqualsCollectionNameTest(){
        assertEquals(COLL_NAME, collection.getMongoCollection().getNamespace().getCollectionName());
    }

    @Test
    public void createIndexListIndexesContainsKeyTest(){
        Document indexDoc = new Document("testkey", 1);
        collection.createIndex(indexDoc).toBlocking().subscribe();
        boolean called = false;
        for (Object index : collection.getMongoCollection().listIndexes())
            if (((Document) index).get("key", Document.class).equals(indexDoc))
                called = true;
        assertTrue(called);
    }

    @Test
    public void getDocumentNotNullTest(){
        assertNotNull(collection.getDocument("testdoc"));
    }

    @Test
    public void getDocumentCollectionEqualsGetMongoCollectionTest(){
        assertEquals(collection.getMongoCollection(), collection.getDocument("testdoc").getCollection());
    }
}
