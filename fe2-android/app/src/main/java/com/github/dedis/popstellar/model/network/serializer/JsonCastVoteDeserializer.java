package com.github.dedis.popstellar.model.network.serializer;

import android.util.Log;

import com.github.dedis.popstellar.model.network.method.message.data.Data;
import com.github.dedis.popstellar.model.network.method.message.data.election.CastVote;
import com.github.dedis.popstellar.model.network.method.message.data.election.ElectionEncryptedVote;
import com.github.dedis.popstellar.model.network.method.message.data.election.ElectionVote;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class JsonCastVoteDeserializer implements JsonDeserializer<CastVote> {

    @Override
    public CastVote deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        Log.d("laoIdField is:", "dsddd");
        JsonArray jsonVote = obj.getAsJsonArray("votes");

        // Parse fields of the Json
        JsonElement electionIdField = json.getAsJsonObject().get("election");
        JsonElement laoIdField = json.getAsJsonObject().get("lao");

        String electionId = context.deserialize(electionIdField, String.class);
        String laoId = context.deserialize(laoIdField, String.class);

        boolean typeValidationInt = true;
        boolean typeValidationString = true;
        // Vote type of a CastVote is either an integer for an OpenBallot election or a
        // String for an Encrypted election, type should be valid for all votes
        for (int i = 0; i < jsonVote.size(); i++) {
            JsonObject voteContent = jsonVote.get(i).getAsJsonObject();
            typeValidationInt =
                    typeValidationInt && voteContent.get("vote").getAsJsonPrimitive().isNumber();
            typeValidationString =
                    typeValidationString && voteContent.get("vote").getAsJsonPrimitive().isString();
        }
        if (typeValidationInt && !typeValidationString) {

            List<ElectionVote> votes = context.deserialize(jsonVote, List<ElectionVote>.class);
            return new CastVote(votes, electionId, laoId);
        } else if (!typeValidationInt && typeValidationString) {
            List<ElectionEncryptedVote> votes = context.deserialize(jsonVote, ElectionVote.class);
            return new CastVote(votes, electionId, laoId);
        } else {
            throw new JsonParseException("Unknown vote type in cast vote message");
        }
    }
}
