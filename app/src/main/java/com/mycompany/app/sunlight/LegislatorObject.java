package com.mycompany.app.sunlight;

import java.util.HashMap;
import java.util.Iterator;

public class LegislatorObject {

	private HashMap<Integer, HashMap<String, String>> legislators = null;
	private HashMap<String, String> legislator = null;

	public LegislatorObject() {
	}

	public String getLegislator(int iter) {

		String chamber = null;
		String last_name = null;
		String party = null;

		legislator = legislators.get(iter);

		Iterator<String> it = legislator.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String value = legislator.get(key);
			if (key.toString() == "last_name") {
				last_name = value.toString();
			}
			if (key.toString() == "chamber") {
				chamber = value.toString();
				if (chamber.equals("senate"))
					chamber = "US Sen";
				if (chamber.equals("house"))
					chamber = "US Rep";
			}

			if (key.toString() == "party") {
				party = value.toString();
				if (party.equals("R"))
					party = "(R)";
				if (party.equals("D"))
					party = "(D)";
			}

		}
		return chamber + " " + last_name + " " + party;
	}

	public String getWebsite(int iter) {

		String website = null;

		legislator = legislators.get(iter);

		Iterator<String> it = legislator.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String value = legislator.get(key);
			if (key.toString() == "website") {
				website = value.toString();
			}
		}
		return website;

	}

	public String getPhone(int iter) {
		String phone = "";
		legislator = legislators.get(iter);
		Iterator<String> it = legislator.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String value = legislator.get(key);
			if (key.toString() == "phone") {
				phone = value.toString();
			}
		}
		return phone;
	}

	public String getEmail(int iter) {
		String email = "";
		legislator = legislators.get(iter);
		Iterator<String> it = legislator.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String value = legislator.get(key);
			if (key.toString() == "oc_email") {
				email = value.toString();
			}
		}
		return email;
	}

	public int getLegislatorCount() {

		return legislators.size();
	}

	/**
	 * @return the legislators
	 */
	public HashMap<Integer, HashMap<String, String>> getLegislators() {
		return legislators;
	}

	/**
	 * @param legislators
	 *            the legislators to set
	 */
	public void setLegislators(HashMap<Integer, HashMap<String, String>> legislators) {
		this.legislators = legislators;
	}

}
