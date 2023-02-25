package com.chrisrobertsfl.coreengine.service.drools;

import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;

import java.io.StringReader;
import java.util.UUID;

import static org.kie.internal.io.ResourceFactory.newReaderResource;

public record StringSource(String source, String sourcePath) implements RuleSource {
    public StringSource(String source) {
        this(source, UUID.randomUUID().toString());
    }

    @Override
    public Resource resource() {
        Resource resource = newReaderResource(new StringReader(source));
        resource.setSourcePath(sourcePath);
        resource.setResourceType(ResourceType.DRL);
        return resource;
    }
}
