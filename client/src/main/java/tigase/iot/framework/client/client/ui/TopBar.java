/*
 * TopBar.java
 *
 * Tigase IoT Framework
 * Copyright (C) 2011-2018 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.iot.framework.client.client.ui;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 *
 * @author andrzej
 */
public class TopBar extends Composite {
	
	private final FlowPanel panel;
	private final Label title;
	
	public TopBar(String name) {
		panel = new FlowPanel();
		panel.setStylePrimaryName("top-bar");
		title = new Label(name);
		panel.add(title);
		
		initWidget(panel);
	}
	
	public void setTitle(String title) {
		this.title.setText(title);
	}
	
	public Label addAction(String name, ClickHandler action) {
		Label close = new Label(name);
		close.setStylePrimaryName("top-bar-action");
		panel.add(close);
		if (action != null) {
			close.addClickHandler(action);
		}
		return close;
	}
	
}
