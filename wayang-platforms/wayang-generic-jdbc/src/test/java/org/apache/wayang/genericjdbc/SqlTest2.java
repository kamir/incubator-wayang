package org.apache.wayang.genericjdbc;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.apache.wayang.basic.data.Record;
import org.apache.wayang.basic.data.Tuple2;
import org.apache.wayang.basic.operators.JoinOperator;
import org.apache.wayang.basic.operators.LocalCallbackSink;
import org.apache.wayang.basic.operators.TableSource;
import org.apache.wayang.core.api.Configuration;
import org.apache.wayang.core.api.WayangContext;
import org.apache.wayang.core.function.FunctionDescriptor;
import org.apache.wayang.core.plan.wayangplan.WayangPlan;
import org.apache.wayang.core.util.ReflectionUtils;
import org.apache.wayang.genericjdbc.operators.GenericJdbcTableSource;
import org.apache.wayang.java.Java;

import java.util.ArrayList;
import java.util.Collection;

/**
* Joining 2 tables , person and orders using GenericJdbc Plugin.
 * Tables reside on multiple instances of mysql platform
 * Test to check the working of generic jdbc plugin for multiple instances of same DBMS */



public class SqlTest2 {


    public static void main(String[] args) {
        WayangPlan wayangPlan;
        Configuration configuration = new Configuration();
        configuration.setProperty("wayang.mysql1.jdbc.url", "jdbc:mysql://localhost:3315/db");
        configuration.setProperty("wayang.mysql1.jdbc.user", "root");
        configuration.setProperty("wayang.mysql1.jdbc.password", "password");
        // Give the driver name through configuration.
        configuration.setProperty("wayang.mysql1.jdbc.driverName", "com.mysql.cj.jdbc.Driver");

        configuration.setProperty("wayang.mysql2.jdbc.url", "jdbc:mysql://localhost:3306/db");
        configuration.setProperty("wayang.mysql2.jdbc.user", "mysql");
        configuration.setProperty("wayang.mysql2.jdbc.password", "password");
        // Give the driver name through configuration.
        configuration.setProperty("wayang.mysql2.jdbc.driverName", "com.mysql.cj.jdbc.Driver");

        WayangContext wayangContext = new WayangContext(configuration)
                .withPlugin(Java.basicPlugin())
                .withPlugin(GenericJdbc.plugin())
                ;

        Collection<Tuple2<Record,Record>> collector = new ArrayList<>();

        /*Give the name of the jdbc platform along with table name*/
        TableSource person = new GenericJdbcTableSource("mysql1","person");
        TableSource orders = new GenericJdbcTableSource("mysql2","orders");

        FunctionDescriptor.SerializableFunction<Record, Object> keyFunctionPerson = record -> record.getField(0);
        FunctionDescriptor.SerializableFunction<Record, Object> keyFunctionOrders = record -> record.getField(1);


        JoinOperator<Record, Record, Object> joinOperator = new JoinOperator<>(
                keyFunctionPerson,
                keyFunctionOrders,
                Record.class,
                Record.class,
                Object.class
        );

        LocalCallbackSink<Tuple2<Record, Record>> sink = LocalCallbackSink.createCollectingSink(collector, ReflectionUtils.specify(Tuple2.class));

        person.connectTo(0,joinOperator,0);
        orders.connectTo(0,joinOperator,1);
        joinOperator.connectTo(0,sink,0);

        wayangPlan = new WayangPlan(sink);

        wayangContext.execute("Multiple instances of same DBMS test", wayangPlan);


        int count = 10;
        for(Tuple2<Record,Record> r : collector) {
            System.out.println(r.getField0().getField(1).toString());
            if(--count == 0 ) {
                break;
            }
        }
        System.out.println("Done");

    }


}


