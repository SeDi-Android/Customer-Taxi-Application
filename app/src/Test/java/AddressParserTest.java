
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.RobolectricTestRunner;

import ru.sedi.customerclient.classes.AddressParser;
import ru.sedi.customerclient.classes.GeoLocation.Nominatium.NominatimGeocoder;

@RunWith(RobolectricTestRunner.class)
public class AddressParserTest {

    @Test
    public void test_removeDoubleWhitespace(){
        String original = "   Москва,      Мира     44";
        String assertation = "Москва, Мира 44";

        AddressParser parser = new AddressParser(original);
        parser.removeDoubleWhitespace();
        Assert.assertTrue(assertation.equalsIgnoreCase(parser.getStringAddress()));
    }

    @Test
    public void test_getElementDivide(){
        // -- 1 test --
        String original = "Москва, Мира 44";
        int count = 2;

        AddressParser parser = new AddressParser(original);
        Assert.assertEquals(parser.getElementDivide(",").length, count);

        // -- 2 test --

        original = "Москва, Мира, 44";
        count = 3;

        parser = new AddressParser(original);
        Assert.assertEquals(parser.getElementDivide(",").length, count);

        // -- 3 test --

        original = "Москва Мира 44";
        count = 3;

        parser = new AddressParser(original);
        Assert.assertEquals(parser.getElementDivide(" ").length, count);

        // -- 3 test --

        original = "МоскваМира44";
        count = 1;

        parser = new AddressParser(original);
        Assert.assertEquals(parser.getElementDivide(" ").length, count);
    }

    @Test
    public void test_tryGetCorrectedAddress(){
        NominatimGeocoder geocoder = new NominatimGeocoder("ru");
        // -- 1 test --
        String original = "семикаракорск ромашенко, 5";
        String best = "семикаракорск, ромашенко 5";

        AddressParser parser = new AddressParser(geocoder, original);
        String address = parser.parse();

        Assert.assertTrue(address.equalsIgnoreCase(best));

        // -- 2 test --

        original = "семикаракорск ромашенко 5";


        parser = new AddressParser(geocoder, original);
        address = parser.parse();

        Assert.assertTrue(address.equalsIgnoreCase(best));

        // -- 3 test --

        original = "красная поляна ромашенко 5";
        best = "красная поляна, ромашенко 5";


        parser = new AddressParser(geocoder, original);
        address = parser.parse();

        Assert.assertTrue(address.equalsIgnoreCase(best));
    }
}
