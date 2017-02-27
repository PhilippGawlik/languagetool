package org.languagetool.rules;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import de.danielnaber.jwordsplitter.*;

/**
 * A rule to split german compound nouns following the rules of german simple language
 * into stem related parts concatenated by using a hyphen.
 *
 * Created by Philipp Gawlik (philipp.gawlik@googlemail.com) on 19.07.16.
 */
public class SimpleGermanCompoundSplitRule extends Rule {

    @Override
    public String getId() {return "KOMPOSITA";}

    @Override
    public String getDescription() {
        return "Eine Regel, um deutsche Komposita zu zerlegen.";  // shown in the configuration dialog
    }

    // Compare two strings in case the suffix differs. If so return suffix (link element).
    private String getLinkElement(String token, String mergeStems) {
        // String to hold difference
        String diff = "";
        int idx = 0;
        // Iterate over symbols of token
        for (int i = 0; i < token.length() && idx < mergeStems.length(); i++) {
            // Compare current symbol and add difference to string diff
            if (mergeStems.charAt(idx) != token.charAt(i)){
                diff += token.charAt(i);
            }
            // Go on if there is no difference
            else {
                idx += 1;
            }
        }
        return diff;
    }

    // Capitalize first letter of a string
    private String capitalize_first_letter(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    // The stems in list parts don't contain affix information of the token any more
    // To make sure affix information isn't lost while generating the suggested correction
    // string with merged stems is generated stem by stem and if at some point there is no prefix
    // of the token string the missing part is added to the generated suggestion.
    private String generateSuggestedCorrection(AnalyzedTokenReadings token, List<String> parts) {
        // Add first stem to suggested correction
        String sugCorrect = parts.get(0);
        // Add first stem to merged stems
        String mergeStems = parts.get(0);
        // Remove added stem from parts
        parts.remove(0);
        // For the remaining stems of the current word
        for (String stem : parts) {
            // Concatenate stems for prefix comparison to merged stems
            mergeStems += stem;
            // If mergeStems is prefix of token concatenate stem to suggested correction by using a hyphen
            if (token.getToken().startsWith(mergeStems)) {
                sugCorrect += "-" + capitalize_first_letter(stem);
            }
            // Else find the missing linking (affix) element and add it to suggestion
            else {
                String linkElem = getLinkElement(token.getToken(), mergeStems);
                sugCorrect += linkElem + "-" + capitalize_first_letter(stem);
            }
        }
        return sugCorrect;
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
        List<RuleMatch> ruleMatches = new ArrayList<>();

        // Get all the tokens
        AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

        // Iterate over all tokens (words)
        for (AnalyzedTokenReadings token : tokens) {
            System.out.println("Token: " + token.getToken());
            for (AnalyzedToken analyzedToken : token.getReadings()) {
                System.out.println("  Lemma: " + analyzedToken.getLemma());
                System.out.println("  POS: " + analyzedToken.getPOSTag());
}
            // Iterate over several readings of the current word
            for (AnalyzedToken analyzedToken : token.getReadings()) {
                //Check if relevant token
                if (analyzedToken.getPOSTag() != null) {
                    // If token is a noun
                    if (analyzedToken.getPOSTag().startsWith("SUB:")) {
                        // check if compound noun is already split and interconnected by hyphens
                        if (token.getToken().indexOf('-') == -1) {
                            // Try to find a split of the noun
                            AbstractWordSplitter splitter = new GermanWordSplitter(true);
                            List<String> parts = splitter.splitWord(token.getToken());
                            // If split exits
                            if (parts.size() > 1) {
                                // Generate suggested correction
                                String sugCorrect = generateSuggestedCorrection(token, parts);
                                RuleMatch ruleMatch = new RuleMatch(this, token.getStartPos(),
                                        token.getEndPos(),
                                        "Trennen Sie komplexe Wörter entsprechend der Sinneinheiten durch Bindestriche.");
                                // the user will see this as a suggested correction
                                ruleMatch.setSuggestedReplacement(sugCorrect);
                                ruleMatches.add(ruleMatch);
                            }
                        }
                    }
                }
            }
        }
        return toRuleMatchArray(ruleMatches);
    }

    @Override
    public void reset() {
        // if we had some internal state kept in member variables, we would need to reset them here
    }
}
