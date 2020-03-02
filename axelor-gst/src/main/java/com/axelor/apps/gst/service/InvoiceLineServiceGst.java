package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import java.util.Map;

public interface InvoiceLineServiceGst {
	public Map<String, Object> getGstValues(Boolean isStateMatched, InvoiceLine invoiceLine);

	public Boolean checkIsStateMatched(Invoice invoice);
}
