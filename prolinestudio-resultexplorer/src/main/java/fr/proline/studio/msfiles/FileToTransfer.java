/* 
 * Copyright (C) 2019 VD225637
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
package fr.proline.studio.msfiles;

import java.io.File;
import javax.swing.tree.TreePath;

/**
 *
 * Link between File (in local file sytem or server file system) and TreePath of a JTree
 * 
 * @author Jean-Philippe
 */
public class FileToTransfer {

    private final File m_file;
    private final TreePath m_path;

    public FileToTransfer(File file, TreePath path) {
        m_file = file;
        m_path = path;
    }

    public File getFile() {
        return m_file;
    }

    public TreePath getPath() {
        return m_path;
    }

}
