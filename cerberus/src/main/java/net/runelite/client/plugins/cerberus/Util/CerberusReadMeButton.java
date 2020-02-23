package net.runelite.client.plugins.cerberus.Util;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import javax.swing.JOptionPane;

public class CerberusReadMeButton implements ActionListener
{
	private static String url = "https://github.com/Im2be/plugins-source/tree/master/cerberus/src/main/resources/net/runelite/client/plugins/cerberus/README.md";

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
		{
			try
			{
				Desktop.getDesktop().browse(new URI(url));
			}
			catch (Exception ex)
			{
				JOptionPane.showConfirmDialog(null, "Could not open '" + url + "' in your browser!");
			}
		}
	}
}
