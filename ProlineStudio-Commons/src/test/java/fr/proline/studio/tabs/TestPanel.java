package fr.proline.studio.tabs;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author MB243701
 */
public class TestPanel extends JPanel implements IWrappedPanel{

    private Long nb; 
    
    private Component tabHeaderComponent;
    
    public TestPanel(Long nb) {
        this.nb = nb;
        init();
    }
    
    private void init(){
        setLayout(new BorderLayout());
        this.add(new JLabel("label "+nb), BorderLayout.CENTER);
    }

    
    @Override
    public String getTitle() {
        return "Panel "+this.nb;
    }


    @Override
    public JPanel getComponent() {
        return this;
    }
    
    @Override
    public Long getId(){
        return this.nb;
    }
    
    @Override
    public void setTabHeaderComponent(Component c){
        this.tabHeaderComponent = c;
    }
    
    @Override
    public Component getTabHeaderComponent(){
        return tabHeaderComponent;
    }
    
}
