package fr.proline.studio.utils;

/**
 * Basic callback used for breaking dependency between different part of code
 * @author JM235353
 */
public abstract class ResultCallback {
    public abstract void run(boolean success);
}
