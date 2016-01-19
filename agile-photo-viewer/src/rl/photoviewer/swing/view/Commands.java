/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.swing.view;

/**
 * Defines constants for action commands.
 * 
 * @author Ruediger Lunde
 * 
 */
public abstract class Commands {
	public static String SELECT_CMD = "SelectCmd";
	public static String FIRST_CMD = "FirstCmd";
	public static String PREV_CMD = "PrevCmd";
	public static String NEXT_CMD = "NextCmd";
	public static String SLIDE_SHOW_CMD = "SlideShowCmd";
	public static String FULL_SCREEN_CMD = "FullScreenCmd";
	public static String DECORATION_CMD = "DecorationCmd";
	public static String SORT_BY_DATE_CMD = "SortByDateCmd";
	public static String ABOUT_CMD = "HelpCmd";
	public static String USE_PHOTO_AS_MAP_CMD = "UsePhotoAsMapCmd";
	public static String CLEAR_MAP_CMD = "ClearMapCmd";
	public static String EXPORT_CMD = "ExportCmd";
	public static String DELETE_SELECTED_PHOTO_CMD = "DeleteSelectedPhotoCmd";
	public static String STORE_SESSION_CMD = "StoreSessionCmd";
	public static String RESTORE_SESSION_CMD = "RestoreSessionCmd";
	public static String EXIT_CMD = "ExitCmd";
	
	public static String SHOW_CAPTION_IN_STATUS_CMD = "ShowCaptionInStatusCmd";
	public static String INC_FONT_SIZE_CMD = "IncFontSizeCmd";
	public static String DEC_FONT_SIZE_CMD = "DecFontSizeCmd";
	
	public static String SET_RATING_FILTER = "SetRatingFilterCmd";
	public static String KEYWORDS_CHANGED_SELECTION_CMD = "KeywordsChangeSelectionCmd";
	public static String KEYWORDS_NEGATION_CMD = "KeywordsNegationCmd";
	public static String KEYWORDS_ADD_CLAUSE_CMD = "KeywordsPushCmd";
	//public static String KEYWORDS_CLEAR_CMD = "KeywordsClearCmd";
	public static String KEYWORDS_DELETE_CMD = "KeywordsDeleteCmd";
}
