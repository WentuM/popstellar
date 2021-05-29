package com.github.dedis.student20_pop.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/** Class to model a message that needs to be signed by witnesses  */
public class WitnessMessage  {

    private  String messageId; /** Base 64 URL encoded ID of the message that we want to sign*/
    private Set<String> witnesses ; /** Set of witnesses that have signed the message*/
    private String title = ""; /** Title that will be displayed for the message*/
    private String description = ""; /** Description that will be displayed for the message*/

    /**
     * Constructor for a  Witness Message
     *
     * @param messageId ID of the message to sign
     */
    public WitnessMessage(String messageId) {
        witnesses = Collections.emptySet();
        this.messageId = messageId;
    }

    /**
     * Method that checks whether the current message has been signed by the Witness public key provided
     *
     * @param pk public key of the witness
     */
    public boolean isSignedBy(String pk) { return witnesses.contains(pk);}


    /**
     * Method to add a new witness that have signed the message
     *
     * @param pk public key of the witness that have signed  the message
     */
    public void addWitness(String pk) { witnesses.add(pk);}

    public String getMessageId() {return messageId;}

    public Set<String> getWitnesses() {  return witnesses;}

    public String getTitle() {return title;}
    public void setTitle(String title) { this.title = title;  }

    public String getDescription() {return description;}
    public void setDescription(String description) { this.description = description;  }



}
