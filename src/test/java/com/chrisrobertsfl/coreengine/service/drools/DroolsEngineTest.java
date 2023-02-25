package com.chrisrobertsfl.coreengine.service.drools;

import com.chrisrobertsfl.coreengine.RuleInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.function.Predicate;

import static com.chrisrobertsfl.coreengine.service.drools.DroolsEngine.assemble;
import static com.chrisrobertsfl.coreengine.service.drools.Option.SHOW_FACTS;
import static com.chrisrobertsfl.coreengine.service.drools.Option.SHOW_RULES;
import static com.chrisrobertsfl.coreengine.service.drools.RuleSource.*;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class DroolsEngineTest {

    public static final String FILE = "src/test/resources/drl/file.drl";
    public static final String STRING = """
            rule "String resource"
                when
                            
                then
                    insert("String resource works");
            end
            """;
    public static final String CLASSPATH = "com/chrisrobertsfl/notation/service/classpath.drl";
    DroolsEngine engine;

    void assertRuleFired(int position, String name) {
        Predicate<RuleInfo> firedRulePredicate = ruleInfo -> ruleInfo.position() == position && ruleInfo.name().equals(name);
        assertTrue(engine.firedRules().stream().anyMatch(firedRulePredicate), () -> format("Should have fired rule '%s' in this position -> %d", name, position));
    }

    @Test
    void parse() {

        // Given:
        engine = assemble()
                .option(SHOW_RULES)
                .option(SHOW_FACTS)
                .rule(file(FILE))
                .rule(string(STRING))
                .rule(classpath(CLASSPATH))
                .insert("xyz")
                .insertAll("abc")
                .init();

        // When:
        engine.run();

        // Then:
        Collection<String> facts = engine.findAll(String.class);
        assertAll(
//                () -> assertRuleFired(0, "Classpath resource"),
//                () -> assertRuleFired(1, "String resource"),
//                () -> assertRuleFired(2, "File resource"),
                () -> assertTrue(facts.contains("xyz")),
                () -> assertTrue(facts.contains("abc")),
                () -> assertTrue(facts.contains("File resource works")),
                () -> assertTrue(facts.contains("String resource works")),
                () -> assertTrue(facts.contains("Classpath resource works"))
        );

    }
}
