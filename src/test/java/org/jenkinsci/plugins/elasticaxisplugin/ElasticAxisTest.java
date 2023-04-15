package org.jenkinsci.plugins.elasticaxisplugin;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertTrue;

import hudson.model.Label;
import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * @author Mark Waite
 */
@RunWith(Parameterized.class)
public class ElasticAxisTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private final String axisName;
    private final String labelString;
    private final boolean ignoreOffline;
    private final boolean doNotExpandLabels;

    private ElasticAxis elasticAxis;

    private static final String AGENT_PREFIX = "agent-";
    private static final String LABEL_PREFIX = "label-";
    private static final String DOES_NOT_EXIST_SUFFIX = "does-not-exist";

    private static final String[] LABEL_SUFFIXES = {"A", "B"};

    public ElasticAxisTest(String labelString, boolean ignoreOffline, boolean doNotExpandLabels) {
        this.ignoreOffline = ignoreOffline;
        this.doNotExpandLabels = doNotExpandLabels;
        String suffix = "-" + ignoreOffline + "-" + doNotExpandLabels;
        this.axisName = "axis-name" + suffix;
        this.labelString = labelString;
    }

    @BeforeClass
    public static void addAgents() throws Exception {
        // Add agents with test labels
        for (String labelSuffix : LABEL_SUFFIXES) {
            addAgent(labelSuffix);
        }
    }

    public static void addAgent(String agentSuffix) throws Exception {
        Label label = new LabelAtom(LABEL_PREFIX + agentSuffix);
        DumbSlave agent = j.createOnlineSlave(label);
        assertTrue(agent.isAcceptingTasks());
        agent.setNodeName(AGENT_PREFIX + agentSuffix);
    }

    @Before
    public void setUp() {
        elasticAxis = new ElasticAxis(axisName, labelString, ignoreOffline, doNotExpandLabels);
    }

    @Parameterized.Parameters(name = "ignoreOffline={0},doNotExpandLabels={1}")
    public static Collection permuteTestArguments() {
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
    public void testEquals() {
        EqualsVerifier.forClass(ElasticAxis.class)
                .usingGetClass()
                .withIgnoredFields("values")
                .verify();
    }

    @Test
    public void testGetLabelString() {
        assertThat(elasticAxis.getLabelString(), is(labelString));
    }

    @Test
    public void testGetIgnoreOffline() {
        assertThat(elasticAxis.getIgnoreOffline(), is(ignoreOffline));
    }

    @Test
    public void testGetDontExpandLabels() {
        assertThat(elasticAxis.getDontExpandLabels(), is(doNotExpandLabels));
    }

    @Test
    public void testRebuild() {
        if (labelString.contains(DOES_NOT_EXIST_SUFFIX)) {
            assertThat(elasticAxis.rebuild(null), is(empty()));
        } else {
            String agentName = labelString.replace(LABEL_PREFIX, AGENT_PREFIX);
            assertThat(elasticAxis.rebuild(null), hasItem(agentName));
        }
    }

    @Test
    public void testGetValues() {
        if (labelString.contains(DOES_NOT_EXIST_SUFFIX)) {
            assertThat(elasticAxis.getValues(), is(empty()));
        } else {
            String agentName = labelString.replace(LABEL_PREFIX, AGENT_PREFIX);
            assertThat(elasticAxis.getValues(), hasItem(agentName));
        }
    }

    @Test
    public void testGetValuesForController() {
        String expectedLabel = j.jenkins.getSelfLabel().getName();
        String nonexistentLabel = "no-such-label";
        elasticAxis =
                new ElasticAxis(axisName, expectedLabel + " || " + nonexistentLabel, ignoreOffline, doNotExpandLabels);
        if (doNotExpandLabels) {
            assertThat(elasticAxis.getValues(), hasItem(expectedLabel + "||" + nonexistentLabel));
        } else {
            assertThat(elasticAxis.getValues(), hasItem(expectedLabel));
        }
    }

    @Test
    public void testGetValuesForAgentAOrAgentB() {
        elasticAxis = new ElasticAxis(axisName, "label-A || label-B", ignoreOffline, doNotExpandLabels);
        if (doNotExpandLabels) {
            assertThat(elasticAxis.getValues(), hasItem("label-A||label-B"));
        } else {
            assertThat(elasticAxis.getValues(), hasItems("agent-A", "agent-B"));
        }
    }
}
