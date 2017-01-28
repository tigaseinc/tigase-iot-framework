/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.iot.framework.client.client.devices;

import tigase.iot.framework.client.values.Light;

/**
 *
 * @author andrzej
 */
public class LightSensor extends DeviceRemoteConfigAware<Integer, Light, tigase.iot.framework.client.devices.LightSensor> {
	
	public LightSensor(tigase.iot.framework.client.devices.LightSensor sensor) {
		super("light-sensor", "\uD83C\uDF05", sensor);
	}

	public void setValue(Integer value) {
		String str = value == null ? "--" : String.valueOf(value);
		
		setValue(str + "\nlm");
	}
	
}