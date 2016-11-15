package org.launchcode.stocks.controllers;

import javax.servlet.http.HttpServletRequest;

import org.launchcode.stocks.models.Stock;
import org.launchcode.stocks.models.StockHolding;
import org.launchcode.stocks.models.StockLookupException;
import org.launchcode.stocks.models.User;
import org.launchcode.stocks.models.dao.StockHoldingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Chris Bay on 5/17/15.
 */
@Controller
public class StockController extends AbstractController {

	@Autowired
	StockHoldingDao stockHoldingDao;

	@RequestMapping(value = "/quote", method = RequestMethod.GET)
	public String quoteForm(Model model) {

		// pass data to template
		model.addAttribute("title", "Quote");
		model.addAttribute("quoteNavClass", "active");
		return "quote_form";
	}

	@RequestMapping(value = "/quote", method = RequestMethod.POST)
	public String quote(String symbol, Model model) {

		try {
			Stock s = Stock.lookupStock(symbol);
			model.addAttribute("stock_desc", s.toString());
			model.addAttribute("stock_price", s.getPrice());

		} catch (StockLookupException e) {
			e.printStackTrace();
			return displayError(e.getMessage(), model);
		}
		model.addAttribute("title", "Quote");
		model.addAttribute("quoteNavClass", "active");

		return "quote_display";
	}

	@RequestMapping(value = "/buy", method = RequestMethod.GET)
	public String buyForm(Model model) {

		model.addAttribute("title", "Buy");
		model.addAttribute("action", "/buy");
		model.addAttribute("buyNavClass", "active");
		return "transaction_form";
	}

	@RequestMapping(value = "/buy", method = RequestMethod.POST)
	public String buy(String symbol, int numberOfShares, HttpServletRequest request, Model model) {

		// TODO - Implement buy action
		symbol = symbol.toUpperCase();
		User user = getUserFromSession(request);
		StockHolding holding;
		Float price;
		
		try {
			holding = StockHolding.buyShares(user, symbol, numberOfShares);
			price = Stock.lookupStock(symbol).getPrice();

		} catch (StockLookupException e) {
			e.printStackTrace();
			return displayError(e.getMessage(), model);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return displayError(e.getMessage(), model);
		}

		stockHoldingDao.save(holding);
		
		String confirmMessage = String.format("Transaction Complete. Purchased " + numberOfShares + 
				" share(s) of " + symbol + " at $" + price + " per share");
		
		model.addAttribute("confirmMessage", confirmMessage);
		model.addAttribute("title", "Buy");
		model.addAttribute("action", "/buy");
		model.addAttribute("buyNavClass", "active");

		return "transaction_confirm";
	}

	@RequestMapping(value = "/sell", method = RequestMethod.GET)
	public String sellForm(Model model) {
		model.addAttribute("title", "Sell");
		model.addAttribute("action", "/sell");
		model.addAttribute("sellNavClass", "active");
		return "transaction_form";
	}

	@RequestMapping(value = "/sell", method = RequestMethod.POST)
	public String sell(String symbol, int numberOfShares, HttpServletRequest request, Model model) {

		// TODO - Implement sell action
		symbol = symbol.toUpperCase();
		User user = getUserFromSession(request);
		StockHolding holding;
		Float price;
		
		try {
			holding = StockHolding.sellShares(user, symbol, numberOfShares);
			price = Stock.lookupStock(symbol).getPrice();

		} catch (StockLookupException e) {
			e.printStackTrace();
			return displayError(e.getMessage(), model);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return displayError(e.getMessage(), model);
		}
		
		stockHoldingDao.save(holding);
		if (holding.getSharesOwned() == 0) {
			stockHoldingDao.delete(holding);
		}
		
		String confirmMessage = String.format("Transaction Complete. Sold " + numberOfShares + 
				" share(s) of " + symbol + " at $" + price + " per share");
		
		model.addAttribute("confirmMessage", confirmMessage);
		model.addAttribute("title", "Sell");
		model.addAttribute("action", "/sell");
		model.addAttribute("sellNavClass", "active");

		return "transaction_confirm";
	}

}
