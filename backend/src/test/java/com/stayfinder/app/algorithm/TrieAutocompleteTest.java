package com.stayfinder.app.algorithm;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrieAutocompleteTest {
    @Test
    void returnsCaseInsensitivePrefixMatches() {
        TrieAutocomplete trie = new TrieAutocomplete();
        trie.insert("Jaipur");
        trie.insert("Jaisalmer");
        trie.insert("Mumbai");

        assertThat(trie.suggest("jai", 5)).containsExactly("Jaipur", "Jaisalmer");
    }

    @Test
    void respectsLimitAndUnknownPrefixes() {
        TrieAutocomplete trie = new TrieAutocomplete();
        List.of("Goa", "Gokarna", "Gorakhpur").forEach(trie::insert);

        assertThat(trie.suggest("go", 2)).hasSize(2);
        assertThat(trie.suggest("xyz", 5)).isEmpty();
    }
}
