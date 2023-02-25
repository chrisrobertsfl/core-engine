package com.chrisrobertsfl.coreengine.service.drools;

import org.kie.api.io.Resource;
import org.kie.internal.io.ResourceFactory;

public record FileSource(String source) implements RuleSource {

    @Override
    public Resource resource() {
        return ResourceFactory.newFileResource(source);
    }
}
