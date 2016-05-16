package internal.review;

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
public class ReviewImportResourceTest {

    GraphDatabaseService db;
    ServerControls server;

    @Before
    public void before(){
        TestServerBuilder builder = TestServerBuilders.newInProcessBuilder().withExtension("/review", ReviewImportResource.class);
        server =  builder.newServer();
        db =server.graph();
        db.execute("Create Index On :Account(id)");
        db.execute("Create Index On :Subject(pr_id)");
        db.execute("Create Index On :Product(prod_id)");
    }

    @Test
    public void verifyEndPoint() throws Throwable
    {

        String location = HTTP.GET(server.httpURI().resolve("review").toString()).location();
        HTTP.Response response = HTTP.withHeaders("Accept","*/*","path","C:/dev/internal.review.csv","max","1320000","skip","1").GET(location+"import/load");

        System.out.println(response);

    }

}
