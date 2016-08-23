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

package com.exorath.exodata.impl.api;

import com.exorath.exodata.impl.impl.IDataAPI;
import com.mongodb.MongoClient;
import rx.Observable;

/**
 * Created by toonsev on 8/22/2016.
 */
public interface DataAPI {
    Observable<ExoDatabase> getDatabase(String name);


    static DataAPI create() {
        return new IDataAPI();
    }

    static DataAPI create(String host) {
        return new IDataAPI(host);
    }

    static DataAPI create(String host, int port) {
        return new IDataAPI(host, port);
    }

    static DataAPI create(MongoClient client) {
        return new IDataAPI(client);
    }
}
