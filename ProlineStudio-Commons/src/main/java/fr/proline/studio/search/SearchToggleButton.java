package fr.proline.studio.search;

import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JToggleButton;

/**
 *
 * @author JM235353
 */
public class SearchToggleButton extends JToggleButton {

    public SearchToggleButton(final SearchFloatingPanel searchPanel) {

        setIcon(IconManager.getIcon(IconManager.IconType.SEARCH11));
        setToolTipText("Search");

        addActionListener(new ActionListener() {

            boolean firstTime = true;

            @Override
            public void actionPerformed(ActionEvent e) {
                searchPanel.setVisible(isSelected());

                if (firstTime) {
                    firstTime = false;
                    searchPanel.setLocation(getX() + getWidth() + 5, getY() + 5);
                }
                
                searchPanel.setFocus();
            }
        });

    }
}
