package hudson.plugins.claim;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

@Extension 
public class ClaimConfig extends GlobalConfiguration {

    public ClaimConfig() {
        load();
    }

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
        save();
        return super.configure(req,formData);
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
        return GlobalConfiguration.all().get(ClaimConfig.class);
    }
}