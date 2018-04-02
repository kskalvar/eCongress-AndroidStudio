package com.mycompany.app.econgress;

import java.util.HashMap;
import java.util.Iterator;

public class LegislatorObject {

	private HashMap<Integer, HashMap<String, String>> legislators = null;
	private HashMap<String, String> legislator = null;

	public LegislatorObject() {
	}

	String getLegislator(int iter) {

		String chamber = null;
		String last_name = null;
		String party = null;

		legislator = legislators.get(iter);

        for (String key : legislator.keySet()) {
            String value = legislator.get(key);
            if (key.equals("last_name")) {
                last_name = value;
            }
            if (key.equals("chamber")) {
                chamber = value;
                if (chamber.equals("senate"))
                    chamber = "US Sen";
                if (chamber.equals("house"))
                    chamber = "US Rep";
            }

            if (key.equals("party")) {
                party = value;
                if (party.equals("R"))
                    party = "(R)";
                if (party.equals("D"))
                    party = "(D)";
            }

        }
		return chamber + " " + last_name + " " + party;
	}

	String getWebsite(int iter) {

		String website = null;

		legislator = legislators.get(iter);
        for (String key : legislator.keySet()) {
            String value = legislator.get(key);
            if (key.equals("website")) {
                website = value;
            }
        }
		return website;

	}

	String getPhone(int iter) {
		String phone = "";
		legislator = legislators.get(iter);
        for (String key : legislator.keySet()) {
            String value = legislator.get(key);
            if (key.equals("phone")) {
                phone = value;
            }
        }
		return phone;
	}

	String getEmail(int iter) {
		String email = null;
		legislator = legislators.get(iter);
        for (String key : legislator.keySet()) {
            String value = legislator.get(key);
            if (key.equals("oc_email")) {
                email = value;
            }
        }
		return email;
	}

	boolean getEmailSend(int iter) {
		String sendMail = null;
		legislator = legislators.get(iter);
        for (String key : legislator.keySet()) {
            String value = legislator.get(key);
            if (key.equals("sendMail")) {
                sendMail = value;
            }
        }
        return "true".equals(sendMail);


	}

	void setEmailSend(int iter, boolean state) {

		legislator = legislators.get(iter);

        if (state) {
            legislator.put("sendMail", "true");
        } else {
            legislator.put("sendMail", "false");
        }
        legislators.put(iter, legislator);
    }

    void clearEmailSend() {

		for ( int iter = 0; iter < getLegislatorCount(); iter++) {
			legislator = legislators.get(iter);
			legislator.put("sendMail", "false");
			legislators.put(iter, legislator);
		}
	}

	int getLegislatorCount() {

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
