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
import java.util.Arrays;
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

/** @author Mark Waite */
@RunWith(Parameterized.class)
public class ElasticAxisTest {

    @ClassRule public static JenkinsRule j = new JenkinsRule();

    private final String axisName;
    private final String labelString;
    private final boolean ignoreOffline;
    private final boolean doNotExpandLabels;

    private ElasticAxis elasticAxis;

    public ElasticAxisTest(boolean ignoreOffline, boolean doNotExpandLabels) {
        this.ignoreOffline = ignoreOffline;
        this.doNotExpandLabels = doNotExpandLabels;
        String suffix = "-" + ignoreOffline + "-" + doNotExpandLabels;
        this.axisName = "axis-name" + suffix;
        this.labelString = "label-string" + suffix;
    }

    @BeforeClass
    public static void addAgents() throws Exception {
        addAgent("A");
        addAgent("B");
    }

    public static void addAgent(String agentSuffix) throws Exception {
        Label label = new LabelAtom("label-" + agentSuffix);
        DumbSlave agent = j.createOnlineSlave(label);
        assertTrue(agent.isAcceptingTasks());
        agent.setNodeName("agent-" + agentSuffix);
    }

    @Before
    public void setUp() {
        elasticAxis = new ElasticAxis(axisName, labelString, ignoreOffline, doNotExpandLabels);
    }

    @Parameterized.Parameters(name = "ignoreOffline={0},doNotExpandLabels={1}")
    public static Collection permuteTestArguments() {
        List<Object[]> arguments = new ArrayList<>();
        Boolean[][] items = {
            {Boolean.FALSE, Boolean.FALSE},
            {Boolean.FALSE, Boolean.TRUE},
            {Boolean.TRUE, Boolean.FALSE},
            {Boolean.TRUE, Boolean.TRUE}
        };
        arguments.addAll(Arrays.asList(items));
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
        assertThat(elasticAxis.rebuild(null), is(empty()));
    }

    @Test
    public void testGetValues() {
        assertThat(elasticAxis.getValues(), is(empty()));
    }

    @Test
    public void testGetValuesForController() {
        elasticAxis =
                new ElasticAxis(axisName, "master || controller", ignoreOffline, doNotExpandLabels);
        if (doNotExpandLabels) {
            assertThat(elasticAxis.getValues(), hasItem("master||controller"));
        } else {
            assertThat(elasticAxis.getValues(), hasItem("master"));
        }
    }

    @Test
    public void testGetValuesForAgentA() {
        elasticAxis = new ElasticAxis(axisName, "label-A", ignoreOffline, doNotExpandLabels);
        if (doNotExpandLabels) {
            assertThat(elasticAxis.getValues(), hasItem("label-A"));
        } else {
            assertThat(elasticAxis.getValues(), hasItem("agent-A"));
        }
    }

    @Test
    public void testGetValuesForAgentAOrAgentB() {
        elasticAxis =
                new ElasticAxis(axisName, "label-A || label-B", ignoreOffline, doNotExpandLabels);
        if (doNotExpandLabels) {
            assertThat(elasticAxis.getValues(), hasItem("label-A||label-B"));
        } else {
            assertThat(elasticAxis.getValues(), hasItems("agent-A", "agent-B"));
        }
    }
}
