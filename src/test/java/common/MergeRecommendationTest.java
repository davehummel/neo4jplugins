package common;

import org.junit.Before;
import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * Created by dmhum_000 on 5/15/2016.
 */
public class MergeRecommendationTest {

    @Before
    public void before(){

    }

    @Test
    public void verifyToString() throws Throwable
    {
        MergeRecommendation mr = new MergeRecommendation();

        mr.addMerge("CAT","cat",false);
        mr.addMerge("Cat","cat",false);
        mr.addMerge("CATTY","CAT",false);
        mr.addMerge("DOG","dog",false);
        mr.addMerge("Doggy","dog",false);
        mr.addMerge("Man","man",false);

        assertEquals("cat¦¦CAT¦CATTY¦Cat\n" +
                "dog¦¦DOG¦Doggy\n" +
                "man¦¦Man\n",mr.toString());
    }

    @Test
    public void verifyFromString() throws Throwable
    {
        Stream<String> stringStream = Stream.of("cat¦¦CAT¦CATTY¦Cat","dog¦¦DOG¦Doggy","man¦¦Man");
        MergeRecommendation mr = new MergeRecommendation(stringStream);

        mr.addMerge("CAT","cat",false);
        mr.addMerge("Cat","cat",false);
        mr.addMerge("CATTY","CAT",false);
        mr.addMerge("DOG","dog",false);
        mr.addMerge("Doggy","dog",false);
        mr.addMerge("Man","man",false);

        assertEquals("cat¦¦CAT¦CATTY¦Cat\n" +
                "dog¦¦DOG¦Doggy\n" +
                "man¦¦Man\n",mr.toString());
    }


    @Test
    public void verifyToReplaces() throws Throwable
    {
        MergeRecommendation mr = new MergeRecommendation();

        mr.addMerge("CAT","cat",false);
        assertEquals("cat",mr.addMerge("CAT","dog",false));
        assertEquals("cat",mr.getMerge("CAT"));

        assertNull(mr.addMerge("CAT","dog",true));
        assertEquals("dog",mr.getMerge("CAT"));
    }
}
