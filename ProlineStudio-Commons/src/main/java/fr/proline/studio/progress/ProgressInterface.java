package fr.proline.studio.progress;

/**
 * Interface to be able to view the progress of a task
 * @author JM235353
 */
public interface ProgressInterface {

    public boolean isLoaded();
        
    public int getLoadingPercentage();
}
