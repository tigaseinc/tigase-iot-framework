/*
 * DeviceRemoteConfigAware.java
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
package tigase.iot.framework.client.client.devices;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import tigase.iot.framework.client.Device.Configuration;
import tigase.iot.framework.client.client.ClientFactory;
import tigase.iot.framework.client.client.ui.Form;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.forms.JabberDataElement;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extended version of {@link DeviceRemoteAware} which adds support for
 * configuration management of a remote device.
 *
 * @author andrzej
 */
public abstract class DeviceRemoteConfigAware<S, T extends tigase.iot.framework.client.Device.IValue<S>, D extends tigase.iot.framework.client.Device<T>>
		extends DeviceRemoteAware<S, T> {

	protected final D device;
	protected final ClientFactory factory;

	public DeviceRemoteConfigAware(ClientFactory factory, String deviceClass, ImageResource icon, D sensor) {
		super(deviceClass, Icons.getByCategory(sensor.getCategory(), icon), sensor);
		this.device = sensor;
		this.factory = factory;

		Widget w = asWidget();
		w.sinkEvents(Event.ONCLICK);
		w.addHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clicked();
			}
		}, ClickEvent.getType());
	}

	protected void clicked() {
		final DialogBox dialog = new DialogBox(true, true);
		dialog.setStylePrimaryName("dialog-window");

		FlowPanel panel = prepareContextMenu(dialog);

		dialog.setWidget(panel);
		dialog.center();
	}

	public void displayConfiguration(Configuration config) {
		try {
			new ConfigureDeviceDlg(factory, config);
		} catch (JaxmppException ex) {
			Logger.getLogger(DeviceRemoteConfigAware.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	protected FlowPanel prepareContextMenu(DialogBox dialog) {
		FlowPanel panel = new FlowPanel();
		panel.setStylePrimaryName("context-menu");
		Label rename = new Label("Change name");
		rename.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showRenameWindow();
				dialog.hide();
			}
		});
		panel.add(rename);
		Label configure = new Label("Configure");
		configure.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showConfigWindow();
				dialog.hide();
			}
		});
		panel.add(configure);

		Label remove = new Label("Remove");
		remove.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialog.hide();
				removeDevice();
			}
		});
		panel.add(remove);

		return panel;
	}

	private void removeDevice() {
		FlowPanel panel = new FlowPanel();
		panel.setStylePrimaryName("context-menu");

		DialogBox dialog = new DialogBox(true, true);
		dialog.setStylePrimaryName("dialog-window");
		dialog.setTitle("Remove device");

		Label warning = new Label("Do you really want to remove this device?");
		panel.add(warning);

		Button remove = new Button("Yes");
		remove.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				try {
					device.remove(new tigase.iot.framework.client.Device.Callback<Object>() {
						@Override
						public void onError(Stanza response, XMPPException.ErrorCondition errorCondition) {
							Window.alert("Failed to removed device: " + response);
						}

						@Override
						public void onSuccess(Object result) {
							try {
								factory.devices().refreshDevices();
							} catch (JaxmppException ex) {
								Logger.getLogger(DeviceRemoteConfigAware.class.getName()).log(Level.SEVERE, null, ex);
							}
						}
					});
					dialog.hide();
				} catch (JaxmppException ex) {
					Logger.getLogger(DeviceRemoteConfigAware.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});
		panel.add(remove);

		dialog.setWidget(panel);
		dialog.center();
	}

	protected void showConfigWindow() {
		try {
			device.retrieveConfiguration(new tigase.iot.framework.client.Device.Callback<Configuration>() {
				@Override
				public void onError(Stanza response, XMPPException.ErrorCondition errorCondition) {
					Window.alert("Failed to retrieve device configuration: " + response);
				}

				@Override
				public void onSuccess(Configuration result) {
					displayConfiguration(result);
				}

			});
		} catch (JaxmppException ex) {
			Window.alert("Failed to retrieve device configuration: " + ex.getMessage());
		}
	}

	protected void showRenameWindow() {
		final DialogBox dialog = new DialogBox(true, true);
		dialog.setStylePrimaryName("dialog-window");

		FlowPanel panel = new FlowPanel();
		panel.setStylePrimaryName("context-menu");
		Label label = new Label("Rename to:");
		panel.add(label);
		TextBox input = new TextBox();
		input.setText(device.getName());
		panel.add(input);

		Button button = new Button("Confirm");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String name = input.getText();
				if (name == null || name.isEmpty() || device.getName().equals(name)) {
					return;
				}
				dialog.hide();
				try {
					device.setName(name, new tigase.iot.framework.client.Device.Callback<String>() {
						@Override
						public void onError(Stanza response, XMPPException.ErrorCondition errorCondition) {
							Window.alert("Could not rename device:" + response);
						}

						@Override
						public void onSuccess(String name) {
							setDescription(name);
						}
					});
				} catch (JaxmppException ex) {
					Logger.getLogger(DeviceRemoteConfigAware.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});
		panel.add(button);
		dialog.setWidget(panel);

		dialog.center();
	}

	private class ConfigureDeviceDlg {

		private final Button advancedButton;
		private final Button confirmButton;

		public ConfigureDeviceDlg(ClientFactory factory, Configuration config) throws JaxmppException {
			FlowPanel panel = new FlowPanel();
			panel.setStylePrimaryName("context-menu");

			Form form = new Form();
			form.setData(config.getValue());

			DialogBox dialog = new DialogBox(true, true);
			dialog.setStylePrimaryName("dialog-window");
			dialog.setTitle("Configuration");

			panel.add(form);

			if (form.hasAdvanced()) {
				advancedButton = new Button("Show advanced");
				advancedButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						try {
							form.showAdvanced(!form.isAdvancedVisible());
							if (form.isAdvancedVisible()) {
								advancedButton.setHTML("Show basic");
							} else {
								advancedButton.setHTML("Show advanced");
							}
							confirmButton.setVisible(!form.isAdvancedVisible());
						} catch (XMLException ex) {
							// should not happen..
						}
					}
				});
				panel.add(advancedButton);
			} else {
				advancedButton = null;
			}

			confirmButton = new Button("Confirm");
			confirmButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					try {
						JabberDataElement config = form.getData();
						dialog.hide();

						device.setConfiguration(config,
												new tigase.iot.framework.client.Device.Callback<Configuration>() {
													public void onError(Stanza response,
																		XMPPException.ErrorCondition errorCondition) {

													}

													public void onSuccess(Configuration result) {

													}
												});
					} catch (JaxmppException ex) {
						Logger.getLogger(DeviceRemoteConfigAware.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			});
			panel.add(confirmButton);

			dialog.setWidget(panel);
			dialog.center();
		}

	}
}
