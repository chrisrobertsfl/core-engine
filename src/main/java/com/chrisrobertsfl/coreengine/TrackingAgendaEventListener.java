package com.chrisrobertsfl.coreengine;


import org.kie.api.definition.rule.Rule;
import org.kie.api.event.rule.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class TrackingAgendaEventListener implements AgendaEventListener {

    private final Logger logger = LoggerFactory.getLogger(TrackingAgendaEventListener.class);


    Integer rulesFired = 0;

     List<RuleInfo> firedRules = new ArrayList<>();

    @Override
    public void matchCreated(MatchCreatedEvent event) {
        // TODO Auto-generated method stub
    }

    @Override
    public void matchCancelled(MatchCancelledEvent event) {
        // TODO Auto-generated method stub
    }

    @Override
    public void beforeMatchFired(BeforeMatchFiredEvent event) {
        // TODO Auto-generated method stub
    }

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        Rule rule = event.getMatch().getRule();
        RuleInfo ruleInfo = new RuleInfo(rulesFired, rule.getName(), (String) rule.getMetaData().get("name"));
        firedRules.add(ruleInfo);
        rulesFired += 1;
    }

    @Override
    public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
        // TODO Auto-generated method stub
    }

    @Override
    public void agendaGroupPushed(AgendaGroupPushedEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
        // TODO Auto-generated method stub

    }

    public List<RuleInfo> getFiredRules() {
        return firedRules;
    }
}