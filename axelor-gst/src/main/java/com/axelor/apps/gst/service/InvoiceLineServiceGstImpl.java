package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.businessproject.service.InvoiceLineProjectServiceImpl;
import com.axelor.apps.gst.db.State;
import com.axelor.apps.purchase.service.PurchaseProductService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class InvoiceLineServiceGstImpl extends InvoiceLineProjectServiceImpl implements InvoiceLineServiceGst {

	@Inject
	public InvoiceLineServiceGstImpl(CurrencyService currencyService, PriceListService priceListService,
			AppAccountService appAccountService, AnalyticMoveLineService analyticMoveLineService,
			AccountManagementAccountService accountManagementAccountService,
			PurchaseProductService purchaseProductService) {
		super(currencyService, priceListService, appAccountService, analyticMoveLineService,
				accountManagementAccountService, purchaseProductService);
	}

	@Override
	public Map<String, Object> fillProductInformation(Invoice invoice, InvoiceLine invoiceLine) throws AxelorException {
		Map<String, Object> productInformation = super.fillProductInformation(invoice, invoiceLine);
		if (!Beans.get(AppService.class).isApp("gst")) {
		      return super.fillProductInformation(invoice, invoiceLine);
		}
		productInformation.putAll(this.getGstValues(invoice, invoiceLine));
		return productInformation;
	}

	@Override
	public Map<String, Object> getGstValues(Invoice invoice, InvoiceLine invoiceLine) {
		Map<String, Object> gstValues = new HashMap<>();
		try {
			if (invoiceLine.getTaxCode().equals("GST")) {
				State invoiceAddressState = invoice.getAddress().getState();
				State companyAddressState = invoice.getCompany().getAddress().getState();
				BigDecimal gstRate = invoiceLine.getProduct().getGstRate();
				gstValues.put("gstRate", gstRate);
//				BigDecimal netAmount = invoiceLine.getExTaxTotal();
				BigDecimal netAmount = invoiceLine.getPriceDiscounted().multiply(invoiceLine.getQty());
				if (!invoiceAddressState.getName().equals(companyAddressState.getName())) {
					System.err.println(gstRate.doubleValue());
					BigDecimal igst = gstRate.multiply(netAmount).divide(new BigDecimal(100));
					invoiceLine.setIgst(igst);
					gstValues.put("igst", igst);
					gstValues.put("sgst", new BigDecimal(0));
					gstValues.put("csgt", new BigDecimal(0));
//					BigDecimal grossAmount = netAmount.add(igst);
//					gstValues.put("inTaxTotal", grossAmount);
					return gstValues;
				} else {
					BigDecimal sgst = gstRate.multiply(netAmount).divide(new BigDecimal(100));
					sgst = sgst.divide(new BigDecimal(2));
					BigDecimal cgst = gstRate.multiply(netAmount).divide(new BigDecimal(100));
					cgst = cgst.divide(new BigDecimal(2));
					gstValues.put("igst", new BigDecimal(0));
					gstValues.put("csgt", cgst);
					gstValues.put("sgst", sgst);
//					BigDecimal grossAmount = netAmount.add(cgst).add(sgst);
//					gstValues.put("inTaxTotal", grossAmount);
//					 System.err.println(gstRate.doubleValue()+"FROM ELSE");
					return gstValues;
				}
			} else {
				gstValues.put("igst", BigDecimal.ZERO);
				gstValues.put("sgst", BigDecimal.ZERO);
				gstValues.put("csgt", BigDecimal.ZERO);
				gstValues.put("gstRate", BigDecimal.ZERO);
				return gstValues;
			}
		} catch (Exception e) {
			System.err.println("EXCEPTION");
		}
		return gstValues;
	}

	@Override
	public InvoiceLine calculateInvoiceLine(InvoiceLine line, Invoice parent) {
		return null;
	}
}
