package com.chrisrobertsfl.coreengine;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.drools.compiler.compiler.JavaDialectConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.util.List;

public class SessionCreator {
    public static KieSession create(List<Resource> resources) {
        System.out.println("resources = " + resources);
        System.out.println("resources.get(0) = " + resources.get(0));
        System.out.println("resources.get(0) = " + ReflectionToStringBuilder.toString(resources.get(0), ToStringStyle.MULTI_LINE_STYLE));

        KieServices services = KieServices.Factory.get();
        KieFileSystem fileSystem = services.newKieFileSystem();
        resources.forEach(fileSystem::write);
        KieRepository repository = services.getRepository();
        repository.addKieModule(repository::getDefaultReleaseId);
        KieBuilder builder = services.newKieBuilder(fileSystem);

        builder.buildAll();
        KieModule module = builder.getKieModule();
        KieContainer container = services.newKieContainer(module.getReleaseId());
        return container.newKieSession();
    }

    //new JavaDialectConfiguration()
    //    .setCompiler("NATIVE")

}
