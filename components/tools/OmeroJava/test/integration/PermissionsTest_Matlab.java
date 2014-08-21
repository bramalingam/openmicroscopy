package integration;

import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ome.formats.OMEROMetadataStoreClient;
import omero.RString;
import omero.api.IAdminPrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.model.Pixels;
import omero.sys.ParametersI;
import omero.sys.Roles;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import edu.emory.mathcs.backport.java.util.Arrays;

public class PermissionsTest_Matlab extends AbstractServerTest{

	//Permission table lists the types of groups that would be created (two copies of each)
	String[] perm_table = {"rwra--","rw----","rwr---","rwrw--"};
	String[] perm_type = {"Read-Annotate-","Private-","Read-Only-","Read-Write-"};	

	//Users created in the database(list obtained from Petr's manually created database, trying to duplicate the setup)
	String[] users={"member-all-1","member-all-2","member-all-3","member-all-4","member-all-5","member-all-6","member-all-7","owner","admin","member-one-ra","member-one-p","member-one-ro","member-one-rw"};
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
					//add annotation
				}

			}		
			client.closeSession();
			
		}
	}
	
	void testImages() {
		
	}

}


