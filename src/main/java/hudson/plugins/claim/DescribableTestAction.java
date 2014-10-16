package hudson.plugins.claim;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.User;
import hudson.tasks.junit.TestAction;
import hudson.util.ListBoxModel;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Date;
import hudson.util.RunList;
import hudson.model.Run;

public abstract class DescribableTestAction extends TestAction implements Describable<DescribableTestAction> {

	private static final Logger LOGGER = Logger.getLogger("claim-plugin");

	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public Descriptor<DescribableTestAction> getDescriptor() {
		return DESCRIPTOR;
	}
	
	private static Comparator<User> comparator = new Comparator<User>() {
		public int compare(User firstUser, User secondUser) {
			return firstUser.getId().compareTo(secondUser.getId());
		}
	};

	private static List<User> cachedUserList;

	@Extension
	public static final class DescriptorImpl extends Descriptor<DescribableTestAction> {
		@Override
		public String getDisplayName() { return "Assignee"; }
		

		public ListBoxModel doFillAssigneeItems() {
			ListBoxModel items = new ListBoxModel();
			// get the current logged in users ID
			String currentUserId = Hudson.getAuthentication().getName();
			User currentUser = null;
			// do we actually have someone logged in at the moment?
			if (currentUserId != null && !currentUserId.isEmpty()) {
				// we do, so lets get the User instance
				currentUser = User.get(currentUserId);

				// do we have someone with that id?
				if (currentUser != null) {
					// add that user to be the very first person in the list.
					items.add(currentUser.getDisplayName() + " (you)", currentUser.getId());
				}
			}
			// lets cache the hell out of this thing
			// its unlikely to change all that much.
			if (cachedUserList == null || cachedUserList.isEmpty()) {
				cachedUserList = new ArrayList<User>();

				HashMap<User, String> uniqueUsers = new HashMap<User, String>();
				Collection<User> users = User.getAll();
				// do we have users?
				if (users != null && !users.isEmpty()) {
					// do we have a user at the moment? (any logged in user?)
					if (currentUser != null) {
						// does that user exist in the list
						if (users.contains(currentUser)) {
							// if so, remove it, because it'll be added at the top above.
							users.remove(currentUser);
						}
					}

					// iterate through the array of users before its sorted
					// to remove those that are "like" each other. This can either starts with or contains.
					// so that usersSorted = minimal list to sort.
					// that plus the clearing of the backing collection should help performance.
					for (User user : users) {

						String userName = user.getDisplayName().toLowerCase();	
						String userId = user.getId();
						String extension = "adp.com";
						boolean userIdContainsExtension = userId.contains(extension);
						boolean userDisplayNameContainsExtension = userName.contains(extension);
						boolean alreadyExistsInUserMapping = uniqueUsers.values().contains(userName);
						if (!userIdContainsExtension && !userDisplayNameContainsExtension && !alreadyExistsInUserMapping) {
							uniqueUsers.put(user, userName);
						}					
					}

					cachedUserList = new ArrayList<User>(uniqueUsers.keySet());
					Collections.sort(cachedUserList, comparator); 
					users = null;
					uniqueUsers = null;


				}
			}
			
			for (User u : cachedUserList) {
				items.add(u.getDisplayName(), u.getId());
			}
			
			return items;

		}
	}

}


