package integration;

import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import omero.RLong;
import omero.RString;
import omero.api.IAdminPrx;
import omero.api.IContainerPrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.OriginalFile;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.model.TagAnnotationI;
import omero.model.TagAnnotation;
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;
import omero.sys.ParametersI;
import omero.sys.Roles;

import org.testng.annotations.Test;

import edu.emory.mathcs.backport.java.util.Arrays;
import pojos.GroupData;
import pojos.PermissionData;

public class PermissionsTest_Matlab extends AbstractServerTest{

	//Permission table lists the types of groups that would be created (two copies of each)
	String[] perm_table = {"rwra--","rw----","rwr---","rwrw--"};
	String[] perm_type = {"Read-Annotate-","Private-","Read-Only-","Read-Write-"};
	String password = "ome";

	//Users created in the database(list obtained from Petr's manually created database, trying to duplicate the setup)
	String[] users={"member-all-1","member-all-2","member-all-3","member-all-4","member-all-5","member-all-6","member-all-7","owner","admin","member-one-ra","member-one-p","member-one-ro","member-one-rw"};

	/**
	 * Creates the permissions corresponding to the specified level.
	 *
	 * @param level The level to handle.
	 * @return
	 */
	private String getPermissions(int level)
	{
		switch (level) {
		case GroupData.PERMISSIONS_GROUP_READ:
			return "rwr---";
		case GroupData.PERMISSIONS_GROUP_READ_LINK:
			return "rwra--";
		case GroupData.PERMISSIONS_GROUP_READ_WRITE:
			return "rwrw--";
		case GroupData.PERMISSIONS_PUBLIC_READ:
			return "rwrwr-";
		}
		return "rw----"; //private group;
	}

	@SuppressWarnings("unchecked")

	void setGroupUp() throws Exception {
		//super.setUp();
		omero.client client = newRootOmeroClient();
		client.enableKeepAlive(60);
		factory = client.getSession();

		List<ExperimenterGroup> groups1 = new ArrayList<ExperimenterGroup>();
		List<ExperimenterGroup> groups2 = new ArrayList<ExperimenterGroup>();

		//Create two copies of every group type mentioned above
		for( int i = 1 ; i <= 2 ; i++)
		{
			for( int j = 0 ; j < perm_table.length ; j++ )
			{
				String groupName = perm_type[j] + Integer.toString(i);

				ExperimenterGroup group = new ExperimenterGroupI();
				group.setName(omero.rtypes.rstring(groupName));
				final Permissions perms = new PermissionsI(perm_table[j]);
				group.getDetails().setPermissions(perms);

				IAdminPrx svc = factory.getAdminService();
				group = new ExperimenterGroupI(svc.createGroup(group), false);
				groups1.add(group);

				if (i==1)
				{
					groups2.add(group);
				}
			}
		}

		RString omeroPassword = omero.rtypes.rstring("ome");
		String Admin = "admin";
		int cntr = 0;
		//Create Users and add them to the respective groups
		ExperimenterGroup default_group;
		List<ExperimenterGroup> target_groups;

		Roles roles = factory.getAdminService().getSecurityRoles();
		ExperimenterGroup userGroup = new ExperimenterGroupI(roles.userGroupId, false);
		ExperimenterGroup system = new ExperimenterGroupI(roles.systemGroupId, false);

		for( int i = 0 ; i < users.length ; i++ )
		{
			target_groups = new ArrayList<ExperimenterGroup>();
			target_groups.add(userGroup);
			String omeroUsername = users[i];			
			Experimenter experimenter = createExperimenterI(omeroUsername, omeroUsername, omeroUsername);

			//Add admin user to system group
			if (omeroUsername.equalsIgnoreCase(Admin))
			{
				default_group = system;
				target_groups.addAll(groups1);
			}
			//Add the first 8 users to all groups
			else if (i<=7)
			{
				default_group =  groups1.get(0);
				target_groups.addAll(groups1);

			}
			//Add the last 4 users to one group alone
			else
			{
				default_group =  groups1.get(cntr);				
				target_groups.add(groups2.get(cntr));
				cntr = cntr+1;
			}
			factory.getAdminService().createExperimenterWithPassword(experimenter, omeroPassword, default_group, target_groups);	

		}
		client.closeSession();
	}

	@Test
	void testImagesSetup() throws Exception{

		//		setGroupUp();
		//Iterate through all the users
		for ( int i=0; i<users.length ; i++)
		{

			omero.client client = new omero.client();
			client.closeSession();

			ServiceFactoryPrx session = client.createSession(users[i], "ome");	
			client.enableKeepAlive(60);

			Experimenter user1 = session.getAdminService().lookupExperimenter(users[i]);
			List<Long> gids = session.getAdminService().getMemberOfGroupIds(user1);

			//Switch group context for the user (Iterate through all the groups, the user is part of)
			for ( int j=0 ; j< gids.size() ; j++)
			{
				ExperimenterGroupI group = new ExperimenterGroupI(gids.get(j), false);
				session.setSecurityContext(group);
				iUpdate = session.getUpdateService();
				mmFactory = new ModelMockFactory(session.getPixelsService());

				//Create 3 new Image Objects(with pixels) and attach it to the session
				for (int k=0 ; k<=2 ; k++)
				{
					Image img = (Image) iUpdate.saveAndReturnObject(mmFactory
							.simpleImage());
					assertNotNull(img);
				}

			}		
			client.closeSession();

		}
	}
	//Fetches imageids from all the users and stores them to an ArrayList
	List<Long[]> getAllImages() throws Exception{

		ArrayList<Long[]> idlist = new ArrayList<Long[]>();
		for (int j=0 ; j<users.length ; j++)
		{
			omero.client client = new omero.client();
			ServiceFactoryPrx session = client.createSession(users[j], password);

			Experimenter user1 = session.getAdminService().lookupExperimenter(users[j]);
			List<Long> gids = session.getAdminService().getMemberOfGroupIds(user1);
			for ( int k=0 ; k< gids.size() ; k++)
			{
				ExperimenterGroupI group = new ExperimenterGroupI(gids.get(k), false);
				session.setSecurityContext(group);
				
				ParametersI params = new omero.sys.ParametersI();
				Long userId = session.getAdminService().getEventContext().userId;
				Long groupId = session.getAdminService().getEventContext().groupId;
				params.exp(omero.rtypes.rlong(userId));	
				
				IContainerPrx proxy = session.getContainerService();
				List<Image> imageList = proxy.getUserImages(params);

				Long[] data;
				for (int i=0; i<imageList.size(); i++)
				{
					data = new Long[3];
					data[0] = imageList.get(i).getId().getValue();
					data[1] = userId;
					data[2] = groupId;
					//idlist.add(imageList.get(i).getId());
					idlist.add(data);

				}
				client.closeSession();
			}
		}
		return idlist;		

	}

	//Iterate through all the users and try to annotate the images
	void annotateAllImages() throws Exception{

		List<Long[]> idlist = getAllImages();
		for (int j=0 ; j<users.length ; j++)
		{
			omero.client client = new omero.client();
			ServiceFactoryPrx session = client.createSession(users[j], password);
			String comment = users[j] + "_comment";
			String tag = users[j] + "_tag";

			Experimenter exp = session.getAdminService().lookupExperimenter(users[j]);
			List<Long> usergroups = session.getAdminService().getMemberOfGroupIds(exp);
			RLong expid = exp.getId();

			//Create Tags
			List<Long> ids = new ArrayList<Long>();
			TagAnnotation c = new TagAnnotationI();
			c.setTextValue(omero.rtypes.rstring(tag));
			c = (TagAnnotation) iUpdate.saveAndReturnObject(c);
			ids.add(c.getId().getValue());

			//Create Comments
			TermAnnotation t = new TermAnnotationI();
			t.setTermValue(omero.rtypes.rstring(comment));
			t = (TermAnnotation) iUpdate.saveAndReturnObject(t);
			ids.add(t.getId().getValue());

			//Create File for FileAnnotation
			OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
					.createOriginalFile());
			assertNotNull(of);
			FileAnnotation f = new FileAnnotationI();
			f.setFile(of);
			f = (FileAnnotation) iUpdate.saveAndReturnObject(f);
			ids.add(f.getId().getValue());


			for ( int k=0 ; k< usergroups.size() ; k++)
			{
				ExperimenterGroupI group = new ExperimenterGroupI(usergroups.get(k), false);
				session.setSecurityContext(group);

				PermissionData perms = new PermissionData(group.getDetails().getPermissions());
				String permsAsString = getPermissions(perms.getPermissionsLevel());
				final String[] subgroups= {perm_table[0], perm_table[3]};
				final List perm_table1 = Arrays.asList(subgroups);
				
				List<IObject> links = new ArrayList<IObject>();
				for (int i=0; i<idlist.size() ; i++)
				{
					Long[] searchterm = idlist.get(i);
					Long groupid = searchterm[2];
					Long imageid = searchterm[0];
					Long userid = searchterm[1];

					if (usergroups.contains(groupid) && (perm_table1.contains(permsAsString) || userid.equals(expid)))
					{
						//Create Links for Tags
						ImageAnnotationLink link = new ImageAnnotationLinkI();
						link.setChild(new TagAnnotationI(c.getId().getValue(), false));
						link.setParent(new ImageI(imageid, false));
						links.add(link);

						//Create Links for Comments
						link = new ImageAnnotationLinkI();
						link.setChild(new TermAnnotationI(t.getId().getValue(), false));
						link.setParent(new ImageI(imageid, false));
						links.add(link);

						//Create Links for Files
						link = new ImageAnnotationLinkI();
						link.setChild(new FileAnnotationI(f.getId().getValue(), false));
						link.setParent(new ImageI(imageid, false));
						links.add(link);
					}

					iUpdate.saveAndReturnArray(links);	
				}

			}
			client.closeSession();
		}
	}

	void moveAllImages() throws Exception {
		
		List<Long[]> idlist = getAllImages();
		for (int j=0 ; j<users.length ; j++)
		{
			omero.client client = new omero.client();
			ServiceFactoryPrx session = client.createSession(users[j], password);

			Experimenter user1 = session.getAdminService().lookupExperimenter(users[j]);
			List<Long> gids = session.getAdminService().getMemberOfGroupIds(user1);
			for ( int k=0 ; k< gids.size() ; k++)
			{
				ExperimenterGroupI group = new ExperimenterGroupI(gids.get(k), false);
				session.setSecurityContext(group);

				Long userId = session.getAdminService().getEventContext().userId;
				Long groupId = session.getAdminService().getEventContext().groupId;



				client.closeSession();
			}
		}
		
	}

}


