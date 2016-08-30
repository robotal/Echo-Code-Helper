package HelloWorld;

import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by taldavidi on 7/12/16.
 */
public class HelloWorldSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {

    private static final Set<String> supportedApplicationIds = new HashSet<String>();
    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds.add("amzn1.echo-sdk-ams.app.485780ef-1de6-4368-96a4-d10b1737588a");
    }

    public HelloWorldSpeechletRequestStreamHandler() {
        super(new HelloWorldSpeechlet(), supportedApplicationIds);
    }
}
