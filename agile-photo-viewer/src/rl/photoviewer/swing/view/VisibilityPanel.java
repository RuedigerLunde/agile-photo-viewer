/*
 * Copyright (C) 2013 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.swing.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import rl.photoviewer.model.PVModel;
import rl.photoviewer.swing.controller.Controller;

public class VisibilityPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	PVModel pvModel;
	Controller controller;

	private final JComboBox<String> ratingCombo;
	private final JTable keywordTable;
	private final JTextArea keywordTextArea;
	private final JToggleButton notButton;

	public VisibilityPanel(PVModel model, Controller controller) {
		pvModel = model;
		this.controller = controller;
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setBackground(Color.DARK_GRAY);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		// maximum height and width for all components
		c.fill = GridBagConstraints.BOTH;

		// adjust free space around the components (in pixel)
		c.insets = new Insets(5, 5, 5, 5);

		ratingCombo = new JComboBox<String>(new String[] { "No Rating Filter",
				">= *", ">= **", ">= ***", ">= ****", ">= *****" });
		ratingCombo.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				ActionEvent ae = new ActionEvent(this, 0,
						Commands.SET_RATING_FILTER);
				VisibilityPanel.this.controller.actionPerformed(ae);
			}});
		ratingCombo.setFocusable(false);
		addAt(ratingCombo, c, 0, 0, 3, 0);
		
		keywordTable = new JTable(new KeywordTableModel("Specify Keyword Expression"));
		keywordTable.addKeyListener(controller);
		keywordTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting() == false) {
							ActionEvent ae = new ActionEvent(this, 0,
									Commands.KEYWORDS_CHANGED_SELECTION_CMD);
							VisibilityPanel.this.controller.actionPerformed(ae);
						}
					}
				});
		JScrollPane scroller = new JScrollPane(keywordTable);
		addAt(scroller, c, 0, 1, 3, 0.95);

		notButton = new JToggleButton("Not");
		notButton.setActionCommand(Commands.KEYWORDS_NEGATION_CMD);
		notButton.addActionListener(controller);
		notButton.setFocusable(false);
		addAt(notButton, c, 0, 2, 1, 0);
		JButton button = new JButton("And");
		button.setActionCommand(Commands.KEYWORDS_ADD_CLAUSE_CMD);
		button.addActionListener(controller);
		button.setFocusable(false);
		addAt(button, c, 1, 2, 1, 0);
		button = new JButton("Delete");
		button.setActionCommand(Commands.KEYWORDS_DELETE_CMD);
		button.addActionListener(controller);
		button.setFocusable(false);
		addAt(button, c, 2, 2, 1, 0);

		keywordTextArea = new JTextArea();
		keywordTextArea.setBackground(Color.DARK_GRAY);
		keywordTextArea.setForeground(Color.LIGHT_GRAY);
		keywordTextArea.setEditable(false);
		keywordTextArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		addAt(keywordTextArea, c, 0, 3, 3, 0.05);
	}

	/**
	 * Helper method, adding a component to a container at a certain grid
	 * position. The container is assumed to have a GridBagLayout.
	 */
	private void addAt(Component comp, GridBagConstraints c, int gridx,
			int gridy, int gridwidth, double weight) {
		c.gridx = gridx;
		c.gridy = gridy;
		c.gridwidth = gridwidth;
		// share extra space
		c.weightx = 0.5;
		c.weighty = weight;
		add(comp, c);
	}

	public void resetSelectedRating() {
		ratingCombo.setSelectedIndex(0);
	}
	
	public int getSelectedRating() {
		return ratingCombo.getSelectedIndex();
	}
	
	public List<String> getSelectedKeywords() {
		if (keywordTable.getSelectionModel().isSelectionEmpty())
			return Collections.emptyList();
		else {
			List<String> result = new ArrayList<String>();
			ListSelectionModel sm = keywordTable.getSelectionModel();
			TableModel tm = keywordTable.getModel();
			for (int i = 0; i < tm.getRowCount(); i++) {
				if (sm.isSelectedIndex(i))
					result.add(pvModel.getAllKeywords().get(i));
			}
			return result;
		}
	}

	public boolean isNegationSelected() {
		return notButton.isSelected();
	}

	public void setNegationSelected(boolean b) {
		notButton.setSelected(b);
	}

	public void setText(String text) {
		keywordTextArea.setText(text);
	}

	public void clear() {
		boolean changed = !keywordTable.getSelectionModel().isSelectionEmpty();
		notButton.setSelected(false);
		keywordTable.getSelectionModel().clearSelection();
		if (!changed) { // update expression text even if selection has not
						// changed
			ActionEvent e = new ActionEvent(this, 0,
					Commands.KEYWORDS_CHANGED_SELECTION_CMD);
			controller.actionPerformed(e);
		}
	}

	public void setFont(Font font) {
		super.setFont(font);
		if (keywordTable != null) {
			keywordTable.setFont(font);
			keywordTable.setRowHeight(font.getSize() + 4);
			keywordTextArea.setFont(font);
		}
	}

	private class KeywordTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		private String colName;

		public KeywordTableModel(String title) {
			colName = title;
			pvModel.addObserver(new Observer() {
				@Override
				public void update(Observable source, Object arg) {
					if (arg == PVModel.METADATA_CHANGED)
						fireTableChanged(new TableModelEvent(
								KeywordTableModel.this));
				}
			});
		}

		@Override
		public String getColumnName(int col) {
			return colName;
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public int getRowCount() {
			return pvModel.getAllKeywords().size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			return pvModel.getAllKeywords().get(row) + " ("
					+ pvModel.getKeywordCounts().get(row) + ")";
		}
	}
}
