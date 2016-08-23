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

import com.exorath.exodata.impl.api.ExoDatabase;
import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by toonsev on 8/22/2016.
 */
public class ExoDatabaseTest {
    private static final String DB_NAME = "testdb";
    private Fongo fongo;
    private MongoClient client;
    private ExoDatabase database;

    @Before
    public void setup() {
        fongo = new Fongo("mongo server 1");
        client = fongo.getMongo();
        database = ExoDatabase.create(client.getDatabase(DB_NAME));
    }
    @Test
    public void getCollectionNotNullTest(){
        assertNotNull(database.getCollection("testcoll"));
    }@Test
    public void getCollectionEmitsItemTest(){
        assertNotNull(database.getCollection("testcoll").toBlocking().first());
    }
    @Test
    public void databaseNotCreatedByDefaultTest(){
        assertEquals(0,client.getUsedDatabases().stream().filter(db -> db.getName().equals(DB_NAME)).count());
    }

    @Test
    public void getCollectionCreatesDatabaseTest(){
        database.getCollection("testcoll").toBlocking().first();
        assertEquals(1,client.getUsedDatabases().stream().filter(db -> db.getName().equals(DB_NAME)).count());
    }


    @Test
    public void getMongoDatabaseNameEqualsTheActualMongoDatabaseName(){
        assertEquals(client.getDatabase(DB_NAME).getName(), database.getMongoDatabase().getName());
    }
}
