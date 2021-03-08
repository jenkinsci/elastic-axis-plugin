package org.jenkinsci.plugins.elasticaxisplugin;

import java.util.Random;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Mark Waite
 */
public class ElasticAxisTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private static final Random RANDOM = new Random();
    private static final boolean IGNORE_OFFLINE = RANDOM.nextBoolean();
    private static final boolean DO_NOT_EXPAND_LABELS = RANDOM.nextBoolean();
    private static final String NAME_SUFFIX = "-" + (100 + RANDOM.nextInt(899)); // 3 digit random number
    private static final String AXIS_NAME = "axis-name" + NAME_SUFFIX;
    private static final String LABEL_STRING = "label-string" + NAME_SUFFIX;

    private ElasticAxis elasticAxis;

    public ElasticAxisTest() {
    }

    @Before
    public void setUp() {
        elasticAxis = new ElasticAxis(AXIS_NAME, LABEL_STRING, IGNORE_OFFLINE, DO_NOT_EXPAND_LABELS);
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
        assertThat(elasticAxis.getLabelString(), is(LABEL_STRING));
    }

    @Test
    public void testGetIgnoreOffline() {
        assertThat(elasticAxis.getIgnoreOffline(), is(IGNORE_OFFLINE));
    }

    @Test
    public void testGetDontExpandLabels() {
        assertThat(elasticAxis.getDontExpandLabels(), is(DO_NOT_EXPAND_LABELS));
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
        elasticAxis = new ElasticAxis(AXIS_NAME, "master", IGNORE_OFFLINE, DO_NOT_EXPAND_LABELS);
        assertThat(elasticAxis.getValues(), hasItem("master"));
    }
}
