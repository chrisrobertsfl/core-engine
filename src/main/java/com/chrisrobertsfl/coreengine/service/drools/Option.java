package com.chrisrobertsfl.coreengine.service.drools;

import com.chrisrobertsfl.coreengine.RuleInfo;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.String.format;

public enum Option {
    SHOW_RULES(Option::showRulesConsumer), SHOW_FACTS(Option::showFactsConsumer);

    private final Consumer<DroolsEngine> engineConsumer;

    Option(Consumer<DroolsEngine> engineConsumer) {
        this.engineConsumer = engineConsumer;
    }

    static void showFactsConsumer(DroolsEngine droolsEngine) {
        Collection<?> all = droolsEngine.findAll().stream().toList();
        DroolsEngine.log.debug("Working Memory ({}):", all.size());
        all.stream()
                .forEach(o -> DroolsEngine.log.debug(String.format("  (%s) -> %s", o.getClass(), o)));
        DroolsEngine.log.debug("");
    }

    static void showRulesConsumer(DroolsEngine droolsEngine) {
        List<RuleInfo> ruleInfos = droolsEngine.firedRules();
        DroolsEngine.log.debug(format("Fired Rules (%d):", ruleInfos.size()));
        ruleInfos.stream()
                .map(ruleInfo -> format("%4d: %s", ruleInfo.position(), ruleInfo.name()))
                .forEach(DroolsEngine.log::debug);
        DroolsEngine.log.debug("");
    }

    public void execute(DroolsEngine droolsEngine) {
        engineConsumer.accept(droolsEngine);
    }
}
