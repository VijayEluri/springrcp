/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.richclient.table;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.springframework.enums.LetterCodedEnum;
import org.springframework.enums.ShortCodedEnum;
import org.springframework.enums.StringCodedEnum;
import org.springframework.richclient.core.UIConstants;
import org.springframework.richclient.table.renderers.BeanTableCellRenderer;
import org.springframework.richclient.table.renderers.BooleanTableCellRenderer;
import org.springframework.richclient.table.renderers.CodedEnumTableCellRenderer;
import org.springframework.richclient.table.renderers.DateTimeTableCellRenderer;
import org.springframework.richclient.table.renderers.OptimizedTableCellRenderer;

/**
 * @author Keith Donald
 */
public class TableUtils {
    public static void scrollToRow(JTable table, int row) {
        if (!(table.getParent() instanceof JViewport)) { return; }
        JViewport viewport = (JViewport)table.getParent();
        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0).
        Rectangle rect = table.getCellRect(row, 0, true);
        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();
        // Translate the cell location so that it is relative
        // to the view, assuming the northwest corner of the
        // view is (0,0)
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);
        // Scroll the area into view
        viewport.scrollRectToVisible(rect);
    }

    public static JTable createStandardSortableTable(TableModel tableModel) {
        JTable table = new JTable();
        ShuttleSortableTableModel sortedModel = new ShuttleSortableTableModel(
                tableModel);
        table.setAutoCreateColumnsFromModel(true);
        table.setModel(sortedModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setPreferredScrollableViewportSize(new Dimension(table
                .getColumnModel().getTotalColumnWidth(), 300));
        installDefaultRenderers(table);
        TableSortIndicator sortIndicator = new TableSortIndicator(table);
        new SortTableCommand(table, sortIndicator.getColumnSortList());
        return table;
    }

    public static void installDefaultRenderers(JTable table) {
        OptimizedTableCellRenderer defaultRenderer = new OptimizedTableCellRenderer();
        BeanTableCellRenderer beanRenderer = new BeanTableCellRenderer();
        table.setDefaultRenderer(Object.class, beanRenderer);
        table.setDefaultRenderer(String.class, defaultRenderer);
        CodedEnumTableCellRenderer er = new CodedEnumTableCellRenderer();
        table.setDefaultRenderer(ShortCodedEnum.class, er);
        table.setDefaultRenderer(StringCodedEnum.class, er);
        table.setDefaultRenderer(LetterCodedEnum.class, er);
        table.setDefaultRenderer(Date.class, new DateTimeTableCellRenderer());
        table.setDefaultRenderer(Boolean.class, new BooleanTableCellRenderer());
    }

    public static void setPreferredColumnWidths(JTable table) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            int w = calculatePreferredColumnWidth(table, col);
            col.setPreferredWidth(w);
            col.setWidth(w);
        }
    }

    /**
     * Calculates the preferred width of a table column based on the header.
     * 
     * @param table
     * @return the preferred table width
     */
    public static int calculatePreferredColumnWidth(JTable table,
            TableColumn col) {
        TableModel model = table.getModel();
        String colName = model.getColumnName(col.getModelIndex());
        return new JLabel(colName).getPreferredSize().width
                + UIConstants.THREE_SPACES + UIConstants.TWO_SPACES;
    }

    /**
     * Calculates the preferred width of a table based on its total column
     * width.
     * 
     * @param table
     * @return the preferred table width
     */
    public static int calculatePreferredWidth(JTable table) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(table.getColumnModel().getTotalColumnWidth(),
                (int)(screenSize.width * .75));
        return width;
    }

    /**
     * Returns the innermost table model associated with this table; if layers
     * of table model filters are wrapping it.
     */
    public static TableModel getUnfilteredTableModel(JTable table) {
        return getUnfilteredTableModel(table.getModel());
    }

    public static TableModel getUnfilteredTableModel(TableModel tableModel) {
        if (tableModel instanceof AbstractTableModelFilter) { return getUnfilteredTableModel(((AbstractTableModelFilter)tableModel)
                .getFilteredModel()); }
        return tableModel;
    }
}