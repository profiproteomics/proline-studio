package fr.proline.studio.dock.container;

public abstract class DockContainerMulti extends DockContainer implements DockMultiInterface {



    public abstract void remove(DockContainer container);

    public abstract boolean isEmpty();

    public abstract DockContainerTab searchTab(int idContainer);
}
