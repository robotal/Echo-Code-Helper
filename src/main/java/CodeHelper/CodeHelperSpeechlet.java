package CodeHelper;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by taldavidi on 7/11/16.
 *
 * Code helper webapp
 */
public class CodeHelperSpeechlet implements Speechlet {

    private static final Logger log = LoggerFactory.getLogger(CodeHelperSpeechlet.class);

    private static String solrURL = "http://localhost:8983/solr/MedicalCoding";
    private static SolrClient client;
    private static String[] codevalue_slots ={"codeslot_one","codeslot_two","codeslot_three","codeslot_four","codeslot_five","codeslot_six","codeslot_seven"};

    static{

        String beanstalkSolrUrl = System.getProperty("solr.url");
        if (beanstalkSolrUrl != null && !beanstalkSolrUrl.equals("")) {
            solrURL = beanstalkSolrUrl;
            log.info("Using '{}' for solr url", solrURL);
        }

        client = new HttpSolrClient.Builder(solrURL).build();
    }

    public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {

        log.info("onSessionStarted requestId={"+request.getRequestId()+"}, sessionId={"+session.getSessionId()+"}");
    }

    public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        log.info("onLaunch requestId={"+session.getSessionId()+"}, sessionId={"+request.getRequestId()+"}");

        return getWelcomeResponse();
    }

    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {

        log.info("onIntent requestId={"+request.getRequestId()+"}, sessionId={"+session.getSessionId()+"}");

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        log.info("Intent received: {}", intentName);


        if ("CodeLookupIntent".equals(intentName)) {

            String codeValue ="";

            log.info("Concatenating code slots: ");

            for(String slotName:codevalue_slots){

                Slot speechSlot = intent.getSlot(slotName);
                if(speechSlot!=null){
                    if(speechSlot.getValue()!=null){
                        codeValue+=speechSlot.getValue();
                        log.info("Slot position: {}, Slot value: {}",slotName, speechSlot.getValue());
                    }
                }
            }
            return getCodeDefinition(codeValue);
        }
        else if("DescriptionQueryIntent".equals(intentName)){

            ArrayList<String> descriptionList = new ArrayList<>();
            String description = intent.getSlot("codedescription").getValue();
            descriptionList.add(description);

            session.setAttribute("prev_description", descriptionList);
            return getCodeName(description);
        }
        else if("QueryAdditionIntent".equals(intentName)){
            String descriptionAddition = intent.getSlot("codedescription").getValue();

            @SuppressWarnings("unchecked")
            ArrayList<String> prevDesc = (ArrayList<String>) session.getAttribute("prev_description");

            if(prevDesc == null){
                prevDesc = new ArrayList<>();
            }
            prevDesc.add(descriptionAddition);
            String newDesc = "";

            for(String descPart: prevDesc){

                newDesc+=descPart + " ";
            }

            session.setAttribute("prev_description", prevDesc);
            return getCodeName(newDesc);
        }
        else if("RemoveLastParameterIntent".equals(intentName)){
            String value = intent.getSlot("levels").getValue();
            int levelsToRemove;
            if(value == null){
                levelsToRemove = 1;
            }
            else{
                try {
                    levelsToRemove = Integer.parseInt(value);
                }catch (NumberFormatException e){
                    levelsToRemove = 0;
                    log.error("Invalid number: ", e);
                }
            }

            @SuppressWarnings("unchecked")
            ArrayList<String> prevDesc = (ArrayList<String>) session.getAttribute("prev_description");

            if(levelsToRemove >= prevDesc.size()){
                String speechText = "I only have " + prevDesc.size() + " search levels memorized. Clearing everything.";
                String repromptText = "I am currently waiting for a question.";
                session.removeAttribute("prev_description");

                return generateAskResponse(speechText, repromptText, null, null);
            }
            else{

                for (int i = 0; i < levelsToRemove; i++) {

                    prevDesc.remove(prevDesc.size()-1);
                }

                String newDesc = "";

                for(String descPart: prevDesc){

                    newDesc+=descPart + " ";
                }

                session.setAttribute("prev_description", prevDesc);
                return getCodeName(newDesc);
            }
        }
        else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelpResponse();
        }
        else if("AMAZON.CancelIntent".equals(intentName)) {
            //wipe session attributes and return welcome message
            for(String atr : session.getAttributes().keySet()){
                session.removeAttribute(atr);
            }

            return getWelcomeResponse();
        }
        else if("AMAZON.StopIntent".equals(intentName)){

            return getGoodbyeMessage();

        }
        else{
            throw new SpeechletException("Invalid Intent");
        }
    }

    public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {

        log.info("onSessionEnded requestId={"+request.getRequestId()+"}, sessionId={"+session.getSessionId()+"}");
    }


    private SpeechletResponse getWelcomeResponse (){

        String speechText = "Welcome to the I10 Code Helper, you can ask me questions about ICD10 codes.";
        String repromptText = "Try asking me to define a code or describe a code for me to find.";

        return generateAskResponse(speechText, repromptText, null, null);
    }

    private SpeechletResponse getGoodbyeMessage(){

        String speechText = "Thanks for using the I10 code helper!";
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech);
    }

    private SpeechletResponse getHelpResponse() {

        String speechText = "You can ask me to define any ICD10 code or give me a description of a code! Look in the companion app for more info.";
        String repromptText = "Try asking me: what is the code for myocardial infarction?";

        String cardTitle = "Help";
        String cardText = "1). You can ask to define and code and give a code value: Define the code R07.9\n" +
                "2). You can ask me to look up a code based on description: What is the code for myocardial infarction\n" +
                "       If the search is too general, try adding some search terms: add to that inferior wall\n" +
                "       You can also tell me to forget the last thing or the last few things you added: Remove the last 2 things I said\n" +
                "       Aftwerwards you can ask me to define one of those codes for the full description: Define the code I22.1";


        return  generateAskResponse(speechText, repromptText, cardTitle, cardText);
    }

    private SpeechletResponse getCodeDefinition (String code){

        String speechText;
        String repromptText;

        if(code == null || code.trim().equals("")){

            speechText = "Sorry, I did not hear a code to define, please repeat yourself.";
            return generateAskResponse(speechText, speechText, null, null);
        }
        else {
            code = code.replace("dot", ".");
            code = code.replace(" ", "");
            code = code.toUpperCase();

            try {

                SolrQuery query = new SolrQuery();
                query.set("q",code);
                QueryResponse response = client.query(query);
                SolrDocumentList documentList = response.getResults();


                if(documentList.getNumFound() == 0){

                    speechText = "Sorry, I couldn't find the code " + code + " please ask again.";
                    repromptText = "I am currently waiting for a question, say quit to exit.";

                    return generateAskResponse(speechText, repromptText, "Code definition lookup", "Unknown code: " + code);
                }
                else {

                    if(documentList.getNumFound() > 1){
                        log.warn("Found multiple definitions for code: " + code);
                    }

                    speechText = "The ICD10-CM definition for the code " + code + " is " + documentList.get(0).get("description");
                    repromptText = "I am listening for another question. If you are done you may say quit or exit to finish.";
                    String cardTitle = "Code Definition lookup - " + code;
                    String cardContent = "Definition of " + code + ": " + documentList.get(0).get("description");

                    return generateAskResponse(speechText, repromptText, cardTitle, cardContent);
                }
            } catch (SolrServerException | IOException e) {
                log.error("Exception: ", e);

                speechText = "Sorry, I couldn't find the code due to an error. It seems something is broken currently.";

                PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
                speech.setText(speechText);

                return SpeechletResponse.newTellResponse(speech);


            }
        }
    }

    /**
     * Takes a description of a code and returns a Speechlet with the closest guessed code.
     * @param codeDescription Description of the code
     * @return A speechlet response indicating the result of the query
     */
    private SpeechletResponse getCodeName(String codeDescription){

        String speechText, repromptText;
        String cardTitle, cardText;

        if(codeDescription == null){

            speechText = "I did not hear a description for a code. Please repeat what you said";

            return generateAskResponse(speechText, speechText, null, null);
        }

        String words[] = codeDescription.split(" ");

        String qParams = "";

        for (String word : words) {

            qParams += "+" + word + " ";
        }

        try {
            SolrQuery query = new SolrQuery();

            query.set("q", qParams);
            query.set("fq", "is_valid:1");

            QueryResponse response= client.query(query);
            SolrDocumentList docList = response.getResults();

            long numDocuments = docList.getNumFound();

            if(numDocuments == 0){

                speechText = "It seems like no codes match that description, please ask again with a more general description of the code.";
                repromptText = "If you have accidentally added unwanted search terms, you can tell me to forget the last thing you said.";
                cardTitle = "Code description matcher";
                cardText = "Search terms: " + codeDescription;

                return generateAskResponse(speechText, repromptText, cardTitle, cardText);
            }
            else if(numDocuments == 1){

                speechText = "The code matching the description you provided is " + docList.get(0).get("code") + ".";
                repromptText = "I am listening for another question. If you are done you may say quit or exit to finish.";

                cardTitle = "Code description matcher";
                cardText = "Closest match: " + docList.get(0).get("code");
                return generateAskResponse(speechText, repromptText, cardTitle, cardText);
            }
            else if(numDocuments <= 5){

                speechText = "The top " + numDocuments + " codes in order of relevance are ";
                for(int i = 0 ; i < numDocuments - 1; i++) {

                    speechText += docList.get(i).get("code") + ", ";
                }

                //remove last comma
                speechText = speechText.substring(0, speechText.length() - 2);
                speechText += " and " + docList.get( (int)numDocuments - 1). get("code") +".";

                repromptText = "You can ask me to narrow the search terms or ask another question";
                cardTitle = "Matching codes: " + numDocuments;

                return generateAskResponse(speechText, repromptText, cardTitle, speechText);
            }
            else{

                speechText = "I found " + numDocuments + " codes matching that description. Please narrow the search by adding other keywords.";

                repromptText = "You can say add to search followed by more search terms to narrow the search.";

                cardTitle = "Codes found: " + numDocuments;
                cardText = "Code description: " + codeDescription;

                return  generateAskResponse(speechText, repromptText, cardTitle, cardText);
            }

        } catch (IOException | SolrServerException e) {
            log.error("Exception in getCodeName(): ", e);

            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText("An internal error occurred preventing me from doing a search.");
            return SpeechletResponse.newTellResponse(speech);
        }
    }

    private SpeechletResponse generateAskResponse(String speechText, String repromptText, String cardTitle, String cardText){

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        Reprompt reprompt = new Reprompt();
        speech.setText(speechText);

        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText(repromptText);
        reprompt.setOutputSpeech(repromptSpeech);

        if(cardTitle != null && cardText != null){

            SimpleCard card = new SimpleCard();
            card.setContent(cardText);
            card.setTitle(cardTitle);
            return SpeechletResponse.newAskResponse(speech,reprompt, card);
        }

        return SpeechletResponse.newAskResponse(speech,reprompt);
    }
}