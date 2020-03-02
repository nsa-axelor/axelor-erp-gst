package com.axelor.apps.gst.service;

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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

		invoice.setNetCsgt(netCgst);
		invoice.setNetIgst(netIgst);
		invoice.setNetSgst(netSgst);
		return invoice;
	}

	public List<InvoiceLine> calculateInvoiceLine(List<InvoiceLine> lines, Boolean isStateMatched) {
//    List<InvoiceLine> lines = invoice.getInvoiceLineList();
		List<InvoiceLine> invoiceItemList = new ArrayList<>();
		lines.forEach(line -> {
			Map<String, Object> map = invoiceLineService.getGstValues(isStateMatched, line);
			BigDecimal igst = (BigDecimal) map.get("igst");
			BigDecimal cgst = (BigDecimal) map.get("csgt");
			BigDecimal sgst = (BigDecimal) map.get("sgst");
			BigDecimal exTaxTotal = line.getPrice().multiply(line.getQty());
			BigDecimal inTaxTotal = (BigDecimal) map.get("inTaxTotal");
			line.setIgst(igst.setScale(2));
			line.setSgst(sgst.setScale(2));
			line.setCsgt(cgst.setScale(2));
			line.setExTaxTotal(exTaxTotal.setScale(2));
			line.setInTaxTotal(inTaxTotal.setScale(2));
//          line.setInvoice(invoice);
			invoiceItemList.add(line);
		});
//    invoice.setInvoiceLineList(invoiceItemList);
//    invoice.getInvoiceLineList().addInvir;
		return invoiceItemList;
	}

	@Override
	public Invoice compute(Invoice invoice) throws AxelorException {

		invoice = super.compute(invoice);
		if (!Beans.get(AppService.class).isApp("gst")) {
			return invoice;
		}
		try {
			final Invoice invoice2 = invoice;
			Boolean isStateMatched = invoiceLineService.checkIsStateMatched(invoice2);
			List<InvoiceLine> lines = this.calculateInvoiceLine(invoice2.getInvoiceLineList(),isStateMatched);
			for(InvoiceLine line : lines) {
				invoice.addInvoiceLineListItem(line);
			}
			invoice.setInvoiceLineList(lines);
			
			Invoice calculatedInvoice = this.getCalculatedInvoice(invoice2, lines);
			invoice.setNetIgst(calculatedInvoice.getNetIgst().setScale(2));
			invoice.setNetCsgt(calculatedInvoice.getNetCsgt().setScale(2));
			invoice.setNetSgst(calculatedInvoice.getNetSgst().setScale(2));
		} catch (Exception e) {
			System.err.println("Exception on compute(overrided)");
		}
		return invoice;
	}
}
