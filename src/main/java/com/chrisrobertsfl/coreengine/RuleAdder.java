package com.chrisrobertsfl.coreengine;

import org.kie.api.io.Resource;
import org.kie.internal.io.ResourceFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.kie.internal.io.ResourceFactory.newClassPathResource;
import static org.kie.internal.io.ResourceFactory.newReaderResource;

public class RuleAdder {

    List<Resource> resources = new ArrayList<>();

    public void add(AdderType adderType, List<String> items) {
        items.parallelStream().filter(adderType.predicate)
                .map(adderType.mapper)
                .forEach(resources::add);
    }

    public List<Resource> getResources() {
        return resources;
    }

    public enum AdderType {
        FILE(s -> s != null, s -> ResourceFactory.newFileResource(s)),
        STRING(s -> s != null, s -> newReaderResource(new StringReader(s))),
        RESOURCE(s -> s != null && !s.isEmpty(), s -> newClassPathResource(s));

        private final Predicate<String> predicate;
        private final Function<String, Resource> mapper;

        AdderType(Predicate<String> predicate, Function<String, Resource> mapper) {
            this.predicate = predicate;
            this.mapper = mapper;
        }
    }

}
