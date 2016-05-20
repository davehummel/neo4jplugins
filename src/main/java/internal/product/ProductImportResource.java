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
package internal.product;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import common.CommonLabels;
import common.CommonProperties;
import common.CommonRelationships;
import org.neo4j.graphdb.*;
import org.neo4j.string.UTF8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static common.CommonLabels.*;
import static common.CommonRelationships.CARRIES;


@Path( "/import" )
public class ProductImportResource
{
    private static final long BATCH_SIZE = 50000;
    private final GraphDatabaseService db;



    Map<Long,Node> accountLRU = lruCache(10);
    Map<Long,Node> merchantLRU = lruCache(10);
    Map<String,Node> brandLRU = lruCache(100);
    Map<String,Node> categoryLRU = lruCache(5000);


    public ProductImportResource( @Context GraphDatabaseService database )
    {
        this.db = database;
    }

    @GET
    @Produces( MediaType.TEXT_PLAIN )
    @Path( "/load" )
    public Response load( @HeaderParam( "path" ) String path, @HeaderParam("skip") long skip, @HeaderParam("max") long max)
    {
        System.out.println("Product Load! path:"+path+" skip:"+skip+" max:"+max);
        Summary summary = new Summary();

        if (max == 0 )
            max = Long.MAX_VALUE;

        try ( BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {
            for (int i = 0; i < skip; i++) {
                reader.readLine();
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

                    if (!processLine(line.toString(), summary, accountLRU, merchantLRU, brandLRU)){
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
                            if (processLine(line, summary, accountLRU, merchantLRU, brandLRU)){
                                summary.processed++;
                                bufferSize++;
                                break;
                            }
                            extendedLine.append(line);
                            if (processLine(extendedLine.toString(), summary, accountLRU, merchantLRU, brandLRU)){
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

            }catch(Exception ex){
                summary.batches++;
                tx.success();
                tx.close();
                ex.printStackTrace();
                System.out.println("Failed:"+summary);
                return Response.status( Status.INTERNAL_SERVER_ERROR ).entity( UTF8.encode( "{\"error\":\""+ex.getMessage()+"\"}" ) ).build();
            }finally{
                tx.success();
                summary.batches++;
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

    static public class Summary {
        public long processed;
        public long failed;
        public long lru_hits;
        public long creates;
        public long batches;
        public long graph_hits;
        public long merch_brand_creates;
        public long merch_brand_reuse;
        public long cat_reuse;
        public long cat_creates;
        public long multi_lines;
        public boolean eof=false;

        @Override
        public String toString() {
            return "Summary{" +
                    "processed=" + processed +
                    ", failed=" + failed +
                    ", lru_hits=" + lru_hits +
                    ", creates=" + creates +
                    ", batches=" + batches +
                    ", graph_hits=" + graph_hits +
                    ", brand_creates=" + merch_brand_creates +
                    ", brand_reuse=" + merch_brand_reuse +
                    ", cat_creates=" + cat_creates +
                    ", cat_reuse=" + cat_reuse +
                    ", multi_lines=" + multi_lines +
                    ", eof=" + eof +
                    '}';
        }
    }
    private boolean processLine(String s, Summary summary, Map<Long, Node> accountLRU, Map<Long, Node> merchantLRU, Map<String, Node> brandLRU) {
        Node productNode = db.createNode(PRODUCT,SUBJECT);
        try{

            ProductFeedLine line = new ProductFeedLine(s);
            CommonProperties.setBaseProductProperties(productNode,line.productID,line.name,line.pageID,line.variant,line.locale,line.description,line.productURL,line.imageURL,line.discontinued,line.date);
            // create / find account
            Node accountNode = accountLRU.get(line.accountID);
            if (accountNode == null) {
                accountNode = db.findNode(ACCOUNT,CommonProperties.ACCOUNT_ID,line.accountID);
                if (accountNode == null) {
                    accountNode = db.createNode(ACCOUNT);
                    CommonProperties.setAccountProperties(accountNode,line.accountID);
                    summary.creates++;
                }else{
                    summary.graph_hits++;
                }
                accountLRU.put(line.accountID,accountNode);
            }else{
                summary.lru_hits++;
            }
            Node merchantNode = merchantLRU.get(line.merchantID);
            if (merchantNode == null) {
                merchantNode = db.findNode(MERCHANT,CommonProperties.MERCHANT_ID,line.merchantID);
                if (merchantNode == null) {
                    merchantNode = db.createNode(MERCHANT, SUBJECT);
                    CommonProperties.setMerchantProperties(merchantNode,line.merchantID,line.merchantGroupID);
                    accountNode.createRelationshipTo(merchantNode, CommonRelationships.OWNS);
                    summary.creates++;
                }else{
                    summary.graph_hits++;
                }
                merchantLRU.put(line.merchantID,merchantNode);
            }else{
                summary.lru_hits++;
            }
            Node brandNode = null;
            if (!line.brand.isEmpty()){
                brandNode = brandLRU.get(line.brand);
                if (brandNode == null) {
                    brandNode = db.findNode(BRAND,CommonProperties.BRAND_NAME,line.brand);
                    if (brandNode == null) {
                        brandNode = db.createNode(BRAND, SUBJECT);
                        CommonProperties.setBrandProperties(brandNode,line.brand);
                        summary.creates++;
                    }else{
                        summary.graph_hits++;
                    }
                    brandLRU.put(line.brand,brandNode);
                }else{
                    summary.lru_hits++;
                }

            }

            Node catEdgeNode = null;
            String categoryPath = line.catPath;

            catEdgeNode = categoryLRU.get(line.merchantID+categoryPath);
            if (catEdgeNode==null){
                List<String> categories = CategoryProcessor.splitCategories(categoryPath);
                catEdgeNode = CategoryProcessor.mergeCategoryPath(merchantNode, categories , summary);
                categoryLRU.put(line.merchantID+categoryPath,catEdgeNode);
            }else{
                summary.cat_reuse++;
            }

            productNode.createRelationshipTo(catEdgeNode,CommonRelationships.FITS);
            if (brandNode!=null) {
                brandNode.createRelationshipTo(productNode, CommonRelationships.MARKETS);
                boolean connectedToMerchant = false;
                for (Relationship r:brandNode.getRelationships(Direction.INCOMING,CommonRelationships.CARRIES)){
                    if (r.getStartNode().getId() == merchantNode.getId()) {
                        summary.merch_brand_reuse++;
                        connectedToMerchant = true;
                        break;
                    }
                }
                if (!connectedToMerchant){
                    summary.merch_brand_creates++;
                    merchantNode.createRelationshipTo(brandNode,CARRIES);
                }
            }
        }catch (Throwable n){
            productNode.delete();
            return false;
        }
        return true;
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
