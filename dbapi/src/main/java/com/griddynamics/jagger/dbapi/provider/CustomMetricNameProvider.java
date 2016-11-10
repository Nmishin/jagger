package com.griddynamics.jagger.dbapi.provider;

import com.google.common.collect.Multimap;
import com.griddynamics.jagger.dbapi.dto.MetricNameDto;
import com.griddynamics.jagger.dbapi.dto.TaskDataDto;
import com.griddynamics.jagger.dbapi.parameter.DefaultMonitoringParameters;
import com.griddynamics.jagger.dbapi.parameter.GroupKey;
import com.griddynamics.jagger.dbapi.util.CommonUtils;
import com.griddynamics.jagger.dbapi.util.DataProcessingUtil;
import com.griddynamics.jagger.dbapi.util.FetchUtil;
import com.griddynamics.jagger.util.StandardMetricsNamesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by kgribov on 4/7/14.
 */
@Component
public class CustomMetricNameProvider implements MetricNameProvider {
    private Logger log = LoggerFactory.getLogger(CustomMetricNameProvider.class);

    private EntityManager entityManager;
    private FetchUtil fetchUtil;
    private Map<GroupKey, DefaultMonitoringParameters[]> monitoringPlotGroups;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setFetchUtil(FetchUtil fetchUtil) {
        this.fetchUtil = fetchUtil;
    }

    @Resource
    public void setMonitoringPlotGroups(Map<GroupKey, DefaultMonitoringParameters[]> monitoringPlotGroups) {
        this.monitoringPlotGroups = monitoringPlotGroups;
    }

    /**
     * Fetch custom metrics names from database
     *
     * @param tests tests data
     * @return set of MetricNameDto representing name of metric
     */
    @Override
    public Set<MetricNameDto> getMetricNames(List<TaskDataDto> tests) {

        Set<MetricNameDto> metrics = new HashSet<>();

        final long temp = System.currentTimeMillis();

        metrics.addAll(getCustomMetricsNamesNewModel(tests));

        metrics.addAll(getCustomTestGroupMetricsNamesNewModel(tests));

        metrics.addAll(getCustomMetricsNamesOldModel(tests));

        log.debug("{} ms spent for fetching {} custom metrics", System.currentTimeMillis() - temp, metrics.size());

        return metrics;
    }

    @Deprecated
    private Set<MetricNameDto> getCustomMetricsNamesOldModel(List<TaskDataDto> tests) {
        Set<Long> taskIds = new HashSet<>();
        Set<String> sessionIds = new HashSet<>();
        for (TaskDataDto tdd : tests) {
            taskIds.addAll(tdd.getIds());
            sessionIds.addAll(tdd.getSessionIds());
        }

        long temp = System.currentTimeMillis();

        // check old data (before jagger 1.2.4 version)
        List<Object[]> metricNames = entityManager.createNativeQuery(
                "SELECT dre.name, selected.id FROM DiagnosticResultEntity dre JOIN "
                        + " ("
                        + "  SELECT wd.workloaddataID, td.id FROM "
                        + "    ("
                        + "     SELECT wd.id AS workloaddataID, wd.sessionId, wd.taskId FROM WorkloadData wd WHERE wd.sessionId IN (:sessionIds)"
                        + "    ) AS wd JOIN   "
                        + "    ( "
                        + "     SELECT td.taskId, td.sessionId, td.id FROM TaskData td WHERE td.id IN (:taskIds)"
                        + "    ) AS td ON wd.taskId=td.taskId AND wd.sessionId=td.sessionId"
                        + " ) AS selected ON dre.workloadData_id=selected.workloaddataID")
                .setParameter("taskIds", taskIds)
                .setParameter("sessionIds", sessionIds)
                .getResultList();

        if (metricNames.isEmpty()) {
            return Collections.emptySet();
        }

        Set<MetricNameDto> metrics = new HashSet<>(metricNames.size());

        log.debug("{} ms spent for fetching {} custom metrics", System.currentTimeMillis() - temp, metricNames.size());

        for (Object[] name : metricNames) {
            if (name == null || name[0] == null) continue;
            for (TaskDataDto td : tests) {
                if (td.getIds().contains(((BigInteger) name[1]).longValue())) {
                    MetricNameDto metric = new MetricNameDto();
                    metric.setTest(td);
                    metric.setMetricName((String) name[0]);
                    metric.setOrigin(MetricNameDto.Origin.METRIC);
                    metrics.add(metric);
                    break;
                }
            }
        }

        return metrics;

    }

    private Set<MetricNameDto> getCustomMetricsNamesNewModel(List<TaskDataDto> tests) {
        try {
            Set<Long> taskIds = CommonUtils.getTestsIds(tests);

            List<Object[]> metricDescriptionEntities = getMetricNames(taskIds);

            if (metricDescriptionEntities.isEmpty()) {
                return Collections.emptySet();
            }

            Set<MetricNameDto> metrics = new HashSet<>(metricDescriptionEntities.size());

            for (Object[] mde : metricDescriptionEntities) {
                for (TaskDataDto td : tests) {
                    if (td.getIds().contains((Long) mde[2])) {
                        String metricName = (String) mde[0];
                        MetricNameDto metricNameDto = new MetricNameDto(td, metricName, (String) mde[1], MetricNameDto.Origin.METRIC);
                        // synonyms are required for new model of standard metrics for correct back compatibility
                        metricNameDto.setMetricNameSynonyms(StandardMetricsNamesUtil.getSynonyms(metricName));
                        metrics.add(metricNameDto);
                        break;
                    }
                }
            }

            return metrics;
        } catch (PersistenceException e) {
            log.debug("Could not fetch data from MetricSummaryEntity: {}", DataProcessingUtil.getMessageFromLastCause(e));
            return Collections.emptySet();
        }
    }

    private Set<MetricNameDto> getCustomTestGroupMetricsNamesNewModel(List<TaskDataDto> tests) {
        try {
            Set<Long> taskIds = CommonUtils.getTestsIds(tests);

            Multimap<Long, Long> testGroupMap = fetchUtil.getTestGroupIdsByTestIds(taskIds);

            List<Object[]> metricDescriptionEntities = getMetricNames(testGroupMap.keySet());

            metricDescriptionEntities = CommonUtils.filterMonitoring(metricDescriptionEntities, monitoringPlotGroups);

            if (metricDescriptionEntities.isEmpty()) {
                return Collections.emptySet();
            }

            Set<MetricNameDto> metrics = new HashSet<>(metricDescriptionEntities.size());

            // add test-group metric names
            for (Object[] mde : metricDescriptionEntities) {
                for (TaskDataDto td : tests) {
                    Collection<Long> allTestsInGroup = testGroupMap.get((Long) mde[2]);
                    if (CommonUtils.containsAtLeastOne(td.getIds(), allTestsInGroup)) {
                        metrics.add(new MetricNameDto(td, (String) mde[0], (String) mde[1], MetricNameDto.Origin.TEST_GROUP_METRIC));
                    }
                }
            }

            return metrics;
        } catch (PersistenceException e) {
            log.debug("Could not fetch test-group data from MetricSummaryEntity: {}", DataProcessingUtil.getMessageFromLastCause(e));
            return Collections.emptySet();
        }
    }

    /**
     * @param taskIds TaskData ids
     * @return list of objects {MetricDescription.id, MetricDescription.dysplayName, TaskData.id}
     */
    private List<Object[]> getMetricNames(Set<Long> taskIds) {
        if (taskIds.isEmpty()) {
            return Collections.emptyList();
        }
        return entityManager.createQuery(
                "select mse.metricDescription.metricId, mse.metricDescription.displayName, mse.metricDescription.taskData.id " +
                        "from MetricSummaryEntity as mse where mse.metricDescription.taskData.id in (:taskIds)")
                .setParameter("taskIds", taskIds)
                .getResultList();
    }
}
