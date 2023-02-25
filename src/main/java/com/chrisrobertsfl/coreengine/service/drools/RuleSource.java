package com.chrisrobertsfl.coreengine.service.drools;

import org.kie.api.io.Resource;

public sealed interface RuleSource permits FileSource, ClasspathSource, StringSource {

    static FileSource file(final String source) {
        return new FileSource(source);
    }

    static StringSource string(final String source) {
        return new StringSource(source);
    }

    static ClasspathSource classpath(final String source) {
        return new ClasspathSource(source);
    }

    default Resource resource() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
