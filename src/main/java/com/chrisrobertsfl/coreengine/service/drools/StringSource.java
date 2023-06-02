package com.chrisrobertsfl.coreengine.service.drools;

import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;

import java.io.StringReader;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.kie.api.io.ResourceType.DRL;
import static org.kie.internal.io.ResourceFactory.newReaderResource;

public record StringSource(String source, String sourcePath) implements RuleSource {
    public StringSource(String source) {
        this(source,randomUUID().toString());
    }

    @Override
    public Resource resource() {
        Resource resource = newReaderResource(new StringReader(source));
        resource.setSourcePath(sourcePath);
        resource.setResourceType(DRL);
        return resource;
    }
}
