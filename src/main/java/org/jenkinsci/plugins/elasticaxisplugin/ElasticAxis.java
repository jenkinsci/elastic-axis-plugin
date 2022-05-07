package org.jenkinsci.plugins.elasticaxisplugin;

import hudson.Extension;
import hudson.matrix.Axis;
import hudson.matrix.AxisDescriptor;
import hudson.matrix.LabelAxis;
import hudson.matrix.MatrixBuild;
import hudson.model.AutoCompletionCandidates;
import hudson.model.Computer;
import hudson.model.Job;
import hudson.model.Node;
import hudson.model.labels.LabelExpression;
import hudson.util.FormValidation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class ElasticAxis extends LabelAxis {

    private static final Logger LOGGER = Logger.getLogger(ElasticAxis.class.getName());

    private final String label;
    private final boolean ignoreOffline;
    private final boolean dontExpandLabels;

    @DataBoundConstructor
    public ElasticAxis(
            String name, String labelString, boolean ignoreOffline, boolean dontExpandLabels) {
        super(name, computeAllNodesInLabel(labelString, dontExpandLabels));
        this.label = labelString;
        this.ignoreOffline = ignoreOffline;
        this.dontExpandLabels = dontExpandLabels;
    }

    public String getLabelString() {
        return label;
    }

    public boolean getIgnoreOffline() {
        return ignoreOffline;
    }

    public boolean getDontExpandLabels() {
        return dontExpandLabels;
    }

    @Override
    public List<String> rebuild(MatrixBuild.MatrixBuildExecution context) {
        return computeNodesInLabel(label, ignoreOffline, dontExpandLabels);
    }

    @Override
    public List<String> getValues() {
        return computeAllNodesInLabel(label, dontExpandLabels);
    }

    private static List<String> computeAllNodesInLabel(String labelName, Boolean dontExpandLabels) {
        return computeNodesInLabel(labelName, false, dontExpandLabels);
    }

    private static List<String> computeNodesInLabel(
            String labelWithNodes, boolean restrictToOnlineNodes, boolean dontExpandLabels) {
        List<String> computedNodes = new ArrayList<>();
        String[] labels = labelWithNodes.split(",");
        for (String aLabel : labels) {
            if (!dontExpandLabels) {
                for (Node node : Jenkins.get().getLabel(aLabel.trim()).getNodes()) {
                    if (shouldAddNode(restrictToOnlineNodes, node.toComputer()))
                        computedNodes.add(node.getSelfLabel().getExpression());
                }
            } else {
                Boolean onlineNodesForLabel = false;
                for (Node node : Jenkins.get().getLabel(aLabel.trim()).getNodes()) {
                    if (shouldAddNode(restrictToOnlineNodes, node.toComputer()))
                        onlineNodesForLabel = true;
                }
                if (onlineNodesForLabel)
                    computedNodes.add(Jenkins.get().getLabel(aLabel.trim()).getExpression());
            }
        }

        return Collections.unmodifiableList(computedNodes);
    }

    private static boolean shouldAddNode(boolean restrictToOnlineNodes, Computer c) {
        if (c == null) return true;
        if (!restrictToOnlineNodes) return true;

        boolean isNodeAvailable = (c.isOnline() || c.isConnecting()) && c.isAcceptingTasks();
        return isNodeAvailable;
    }

    @Extension
    public static class DescriptorImpl extends AxisDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.ElasticAxisDisplayName();
        }

        @Override
        public Axis newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new ElasticAxis(
                    formData.getString("name"),
                    formData.getString("labelString"),
                    formData.getBoolean("ignoreOffline"),
                    formData.getBoolean("dontExpandLabels"));
        }

        @RequirePOST
        public FormValidation doCheckLabelString(
                @AncestorInPath Job<?, ?> job, @QueryParameter String value) {
            job.checkPermission(hudson.model.Item.CONFIGURE);
            String[] labels = value.split(",");
            List<FormValidation> aggregatedNotOkValidations = new ArrayList<>();
            for (String oneLabel : labels) {
                FormValidation validation = LabelExpression.validate(oneLabel, job);
                if (!validation.equals(FormValidation.ok())) {
                    LOGGER.log(
                            Level.FINEST,
                            "Remembering not ok validation {1} for label {0}",
                            new Object[] {oneLabel, validation});
                    aggregatedNotOkValidations.add(validation);
                }
            }
            if (!aggregatedNotOkValidations.isEmpty()) {
                FormValidation aggregatedValidations =
                        FormValidation.aggregate(aggregatedNotOkValidations);
                LOGGER.log(
                        Level.FINEST,
                        "Returning aggregated not ok validation {1} for labels {0}",
                        new Object[] {labels, aggregatedValidations});
                return aggregatedValidations;
            }
            LOGGER.log(Level.FINEST, "Returning ok validation for labels {0}", labels);

            return FormValidation.ok();
        }

        @RequirePOST
        public AutoCompletionCandidates doAutoCompleteLabelString(@QueryParameter String value) {
            return LabelExpression.autoComplete(value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ElasticAxis that = (ElasticAxis) o;
        return Objects.equals(getName(), that.getName())
                && Objects.equals(label, that.label)
                && Objects.equals(ignoreOffline, that.ignoreOffline)
                && Objects.equals(dontExpandLabels, that.dontExpandLabels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), label, ignoreOffline, dontExpandLabels);
    }
}
