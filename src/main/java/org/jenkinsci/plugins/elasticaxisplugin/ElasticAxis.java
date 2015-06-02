package org.jenkinsci.plugins.elasticaxisplugin;

import hudson.Extension;
import hudson.Util;
import hudson.matrix.Axis;
import hudson.matrix.AxisDescriptor;
import hudson.matrix.LabelAxis;
import hudson.matrix.MatrixBuild;
import hudson.model.AutoCompletionCandidates;
import hudson.model.Messages;
import hudson.model.Computer;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.util.FormValidation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import antlr.ANTLRException;

public class ElasticAxis extends LabelAxis {

	private String label;
	private boolean ignoreOffline;
        private boolean dontExpandLabels;

    @DataBoundConstructor
    public ElasticAxis(String name, String labelString, 
            boolean ignoreOffline, boolean dontExpandLabels) {
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
    
	private static List<String> computeNodesInLabel(String labelWithNodes, boolean restrictToOnlineNodes, boolean dontExpandLabels) {
		List<String> computedNodes=new ArrayList<String>();
		String[] labels = labelWithNodes.split(",");
		for (String aLabel : labels) {
                    if(!dontExpandLabels){
			for(Node node : Jenkins.getInstance().getLabel(aLabel.trim()).getNodes()) {
				if (shouldAddNode(restrictToOnlineNodes, node.toComputer())) 
					computedNodes.add(node.getSelfLabel().getExpression());
			}
                    } else {
                        Boolean onlineNodesForLabel = false;
                        for(Node node : Jenkins.getInstance().getLabel(aLabel.trim()).getNodes()) {
				if (shouldAddNode(restrictToOnlineNodes, node.toComputer())) 
					onlineNodesForLabel = true;
			}
                        if(onlineNodesForLabel)
                            computedNodes.add(Jenkins.getInstance().getLabel(aLabel.trim()).getExpression());
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
            return "ElasticAxis";
        }

        @Override
        public Axis newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new ElasticAxis(
                    formData.getString("name"),
                    formData.getString("labelString"),
                    formData.getBoolean("ignoreOffline"),
                    formData.getBoolean("dontExpandLabels")
            );
        }
        
        public FormValidation doCheckLabelString(@QueryParameter String value) {
        	String[] labels = value.split(" ");
        	for (String oneLabel : labels) {
        		FormValidation validation = checkOneLabel(oneLabel.trim());
				if (!validation.equals(FormValidation.ok()))
					return validation;
			}
            return FormValidation.ok();
        }

		private FormValidation checkOneLabel(String oneLabel) {
			if (Util.fixEmpty(oneLabel)==null)
				return FormValidation.ok(); 
			
			Label l = Jenkins.getInstance().getLabel(oneLabel);
			if (l.isEmpty()) {
				for (LabelAtom a : l.listAtoms()) {
					if (a.isEmpty()) {
						LabelAtom nearest = LabelAtom.findNearest(a.getName());
						return FormValidation.warning(Messages.AbstractProject_AssignedLabelString_NoMatch_DidYouMean(a.getName(),nearest.getDisplayName()));
					}
				}
				return FormValidation.warning(Messages.AbstractProject_AssignedLabelString_NoMatch());
			}
			return FormValidation.ok();
		}
        
        public AutoCompletionCandidates doAutoCompleteLabelString(@QueryParameter String value) {
            AutoCompletionCandidates c = new AutoCompletionCandidates();
            Set<Label> labels = Jenkins.getInstance().getLabels();
            List<String> queries = new AutoCompleteSeeder(value).getSeeds();

            for (String term : queries) {
                for (Label l : labels) {
                    if (l.getName().startsWith(term)) {
                        c.add(l.getName());
                    }
                }
            }
            return c;
        }
    }

}