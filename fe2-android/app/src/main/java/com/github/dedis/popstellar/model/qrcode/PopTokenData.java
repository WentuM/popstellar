package com.github.dedis.popstellar.model.qrcode;

import com.github.dedis.popstellar.model.Immutable;
import com.github.dedis.popstellar.model.network.serializer.JsonUtils;
import com.google.gson.Gson;

/**
 * Represent the data held in a QRCode used to display roll call tokens
 */
@Immutable
public class PopTokenData {

    private final String poptoken;

    public PopTokenData(String popToken){
        this.poptoken = popToken;
    }

    /**
     * Extract data from the given json string
     *
     * @param gson is used to parse the json string into the object
     * @param json representation of the data
     * @return the extracted data
     * @throws com.google.gson.JsonParseException if the value cannot be parsed
     */
    public static PopTokenData extractFrom(Gson gson, String json) {
        JsonUtils.verifyJson(JsonUtils.POP_TOKEN_SCHEME, json);
        return gson.fromJson(json, PopTokenData.class);
    }

    public String getPopToken(){
        return poptoken;
    }

}
