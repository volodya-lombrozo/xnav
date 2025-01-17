package com.github.lombrozo.xnav;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Navigator}.
 * @since 0.1
 */
final class XmlTest {

    @Test
    void convertsDocumentToString() {
        MatcherAssert.assertThat(
            "Document is not converted to string",
            new Xml("<doc></doc>").toString(),
            Matchers.equalTo("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><doc/>")
        );
    }

    @Test
    void convertsNodeToString() {
        MatcherAssert.assertThat(
            "Node is not converted to string",
            new Xml("<doc><node>text</node></doc>").child("doc").child("node").toString(),
            Matchers.equalTo("<node>text</node>")
        );
    }
}
