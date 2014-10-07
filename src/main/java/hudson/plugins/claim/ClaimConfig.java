package hudson.plugins.claim;

import hudson.Extension;
import jenkins.model.GlobalPluginConfiguration;
import net.sf.json.JSONObject;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.StaplerRequest;

@Extension 
public class ClaimConfig extends GlobalPluginConfiguration {

    public ClaimConfig() {
        load();
    }
    private static final Logger LOGGER = Logger.getLogger("claim-plugin");
    /**
     * Whether we want to send emails to the assignee when items are claimed/assigned
     */
    private boolean sendEmails;
	
    /**
     * Whether we want to send flowdock messages when a build is unclaimed.
     */
	private boolean sendFlowdockPostsOnUnclaim;
	
	/**
     * Whether we want to send flowdock messages when a build is claimed.
     */
	private boolean sendFlowdockPostsOnClaim;
	
	private String flowdockApiToken;
	
	private String specialBuildRegex;


    /**
     * This human readable name is used in the configuration screen.
     */
    public String getDisplayName() {
        return "Claim";
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        // To persist global configuration information,
        // set that to properties and call save().
        sendEmails = formData.getBoolean("sendEmails");
		sendFlowdockPostsOnUnclaim = formData.getBoolean("sendFlowdockPostsOnUnclaim");
		sendFlowdockPostsOnClaim = formData.getBoolean("sendFlowdockPostsOnClaim");
		flowdockApiToken = formData.getString("flowdockApiToken");
		specialBuildRegex = formData.getString("specialBuildRegex");
        save();
        return super.configure(req,formData);
    }
	
	public void setflowdockApiToken(String _flowdockApiToken) {
		flowdockApiToken = _flowdockApiToken;
	}
	
	public String getflowdockApiToken() {
		return flowdockApiToken;
	}
	
	public void setspecialBuildRegex(String _specialBuildRegex) {
		specialBuildRegex = _specialBuildRegex;
	}
	
	public String getspecialBuildRegex() {
		return specialBuildRegex;
	}
	
	public void setSendFlowdockPostsOnClaim(boolean _sendFlowdockPostsOnClaim) {
		sendFlowdockPostsOnClaim = _sendFlowdockPostsOnClaim;
	}
	
	public void setSendFlowdockPostsOnUnclaim(boolean _sendFlowdockPostsOnUnclaim) {
		sendFlowdockPostsOnUnclaim = _sendFlowdockPostsOnUnclaim;
	}
	
	public boolean shouldSendFlowdockPostsOnUnclaim() { 
		return sendFlowdockPostsOnUnclaim;
	}
	
	public boolean shouldSendFlowdockPostsOnClaim() { 
		return sendFlowdockPostsOnClaim;
	}

    /**
     * This method returns true if the global configuration says we should send mails on build claims
     * @return true if configuration is that we send emails for claims, false otherwise
     */
    public boolean getSendEmails() {
        return sendEmails;
    }
	
	public boolean getSendFlowdockPostsOnClaim() { 
		return sendFlowdockPostsOnClaim;
	}
	
	public boolean getSendFlowdockPostsOnUnclaim() { 
		return sendFlowdockPostsOnUnclaim;
	}
    
    /**
     * Set whether we should send emails
     * @param val the setting to use
     */
    public void setSendEmails(boolean val) {
        sendEmails = val;
    }
    
    /**
     * get the current claim configuration
     * @return the global claim configuration
     */
    public static ClaimConfig get() {
        return GlobalPluginConfiguration.all().get(ClaimConfig.class);
    }
}
