package internal.product;

import common.CommonLabels;
import common.CommonProperties;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilder;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.test.server.HTTP;

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
    public void verifyEndPoint() throws Throwable
    {

       // client.target()
        String location = HTTP.GET(server.httpURI().resolve("product").toString()).location();
        HTTP.Response response = HTTP.withHeaders("Accept","*/*","path","C:/dev/product.csv","max","1320000","skip","1").GET(location+"import/load");

        System.out.println(response);

    }

}
