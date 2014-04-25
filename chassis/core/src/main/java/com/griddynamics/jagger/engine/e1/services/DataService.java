package com.griddynamics.jagger.engine.e1.services;

import com.griddynamics.jagger.engine.e1.services.data.service.MetricEntity;
import com.griddynamics.jagger.engine.e1.services.data.service.MetricValueEntity;
import com.griddynamics.jagger.engine.e1.services.data.service.SessionEntity;
import com.griddynamics.jagger.engine.e1.services.data.service.TestEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Provides access to tests results, stored in jagger database. Data contains a full information about sessions, tests, metrics and etc.
 * @author Gribov Kirill
 * @n
 *
 * */
public interface DataService extends JaggerService {

    /** Returns session's entity for specify session's id
     * @author Gribov Kirill
     * @n
     * @param sessionId - session's id
     * @return session's entity*/
    SessionEntity getSession(String sessionId);

    /** Returns session's entities for specify session's ids
     * @author Gribov Kirill
     * @n
     * @param sessionIds - session's ids
     * @return list of session's entities */
    Set<SessionEntity> getSessions(Set<String> sessionIds);

    /** Returns tests for specify session
     * @author Gribov Kirill
     * @n
     * @param session - session entity
     * @return list of test entities */
    Set<TestEntity> getTests(SessionEntity session);

    /** Returns tests for specify session's id
     * @author Gribov Kirill
     * @n
     * @param sessionId - session's id
     * @return list of test entities */
    Set<TestEntity> getTests(String sessionId);

    /** Returns all tests for specify list of session's ids
     * @author Gribov Kirill
     * @n
     * @param sessionIds - list of session's ids
     * @return map of <session id, list of test entities> pairs*/
    Map<String, Set<TestEntity>> getTests(Set<String> sessionIds);

    //??? why no getTests for list of SessionEntity

    /** Returns test entity for specify session's id and test name
     * @author Gribov Kirill
     * @n
     * @param sessionId - session's id
     * @param testName - name of test
     * @return test entity */
    TestEntity getTestByName(String sessionId, String testName);

    /** Returns test entity for specify session and test name
     * @author Gribov Kirill
     * @n
     * @param session - session entity
     * @param testName - name of test
     * @return test entity */
    TestEntity getTestByName(SessionEntity session, String testName);

    /** Returns map, where key is session's id and value is test entity with specify name
     * @author Gribov Kirill
     * @n
     * @param sessionIds - list of session's ids
     * @param testName - name of test
     * @return map of <session id, test entity> pairs*/
    Map<String, TestEntity> getTestsByName(Set<String> sessionIds, String testName);

    /** Returns all metric entities for specify test id
     * @author Gribov Kirill
     * @n
     * @param testId - test id
     * @return list of metric entities*/
    Set<MetricEntity> getMetrics(Long testId);

    /** Returns all metric entities for specify test
     * @author Gribov Kirill
     * @n
     * @param test - test entity
     * @return list of metric entities*/
    Set<MetricEntity> getMetrics(TestEntity test);

    /** Returns map, where key is test entity and value is a list of all test metrics
     * @author Gribov Kirill
     * @n
     * @param tests - list of tests
     * @return map of <test entity, list of metric entity> pairs*/
    Map<TestEntity, Set<MetricEntity>> getMetricsByTests(Set<TestEntity> tests);

    /** Returns map, where key is test id and value is a list of all test metrics
     * @author Gribov Kirill
     * @n
     * @param testIds - list of tests ids
     * @return map of <test id, list of metric entity> pairs*/
    Map<Long, Set<MetricEntity>> getMetricsByTestIds(Set<Long> testIds);

    //??? Set vs List



    /** Returns all metric values for specify test id and metric id
     * @author Gribov Kirill
     * @n
     * @param testId - test id
     * @param metricId - metric id
     * @return list of metric values*/
    List<MetricValueEntity> getMetricValues(Long testId, String metricId);

    /** Returns all metric values for specify test id and metric entity
     * @author Gribov Kirill
     * @n
     * @param testId - test id
     * @param metric - metric entity
     * @return list of metric values*/
    List<MetricValueEntity> getMetricValues(Long testId, MetricEntity metric);

    /** Returns all metric values for specify test and metric entity
     * @author Gribov Kirill
     * @n
     * @param test - test entity
     * @param metric - metric entity
     * @return list of metric values*/
    List<MetricValueEntity> getMetricValues(TestEntity test, MetricEntity metric);

    /** Search for metric values for specify test id and list of metric ids
     * @author Gribov Kirill
     * @n
     * @param testId - id of test
     * @param metricIds - a list of metric ids
     * @return map of <metric id, list of metric values> pairs*/
    Map<String, List<MetricValueEntity>> getMetricValuesByIds(Long testId, List<String> metricIds);

    /** Search for metric values for specify test and list of metrics
     * @author Gribov Kirill
     * @n
     * @param test - test entity
     * @param metrics - a list of metric entities
     * @return map of <metric, list of metric values> pairs*/
    Map<MetricEntity, List<MetricValueEntity>> getMetricValues(TestEntity test, List<MetricEntity> metrics);

    /** Search for metric values for specify test id and list of metrics
     * @author Gribov Kirill
     * @n
     * @param testId - test id
     * @param metrics - a list of metric entities
     * @return map of <metric, list of metric values> pairs*/
    Map<MetricEntity, List<MetricValueEntity>> getMetricValues(Long testId, List<MetricEntity> metrics);
}
