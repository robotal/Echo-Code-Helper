package CodeHelper;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by taldavidi on 7/11/16.
 */
public class CodeHelperSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {

    private static final Set<String> supportedApplicationIds = new HashSet<String>();
    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds.add("amzn1.echo-sdk-ams.app.c468a276-216a-4adb-8537-0744b6afce81");
    }

    public CodeHelperSpeechletRequestStreamHandler() {

        super(new CodeHelperSpeechlet(), supportedApplicationIds);
    }
}
