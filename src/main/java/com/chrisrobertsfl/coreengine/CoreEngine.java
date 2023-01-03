package com.chrisrobertsfl.coreengine;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface CoreEngine {

    CoreEngine init();

    boolean exists(Object fact);

    <T> Collection<T> findAll(Class<T> type);

    CoreEngine dumpRules();

    CoreEngine insert(Object fact);

    CoreEngine run();

    void destroy();

    <T> Stream<T> query(String name, Class<T> as);

    CoreEngine insertAll(List<?> facts);
}


