package fr.proline.studio.filter;

import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JButton;
import org.openide.windows.WindowManager;

/**
 * Button to access to filter dialog from a toolbar
 * @author JM235353
 */
public abstract class FilterButton extends JButton implements ActionListener {
    
    private FilterTableModelInterface m_tableModelFilterInterface;
    
    public FilterButton(FilterTableModelInterface tableModelFilterInterface) {

        setModelFilterInterface(tableModelFilterInterface);
        
        setIcon(IconManager.getIcon(IconManager.IconType.FUNNEL ));
        setFocusPainted(false);
        setToolTipText("Filter...");

        addActionListener(this);
    }

    public final void setModelFilterInterface(FilterTableModelInterface tableModelFilterInterface) {
        m_tableModelFilterInterface = tableModelFilterInterface;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (!m_tableModelFilterInterface.isLoaded()) {

            ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_tableModelFilterInterface, "Data loading", "Filtering is not available while data is loading. Please Wait.");
            dialog.setLocation( getLocationOnScreen().x + getWidth() + 5,  getLocationOnScreen().y + getHeight() + 5);
            dialog.setVisible(true);
            
            if (!dialog.isWaitingFinished()) {
                return;
            }
        }
        
        FilterDialog dialog = FilterDialog.getDialog(WindowManager.getDefault().getMainWindow());
        dialog.setLocation( getLocationOnScreen().x + getWidth() + 5,  getLocationOnScreen().y + getHeight() + 5);
        LinkedHashMap<Integer, Filter> filtersMap = m_tableModelFilterInterface.getFilters();
        Filter[] filters = new Filter[filtersMap.size()];
        int index = 0;
        for (Map.Entry<Integer, Filter> entry : filtersMap.entrySet()) {
            filters[index++] = entry.getValue();
        }
        
        
        dialog.setFilers(filters);
        dialog.setVisible(true);

        if (dialog.getButtonClicked() == FilterDialog.BUTTON_OK) {

            boolean filterIsUsed = false;
            int nbFilter = filters.length;
            for (int i = 0; i < nbFilter; i++) {
                if (filters[i].isUsed()) {
                    filterIsUsed = true;
                    break;
                }
            }
            Icon funnelIcon = filterIsUsed ? IconManager.getIcon(IconManager.IconType.FUNNEL_ACTIVATED) : IconManager.getIcon(IconManager.IconType.FUNNEL);
            setIcon(funnelIcon);


            m_tableModelFilterInterface.filter();
            
            filteringDone();
        }
    }
    
    protected abstract void filteringDone();
}
