package org.jenkinsci.plugins.elasticaxisplugin;

import hudson.Extension;
import hudson.matrix.Axis;
import hudson.matrix.AxisDescriptor;
import hudson.matrix.LabelAxis;
import hudson.matrix.MatrixBuild;
import hudson.model.Computer;
import hudson.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class ElasticAxis extends LabelAxis {

	private String label;
	private boolean ignoreOffline;

    @DataBoundConstructor
    public ElasticAxis(String name, String labelString, boolean ignoreOffline) {
        super(name, computeAllNodesInLabel(labelString));
		this.label = labelString;
		this.ignoreOffline = ignoreOffline;
    }
    
    public String getLabelString() {
    	return label;
    }
    
    public boolean getIgnoreOffline() {
    	return ignoreOffline;
    }

	@Override
    public List<String> rebuild(MatrixBuild.MatrixBuildExecution context) {
        return computeNodesInLabel(label, ignoreOffline);
    }

    @Override
    public List<String> getValues() {
    	return computeAllNodesInLabel(label);
    }

    private static List<String> computeAllNodesInLabel(String labelName) {
		return computeNodesInLabel(labelName, false);
	}
    
	private static List<String> computeNodesInLabel(String labelWithNodes, boolean restrictToOnlineNodes) {
		List<String> computedNodes=new ArrayList<String>();
		for(Node node : Jenkins.getInstance().getLabel(labelWithNodes).getNodes()) {
		  if (shouldAddNode(restrictToOnlineNodes, node.toComputer())) 
		      computedNodes.add(node.getDisplayName());
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
                    formData.getBoolean("ignoreOffline")
            );
        }
    }

}