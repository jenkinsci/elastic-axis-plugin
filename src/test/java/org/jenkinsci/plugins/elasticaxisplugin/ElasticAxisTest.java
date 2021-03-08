package org.jenkinsci.plugins.elasticaxisplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.Test;

import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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

    public ElasticAxisTest(boolean ignoreOffline, boolean doNotExpandLabels) {
        this.ignoreOffline = ignoreOffline;
        this.doNotExpandLabels = doNotExpandLabels;
        String suffix = "-" + ignoreOffline + "-" + doNotExpandLabels;
        this.axisName = "axis-name" + suffix;
        this.labelString = "label-string" + suffix;
    }

    @Before
    public void setUp() {
        elasticAxis = new ElasticAxis(axisName, labelString, ignoreOffline, doNotExpandLabels);
    }

    @Parameterized.Parameters(name = "ignoreOffline={0},doNotExpandLabels={1}")
    public static Collection permuteTestArguments() {
        List<Object[]> arguments = new ArrayList<>();
        Boolean[] itemFF = {Boolean.FALSE, Boolean.FALSE};
        arguments.add(itemFF);
        Boolean[] itemFT = {Boolean.FALSE, Boolean.TRUE};
        arguments.add(itemFT);
        Boolean[] itemTF = {Boolean.TRUE, Boolean.FALSE};
        arguments.add(itemTF);
        Boolean[] itemTT = {Boolean.TRUE, Boolean.TRUE};
        arguments.add(itemTT);
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
        elasticAxis = new ElasticAxis(axisName, "master || controller", ignoreOffline, doNotExpandLabels);
        assertThat(elasticAxis.getValues(), hasItem(doNotExpandLabels ? "master||controller" : "master"));
    }
}
