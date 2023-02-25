package com.chrisrobertsfl.coreengine.service.drools;

import com.chrisrobertsfl.coreengine.TrackingAgendaEventListener;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

public class Assembler {
    Set<Option> options = new LinkedHashSet<>();
    List<RuleSource> ruleSources = new ArrayList<>();
    List<Object> insertions = new ArrayList<>();

    public Assembler option(Option option) {
        options.add(option);
        return this;
    }

    public Assembler rule(RuleSource ruleSource) {
        ruleSources.add(ruleSource);
        return this;
    }

    public Assembler rules(RuleSource... ruleSources) {
        this.ruleSources.addAll(isNull(ruleSources) ? emptyList() : List.of(ruleSources));
        return this;
    }

    public Assembler insert(Object insertion) {
        insertions.add(insertion);
        return this;
    }

    public Assembler insertAll(List<?> insertions) {
        this.insertions.addAll(insertions);
        return this;
    }

    public Assembler insertAll(Object... insertions) {
        return insertAll(isNull(insertions) ? emptyList() : List.of(insertions));
    }

    public DroolsEngine run() {
        return init().run();
    }

    public DroolsEngine init() {
        DroolsEngine instance = new DroolsEngine();
        instance.ruleSources = ruleSources;
        instance.insertions = insertions;
        instance.trackingAgendaEventListener = new TrackingAgendaEventListener();
        instance.options = options;
        return instance.init();
    }

}
