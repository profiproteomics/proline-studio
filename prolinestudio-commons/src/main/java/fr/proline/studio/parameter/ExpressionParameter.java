/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.parameter;

import fr.proline.studio.gui.expressionbuilder.ExpressionBuilderPanel;
import fr.proline.studio.gui.expressionbuilder.ExpressionEntity;
import java.util.ArrayList;
import javax.swing.JComponent;

/**
 * Parameter to select an expression (with functions, variables)
 * @author JM235353
 */
public class ExpressionParameter extends AbstractParameter {

    private ArrayList<ExpressionEntity> m_functions = null;
    private ArrayList<ExpressionEntity> m_variables = null;
    private ArrayList<ExpressionEntity> m_calcFunctions = null;
    private int m_nbButtonsHorizontal;
    
    public ExpressionParameter(String key, String name, ArrayList<ExpressionEntity> functions, ArrayList<ExpressionEntity> variables, ArrayList<ExpressionEntity> calcFunctions, int nbButtonsHorizontal) {
        super(key, name, String.class, ExpressionBuilderPanel.class);
        
        m_functions = functions;
        m_variables = variables;
        m_calcFunctions = calcFunctions;
        m_nbButtonsHorizontal = nbButtonsHorizontal;
    }
    
    @Override
    public JComponent getComponent(Object value) {
        if (m_parameterComponent !=null) {
            return m_parameterComponent;
        }

        if (m_graphicalType.equals(ExpressionBuilderPanel.class)) {

            ExpressionBuilderPanel expressionBuilderPanel = new ExpressionBuilderPanel(m_nbButtonsHorizontal);

            for (ExpressionEntity entity : m_functions) {
                expressionBuilderPanel.addFunction(entity);
            }
            
            for (ExpressionEntity entity : m_variables) {
                expressionBuilderPanel.addVariable(entity);
            }
            
            for (ExpressionEntity entity : m_calcFunctions) {
                expressionBuilderPanel.addCalcButton(entity);
            }

            m_parameterComponent = expressionBuilderPanel;
            return expressionBuilderPanel;
        }


        return null;
    }

    @Override
    public void initDefault() {
        // nothing to do
    }

    @Override
    public ParameterError checkParameter() {
        // TODO
        return null;
    }

    @Override
    public String getStringValue() {
        return getObjectValue().toString();
    }
    
    public String getHumanExpression() {
        return ((ExpressionBuilderPanel) m_parameterComponent).getDisplayExpression();
    }

    @Override
    public Object getObjectValue() {
        if (m_graphicalType.equals(ExpressionBuilderPanel.class) && (m_parameterComponent != null)) {
           return ((ExpressionBuilderPanel) m_parameterComponent).getCodeExpression();
        }
        return null; // should not happen
    }

    @Override
    public void setValue(String v) {
        // nothing to do
    }

    @Override
    public boolean isEdited() {
        return true;
    }
    
}
