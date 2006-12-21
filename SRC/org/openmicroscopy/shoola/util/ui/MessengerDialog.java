/*
 * org.openmicroscopy.shoola.util.ui.MessengerDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui;



//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies

/** 
 * A dialog used to collect and send  comments or error messages.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * after code from 
 * @author Brian Loranger &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:brian.loranger@lifesci.dundee.ac.uk">
 * brian.loranger@lifesci.dundee.ac.uk</a>
 * 
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MessengerDialog 
	extends JDialog
{

	/** Identifies the error dialog type. */
	public static final int			ERROR_TYPE = 0;
	
	/** Identifies the error dialog type. */
	public static final int			COMMENT_TYPE = 1;
	
	/** Bound property indicating to send the message. */
	public static final String		SEND_PROPERTY = "send";
	
	/** The default size of the window. */
	private static final Dimension 	DEFAULT_SIZE = new Dimension(700, 400);
	
	/** The tooltip of the {@link #cancelButton}. */
	private static final String		CANCEL_TOOLTIP = "Cancel your message";
	
	/** The tooltip of the {@link #sendButton}. */
	private static final String		SEND_TOOLTIP = "Send the information to " +
										"the development team";
	
	/** The tooltip of the {@link #copyButton}. */
	private static final String		COPY_TOOLTIP = "Copy the Exception " +
									"Message to the clipboard";
	
	/** The default message displayed. */
	private static final String		MESSAGE = "Thank you for taking the time " +
			"to send us your comments. \n\n" +
			"Your feedback will be used to further the development of " +
			"OMERO and improve our software. Any personal details you " +
			"provide are purely optional, and will only be used for " +
			"development purposes.";
	
	/** The default message displayed. */
	private static final String		DEBUG_MESSAGE = "An error message has " +
			"been generated by the application.\n\n" +
			"To help us improve our software, please fill " +
			"out the following form. Your personal details are purely " +
			"optional, and will only be used for development purposes.\n\n" +
			"Please note that your application may need to be restarted " +
			"to work properly.";
	
	/** Value of the  comment field. */
	private static final String		COMMENT_FIELD = "Comment: ";
	
	/** Value of the comment field when an exception is specified. */
	private static final String		DEBUG_COMMENT_FIELD ="What you were doing" +
														" when you crashed?";
	
	/** Value of the field. */
	private static final String		EMAIL_FIELD = "Email: ";
	
	/** The default tooltip of the e-mail area. */
	private static final String 	EMAIL_TOOLTIP = "Enter your email " +
												"address here.";
	
	/** The e-mail field's suffix. */
	private static final String		EMAIL_SUFFIX = " (Optional)";

	/** 
	 * One of the following constants: {@link #ERROR_TYPE} or 
	 * {@link #COMMENT_TYPE}.
	 */
	private int				dialogType;
	
	/** Button to close and dispose of the window. */
	private JButton 		cancelButton;
	
	/** Button to post the message. */
	private JButton			sendButton;
	
	/** The area displaying the <code>e-mail address</code>. */
	private JTextField		emailArea;
	
	/** The comment Area. */
	private MultilineLabel	commentArea;

	/** The e-mail address of the user submitting the message. */
	private String			emailAddress;
	
	/** The execption to handle, <code>null</code> if no exception. */
	private Exception		exception;
	
	/** The text pane displaying the error message. */
	private JTextPane		debugArea;
	
	/** Button to copy the message on the clipBoard. */
	private JButton			copyButton;
	
	/**
	 * Formats the specified button.
	 * 
	 * @param b			The button to format.
	 * @param mnemonic	The keycode that indicates a mnemonic key.
	 * @param tooltip	The button's tooltip.
	 */
	private void formatButton(JButton b, int mnemonic, String tooltip)
	{
		b.setMnemonic(mnemonic);
        b.setOpaque(false);
        b.setToolTipText(tooltip);
	}
    
    /** Hides the window and disposes. */
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/** Copies the error message on the clipboard. */
	private void copy()
	{
		if (debugArea != null) {
			debugArea.selectAll();
			debugArea.copy();
		}
	}
	
	/** Sends the message. */
	private void send()
	{
		String email = emailArea.getText().trim();
		String comment = commentArea.getText().trim();
		String error = null;
		if (debugArea != null)  error = debugArea.getText().trim();
		MessengerDetails details = new MessengerDetails(email, comment);
		details.setExtra("Add extra info");
		details.setError(error); 
		firePropertyChange(SEND_PROPERTY, null, details);
	}
	
	/** Initializes the various components. */
	private void initComponents()
	{
		cancelButton = new JButton("Cancel");
		formatButton(cancelButton, 'C', CANCEL_TOOLTIP);
		cancelButton.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) { close(); }
		
		});
		sendButton = new JButton("Send");
		formatButton(sendButton, 'S', SEND_TOOLTIP);
		sendButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) { send(); }
		
		});
		
        emailArea = new JTextField(20);
        emailArea.setToolTipText(EMAIL_TOOLTIP);
        emailArea.setText(emailAddress);
        commentArea = new MultilineLabel();
        commentArea.setEditable(true);
        commentArea.setOpaque(true);
        if (exception != null) {
        	debugArea = buildExceptionArea();
        	copyButton = new JButton("Copy to Clipboard");
        	formatButton(cancelButton, 'C', COPY_TOOLTIP);
        	copyButton.addActionListener(new ActionListener() {
        		
    			public void actionPerformed(ActionEvent e) { copy(); }
    		
    		});
        }
	}

	/**
	 * Builds the UI component displayin the exception.
	 * 
	 * @return See above.
	 */
	private JTextPane buildExceptionArea()
	{
		StyleContext context = new StyleContext();
        StyledDocument document = new DefaultStyledDocument(context);

        JTextPane textPane = new JTextPane(document);
        textPane.setOpaque(false);
        textPane.setEditable(false);

        // Create one of each type of tab stop
        java.util.List list = new ArrayList();
        
        // Create a left-aligned tab stop at 100 pixels from the left margin
        float pos = 15;
        int align = TabStop.ALIGN_LEFT;
        int leader = TabStop.LEAD_NONE;
        TabStop tstop = new TabStop(pos, align, leader);
        list.add(tstop);
        
        // Create a right-aligned tab stop at 200 pixels from the left margin
        pos = 15;
        align = TabStop.ALIGN_RIGHT;
        leader = TabStop.LEAD_NONE;
        tstop = new TabStop(pos, align, leader);
        list.add(tstop);
        
        // Create a center-aligned tab stop at 300 pixels from the left margin
        pos = 15;
        align = TabStop.ALIGN_CENTER;
        leader = TabStop.LEAD_NONE;
        tstop = new TabStop(pos, align, leader);
        list.add(tstop);
        
        // Create a decimal-aligned tab stop at 400 pixels from the left margin
        pos = 15;
        align = TabStop.ALIGN_DECIMAL;
        leader = TabStop.LEAD_NONE;
        tstop = new TabStop(pos, align, leader);
        list.add(tstop);
        
        // Create a tab set from the tab stops
        TabStop[] tstops = (TabStop[]) list.toArray(new TabStop[0]);
        TabSet tabs = new TabSet(tstops);
        
        // Add the tab set to the logical style;
        // the logical style is inherited by all paragraphs
        Style style = textPane.getLogicalStyle();
        StyleConstants.setTabSet(style, tabs);
        textPane.setLogicalStyle(style);
        Style debugStyle = document.addStyle("StyleName", null);
        StyleConstants.setForeground(debugStyle, Color.BLACK);
        StyleConstants.setFontFamily(debugStyle, "SansSerif");
        StyleConstants.setFontSize(debugStyle, 12);
        StyleConstants.setBold(debugStyle, false);
        //Get the full debug text
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        try {
        	document.insertString(document.getLength(), sw.toString(), style);
        } catch (BadLocationException e) {}
        return textPane;
	}
	
	/**
	 * Builds and lays out the panel hosting the <code>comment</code> details.
	 * 
	 * @param comment		The comment's text.
	 * @param mnemonic 		The keycode that indicates a mnemonic key.
	 * @return See above.
	 */
	private JPanel buildCommentAreaPanel(String comment, int mnemonic)
	{
		JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        double size[][] = {{TableLayout.FILL}, {20, TableLayout.FILL}};
        TableLayout layout = new TableLayout(size);
        panel.setLayout(layout);
        JScrollPane areaScrollPane = new JScrollPane(commentArea);
        areaScrollPane.setVerticalScrollBarPolicy(
        		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        JLabel label = new JLabel(comment);
        label.setOpaque(false);
        label.setDisplayedMnemonic(mnemonic);
        panel.add(label, "0, 0, l, c");
        panel.add(areaScrollPane, "0, 1, f, f");
        return panel;
	}
	
	/**
	 * Builds and lays out the panel hosting the <code>email</code> details.
	 * 
	 * @param mnemonic The keycode that indicates a mnemonic key.
	 * @return See above.
	 */
	private JPanel buildEmailAreaPanel(int mnemonic)
	{
		double[][] size = null;
        
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        if (EMAIL_SUFFIX.length() == 0)
            size = new double[][]{{TableLayout.PREFERRED, TableLayout.FILL}, 
        							{30}};
        else
            size = new double[][] 
                   {{TableLayout.PREFERRED,TableLayout.FILL, 
                	   TableLayout.PREFERRED}, {30}};
     
        TableLayout layout = new TableLayout(size);
        panel.setLayout(layout);       

        JLabel label = new JLabel(EMAIL_FIELD);
        label.setDisplayedMnemonic(mnemonic);
        label.setLabelFor(emailArea);
        label.setOpaque(false);

        panel.add(label, "0, 0, r, c");        
        panel.add(emailArea, "1, 0, f, c");

        if (EMAIL_SUFFIX.length() != 0)
            panel.add(new JLabel(EMAIL_SUFFIX), "2,0, l, c");

		return panel;
	}
	
	/**
	 * Builds and lays out the panel hosting the debug information.
	 * 
	 * @return See above.
	 */
	private JPanel buildDebugPanel()
	{
		JPanel panel = new JPanel();
        panel.setOpaque(false);
        double tableSize[][] = {{TableLayout.FILL}, // columns
        						{TableLayout.FILL, 32}}; // rows
        TableLayout layout = new TableLayout(tableSize);
        panel.setLayout(layout);       
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(debugArea), "0, 0");
        panel.add(copyButton, "0, 1, c, b");
        return panel;
	}
	
	/**
	 * Builds and lays out the panel hosting the comments.
	 * 
	 * @param instructions	The message explaining to the user what to do.
	 * @param comment		The comment's text.
	 * @param icon			The icon to display.
	 * @return See above.
	 */
	private JPanel buildCommentPanel(String instructions, String comment, 
									Icon icon)
	{
		JPanel commentPanel = new JPanel();
        int iconSpace = 0;
        if (icon != null) iconSpace = icon.getIconWidth()+20;
        
        double tableSize[][] =  
        		{{iconSpace, (160 - iconSpace), TableLayout.FILL}, // columns
                {100, 30, TableLayout.FILL}}; // rows
        TableLayout layout = new TableLayout(tableSize);
        commentPanel.setLayout(layout);  
        commentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        if (icon != null)
        	commentPanel.add(new JLabel(icon), "0, 0, l, c");
        commentPanel.add(UIUtilities.buildTextPane(instructions), "1, 0, 2, 0");
        commentPanel.add(buildEmailAreaPanel('E'), "0, 1, 2, 1");
        commentPanel.add(buildCommentAreaPanel(comment, 'W'), "0, 2, 2, 2");
		return commentPanel;
	}
	
	/** 
	 * Builds the UI component hosting the debug information.
	 * 
	 * @return Se above
	 */
	private JTabbedPane buildExceptionPane()
	{
        JTabbedPane tPane = new JTabbedPane();
        tPane.setOpaque(false);
        IconManager icons = IconManager.getInstance();
        Icon icon = icons.getIcon(IconManager.ERROR_ICON_64);
        if (icon == null) icon = UIManager.getIcon("OptionPane.errorIcon");
        tPane.addTab("Comments", null, 
        		buildCommentPanel(DEBUG_MESSAGE, DEBUG_COMMENT_FIELD, icon), 
        		"Your comments go here.");
        tPane.addTab("Error Message", null, buildDebugPanel(),
        			"The Exception Message.");
		return tPane;
	}
	
	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		double tableSize[][] = {{TableLayout.FILL, 100, 5, 100, 10}, // columns
								{TableLayout.FILL, 40}}; // rows
        TableLayout layout = new TableLayout(tableSize);
        mainPanel.setLayout(layout);       
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        //Add the buttons.
        mainPanel.add(cancelButton, "1, 1, f, c");
        mainPanel.add(sendButton, "3, 1, f, c");
        JComponent component;
        if (exception == null) {
        	IconManager icons = IconManager.getInstance();
            Icon icon = icons.getIcon(IconManager.COMMENT_ICON_64);
            if (icon == null)
            	icon = UIManager.getIcon("OptionPane.questionIcon");
        	component = buildCommentPanel(MESSAGE, COMMENT_FIELD, icon);
        } else {
        	component = buildExceptionPane();
        }
        mainPanel.add(component, "0, 0, 4, 0");
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}
	
	/** 
	 * Initializes the dialog.
	 * 
	 *  @param title The title of the dialog.
	 */
	private void initialize(String title)
	{
		setTitle(title);
		setSize(DEFAULT_SIZE);
		initComponents();
		buildGUI();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent		The parent of this dialog.
	 * @param title			The dialog's title.
	 * @param emailAddress	The e-mail address of the current user.
	 */
	public MessengerDialog(JFrame parent, String title, String emailAddress)
	{
		super(parent);
		this.emailAddress = emailAddress;
		dialogType = COMMENT_TYPE;
		initialize(title);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent		The parent of this dialog.
	 * @param title			The dialog's title.
	 * @param emailAddress	The e-mail address of the current user.
	 * @param teamAddress	The e-mail address of the development team.
	 * @param exception		The exception to handle.
	 */
	public MessengerDialog(JFrame parent, String title, String emailAddress, 
						Exception exception)
	{
		super(parent);
		dialogType = ERROR_TYPE;
		this.emailAddress = emailAddress;
		this.exception = exception;
		initialize(title);
	}	
	
	/**
	 * Returns the type associated to this widget. 
	 * 
	 * @return See above.
	 */
	public int getDialogType() { return dialogType; }
	
}
