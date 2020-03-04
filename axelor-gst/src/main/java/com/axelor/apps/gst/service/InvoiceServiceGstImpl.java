package com.axelor.apps.gst.service;

import java.math.BigDecimal;
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
import com.axelor.exception.service.TraceBackService;
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
	}

	@Inject
	InvoiceLineServiceGst invoiceLineService;

	public Invoice getCalculatedInvoice(Invoice invoice, List<InvoiceLine> lineList) {
		BigDecimal netIgst = BigDecimal.ZERO;
		BigDecimal netCgst = BigDecimal.ZERO;
		BigDecimal netSgst = BigDecimal.ZERO;
		Boolean isStateMatched = invoiceLineService.checkIsStateMatched(invoice);
		for (InvoiceLine line : lineList) {
			Map<String, Object> map = invoiceLineService.getGstValues(isStateMatched, line);
			BigDecimal igst = (BigDecimal) map.get("igst");
			BigDecimal cgst = (BigDecimal) map.get("csgt");
			BigDecimal sgst = (BigDecimal) map.get("sgst");
			netIgst = netIgst.add(igst);
			netSgst = netSgst.add(cgst);
			netCgst = netCgst.add(sgst);
		}
		invoice.setNetCsgt(netCgst.setScale(2));
		invoice.setNetIgst(netIgst.setScale(2));
		invoice.setNetSgst(netSgst.setScale(2));
		return invoice;
	}

	public List<InvoiceLine> calculateInvoiceLine(List<InvoiceLine> lines, Boolean isStateMatched) {
		lines.forEach(line -> {
			Map<String, Object> map = invoiceLineService.getGstValues(isStateMatched, line);
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
		});
		return lines;
	}

	@Override
	public Invoice compute(Invoice invoice) throws AxelorException {
		invoice = super.compute(invoice);
		if (!Beans.get(AppService.class).isApp("gst")) {
			return invoice;
		}
		try {
			Boolean isStateMatched = invoiceLineService.checkIsStateMatched(invoice);
			List<InvoiceLine> lines = this.calculateInvoiceLine(invoice.getInvoiceLineList(), isStateMatched);			
			invoice.setInvoiceLineList(lines);
			invoice = this.getCalculatedInvoice(invoice, lines);
		} catch (Exception e) {
			TraceBackService.trace(e);
		}
		return invoice;
	}
}
