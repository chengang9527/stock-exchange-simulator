package vn.com.vndirect.exchangesimulator.matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import vn.com.vndirect.exchangesimulator.datastorage.memory.InMemory;
import vn.com.vndirect.exchangesimulator.model.ExecutionReport;
import vn.com.vndirect.exchangesimulator.model.NewOrderSingle;
import vn.com.vndirect.exchangesimulator.model.SecurityStatus;

@Component
public class ATCSessionAllOrderMatcher {
	
	
	@Autowired
	private InMemory memory; 
	private Map<String, ATCSessionMatcher> matcherMap = new HashMap<String, ATCSessionMatcher>();
	
	public void push(NewOrderSingle order){
		String  symbol = order.getSymbol();
		if (!matcherMap.containsKey(symbol)){
			int floorPrice = getFloor(symbol);
			int ceilingPrice = getCeil(symbol);
			PriceRange range = new PriceRange(floorPrice, ceilingPrice, 100);
			matcherMap.put(symbol, new ATCSessionMatcher(range, symbol));
		}
		matcherMap.get(symbol).push(order);
	}
	
	protected int getFloor(String symbol){
		SecurityStatus securityStatus = (SecurityStatus) memory.get("securitystatus", symbol);
		if (securityStatus == null) return 100;
		return (int) Math.floor(securityStatus.getLowPx());	}
	
	protected int getCeil(String symbol){
		SecurityStatus securityStatus = (SecurityStatus) memory.get("securitystatus", symbol);		
		if (securityStatus == null) return 500000;
		return (int) Math.floor(securityStatus.getHighPx());
	}

	public void clear(){
		matcherMap.clear();
	}

	public List<ExecutionReport> processATC() {
		for(Map.Entry<String , ATCSessionMatcher> entry : matcherMap.entrySet()) {
			entry.getValue().processATC();
		}
		return getExecutionReport();
	}
	
	private List<ExecutionReport> getExecutionReport() {
		List<ExecutionReport> reports = new ArrayList<ExecutionReport>();
		for(Map.Entry<String , ATCSessionMatcher> entry : matcherMap.entrySet()) {
			reports.addAll(entry.getValue().getMatchedResult());
			reports.addAll(entry.getValue().getExpiredResult());
		}
		return reports;
		
	}
}
