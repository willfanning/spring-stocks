package net.cs50.finance.controllers;

import net.cs50.finance.models.StockHolding;
import net.cs50.finance.models.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * Created by Chris Bay on 5/17/15.
 */
@Controller
public class PortfolioController extends AbstractFinanceController {

    @RequestMapping(value = "/portfolio")
    public String portfolio(HttpServletRequest request, Model model){

        // TODO - Implement portfolio display
        User user = this.getUserFromSession(request);
        Collection<StockHolding> holdings = user.getPortfolio().values();

        model.addAttribute("holdings", holdings);
        model.addAttribute("cash", user.getCash());
        model.addAttribute("title", "Portfolio");
        model.addAttribute("portfolioNavClass", "active");

        return "portfolio";
    }

}
