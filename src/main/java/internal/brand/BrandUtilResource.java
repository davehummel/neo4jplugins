/*
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package internal.brand;

import common.CommonProperties;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.MapUtil;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.Map;


@Path( "/util" )
public class BrandUtilResource
{

    GraphDatabaseService db;

    public BrandUtilResource(@Context GraphDatabaseService database )
    {
        this.db = database;
    }

    @GET
    @Produces( MediaType.TEXT_PLAIN )
    @Path( "/export" )
    public Response export(final @HeaderParam("skip") long skip,final @HeaderParam("limit") long limit) {
        System.out.println("Brand Export! skip:" + skip + " max:" + limit);
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                PrintWriter writer = new PrintWriter(os);
                Result res = db.execute("match (b:Brand) RETURN b.name ORDER BY b.name SKIP {skip} LIMIT {limit}", MapUtil.map("limit", limit, "skip", skip,"name",CommonProperties.BRAND_NAME));
                while (res.hasNext()){
                    Map<String, Object> item = res.next();
                    writer.write((String)item.get("b.name"));
                    writer.write('\n');
                }
                writer.flush();
                writer.close();
            }
        };
        return Response.ok().entity( stream ).type( MediaType.TEXT_PLAIN ).build();
    }
}
