package org.languagetool.rules;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.languagetool.AnalyzedSentence;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 */
public class SimpleGermanMetaphorsRule extends Rule {

    @Override
    public String getId() {return "METAPHERN";}

    @Override
    public String getDescription() {
        return "Leichte Sprache: Metaphern und Redewendungen";  // shown in the configuration dialog
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
        Trie trie = new Trie().onlyWholeWords();
        Map keyword2replacement = new HashMap();

        String filename = "org/languagetool/word_list/metaphor_and_expressions_without_flection.txt";
        ClassLoader classLoader = getClass().getClassLoader();
	    File file = new File(classLoader.getResource(filename).getFile());
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split("\t");
                String keyword = parts[0];
                trie.addKeyword(keyword);
                if (parts.length > 1) {
                    keyword2replacement.put(keyword, parts[1]);
                }
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<RuleMatch> ruleMatches = new ArrayList<>();
        String text = sentence.getText();
        Collection<Emit> emits = trie.parseText(text);
        Iterator<Emit> itr = emits.iterator();
        while(itr.hasNext()) {
            Emit elem = itr.next();
            String keyword = elem.getKeyword();
//            System.out.println(" \tIterator Element ");
//            System.out.println(" \tBegin: " + elem.getStart());
//            System.out.println(" \tEnd: " + elem.getEnd());
//            System.out.println(" \tKeyword: " + keyword);

            RuleMatch ruleMatch = new RuleMatch(this, elem.getStart(), elem.getEnd()+1, "Metapher/Redewendung '" + keyword + "' gefunden");
            if (keyword2replacement.containsKey(keyword)) {
                ruleMatch.setSuggestedReplacement((String) keyword2replacement.get(keyword));  // the user will see this as a suggested correction
            }
            else {
                ruleMatch.setSuggestedReplacement("Vermeiden Sie Metaphern oder Redewendungen.");
            }
            ruleMatches.add(ruleMatch);
        }

        return toRuleMatchArray(ruleMatches);
    }

    @Override
    public void reset() {
        // if we had some internal state kept in member variables, we would need to reset them here
    }
}
