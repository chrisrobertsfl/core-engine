package com.chrisrobertsfl.coreengine.service.drools;

import org.kie.api.io.Resource;

import static org.kie.internal.io.ResourceFactory.newClassPathResource;

public record ClasspathSource(String source) implements RuleSource {

    @Override
    public Resource resource() {
        return newClassPathResource(source);
    }
}
