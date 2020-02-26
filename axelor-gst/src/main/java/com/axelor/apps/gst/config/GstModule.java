package com.axelor.apps.gst.config;

import com.axelor.app.AxelorModule;
import com.axelor.apps.businessproject.service.InvoiceLineProjectServiceImpl;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.apps.gst.service.InvoiceServiceGstImpl;
import com.axelor.apps.gst.service.InvoiceLineServiceGst;
import com.axelor.apps.gst.service.InvoiceLineServiceGstImpl;

public class GstModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(InvoiceLineServiceGst.class).to(InvoiceLineServiceGstImpl.class);
    bind(InvoiceLineProjectServiceImpl.class).to(InvoiceLineServiceGstImpl.class);
    bind(InvoiceServiceProjectImpl.class).to(InvoiceServiceGstImpl.class);
    
  }
}
