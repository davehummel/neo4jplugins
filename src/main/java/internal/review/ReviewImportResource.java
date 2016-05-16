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
package internal.review;

import org.neo4j.graphdb.*;
import org.neo4j.string.UTF8;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;


@Path( "/import" )
public class ReviewImportResource
{
    private static final long BATCH_SIZE = 500000;
    private final GraphDatabaseService db;

    static final RelationshipType SELLS = RelationshipType.withName("SELLS");
    static final RelationshipType OWNS = RelationshipType.withName("OWNS");
    static final RelationshipType APPLIES = RelationshipType.withName("APPLIES");

    static final Label MERCHANT = Label.label("Merchant");
    static final Label ACCOUNT = Label.label("Account");

    static final Label SUBJECT = Label.label("Subject");
    static final Label PRODUCT = Label.label("Product");
    static final Label UGC = Label.label("UGC");
    static final Label REVIEW = Label.label("Review");

    Map<Long,Node> subjectLRU = lruCache(100);

    public ReviewImportResource(@Context GraphDatabaseService database )
    {
        this.db = database;
    }

    @GET
    @Produces( MediaType.TEXT_PLAIN )
    @Path( "/load" )
    public Response load( @HeaderParam( "path" ) String path, @HeaderParam("skip") long skip, @HeaderParam("max") long max)
    {
        System.out.println("Review Load! path:"+path+" skip:"+skip+" max:"+max);

        Summary summary = new Summary();

        if (max == 0 )
            max = Long.MAX_VALUE;


        try ( BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {
            for (int i = 0; i < skip; i++) {
                if (reader.readLine() == null) {
                    summary.eof = true;
                    break;
                }
            }
            long bufferSize = 0;
            Transaction tx = db.beginTx();
            try {
                for (int i = 0; i < max; i++) {
                    if (bufferSize >= BATCH_SIZE) {
                        tx.success();
                        tx.close();
                        tx = db.beginTx();
                        summary.batches++;
                        bufferSize = 0;
                    }
                    String line = reader.readLine();
                    if (line == null) {
                        summary.eof = true;
                        break;
                    }

                    if (!processLine(line.toString(), summary, subjectLRU )){
                        StringBuilder extendedLine = new StringBuilder(line);
                        boolean success = false;
                        for (int retry = 0; retry<20; retry++) {
                            summary.failed++;
                            if (i == max){
                                break;
                            }
                            line = reader.readLine();
                            i++;
                            if (line == null) {
                                summary.eof = true;
                                break;
                            }
                            if (processLine(line, summary, subjectLRU)){
                                summary.processed++;
                                bufferSize++;
                                break;
                            }
                            extendedLine.append(line);
                            if (processLine(extendedLine.toString(), summary, subjectLRU)){
                                summary.processed++;
                                bufferSize++;
                                summary.failed-=retry+1;
                                summary.multi_lines++;
                                // System.out.println("Combined lines:"+(retry+1));
                                success = true;
                                break;
                            }
                        }
                        if (!success) {
                            //System.out.println("Failed on:"+extendedLine.toString());
                        }
                    }else{
                        summary.processed++;
                        bufferSize++;
                    }

                    if (i % 10000 == 0) {

                        System.out.println(summary);
                    }
                }
                tx.success();
            }catch(Exception ex){
                summary.batches++;
                tx.success();
                tx.close();
                ex.printStackTrace();
                System.out.println("Failed:"+summary);
                return Response.status( Status.INTERNAL_SERVER_ERROR ).entity( UTF8.encode( "{\"error\":\""+ex.getMessage()+"\"}" ) ).build();
            }finally{
                summary.batches++;
                tx.success();
                tx.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed:"+summary);
            return Response.status( Status.INTERNAL_SERVER_ERROR ).entity( UTF8.encode( "{\"error\":\""+e.getMessage()+"\"}" ) ).build();
        }

        System.out.println("Done:"+summary);

        return Response.status( Status.OK ).entity( UTF8.encode("{\""+path+"\":\"OK\" , \"processed\":"+summary.processed+", \"failed\":"+summary.failed+", \"eof\":"+summary.eof+" }" ) ).build();


    }

    private boolean processLine(String s, Summary summary, Map<Long, Node> subjectLRU) {
        try{
            ReviewFeedLine line = new ReviewFeedLine(s);
            // create / find account
            Node productNode = subjectLRU.get(line.productID);
            if (productNode == null) {
                productNode = db.findNode(PRODUCT,"prod_id",line.productID);
                if (productNode == null) {
                    Node merchantNode = db.findNode(SUBJECT,"pr_id",Long.toString(line.merchantID));
                    if (merchantNode == null){
                        summary.no_merchant++;
                        Node accountNode = db.findNode(ACCOUNT,"id",line.accountID);
                        if (accountNode == null){
                            accountNode = db.createNode(ACCOUNT);
                            accountNode.setProperty("id", line.accountID);
                            summary.creates++;
                        }
                        merchantNode = db.createNode(MERCHANT, SUBJECT);
                        merchantNode.setProperty("pr_id", Long.toString(line.merchantID));
                        merchantNode.setProperty("grp_id",line.merchantGroupID);
                        accountNode.createRelationshipTo(merchantNode, OWNS);
                        summary.creates++;
                    }
                    productNode = db.createNode(PRODUCT,SUBJECT);
                    productNode.setProperty("pr_id",Long.toString(line.merchantID)+":"+line.productID+"::en_us");
                    productNode.setProperty("page_id",line.productID);
                    productNode.setProperty("model_id",line.productID);
                    productNode.setProperty("prod_id",line.productID);
                    merchantNode.createRelationshipTo(productNode,SELLS);
                    summary.creates++;
                    summary.new_products++;
                }else{
                    summary.graph_hits++;
                }
                subjectLRU.put(line.productID,productNode);
            }else{
                summary.lru_hits++;
            }

            Node reviewNode = db.createNode(UGC,REVIEW);
            reviewNode.setProperty("id",line.reviewID);
            reviewNode.setProperty("src",line.source);
            reviewNode.setProperty("r_type",line.reviewerType);
            reviewNode.setProperty("date",line.date);

            reviewNode.setProperty("des_"+line.merchantID,line.l1Reason.isEmpty()?"OK":line.l1Reason);

            reviewNode.createRelationshipTo(productNode,APPLIES);

        }catch (Throwable n){
            return false;
        }
        return true;
    }

    static public class Summary {
        public long processed;
        public long failed;
        public long creates;
        public long batches;
        public long lru_hits;
        public long graph_hits;
        public long new_products;
        public long no_merchant;
        public long multi_lines;
        public boolean eof=false;

        @Override
        public String toString() {
            return "Summary{" +
                    "processed=" + processed +
                    ", failed=" + failed +
                    ", creates=" + creates +
                    ", batches=" + batches +
                    ", lru_hits=" + lru_hits +
                    ", graph_hits=" + graph_hits +
                    ", new_products=" + new_products +
                    ", no_merchant=" + no_merchant +
                    ", multi_lines=" + multi_lines +
                    ", eof=" + eof +
                    '}';
        }
    }

    private <K,V> Map<K,V> lruCache(final int maxSize) {
        return new LinkedHashMap<K, V>(maxSize * 4 / 3, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        };
    }
}
