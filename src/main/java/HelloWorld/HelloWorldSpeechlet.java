package HelloWorld;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by taldavidi on 7/12/16.
 */
public class HelloWorldSpeechlet implements Speechlet{

    Logger logger = LoggerFactory.getLogger(HelloWorldSpeechlet.class);

    public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        logger.info("onSessionStarted: request id= {}, session id={}",request.getRequestId(), session.getSessionId());

    }

    public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        logger.info("onLaunch: request id= {}, session id={}",request.getRequestId(), session.getSessionId());
        return welcomeMessage();

    }

    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {

        logger.info("on intent: request id = {}, session id={}",request.getRequestId(), session.getSessionId());
        Intent myIntent = request.getIntent();

        String value = myIntent.getName();

        if("HelloIntent".equals(value)){
            return helloMessage();
        }
        else if("AMAZON.HelpIntent".equals(value)){

            return helpMessage();
        }
        else{
            throw new SpeechletException("Invalid Intent");
        }
    }

    public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {

        logger.info("ending session: request id = {}, session id = {}",request.getRequestId(),session.getSessionId());
    }

    private SpeechletResponse helloMessage(){

        String speechText = "Hello world, my name is Alexa and I am pretty useless.";
        SimpleCard card = new SimpleCard();
        card.setTitle("Hello world.");
        card.setContent(speechText);

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech,card);
    }

    private SpeechletResponse helpMessage(){

        String speechText = "Just ask me to say hello!";
        SimpleCard card = new SimpleCard();
        card.setTitle("Help - Hello, Alexa");
        card.setContent(speechText);

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech,reprompt,card);
    }

    private SpeechletResponse welcomeMessage(){

        String speechText = "Welcome to the Hello Alexa skill. You can say hi!";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Hello World");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);

    }
}
