function fas = getPlateFileAnnotations(session, ids, varargin)
% GETPLATEFILEANNOTATIONS Retrieve file annotations linked to plates
%
%    fas = getPlateFileAnnotations(session, ids) returns all file
%    annotations linked to the plates specified by the input identifiers
%    and owned by the session user.
%
%    fas = getPlateFileAnnotations(session, plates) returns all file
%    annotations linked to the input plates and owned by the session user.
%
%    fas = getPlateFileAnnotations(session,  ids, 'include', include) only
%    returns file annotations with the input namespace.
%
%    fas = getPlateFileAnnotations(session,  ids, 'exclude', exclude)
%    excludes file annotations with the input namespace.
%
%    Examples:
%
%        fas = getPlateFileAnnotations(session, ids)
%        fas = getPlateFileAnnotations(session, plates)
%        fas = getPlateFileAnnotations(session, ids, 'include', include)
%        fas = getPlateFileAnnotations(session, ids, 'exclude', exclude)
%
% See also: GETOBJECTANNOTATIONS, GETPLATECOMMENTANNOTATIONS,
% GETPLATETAGANNOTATIONS, GETPLATEXMLANNOTATIONS

% Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
% All rights reserved.
%
% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License along
% with this program; if not, write to the Free Software Foundation, Inc.,
% 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

fas = getObjectAnnotations(session, 'file', 'plate', ids, varargin{:});