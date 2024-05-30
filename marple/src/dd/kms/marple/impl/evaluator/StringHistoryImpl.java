package dd.kms.marple.impl.evaluator;

import dd.kms.marple.api.evaluator.StringHistory;

import java.util.ArrayList;
import java.util.List;

class StringHistoryImpl implements StringHistory
{
	private final List<String>	strings		= new ArrayList<>();
	private int					lookupIndex	= 0;

	@Override
	public void addString(String s) {
		strings.add(s);
		lookupIndex = strings.size();
	}

	@Override
	public String lookup(LookupDirection lookupDirection) {
		int searchDirection = lookupDirection == LookupDirection.PREVIOUS ? -1 : 1;
		lookupIndex = Math.min(Math.max(0, lookupIndex + searchDirection), strings.size() - 1);
		return lookupIndex >= 0 ? strings.get(lookupIndex) : null;
	}
}
