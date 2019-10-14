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
package fr.proline.studio.utils;

import static fr.proline.studio.utils.GlobalValues.PUBLIC_RELEASE_VERSION;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

/**
 * Static Util methods used for the help functionnality
 * @author JM235353
 */
public class HelpUtils {
    
    /**
     * Transform URL of type
     * "http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:rsvalidation"
     * to
     * "http://www.profiproteomics.fr/doc/1.0/how_to/studio/rsvalidation.html"
     * @param helpURL
     * @return 
     */
    public static String convertURLToCurrentHelp(String helpURL) {
        if (helpURL == null) {
            return null;
        }
        final String START_URL = "http://biodev.extra.cea.fr/docs/proline/doku.php";
        final String START_URL_WITH_PARAMETER = "http://biodev.extra.cea.fr/docs/proline/doku.php?id=";
        final String NEW_URL = "http://www.profiproteomics.fr/software/doc/1.6/";
        if (PUBLIC_RELEASE_VERSION && helpURL.startsWith(START_URL)) {
            if (helpURL.startsWith(START_URL_WITH_PARAMETER)) {
                helpURL = NEW_URL + helpURL.substring(START_URL_WITH_PARAMETER.length()).replaceAll(" ", "_").replaceAll(":", "/") + ".html";
            } else {
                helpURL = NEW_URL + "start.html";
            }
        }

        return helpURL;
    }

    public static String getFileName(String path, String[] suffixes) {

        path = path.toLowerCase();

        if (path.contains("/")) {
            path = path.substring(path.lastIndexOf("/") + 1);
        }
        if (path.contains("\\")) {
            path = path.substring(path.lastIndexOf("\\") + 1);
        }
        for (String suffix : suffixes) {
            if (path.contains(suffix.toLowerCase())) {
                path = path.substring(0, path.indexOf(suffix.toLowerCase()));
            }
        }

        return path;
    }

    public static String createRedirectPage(String url) {
        return "<html><head>"
                +"<meta http-equiv=\"refresh\" content=\"0;url="+url+"\" />"
                +"</head></html>";
    }

    public static URI createRedirectTempFile(String documentationSuffix) {
        BufferedWriter writer = null;
        File tmpFile = null;
        try {
            // creates temporary file
            tmpFile = File.createTempFile("redirect", ".html", null);
            // deletes file when the virtual machine terminate
            tmpFile.deleteOnExit();
            // writes redirect page content to file 
            writer = new BufferedWriter(new FileWriter(tmpFile));
            writer.write(createRedirectPage(new File(".").getCanonicalPath() + File.separatorChar + "documentation" + File.separatorChar + "Proline_UserGuide.docx.html#"+documentationSuffix));
            writer.close();
        } catch (IOException e) {
            return null;
        }
        return tmpFile.toURI();
    }
}
