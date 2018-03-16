package com.axelor.apps.prestashop.entities;

/**
 * Type of resources handled by Prestashop's API.
 * Only those actually used by Axelor sync are declared,
 * feel free to enhance.
 */
public enum PrestashopResourceType {
	ADDRESSES("addresses"),
	CARTS("carts"),
	COUNTRIES("countries"),
	CURRENCIES("currencies"),
	CUSTOMERS("customers"),
	DELIVERIES("deliveries"),
	IMAGES("images"),
	LANGUAGES("languages"),
	ORDER_DETAILS("order_details"),
	ORDER_HISTORIES("order_histories"),
	ORDER_INVOICES("order_invoices"),
	ORDER_PAYMENTS("order_payments"),
	ORDER_STATUSES("order_states"),
	ORDERS("orders"),
	PRODUCT_CATEGORIES("categories"),
	PRODUCTS("products"),
	// No typo… really
	STOCK_AVAILABLES("stock_availables");

	final String label;

	private PrestashopResourceType(final String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}