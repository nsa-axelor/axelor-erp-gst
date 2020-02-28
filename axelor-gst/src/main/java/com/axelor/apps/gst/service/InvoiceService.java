package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import java.util.List;

public interface InvoiceService {
  public Invoice getCalculatedInvoice(Invoice invoice, List<InvoiceLine> lineList);

  public List<InvoiceLine> recalculateInvoiceLine(Invoice invoice);
}
