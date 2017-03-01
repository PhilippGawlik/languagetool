package org.languagetool.rules;

import de.danielnaber.jwordsplitter.AbstractWordSplitter;
import de.danielnaber.jwordsplitter.GermanWordSplitter;
import org.apache.commons.lang.ArrayUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A rule to constrain the sentence length to 12 Words while ignoring punctuation.
 *
 * Created by Philipp Gawlik (philipp.gawlik@googlemail.com) on 28.02.17.
 */
public class SimpleGermanSentenceLengthRule extends Rule {

    @Override
    public String getId() {return "SATZ";}

    @Override
    public String getDescription() {
        return "Eine Regel, um die maximale Satzlänge auf 12 Worte zu beschränken.";  // shown in the configuration dialog
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
        List<RuleMatch> ruleMatches = new ArrayList<>();
        String[] blacklist = {"\"", "#", "$", "%", "&", "\\", "*", "-", ",", "(", ")", ":", ";", "=", "+", "'", "/", "[", "]", "_", "`", "´", ".", "!", "?", "<", ">", "@", "^", "{", "}", "|", "~"};
        // Let's get all the tokens (i.e. words) of this sentence, but not the spaces:
        AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

        // No let's iterate over those - note that the first token will
        // be a special token that indicates the start of a sentence:
        Integer buffer = -1;  // start with -1 to ignore initial token
        for (AnalyzedTokenReadings token : tokens) {
            System.out.println("Token: " + token.getToken());  // the original word from the input text

            if (ArrayUtils.contains(blacklist, token.getToken())) {
                continue;
            }
            else {
                buffer += 1;
            }
            System.out.println("Buffer: " + buffer);  // the original word from the input text
            // You can add your own logic here to find errors. Here, we just consider
            // the word "demo" an error and create a rule match that LanguageTool will
            // then show to the user:
            if (buffer > 12) {
                RuleMatch ruleMatch = new RuleMatch(this, tokens[0].getStartPos(),
                        tokens[tokens.length-1].getEndPos(),
                        "Vermeiden Sie Sätze mit mehr als 12 Wörtern.");
//                ruleMatch.setSuggestedReplacement("Vermeiden Sie Sätze mit mehr als 12 Wörtern.");
                ruleMatches.add(ruleMatch);
                break;
            }
        }
        return toRuleMatchArray(ruleMatches);
    }

    @Override
    public void reset() {
        // if we had some internal state kept in member variables, we would need to reset them here
    }
}
