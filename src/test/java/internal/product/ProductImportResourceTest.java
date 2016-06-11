package internal.product;

import common.CommonLabels;
import common.CommonProperties;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilder;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.test.server.HTTP;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Created by dmhum_000 on 5/13/2016.
 */
public class ProductImportResourceTest {

    GraphDatabaseService db;
    ServerControls server;

    @Before
    public void before(){
        TestServerBuilder builder = TestServerBuilders.newInProcessBuilder().withExtension("/product",ProductImportResource.class);
        server =  builder.newServer();
        db =server.graph();
        db.execute("Create Index On :"+ CommonLabels.ACCOUNT.name()+"("+ CommonProperties.ACCOUNT_ID+")");
        db.execute("Create Index On :"+ CommonLabels.MERCHANT.name()+"("+ CommonProperties.MERCHANT_ID+")");
        db.execute("Create Index On :"+ CommonLabels.BRAND.name()+"("+ CommonProperties.BRAND_NAME+")");
        db.execute("Create Index On :"+ CommonLabels.PRODUCT.name()+"("+ CommonProperties.PRODUCT_NAME+")");
        db.execute("Create Index On :"+ CommonLabels.PRODUCT.name()+"("+ CommonProperties.PRODUCT_ID+")");
        db.execute("Create Index On :"+ CommonLabels.CATEGORY.name()+"("+ CommonProperties.CATEGORY_NAME+")");
    }

    @Test
    public void testBatch() throws Throwable
    {
//        // client.target()
//        String location = HTTP.POST(server.httpURI().resolve("product").toString()).location();
//        HTTP.Response response = HTTP.POST(location+"import/batch","my content");
//
//        System.out.println(response);

        Client client = ResteasyClientBuilder.newClient();
        WebTarget target = client.target(server.httpURI()).path("product/import/batch");

        Form form = new Form();
        form.param("x", "foo");
        form.param("y", "bar");

        Response x = target.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        System.out.println(x);
    }

    @Test
    public void testFile() throws Throwable
    {
       // client.target()
        String location = HTTP.GET(server.httpURI().resolve("product").toString()).location();
        HTTP.Response response = HTTP.withHeaders("Accept","*/*","file","C:/dev/product.csv","max","1320000","skip","1").GET(location+"import/file");

        System.out.println(response);

    }

}
