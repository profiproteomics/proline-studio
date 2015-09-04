package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import java.util.Stack;

public class navigationData {

    Boolean NavigationFlag = false;
    String currentCard = "Main Panel";
    Stack<String> previous = new Stack<>();
    Stack<String> next = new Stack<>();

}
