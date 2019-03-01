/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import java.io.File;

/**
 *
 * @author AK249877
 */
public class MsListenerEntryUpdateParameter {

    public enum State {

        START_DOWNLOADING, DOWNLOAD_COMPLETE
    }

    private final File m_file;
    private final State m_state;

    public MsListenerEntryUpdateParameter(File file, State state) {
        m_file = file;
        m_state = state;
    }

    public File getFile() {
        return m_file;
    }

    public State getState() {
        return m_state;
    }

}
