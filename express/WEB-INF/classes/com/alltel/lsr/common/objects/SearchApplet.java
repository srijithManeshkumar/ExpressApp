package com.alltel.lsr.common.objects;

// JDK import
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

import com.alltel.lsr.common.objects.*;

public class SearchApplet extends Applet implements ActionListener
{
	private TextField queryField;
	private Button submitButton;

	public void init()
	{
		setFont(new Font("Serif",Font.BOLD, 18));
		add(new Label("Search String"));
		queryField=new TextField(40);
		queryField.addActionListener(this);
		add(queryField);
		submitButton = new Button("Send to Search Engines");
		submitButton.addActionListener(this);
		add(submitButton);
	}
	public void actionPerformed(ActionEvent event)
	{
		String query=URLEncoder.encode(queryField.getText());
		Out[] commonSpecs = Out.getCommonSpecs();
		for (int i=0;i<commonSpecs.length;i++)
		{
			try {
				Out spec = commonSpecs[i];
				URL searchURL = new URL(spec.makeURL(query, "10"));
				String frameName = "results" + i;
				getAppletContext().showDocument(searchURL,frameName);
			}
			catch(MalformedURLException mue) {}
		}
	}
}
