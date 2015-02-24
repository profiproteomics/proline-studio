package fr.proline.studio.utils;

/**
 *
 * @author JM235353
 */
public class MiscellaneousUtils {
    
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
        final String NEW_URL = "http://proline.profiproteomics.fr/doc/1.0/";
        if (helpURL.startsWith(START_URL)) {
            if (helpURL.startsWith(START_URL_WITH_PARAMETER)) {
                helpURL = NEW_URL+helpURL.substring(START_URL_WITH_PARAMETER.length()).replaceAll(" ", "_").replaceAll(":","/")+".html";
            } else {
                helpURL = NEW_URL+"start.html";
            }
        }
        
        return helpURL;
    }
}
