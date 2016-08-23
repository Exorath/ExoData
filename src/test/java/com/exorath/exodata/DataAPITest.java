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

import com.exorath.exodata.impl.api.DataAPI;
import com.exorath.exodata.impl.api.ExoDatabase;
import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by toonsev on 8/13/2016.
 */
public class DataAPITest {
    private static final String DB_NAME = "testdb";
    private Fongo fongo;
    private MongoClient client;
    private DataAPI dataAPI;

    @Before
    public void setup(){
        fongo = new Fongo("mongo server 1");
        client = fongo.getMongo();
        dataAPI = DataAPI.create(client);
    }
    @Test
    public void getDatabaseNotNullTest(){
        assertNotNull(dataAPI.getDatabase(DB_NAME));
    }
    @Test
    public void getDatabaseEmitsItemTest(){
        assertNotNull(dataAPI.getDatabase(DB_NAME).toBlocking().first());
    }
    @Test
    public void getDatabaseCompletesTest(){
        AtomicBoolean called = new AtomicBoolean(false);
        dataAPI.getDatabase(DB_NAME).toBlocking().subscribe(db -> {}, error -> {}, () -> called.set(true));
        assertTrue(called.get());
    }

    @Test
    public void getDatabaseDoesNotCreateDatabase(){
        ExoDatabase database = dataAPI.getDatabase(DB_NAME).toBlocking().first();
        assertEquals(0,client.getUsedDatabases().stream().filter(db -> db.getName().equals(DB_NAME)).count());
    }
    @Test
    public void getDatabaseCreatesDatabaseAfterGetCollectionIsCalledOnDatabaseTest(){
        ExoDatabase database = dataAPI.getDatabase(DB_NAME).toBlocking().first();
        database.getCollection("abc").toBlocking().first();
        assertEquals(1,client.getUsedDatabases().stream().filter(db -> db.getName().equals("testdb")).count());
    }
}
