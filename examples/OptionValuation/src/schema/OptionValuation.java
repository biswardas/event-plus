package schema;

import org.jquantlib.Settings;
import org.jquantlib.daycounters.Actual365Fixed;
import org.jquantlib.daycounters.DayCounter;
import org.jquantlib.exercise.EuropeanExercise;
import org.jquantlib.exercise.Exercise;
import org.jquantlib.instruments.EuropeanOption;
import org.jquantlib.instruments.Option;
import org.jquantlib.instruments.Payoff;
import org.jquantlib.instruments.PlainVanillaPayoff;
import org.jquantlib.instruments.VanillaOption;
import org.jquantlib.pricingengines.AnalyticEuropeanEngine;
import org.jquantlib.processes.BlackScholesMertonProcess;
import org.jquantlib.quotes.Handle;
import org.jquantlib.quotes.Quote;
import org.jquantlib.quotes.SimpleQuote;
import org.jquantlib.termstructures.BlackVolTermStructure;
import org.jquantlib.termstructures.YieldTermStructure;
import org.jquantlib.termstructures.volatilities.BlackConstantVol;
import org.jquantlib.termstructures.yieldcurves.FlatForward;
import org.jquantlib.time.Calendar;
import org.jquantlib.time.Date;
import org.jquantlib.time.Month;
import org.jquantlib.time.calendars.Target;

import com.biswa.ep.annotations.EPAttrType;
import com.biswa.ep.annotations.EPAttribute;
import com.biswa.ep.annotations.EPConType;
import com.biswa.ep.annotations.EPContainer;
import com.biswa.ep.annotations.EPContext;
import com.biswa.ep.annotations.EPPublish;

@EPContext()
public interface OptionValuation {
	@EPContainer(type = EPConType.Subscription, params = { "verbose=true" })
	public interface Reuters {
		@EPAttribute(type = EPAttrType.SubProcessor, processor = "processor.SlowProcessor")
		Double marketData = null;
	}
	static class Helper{
		public static VanillaOption europeanOption(Handle<Quote> underlyingH, Handle<YieldTermStructure> flatDividendTS, Handle<YieldTermStructure> flatTermStructure, Handle<BlackVolTermStructure> flatVolTS, Payoff payoff, Exercise europeanExercise) {

	        final BlackScholesMertonProcess bsmProcess = new BlackScholesMertonProcess(underlyingH, flatDividendTS, flatTermStructure, flatVolTS);

	        // European Options
	        final VanillaOption europeanOption = new EuropeanOption(payoff, europeanExercise);

	        europeanOption.setPricingEngine(new AnalyticEuropeanEngine(bsmProcess));
	        return europeanOption;
		}
	}
	@EPContainer(publish=EPPublish.RMI,generator = "generator.OptionGenerator")
	class InputStocks {
		@EPAttribute(type=EPAttrType.Static)
		final void setSettings(){
	        final Date todaysDate = new Date(15, Month.May, 1998);
	        new Settings().setEvaluationDate(todaysDate);
		}
		@EPAttribute(type=EPAttrType.Static)
        final Calendar calendar = new Target();

		@EPAttribute(type=EPAttrType.Static)
        final DayCounter dayCounter = new Actual365Fixed();

		@EPAttribute(type=EPAttrType.Subscriber,depends="symbol",container="Reuters")
		final private double priceChange = 0.0;
		// set up dates
		final String symbol=null;
		final private Date settlementDate=null;
        final private Date maturity = null;
        final Option.Type type = Option.Type.Put;
        final double strike = 0;
		final double underlying = priceChange+36.0;
        
        final private Handle<Quote> underlyingH = new Handle<Quote>(new SimpleQuote(underlying));
        
        // our options
        final double riskFreeRate = 0.06;
        final double volatility = 0.2;
        final double dividendYield = 0.00;



        // Define exercise for European Options
        final private Exercise europeanExercise = maturity!=null?new EuropeanExercise(maturity):null;

        // bootstrap the yield/dividend/volatility curves
        final private Handle<YieldTermStructure> flatDividendTS = settlementDate!=null?new Handle<YieldTermStructure>(new FlatForward(settlementDate, dividendYield, dayCounter)):null;
        final private Handle<YieldTermStructure> flatTermStructure = settlementDate!=null? new Handle<YieldTermStructure>(new FlatForward(settlementDate, riskFreeRate, dayCounter)):null;
        final private Handle<BlackVolTermStructure> flatVolTS = settlementDate!=null? new Handle<BlackVolTermStructure>(new BlackConstantVol(settlementDate, calendar, volatility, dayCounter)):null;

		final private Payoff payoff = new PlainVanillaPayoff(type, strike);
		final private VanillaOption europeanOption=Helper.europeanOption(underlyingH,flatDividendTS,flatTermStructure,flatVolTS,payoff,europeanExercise);
		final double tv=europeanOption.NPV();
		final double rho = europeanOption.rho();
		final double delta=europeanOption.delta();
		final double gamma=europeanOption.gamma();
		final double vega = europeanOption.vega();
		final double theta=europeanOption.theta();
		@SuppressWarnings("unused")
		private void log(double tv,double rho,double delta,double gamma,double vega,double theta){
			System.out.println("tv="+tv+" rho="+rho+" delta="+delta+" gamma="+gamma+" vega="+vega+" theta="+theta);
		}
	}
}
