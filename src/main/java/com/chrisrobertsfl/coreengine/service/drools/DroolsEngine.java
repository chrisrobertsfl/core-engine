package com.chrisrobertsfl.coreengine.service.drools;

import com.chrisrobertsfl.coreengine.RuleInfo;
import com.chrisrobertsfl.coreengine.SessionCreator;
import com.chrisrobertsfl.coreengine.TrackingAgendaEventListener;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class DroolsEngine {

    public static final Logger log = LoggerFactory.getLogger(DroolsEngine.class);

    Set<Option> options;
    List<RuleSource> ruleSources;
    List<?> insertions;
    KieSession session;
    TrackingAgendaEventListener trackingAgendaEventListener;

    public static Assembler assemble() {
        return new Assembler();
    }

    public Collection<?> findAll() {
        return findAll(Object.class);
    }

    public <T> Collection<T> findAll(Class<T> type) {
        Collection<?> objects = session.getObjects();
        return objects.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(toList());
    }

    public KieSession session() {
        return session;
    }

    public DroolsEngine init() {
        List<Resource> resources = ruleSources.stream()
                .map(RuleSource::resource)
                .collect(Collectors.toList());
        session = SessionCreator.create(resources);
        session.addEventListener(trackingAgendaEventListener);
        insertions.forEach(insertion -> session.insert(insertion));
        return this;
    }

    public DroolsEngine run() {
        session.fireAllRules();
        options.forEach(option -> option.execute(this));
        return this;
    }

    public List<RuleInfo> firedRules() {
        return trackingAgendaEventListener.getFiredRules();
    }

}
