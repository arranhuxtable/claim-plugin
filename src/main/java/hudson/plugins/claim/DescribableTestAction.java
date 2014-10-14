package hudson.plugins.claim;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.User;
import hudson.tasks.junit.TestAction;
import hudson.util.ListBoxModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class DescribableTestAction extends TestAction implements Describable<DescribableTestAction> {

	//private static final Logger LOGGER = Logger.getLogger("claim-plugin");

	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public Descriptor<DescribableTestAction> getDescriptor() {
		return DESCRIPTOR;
	}
	
	private static Comparator<User> comparator = new Comparator<User>() {
		public int compare(User c1, User c2) {
			return c1.getId().compareTo(c2.getId()); 
		}
	};

	@Extension
	public static final class DescriptorImpl extends Descriptor<DescribableTestAction> {
		@Override
		public String getDisplayName() { return "Assignee"; }

		public ListBoxModel doFillAssigneeItems() {
			ListBoxModel items = new ListBoxModel();

			// sort in case the users are not already in sort order
			// with the current user at the top of the list
			String currentUserId = Hudson.getAuthentication().getName();
			User currentUser = null;
			if (currentUserId != null) {
				currentUser = User.get(currentUserId);
			}
			if (currentUser != null) {
				items.add(currentUser.getDisplayName(), currentUser.getId());
			}
			
			Collection<User> users = User.getAll();
			if (users != null) {
				if (currentUser != null) {
					if (users.contains(currentUser)) {
						users.remove(currentUser);
					}
				}
				
				for (User u : users) {
					items.add(u.getDisplayName(), u.getId());
				}
				
				// iterate through the array of users before its sorted
				// to remove those that are "like" each other. This can either starts with or contains.
				// so that usersSorted = minimal list to sort.
				// that plus the clearing of the backing collection should help performance.
				
				List<User> usersSorted = new ArrayList<User>(users);
				Collections.sort(usersSorted, comparator); 
				users = null; // get rid of the backing collection now we have a copy sorted.
				for (User u : usersSorted) {
					items.add(u.getDisplayName(), u.getId());
				}
				
			}
			
			return items;

		}
	}

}