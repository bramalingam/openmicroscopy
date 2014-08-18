clear all;close all;

%Help : http://www.openmicroscopy.org/community/viewtopic.php?f=6&t=2965
import omero.cmd.Chgrp;
import omero.cmd.DoAll
import omero.cmd.CmdCallbackI;

import omero.rtypes.rdouble;
import omero.rtypes.rint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import omero.grid.Column;
import omero.grid.LongColumn;
import omero.grid.TablePrx;
import omero.model.Channel;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.ExperimenterGroup;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.LogicalChannel;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAnnotationLink;
import omero.model.PlateAnnotationLinkI;
import omero.model.PlateI;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.Reagent;
import omero.model.Rect;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.RoiAnnotationLink;
import omero.model.RoiAnnotationLinkI;
import omero.model.RoiI;
import omero.model.Screen;
import omero.model.ScreenPlateLink;
import omero.model.ScreenPlateLinkI;
import omero.model.Shape;
import omero.model.StatsInfo;
import omero.model.Well;
import omero.model.WellSample;
import omero.sys.EventContext;
% String perms = "rw----";

%Params
temp_number='Trial5-';
importopt = 2;

%Load Omero
[client session] = loadOmero('ice.config');
% session = client.createSession(username, password);
client.enableKeepAlive(60);
scalingFactor=500;

perm_table={'rwra--','rw----','rwr---','rwrw--'};
perm_type={'Read-Annotate-','Private-','Read-Only-','Read-Write-'};
users={'member-all-1','member-all-2','member-all-3','member-all-4','member-all-5','member-all-6','member-all-7','owner','admin','member-one-ra','member-one-p','member-one-ro','member-one-rw'};

% %Temporary Snippents
% for i=1:length(perm_type)
%     perm_type{i}=[temp_number perm_type{i}];
% end
% 
% for k=1:length(users)
%     users{k}=[temp_number users{k}];
% end


%Create Group
omero.model.ExperimenterGroupI();
userGroup = omero.model.ExperimenterGroupI(1,false);%Put the user in root
groupidvec=[];
groups=userGroup;
cntr=1;
for i=1:4
    
    for j=1:2
        
        GroupName = ([perm_type{i} num2str(j)]);
        group = omero.model.ExperimenterGroupI();
        group.setName(rstring(GroupName));
        group.getDetails().setPermissions(omero.model.PermissionsI(perm_table{i}));
        newgroupid = session.getAdminService.createGroup(group);
        
        groupidvec=[groupidvec ; newgroupid];
        newGroup(cntr) = omero.model.ExperimenterGroupI(newgroupid, false); %#ok<SAGROW>
        groups = ([groups newGroup(cntr)]);
        
        if j==1
            groups2(i)=newGroup(cntr);
        end
        cntr=cntr+1;
    end
    
end

groups1=toJavaList(groups);
groups2=toJavaList(groups2);

cntr=1;
for i=1:length(users)
    
    %Create User
    omeroUsername = [users{i}];
    experimenter = omero.model.ExperimenterI();
    experimenter.setFirstName(rstring(users{i}))
    experimenter.setLastName(rstring(users{i}))
    experimenter.setOmeName(rstring(omeroUsername))    
    
    if i<=8
        target_groups=groups1;
        default_group=session.getAdminService.getGroup(groupidvec(1));
    elseif strcmp([temp_number 'admin'],omeroUsername)
        %Add admin to system group
        system_group=session.getAdminService.getEventContext.groupId;
        default_group=session.getAdminService.getGroup(system_group);
        target_groups=groups1;
    else
        default_group=session.getAdminService.getGroup(groupidvec(cntr));
        target_groups=toJavaList(default_group);
        cntr=cntr+2;
    end
    
    session.getAdminService.createExperimenterWithPassword(experimenter, rstring('ome'), default_group, target_groups);
    
    if strcmp([temp_number 'owner'],omeroUsername)
        experimenter=session.getAdminService.lookupExperimenter(omeroUsername);
        for j=1:length(groups1)
            session.getAdminService.setGroupOwner(groups1.get(j),experimenter);
        end
    end
    
end

%Add root to all the groups1
session.getAdminService.addGroups(omero.model.ExperimenterI(session.getAdminService.getEventContext.userId(), false), groups1);
