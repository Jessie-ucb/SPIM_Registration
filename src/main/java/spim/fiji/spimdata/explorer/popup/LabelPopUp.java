/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2007 - 2017 Fiji developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */
package spim.fiji.spimdata.explorer.popup;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

import spim.fiji.spimdata.explorer.ViewSetupExplorerPanel;

public class LabelPopUp extends JLabel implements ViewExplorerSetable
{
	private static final long serialVersionUID = 5234649267634013390L;

	public LabelPopUp( final String text )
	{
		super( text );
		this.setFont( new Font( Font.SANS_SERIF, Font.ITALIC, 11 ) );
		this.setForeground( new Color( 128, 128, 128 ));
	}

	@Override
	public JLabel setViewExplorer( final ViewSetupExplorerPanel< ?, ? > panel )
	{
		return this;
	}
}
