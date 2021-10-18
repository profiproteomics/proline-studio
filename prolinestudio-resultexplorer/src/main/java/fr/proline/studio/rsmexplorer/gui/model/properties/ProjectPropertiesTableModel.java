/*
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.gui.model.properties;

import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.ProjectUserAccountMap;
import fr.proline.studio.table.DataGroup;
import fr.proline.studio.table.PropertiesTableModel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Set;

public class ProjectPropertiesTableModel extends PropertiesTableModel {

  Project m_project;

  public ProjectPropertiesTableModel(Project p) {
    m_project = p;
    init();
  }

  private void init(){
    m_datasetNameArray = new ArrayList<>();
    m_datasetNameArray.add(m_project.getName());
    DataGroup dg = new ProjectInformationGroup(0);
    m_dataGroupList = new ArrayList<>(1);
    m_dataGroupList.add(dg);
  }

  public class ProjectInformationGroup extends DataGroup {

    private static final int ROWTYPE_PROJECT_ID = 0;
    private static final int ROWTYPE_PROJECT_NAME = 1;
    private static final int ROWTYPE_PROJECT_DESCRIPTION = 2;
    private static final int ROWTYPE_PROJECT_OWNER = 3;
    private static final int ROWTYPE_PROJECT_MEMBERS = 4;

    private static final int ROW_COUNT = 5; //<-- get in sync
    private final Color GROUP_COLOR_BACKGROUND = new Color(76, 166, 107);

    public ProjectInformationGroup(int rowStart) {
      super("Project Information", rowStart);
    }

    @Override
    public GroupObject getGroupNameAt(int rowIndex) {

      switch (rowIndex) {
        case ROWTYPE_PROJECT_ID:
          return new GroupObject("Id", this);
        case ROWTYPE_PROJECT_NAME:
          return new GroupObject("Name", this);
        case ROWTYPE_PROJECT_DESCRIPTION:
          return new GroupObject("Description", this);
        case ROWTYPE_PROJECT_OWNER:
          return new GroupObject("Owner", this);
        case ROWTYPE_PROJECT_MEMBERS:
          return new GroupObject("Shared with", this);
      }

      return null;
    }

    @Override
    public GroupObject getGroupValueAt(int rowIndex, int columnIndex) {

      if (m_project == null) {
        return new GroupObject("", this);
      }

      switch (rowIndex) {
        case ROWTYPE_PROJECT_ID:
          return new GroupObject(String.valueOf(m_project.getId()), this);
        case ROWTYPE_PROJECT_NAME:
          return new GroupObject(m_project.getName(), this);
        case ROWTYPE_PROJECT_DESCRIPTION:
          return new GroupObject(m_project.getDescription(), this);
        case ROWTYPE_PROJECT_OWNER:
          return new GroupObject(m_project.getOwner().getLogin(), this);
        case ROWTYPE_PROJECT_MEMBERS: {
          StringBuilder sb = new StringBuilder();
          Set<ProjectUserAccountMap> members = m_project.getProjectUserAccountMap();
          boolean first = true;
          for (ProjectUserAccountMap nextMember : members) {
            if (!first) {
              sb.append(", ");
            } else
              first = false;

            sb.append(nextMember.getUserAccount().getLogin());
          }
          return new GroupObject(sb.toString(), this);
        }
      }

      return null;
    }

    @Override
    public Color getGroupColor(int row) {
      return GROUP_COLOR_BACKGROUND;
    }

    @Override
    public int getRowCountImpl() {
      return ROW_COUNT;
    }

  }
}

