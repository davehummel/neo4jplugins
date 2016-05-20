package common;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.string.UTF8;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by dmhum_000 on 5/17/2016.
 */

@Path( "/util" )
public class CommonResource {

    private static final long BATCH_SIZE = 500000;
    private final GraphDatabaseService db;

    public CommonResource(@javax.ws.rs.core.Context GraphDatabaseService database) {
        this.db = database;
    }

    @GET
    @Produces( MediaType.TEXT_PLAIN )
    @Path("build_index")
    public Response buildindex() {
        db.execute("Create Index On :" + CommonLabels.ACCOUNT.name() + "(" + CommonProperties.ACCOUNT_ID + ")");
        db.execute("Create Index On :" + CommonLabels.MERCHANT.name() + "(" + CommonProperties.MERCHANT_ID + ")");
        db.execute("Create Index On :" + CommonLabels.BRAND.name() + "(" + CommonProperties.BRAND_NAME + ")");
        db.execute("Create Index On :" + CommonLabels.PRODUCT.name() + "(" + CommonProperties.PRODUCT_NAME + ")");
        db.execute("Create Index On :" + CommonLabels.PRODUCT.name() + "(" + CommonProperties.PRODUCT_ID + ")");
        db.execute("Create Index On :" + CommonLabels.CATEGORY.name() + "(" + CommonProperties.CATEGORY_NAME + ")");

        return Response.status( Response.Status.OK ).entity( UTF8.encode("Done") ).build();

    }
}
