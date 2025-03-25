package org.jenkinsci.plugins.elasticaxisplugin;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.Label;
import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import java.util.ArrayList;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * @author Mark Waite
 */
@WithJenkins
class ElasticAxisTest {

    private static JenkinsRule j;

    private static final String AGENT_PREFIX = "agent-";
    private static final String LABEL_PREFIX = "label-";
    private static final String DOES_NOT_EXIST_SUFFIX = "does-not-exist";

    private static final String[] LABEL_SUFFIXES = {"A", "B"};

    @BeforeAll
    static void setUp(JenkinsRule rule) throws Exception {
        j = rule;

        // Add agents with test labels
        for (String labelSuffix : LABEL_SUFFIXES) {
            addAgent(labelSuffix);
        }
    }

    private static void addAgent(String agentSuffix) throws Exception {
        Label label = new LabelAtom(LABEL_PREFIX + agentSuffix);
        DumbSlave agent = j.createOnlineSlave(label);
        assertTrue(agent.isAcceptingTasks());
        agent.setNodeName(AGENT_PREFIX + agentSuffix);
    }

    private static ElasticAxis getElasticAxis(String labelString, boolean ignoreOffline, boolean doNotExpandLabels) {
        String axisName = "axis-name" + "-" + ignoreOffline + "-" + doNotExpandLabels;
        return new ElasticAxis(axisName, labelString, ignoreOffline, doNotExpandLabels);
    }

    static List<Object[]> parameters() {
        List<Object[]> arguments = new ArrayList<>();
        Boolean[] possibleValues = {Boolean.TRUE, Boolean.FALSE};
        for (Boolean ignoreOffline : possibleValues) {
            for (Boolean doNotExpandLabels : possibleValues) {
                for (String labelSuffix : LABEL_SUFFIXES) {
                    // Test for known agents
                    Object[] argument = {AGENT_PREFIX + labelSuffix, ignoreOffline, doNotExpandLabels};
                    arguments.add(argument);
                }
                // Test that a non-existing agent matches nothing
                Object[] argument = {AGENT_PREFIX + DOES_NOT_EXIST_SUFFIX, ignoreOffline, doNotExpandLabels};
                arguments.add(argument);
            }
        }
        return arguments;
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(ElasticAxis.class)
                .usingGetClass()
                .withIgnoredFields("values")
                .verify();
    }

    @ParameterizedTest(name = "ignoreOffline={1},doNotExpandLabels={2}")
    @MethodSource("parameters")
    void testGetLabelString(String labelString, boolean ignoreOffline, boolean doNotExpandLabels) {
        ElasticAxis elasticAxis = getElasticAxis(labelString, ignoreOffline, doNotExpandLabels);
        assertThat(elasticAxis.getLabelString(), is(labelString));
    }

    @ParameterizedTest(name = "ignoreOffline={1},doNotExpandLabels={2}")
    @MethodSource("parameters")
    void testGetIgnoreOffline(String labelString, boolean ignoreOffline, boolean doNotExpandLabels) {
        ElasticAxis elasticAxis = getElasticAxis(labelString, ignoreOffline, doNotExpandLabels);
        assertThat(elasticAxis.getIgnoreOffline(), is(ignoreOffline));
    }

    @ParameterizedTest(name = "ignoreOffline={1},doNotExpandLabels={2}")
    @MethodSource("parameters")
    void testGetDontExpandLabels(String labelString, boolean ignoreOffline, boolean doNotExpandLabels) {
        ElasticAxis elasticAxis = getElasticAxis(labelString, ignoreOffline, doNotExpandLabels);
        assertThat(elasticAxis.getDontExpandLabels(), is(doNotExpandLabels));
    }

    @ParameterizedTest(name = "ignoreOffline={1},doNotExpandLabels={2}")
    @MethodSource("parameters")
    void testRebuild(String labelString, boolean ignoreOffline, boolean doNotExpandLabels) {
        ElasticAxis elasticAxis = getElasticAxis(labelString, ignoreOffline, doNotExpandLabels);
        if (labelString.contains(DOES_NOT_EXIST_SUFFIX)) {
            assertThat(elasticAxis.rebuild(null), is(empty()));
        } else {
            String agentName = labelString.replace(LABEL_PREFIX, AGENT_PREFIX);
            assertThat(elasticAxis.rebuild(null), hasItem(agentName));
        }
    }

    @ParameterizedTest(name = "ignoreOffline={1},doNotExpandLabels={2}")
    @MethodSource("parameters")
    void testGetValues(String labelString, boolean ignoreOffline, boolean doNotExpandLabels) {
        ElasticAxis elasticAxis = getElasticAxis(labelString, ignoreOffline, doNotExpandLabels);
        if (labelString.contains(DOES_NOT_EXIST_SUFFIX)) {
            assertThat(elasticAxis.getValues(), is(empty()));
        } else {
            String agentName = labelString.replace(LABEL_PREFIX, AGENT_PREFIX);
            assertThat(elasticAxis.getValues(), hasItem(agentName));
        }
    }

    @ParameterizedTest(name = "ignoreOffline={1},doNotExpandLabels={2}")
    @MethodSource("parameters")
    void testGetValuesForController(String labelString, boolean ignoreOffline, boolean doNotExpandLabels) {
        String expectedLabel = j.jenkins.getSelfLabel().getName();
        String nonexistentLabel = "no-such-label";
        String axisName = "axis-name" + "-" + ignoreOffline + "-" + doNotExpandLabels;
        ElasticAxis elasticAxis =
                new ElasticAxis(axisName, expectedLabel + " || " + nonexistentLabel, ignoreOffline, doNotExpandLabels);
        if (doNotExpandLabels) {
            assertThat(elasticAxis.getValues(), hasItem(expectedLabel + "||" + nonexistentLabel));
        } else {
            assertThat(elasticAxis.getValues(), hasItem(expectedLabel));
        }
    }

    @ParameterizedTest(name = "ignoreOffline={1},doNotExpandLabels={2}")
    @MethodSource("parameters")
    void testGetValuesForAgentAOrAgentB(String labelString, boolean ignoreOffline, boolean doNotExpandLabels) {
        String axisName = "axis-name" + "-" + ignoreOffline + "-" + doNotExpandLabels;
        ElasticAxis elasticAxis = new ElasticAxis(axisName, "label-A || label-B", ignoreOffline, doNotExpandLabels);
        if (doNotExpandLabels) {
            assertThat(elasticAxis.getValues(), hasItem("label-A||label-B"));
        } else {
            assertThat(elasticAxis.getValues(), hasItems("agent-A", "agent-B"));
        }
    }
}
