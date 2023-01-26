package com.chrisrobertsfl.coreengine.service;

import com.chrisrobertsfl.coreengine.RuleInfo;
import com.chrisrobertsfl.coreengine.TrackingAgendaEventListener;
import com.chrisrobertsfl.coreengine.model.EngineValidationException;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.definition.type.FactField;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.chrisrobertsfl.coreengine.base.Invariants.isTrueOrElseThrow;
import static com.chrisrobertsfl.coreengine.base.MoreStrings.dotsToSlashes;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EngineServiceImplTest {

    @Mock
    IdGenerator idGenerator;

    @Mock
    KnowledgeRepository knowledgeRepository;

    @Mock
    EngineRepository engineRepository;

    public static String camelCase(String input) {
        return input.chars()
                .filter(Character::isLetterOrDigit)
                .mapToObj(c -> (char) c)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        l -> {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < l.size(); i++) {
                                char c = l.get(i);
                                if (i == 0) {
                                    sb.append(Character.toLowerCase(c));
                                } else if (Character.isUpperCase(c)) {
                                    sb.append(" ");
                                    sb.append(Character.toLowerCase(c));
                                } else {
                                    sb.append(c);
                                }
                            }
                            return sb.toString();
                        }
                ));
    }

    @Test
    void configureDroolsEngine() {
        // Given:
        when(idGenerator.generate()).thenReturn("1");
        List<RuleResource> ruleResources = List.of(
                new StringResource("com.example", "hello.drl", "rule content"),
                new UrlResource("com.example", "welcome.drl", "http://welcome.chrisrobertsfl.com")
        );
        KnowledgeBase knowledgeBase = new KnowledgeBase(ruleResources);
        when(knowledgeRepository.findById("knowledge id")).thenReturn(Optional.of(knowledgeBase));
        EngineServiceImpl service = new EngineServiceImpl(idGenerator, knowledgeRepository, null);

        // When:
        DroolsEngine engine = service.configure("knowledge id");

        // Then:
        assertEquals("1", engine.id());
        assertEquals(ruleResources, engine.ruleResources());
    }

    @Test
    void facts() {
        String content = """
                package com.chrisrobertsfl.coreengine.service;
                                
                declare Shape
                    color : String
                end
                                
                rule "Change shape color to green"
                    when
                        shape : Shape()
                    then
                        shape.setColor("green");
                        update(shape);
                end
                """;
        Configuration configuration = Configuration.builder()
                .ruleResources(List.of(
                        new StringResource("com.chrisrobertsfl.coreengine.service", "Shape", content)
                ))
                .build();
        KieSession kieSession = configuration.newInstance();
        String json = """
                {
                    "color" : "red"
                }
                """;
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> objectMap = new Gson().fromJson(json, type);
        String packageName = "com.chrisrobertsfl.coreengine.service";
        String objectType = "Shape";
        FactType factType = kieSession.getKieBase().getFactType(packageName, objectType);
        System.out.println("factType = " + factType);
        Object factObject = null;
        try {
            factObject = factType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        System.out.println("factObject = " + factObject);
        for (String key : objectMap.keySet()) {
            try {
                Object value = objectMap.get(key);
                factType.set(factObject, camelCase(key), value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("factObject = " + factObject);
        kieSession.insert(factObject);
        TrackingAgendaEventListener trackingAgendaEventListener = new TrackingAgendaEventListener();
        kieSession.addEventListener(trackingAgendaEventListener);
        kieSession.fireAllRules();
        dumpRules("My rules fired",  System.out::println, trackingAgendaEventListener);
        kieSession.getObjects().forEach(System.out::println);
        String fetched = fetchObject("Shape", kieSession, "com.chrisrobertsfl.coreengine.service");
        System.out.println("fetched = " + fetched);
        }

    void dumpRules(final String header, Consumer<String> consumer, TrackingAgendaEventListener trackingAgendaEventListener) {
        List<RuleInfo> ruleInfos = trackingAgendaEventListener.getFiredRules();
        consumer.accept("");
        consumer.accept(format("%s (%d):", header, ruleInfos.size()));
        ruleInfos.stream()
                .map(ruleInfo -> format("%4d: %s", ruleInfo.position(), ruleInfo.name()))
                .forEach(consumer::accept);
    }

    public String fetchObject(String objectType, KieSession kieSession, String packageName) {
        Object factObject = kieSession.getObjects(o -> {
            String simpleName = o.getClass().getSimpleName();
            System.out.println("simpleName = " + simpleName);
            return simpleName.equals(objectType);
        }).stream().findFirst().orElse(null);
        System.out.println("factObject = " + factObject);
        if (factObject != null) {
            FactType factType = kieSession.getKieBase().getFactType(packageName, objectType);
            Map<String, Object> factMap = factType.getFields().stream().collect(Collectors.toMap(FactField::getName, factField -> {
                try {
                    return factType.get(factObject, factField.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }));
            return new Gson().toJson(factMap);
        }
        return null;
    }

    @Disabled
    @Test
    void createAndFireDroolsEngine() {

        // Given:
        String content = """
                package com.chrisrobertsfl.coreengine.service;
                                
                declare Shape
                    color : String
                end
                                
                rule "Change shape color to green"
                    when
                        shape : Shape()
                    then
                        shape.setColor("green");
                        update(shape);
                end
                """;
        DroolsEngine droolsEngine = new DroolsEngine(Configuration.builder()
                .ruleResources(List.of(
                        new StringResource("com.chrisrobertsfl.coreengine.service", "Shape", content)
                ))
                .build());
        when(engineRepository.findById("1")).thenReturn(Optional.of(droolsEngine));
        EngineServiceImpl service = new EngineServiceImpl(idGenerator, knowledgeRepository, engineRepository);

        // When:
        DroolsEngine engine = service.findByEngineId("1")
                .orElseThrow(() -> new RuntimeException("No engine found"))
                .create()
                .fire();

        // Then:
        assertEquals(new Shape("green"), engine
                .findAll(Shape.class)
                .stream()
                .findFirst()
                .orElse(null));
    }

    interface IdGenerator {
        String generate();
    }

    public sealed interface RuleResource permits StringResource, UrlResource {

        String name();

        String packageName();

        default String path() {
            String path = format("src/main/resources/%s/%s.drl", dotsToSlashes(packageName()), name());
            return path;
        }

        Optional<String> content();
    }

    public interface KnowledgeRepository {
        Optional<KnowledgeBase> findById(String knowledgeId);
    }

    public interface EngineRepository {
        Optional<DroolsEngine> findById(String engineId);
    }

    @Value
    public static class Shape {
        String color;
    }

    public record StringResource(String packageName, String name, String text) implements RuleResource {

        @Override
        public Optional<String> content() {
            return Optional.ofNullable(isNullOrEmpty(text) ? null : text);
        }
    }

    public record UrlResource(String packageName, String name, String url) implements RuleResource {
        @Override
        public Optional<String> content() {
            return Optional.ofNullable(url)
                    .map(this::fetchContent);
        }

        String fetchContent(String url) {
            try {
                return IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
            } catch (IOException e) {
                return null;
            }
        }
    }

    public static class EngineServiceImpl {

        EngineRepository engineRepository;
        KnowledgeRepository knowledgeRepository;
        IdGenerator idGenerator;

        public EngineServiceImpl(IdGenerator idGenerator, KnowledgeRepository knowledgeRepository, EngineRepository engineRepository) {
            this.idGenerator = idGenerator;
            this.knowledgeRepository = knowledgeRepository;
            this.engineRepository = engineRepository;
        }

        public DroolsEngine configure(String knowledgeId) {
            isTrueOrElseThrow(isNotBlank(knowledgeId), () -> new EngineValidationException("Knowledge base id is missing"));
            KnowledgeBase knowledgeBase = knowledgeRepository.findById(knowledgeId)
                    .orElseThrow(() -> new EngineValidationException(format("Could not find knowledge base with id %s", knowledgeId)));
            Configuration configuration = Configuration.builder()
                    .id(idGenerator.generate())
                    .ruleResources(knowledgeBase.ruleResources())
                    .build();
            return new DroolsEngine(configuration);
        }

        public Optional<DroolsEngine> findByEngineId(String engineId) {
            return engineRepository.findById(engineId);
        }
    }

    public record KnowledgeBase(List<RuleResource> ruleResources) {
    }

    public static class DroolsEngine {

        KieSession instance;

        Configuration configuration;

        public DroolsEngine(Configuration configuration) {
            this.configuration = configuration;
        }

        public String id() {
            return configuration.getId();
        }

        public List<RuleResource> ruleResources() {
            return configuration.getRuleResources();
        }

        public DroolsEngine fire() {
            instance.fireAllRules();
            return this;
        }

        public <T> Collection<T> findAll(Class<T> type) {
            Collection<?> objects = instance.getObjects();
            return objects.stream()
                    .filter(type::isInstance)
                    .map(type::cast)
                    .collect(toList());
        }

        public DroolsEngine create() {
            instance = ofNullable(instance)
                    .orElse(configuration.newInstance());
            return this;
        }
    }

    @Data
    @Builder
    static class Configuration {
        String id;
        List<RuleResource> ruleResources;

        public static KieSession create(List<RuleResource> ruleResources) {
            ruleResources.forEach(System.out::println);
            ruleResources.stream().map(RuleResource::path).forEach(System.out::println);
            KieServices services = KieServices.Factory.get();
            KieFileSystem fileSystem = services.newKieFileSystem();
            ruleResources.forEach(r -> fileSystem.write(r.path(), r.content()
                    .orElseThrow(() -> new EngineValidationException(format("Cannot retrieve content from rule resource path: %s", r.path())))));
            KieRepository repository = services.getRepository();
            repository.addKieModule(repository::getDefaultReleaseId);
            KieBuilder builder = services.newKieBuilder(fileSystem);
            builder.buildAll();
            KieModule module = builder.getKieModule();
            KieContainer container = services.newKieContainer(module.getReleaseId());
            return container.newKieSession();
        }

        public KieSession newInstance() {
            return create(ruleResources);
        }
    }

}
