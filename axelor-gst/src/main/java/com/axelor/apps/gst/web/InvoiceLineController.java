package com.axelor.apps.gst.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.gst.service.InvoiceLineServiceGst;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class InvoiceLineController {

	@Inject
	InvoiceLineServiceGst invoiceLineService;

	public void computeGstRates(ActionRequest request, ActionResponse response) {
		if (Beans.get(AppService.class).isApp("gst")) {
			try {
				Invoice parent = request.getContext().getParent().asType(Invoice.class);
				InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
				Map<String, Object> gstValues = new HashMap<>();
				System.err.println("testing");
				gstValues.putAll(invoiceLineService.getGstValues(parent, invoiceLine));
				gstValues.forEach((key, value) -> {
					response.setValue(key, value);
				});
//      System.err.println(parent.getAddressStr() + " || " + invoiceLine.getProductName());
			} catch (Exception e) {
				System.err.println("ERROR-compute-gst-rate");
				// e.printStackTrace();
			}
		}
	}
}
