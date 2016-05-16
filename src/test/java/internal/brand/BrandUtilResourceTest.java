package internal.brand;

import common.CommonLabels;
import common.CommonProperties;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilder;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.test.server.HTTP;

/**
 * Created by dmhum_000 on 5/13/2016.
 */
public class BrandUtilResourceTest {

    GraphDatabaseService db;
    ServerControls server;

    @Before
    public void before(){
        TestServerBuilder builder = TestServerBuilders.newInProcessBuilder().withExtension("/brand",BrandUtilResource.class);
        server =  builder.newServer();
        db =server.graph();

        try(Transaction tx = db.beginTx()) {
            for (int i = 0; i < 100; i++)
                db.createNode(CommonLabels.BRAND).setProperty(CommonProperties.BRAND_NAME, "test" + i);
            tx.success();
        }
    }

    @Test
    public void verifyEndPoint() throws Throwable
    {

       // client.target()
        String location = HTTP.GET(server.httpURI().resolve("brand").toString()).location();
        HTTP.Response response = HTTP.withHeaders("Accept","*/*","limit","1320000","skip","0").GET(location+"util/export");

        System.out.println(response);

    }

}
