package hudson.plugins.claim;

import hudson.model.Run;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectStreamException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.io.*;
import java.util.logging.Logger;

public class ClaimBuildAction extends AbstractClaimBuildAction<Run> {

    private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger("claim-plugin");

    private transient Run run;

    public ClaimBuildAction(Run run) {
        super(run);
    }

    public String getDisplayName() {
        return Messages.ClaimBuildAction_DisplayName();
    }
	
	public String getBuildNumber() {
		return owner.getDisplayName().replace("#", "No. ");
	}
	
	public String getJobName() {
		return owner.getParent().getDisplayName();
	}
	
	public String getJenkinsUrl() {
		return owner.getAbsoluteUrl();
	}

    @Override
    public String getNoun() {
        return Messages.ClaimBuildAction_Noun();
    }

    public Object readResolve() throws ObjectStreamException {
        if (run != null && owner == null) {
            owner = run;
        }
        return this;
    }
	
	@Override
    public void claim(String claimedBy, String reason, String assignedBy, boolean sticky) {
        super.claim(claimedBy, reason, assignedBy, sticky);
		LOGGER.info(claimedBy + " is claiming " + getJobName() + " (build " + getBuildNumber() + ") because: " + reason);
		ClaimConfig config = ClaimConfig.get();
		if (config != null && config.shouldSendFlowdockPostsOnClaim()) {
			LOGGER.info("Configuration is set to send flowdock posts upon claiming a build...");
			try {
				sendFlowdockPostForClaim(claimedBy, assignedBy, reason);
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				String exceptionAsString = sw.toString();
				LOGGER.info("Error when sending the Flowdock notification: " + exceptionAsString);
			}
		}
		else {
			LOGGER.info("Configuration is NOT set to send flowdock posts upon claiming a build...");
		}

    }
	
	@Override
    public void unclaim() {
		String user = getClaimedBy();
		String buildNumber = getBuildNumber();
		String jobName = getJobName();
		String jenkinsUrl = getJenkinsUrl();
        super.unclaim();
		ClaimConfig config = ClaimConfig.get();
		if (config != null && config.shouldSendFlowdockPostsOnUnclaim()) {
			LOGGER.info("Configuration is set to send flowdock posts upon dropping the claim of a build...");
			try {
				sendFlowdockPostForUnclaim(user, buildNumber, jobName, jenkinsUrl);
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				String exceptionAsString = sw.toString();
				LOGGER.info("Error when sending the Flowdock notification: " + exceptionAsString);
			}
		}
		else {
			LOGGER.info("Configuration is NOT set to send flowdock posts upon unclaiming a build...");
		}

    }
	
    @Override
    public String getUrl() {
    	return owner.getUrl();
	}
	
	public boolean hasBeenAssignedToUser(String claimedBy, String assignedBy) { 
		return claimedBy != assignedBy;
	}

	private void sendFlowdockPostForUnclaim(String claimedBy, String buildNumber, String jobName, String jenkinsUrl) throws Exception {
		LOGGER.info("Beginning to send flowdock notification for a build unclaim....");
	
        StringBuffer chatMessage = new StringBuffer();
        chatMessage.append("A failing build was just unclaimed!\r\n");
        chatMessage.append("Job: ").append(jobName).append(".\r\n");
        chatMessage.append("Build: ").append(buildNumber).append(".\r\n");
		chatMessage.append("No longer claimed by: ").append(claimedBy).append(".\r\n");
        chatMessage.append("Job URL: ").append(jenkinsUrl);

		sendFlowdockPost(chatMessage.toString(), "jenkins", "#unclaim");
	}

	public void sendFlowdockPost(String message, String username, String tags) throws Exception {
		LOGGER.info("Chat message we will send is: " + message);
        // The post data for the request containing url-encoded values
        StringBuffer postData = new StringBuffer();
        postData.append("content=").append(urlEncode(message));
        postData.append("&tags=").append(urlEncode(tags));
        postData.append("&external_user_name=").append(
                urlEncode(username));

        // flow token of a flow
        //String flowToken = "7170352b7bfbc0d71f6964f15903d986";
		ClaimConfig config = ClaimConfig.get();
		
		if (config == null || config.getflowdockApiToken() == null || config.getflowdockApiToken().isEmpty()) {
			throw new Exception("Unable to get the flowdock API token from configuration, please check the configuration.");
		}
		
		//String flowToken = "ca2285fc66094f5eb123001697841420";
		String flowToken = config.getflowdockApiToken();
        // prepare the HTTP request
        URL url;
		
        HttpURLConnection connection = null;

        String flowdockUrl = "https://api.flowdock.com/messages/chat/"
                + flowToken;
        url = new URL(flowdockUrl);

        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length",
                String.valueOf(postData.toString().getBytes().length));
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        // send the POST request
		//TODO ADD BACK IN THE DEVELOP ETC CHECK
		LOGGER.info("POSTing the Flowdock notification to: " + flowToken);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(postData.toString());
        wr.flush();
        wr.close();
        

        // handle the response code
        if (connection.getResponseCode() != 200) {
            StringBuffer responseContent = new StringBuffer();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        connection.getErrorStream()));
                String responseLine;
                while ((responseLine = in.readLine()) != null) {
                    responseContent.append(responseLine);
                }
                in.close();
            } catch (Exception ex) {
                throw new Exception(
                        "Flowdock returned an error response with status "
                                + connection.getResponseCode() + " "
                                + connection.getResponseMessage() + ", "
                                + responseContent.toString() + "\n\nURL: "
                                + flowdockUrl);
            }
        }
		
		LOGGER.info("Finished POSTing the Flowdock notification to: " + flowToken + " with a response code of: " + connection.getResponseCode());
	}
	
    private void sendFlowdockPostForClaim(String claimedBy, String assignedBy, String reason)
            throws Exception {
		LOGGER.info("Beginning to send flowdock notification for a build claim....");
	
        // This StringBuffer contains the message that will be sent to the
        // flowdock chat
        StringBuffer chatMessage = new StringBuffer();
        chatMessage.append("A failing build was just claimed!\r\n");
        chatMessage.append("Job: ").append(getJobName()).append(".\r\n");
        chatMessage.append("Build: ").append(getBuildNumber()).append(".\r\n");
		if (hasBeenAssignedToUser(claimedBy, assignedBy)) {
			chatMessage.append("Claimed by: ").append(claimedBy).append(" (assigned by: " + assignedBy + ")").append(".\r\n");	
		} 
		else {
			chatMessage.append("Claimed by: ").append(claimedBy).append(".\r\n");
		}
        
        chatMessage.append("Reason: ").append(reason).append(".\r\n");
        chatMessage.append("Job URL: ").append(getJenkinsUrl());

		sendFlowdockPost(chatMessage.toString(), "jenkins", "#claim");
       


    }

    /**
     * URL-encode a String object so that it is safe to be POSTed.
     * 
     * @param s
     *            The String to be encoded.
     * @return The URL-encoded String
     * @throws UnsupportedEncodingException
     *             If the encoding is not supported.
     */
    private String urlEncode(final String s)
            throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "UTF-8");
    }

}
