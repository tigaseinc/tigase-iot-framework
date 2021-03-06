/*
 * SubscriptionModule.java
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
package tigase.iot.framework.client.modules;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.criteria.Or;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class SubscriptionModule
		extends AbstractIQModule {

	public static final String SUBSCRIPTION_XMLNS = "http://tigase.org/protocol/subscriptions#iot";

	private static final Criteria CRITERIA = ElementCriteria.name("iq")
			.add(new Or(ElementCriteria.name("subscription", SUBSCRIPTION_XMLNS),
						ElementCriteria.name("usage", SUBSCRIPTION_XMLNS)));

	@Override
	public Criteria getCriteria() {
		return CRITERIA;
	}

	@Override
	public String[] getFeatures() {
		return new String[0];
	}

	@Override
	protected void processGet(IQ iq) throws JaxmppException {
		throw new XMPPException(XMPPException.ErrorCondition.bad_request);
	}

	@Override
	protected void processSet(IQ iq) throws JaxmppException {
		Element el = iq.getChildrenNS("subscription", SUBSCRIPTION_XMLNS);
		if (el != null) {
			processSubscription(Subscription.fromElement(el));
		}
		el = iq.getChildrenNS("usage", SUBSCRIPTION_XMLNS);
		if (el != null) {
			processSubscriptionUsage(SubscriptionUsage.fromElement(el));
		}

		IQ result = IQ.create();
		result.setId(iq.getId());
		result.setType(StanzaType.result);
		result.setTo(iq.getFrom());
		write(result);
	}

	public void retrieveSubscription(final SubscriptionCallback callback) throws JaxmppException {
		Element iq = ElementFactory.create("iq");
		iq.setAttribute("type", "get");
		iq.addChild(ElementFactory.create("subscription", null, SUBSCRIPTION_XMLNS));
		context.getWriter().write(iq, new SubscriptionCallback() {
			@Override
			public void onError(Stanza stanza, XMPPException.ErrorCondition errorCondition) throws JaxmppException {
				if (callback != null) {
					callback.onError(stanza, errorCondition);
				}
			}

			@Override
			public void onSuccess(Subscription subscription) throws JaxmppException {
				processSubscription(subscription);
				if (callback != null) {
					callback.onSuccess(subscription);
				}
			}

			@Override
			public void onTimeout() throws JaxmppException {
				if (callback != null) {
					callback.onTimeout();
				}
			}
		});
	}

	protected void processSubscription(Subscription subscription) throws XMLException {
		if (subscription != null) {
			context.getEventBus()
					.fire(new SubscriptionChangedHandler.SubscriptionChangedEvent(context.getSessionObject(),
																				  subscription));
		}
	}

	protected void processSubscriptionUsage(SubscriptionUsage usage) throws XMLException {
		if (usage != null) {
			context.getEventBus()
					.fire(new SubscriptionUsageChangedHandler.SubscriptionUsageChangedEvent(context.getSessionObject(),
																							usage));
		}
	}

	public interface SubscriptionChangedHandler
			extends EventHandler {

		void subscriptionChanged(SessionObject sessionObject, Subscription subscription);

		class SubscriptionChangedEvent
				extends JaxmppEvent<SubscriptionChangedHandler> {

			public final Subscription subscription;

			SubscriptionChangedEvent(SessionObject sessionObject, Subscription subscription) {
				super(sessionObject);
				this.subscription = subscription;
			}

			@Override
			public void dispatch(SubscriptionChangedHandler subscriptionChangedHandler) throws Exception {
				subscriptionChangedHandler.subscriptionChanged(sessionObject, subscription);
			}
		}
	}

	public interface SubscriptionUsageChangedHandler
			extends EventHandler {

		void subscriptionUsageChanged(SessionObject sessionObject, SubscriptionUsage subscriptionUsage);

		class SubscriptionUsageChangedEvent
				extends JaxmppEvent<SubscriptionUsageChangedHandler> {

			private final SubscriptionUsage usage;

			protected SubscriptionUsageChangedEvent(SessionObject sessionObject, SubscriptionUsage usage) {
				super(sessionObject);
				this.usage = usage;
			}

			@Override
			public void dispatch(SubscriptionUsageChangedHandler handler) throws Exception {
				handler.subscriptionUsageChanged(sessionObject, usage);
			}
		}

	}

	public static class Subscription {

		public final String id;
		public final int devices;
		public final double changesPerMinute;

		Subscription(String id, int devices, double changesPerMinute) {
			this.id = id;
			this.devices = devices;
			this.changesPerMinute = changesPerMinute;
		}

		private static Subscription fromElement(Element elem) throws XMLException {
			if (elem == null) {
				throw new IllegalArgumentException("null");
			}

			String id = elem.getAttribute("id");
			int devices = -1;
			double changesPerMinute = -1.0;
			String tmp = elem.getAttribute("devices-limit");
			if (tmp != null) {
				devices = Integer.parseInt(tmp);
			}
			tmp = elem.getAttribute("changes-per-minute");
			if (tmp != null) {
				changesPerMinute = Double.parseDouble(tmp);
			}

			return new Subscription(id, devices, changesPerMinute);
		}
	}

	public static abstract class SubscriptionCallback
			implements AsyncCallback {

		public abstract void onSuccess(Subscription subscription) throws JaxmppException;

		@Override
		public void onSuccess(Stanza responseStanza) throws JaxmppException {
			onSuccess(Subscription.fromElement(responseStanza.getChildrenNS("subscription", SUBSCRIPTION_XMLNS)));
		}
	}

	public static class SubscriptionUsage {

		public final double changesPerMinute;
		public final Double changesPerMinuteAvg;

		SubscriptionUsage(double changesPerMinute, Double changesPerMinuteAvg) {
			this.changesPerMinute = changesPerMinute;
			this.changesPerMinuteAvg = changesPerMinuteAvg;
		}

		private static SubscriptionUsage fromElement(Element elem) throws XMLException {
			if (elem == null) {
				throw new IllegalArgumentException("null");
			}

			double changesPerMinute = -1.0;
			String tmp = elem.getAttribute("changes-per-minute");
			if (tmp != null) {
				changesPerMinute = Double.parseDouble(tmp);
			}

			Double changesPerMinuteAvg = null;
			tmp = elem.getAttribute("changes-per-minute-avg");
			if (tmp != null) {
				changesPerMinuteAvg = Double.parseDouble(tmp);
			}

			return new SubscriptionUsage(changesPerMinute, changesPerMinuteAvg);
		}

	}
}
