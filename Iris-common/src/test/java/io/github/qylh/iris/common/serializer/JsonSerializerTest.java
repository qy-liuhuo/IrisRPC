/*
 *    Licensed to the Apache Software Foundation (ASF) under one
 *    or more contributor license agreements.  See the NOTICE file
 *    distributed with this work for additional information
 *    regarding copyright ownership.  The ASF licenses this file
 *    to you under the Apache License, Version 2.0 (the
 *    "License"); you may not use this file except in compliance
 *    with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 */
package io.github.qylh.iris.common.serializer;

import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class JsonSerializerTest {
    
    @Data
    static class Person {
        
        private String name;
        private int age;
        private Date birthday;
        private String[] friends;
    }
    
    @Test
    public void testDeserialize() {
        Person person = JsonSerializer.deserialize("{\"name\":\"Tom\",\"age\":18,\"birthday\":\"2021-07-01 00:00:00\",\"friends\":[\"Alice\",\"Bob\"]}", Person.class);
        Assert.assertEquals("Tom", person.getName());
        Assert.assertEquals(18, person.getAge());
        Assert.assertEquals("Alice", person.getFriends()[0]);
    }
    
    @Test
    public void testDeserializeArray() {
        List<String> friends = JsonSerializer.deserializeArray("[\"Alice\",\"Bob\"]", String.class);
        Assert.assertEquals(2, friends.size());
    }
    
    @Test
    public void testSerialize() {
        Person testPerson = new Person();
        testPerson.setName("Tom");
        testPerson.setAge(18);
        testPerson.setBirthday(new Date());
        testPerson.setFriends(new String[]{"Alice", "Bob"});
        String json = JsonSerializer.serialize(testPerson);
        Assert.assertEquals("{\"name\":\"Tom\",\"age\":18,\"birthday\":\"" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(testPerson.getBirthday()) + "\",\"friends\":[\"Alice\",\"Bob\"]}",
                json);
        
    }
}