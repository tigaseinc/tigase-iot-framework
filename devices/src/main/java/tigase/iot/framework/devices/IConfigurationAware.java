/*
 * IConfigurationAware.java
 *
 * Tigase IoT Framework
 * Copyright (C) 2011-2017 "Tigase, Inc." <office@tigase.com>
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

package tigase.iot.framework.devices;

/**
 * Interface implemented by all components of Tigase IoT Framework which are configuration
 * aware and required to be registered for configuration and reconfiguration using PubSub
 * protocol.
 *
 * Created by andrzej on 04.11.2016.
 */
public interface IConfigurationAware {

	/**
	 * Retrieve name of a component
	 *
	 * @return name
	 */
	String getName();

	/**
	 * Retrieve label of a component
	 * @return
	 */
	String getLabel();
}
