package com.axelor.apps.gst.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class InvoiceServiceGstImpl extends InvoiceServiceProjectImpl {

	@Inject
	public InvoiceServiceGstImpl(ValidateFactory validateFactory, VentilateFactory ventilateFactory,
			CancelFactory cancelFactory, AlarmEngineService<Invoice> alarmEngineService, InvoiceRepository invoiceRepo,
			AppAccountService appAccountService, PartnerService partnerService,
			com.axelor.apps.account.service.invoice.InvoiceLineService invoiceLineService,
			AccountConfigService accountConfigService) {
		super(validateFactory, ventilateFactory, cancelFactory, alarmEngineService, invoiceRepo, appAccountService,
				partnerService, invoiceLineService, accountConfigService);
		// TODO Auto-generated constructor stub
	}

	@Inject
	InvoiceLineServiceGst invoiceLineService;

	public Invoice getCalculatedInvoice(Invoice invoice, List<InvoiceLine> lineList) {
		BigDecimal netIgst = new BigDecimal(0);
		BigDecimal netCgst = new BigDecimal(0);
		BigDecimal netSgst = new BigDecimal(0);
		for (InvoiceLine line : lineList) {
			Map<String, Object> map = invoiceLineService.getGstValues(invoice, line);
			BigDecimal igst = (BigDecimal) map.get("igst");
			BigDecimal cgst = (BigDecimal) map.get("csgt");
			BigDecimal sgst = (BigDecimal) map.get("sgst");
			netIgst = netIgst.add(igst);
			netSgst = netSgst.add(cgst);
			netCgst = netCgst.add(sgst);
		}

		invoice.setNetCsgt(netCgst);
		invoice.setNetIgst(netIgst);
		invoice.setNetSgst(netSgst);
		return invoice;
	}

	public List<InvoiceLine> recalculateInvoiceLine(Invoice invoice) {
		List<InvoiceLine> lines = invoice.getInvoiceLineList();
		List<InvoiceLine> invoiceItemList = new ArrayList<>();
		lines.forEach(line -> {
			Map<String, Object> map = invoiceLineService.getGstValues(invoice, line);
			BigDecimal igst = (BigDecimal) map.get("igst");
			BigDecimal cgst = (BigDecimal) map.get("csgt");
			BigDecimal sgst = (BigDecimal) map.get("sgst");
			BigDecimal exTaxTotal = line.getPrice().multiply(line.getQty());
			BigDecimal inTaxTotal = (BigDecimal) map.get("inTaxTotal");
			line.setIgst(igst);
			line.setSgst(sgst);
			line.setCsgt(cgst);
			line.setExTaxTotal(exTaxTotal);
			line.setInTaxTotal(inTaxTotal);
			invoiceItemList.add(line);
		});
		return invoiceItemList;
	}

	@Override
	public Invoice compute(Invoice invoice) throws AxelorException {

		Invoice invoice2 = super.compute(invoice);
		if (!Beans.get(AppService.class).isApp("gst")) {
			return invoice2;
		}
		try {
			List<InvoiceLine> lines = this.recalculateInvoiceLine(invoice2);
			invoice2.setInvoiceLineList(lines);
			Invoice calculatedInvoice = this.getCalculatedInvoice(invoice, lines);
			invoice2.setNetIgst(calculatedInvoice.getNetIgst().setScale(2));
			invoice2.setNetCsgt(calculatedInvoice.getNetCsgt().setScale(2));
			invoice2.setNetSgst(calculatedInvoice.getNetSgst().setScale(2));
		} catch (Exception e) {
			System.err.println("Exception on compute(overrided)");
		}
		return invoice2;
	}

}
