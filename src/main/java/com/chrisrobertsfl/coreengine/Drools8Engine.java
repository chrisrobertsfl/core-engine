package com.chrisrobertsfl.coreengine;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResultsRow;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.chrisrobertsfl.coreengine.RuleAdder.AdderType.FILE;
import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

@Slf4j
public class Drools8Engine implements CoreEngine {
    final RuleAdder ruleAdder = new RuleAdder();
    KieSession session;
    TrackingAgendaEventListener trackingAgendaEventListener;
    String focus;

    Consumer<String> consumer;
    private List<String> ruleFiles = List.of();

    public static Drools8EngineBuilder builder() {
        return new Drools8EngineBuilder();
    }

    @Override
    public Drools8Engine init(String... moreRuleFiles) {
        this.ruleFiles.addAll(List.of(moreRuleFiles));
        ruleAdder.add(FILE, ruleFiles);
        session = SessionCreator.create(ruleAdder.getResources());
        trackingAgendaEventListener = new TrackingAgendaEventListener();
        session.addEventListener(trackingAgendaEventListener);
        return this;
    }

    public boolean exists(final Object fact) {
        return findAll().stream().anyMatch(f -> Objects.equals(f, fact));
    }

    @Override
    public <T> Collection<T> findAll(Class<T> type) {
        Collection<?> objects = session.getObjects();
        return objects.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(toList());
    }

    public <T> boolean existsOne(Class<T> type) {
        return findAll(type).stream().count() == 1;
    }

    public Drools8Engine dumpRules(final String header) {
        return dumpRules(header, consumer);
    }

    Drools8Engine dumpRules(final String header, Consumer<String> consumer) {
        List<RuleInfo> ruleInfos = firedRules();
        consumer.accept(format("%s (%d):", header, ruleInfos.size()));
        ruleInfos.stream()
                .map(ruleInfo -> format("%4d: %s", ruleInfo.position(), ruleInfo.name()))
                .forEach(consumer::accept);
        return this;
    }

    public List<RuleInfo> firedRules() {
        return trackingAgendaEventListener.getFiredRules();
    }

    @Override
    public Drools8Engine dumpRules() {
        return dumpRules("Fired Rules");
    }

    public Collection<?> findAll() {
        return findAll(Object.class);
    }

    @Override
    public Drools8Engine insert(final Object fact) {
        session.insert(fact);
        return this;
    }

    @Override
    public Drools8Engine insertAll(List<?> facts) {
        facts.forEach(this::insert);
        return this;
    }

    @Override
    public Drools8Engine run() {
        consumer.accept(format("focus ---> %s", focus));
        session.getAgenda().getAgendaGroup(focus);
        session.fireAllRules();
        return this;
    }

    @Override
    public Drools8Engine reset() {
        session.dispose();
        return this;
    }

    @Override
    public void destroy() {
        if (session != null) session.dispose();
    }

    @Override
    public <T> Stream<T> query(final String name, final Class<T> as) {
        return stream(getQueryResultsRowSpliterator(name), false)
                .map(row -> row.get(uncapitalize(as.getSimpleName())))
                .map(as::cast);
    }

    Spliterator<QueryResultsRow> getQueryResultsRowSpliterator(final String name) {
        return spliteratorUnknownSize(session.getQueryResults(name).iterator(), ORDERED);
    }

    public static class Drools8EngineBuilder {
        static final Consumer<String> DEFAULT_CONSUMER = System.out::println;

        static final String DEFAULT_FOCUS = "MAIN";
        List<String> ruleFiles = new ArrayList<>();
        private Consumer<String> consumer;
        private String focus;

        public Drools8Engine build() {
            Drools8Engine instance = new Drools8Engine();
            instance.consumer = firstNonNull(consumer, DEFAULT_CONSUMER);
            instance.focus = firstNonNull(focus, DEFAULT_FOCUS);
            instance.ruleFiles = ruleFiles;
            return instance;
        }

        public Drools8EngineBuilder focus(String focus) {
            this.focus = focus;
            return this;
        }

        public Drools8EngineBuilder consumer(Consumer<String> consumer) {
            this.consumer = consumer;
            return this;
        }

        public Drools8EngineBuilder ruleFiles(String... ruleFiles) {
            this.ruleFiles.addAll(isNull(ruleFiles) || ruleFiles.length == 0 ? List.of() : Arrays.asList(ruleFiles));
            return this;
        }
    }


}
