/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

/**
 *
 * A Parameter for a databox is defined by its type (class of the instance returned)
 * and its subtype
 * 
 * @author Jean-Philippe
 */
public enum ParameterSubtypeEnum {
    SINGLE_DATA, // one instance of the class
    LIST_DATA,    // instances list of the class
    
    // you can define here specific subtypes to limit or discriminate returned values
    LEAF_PTMPeptideInstance,
    PARENT_PTMPeptideInstance,
    PEPTIDES_SELECTION_LIST
}
