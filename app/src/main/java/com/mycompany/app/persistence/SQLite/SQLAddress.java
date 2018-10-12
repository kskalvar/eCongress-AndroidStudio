package com.mycompany.app.persistence.SQLite;

public class SQLAddress {
	
	private String PREFIX 		= "";
	private String FIRSTNAME 	= "";
	private String MI 			= "";
	private String LASTNAME 	= "";
	private String ADDRESS1 	= "";
	private String ADDRESS2 	= "";
	private String ZIP 			= "";
	private String PLUS4 		= "";
	private String STATE 		= "";
	private String TELEPHONE 	= "";
	private String TEST         = "";
	private String JSON         = "";


	public String getPREFIX() {
		return PREFIX;
	}
	public void setPREFIX(String pREFIX) {
		PREFIX = pREFIX;
	}
	public String getFIRSTNAME() {
		return FIRSTNAME;
	}
	public void setFIRSTNAME(String fIRSTNAME) {
		FIRSTNAME = fIRSTNAME;
	}
	public String getMI() {
		return MI;
	}
	public void setMI(String mI) {
		MI = mI;
	}
	public String getLASTNAME() {
		return LASTNAME;
	}
	public void setLASTNAME(String lASTNAME) {
		LASTNAME = lASTNAME;
	}
	public String getADDRESS1() {
		return ADDRESS1;
	}
	public void setADDRESS1(String aDDRESS1) {
		ADDRESS1 = aDDRESS1;
	}
	public String getADDRESS2() {
		return ADDRESS2;
	}
	public void setADDRESS2(String aDDRESS2) {
		ADDRESS2 = aDDRESS2;
	}
	public String getZIP() {
		return ZIP;
	}
	public void setZIP(String zIP) {
		ZIP = zIP;
	}
	public String getPLUS4() {
		return PLUS4;
	}
	public void setPLUS4(String pLUS4) {
		PLUS4 = pLUS4;
	}
	public String getSTATE() {
		return STATE;
	}
	public void setSTATE(String sTATE) {
		STATE = sTATE;
	}
	public String getTELEPHONE() {
		return TELEPHONE;
	}
	public void setTELEPHONE(String tELEPHONE) {
		TELEPHONE = tELEPHONE;
	}
	public String getTEST() {return TEST;}
	public void setTEST(String tEST) {
		TEST = tEST;
	}
	public String getJSON() {return JSON;}
	public void setJSON(String jSON) { JSON = jSON; }

	public String getSignature() {
		
		String signature = "";
		
		/*
		 * format signature Name
		 */

		String prefix = null;
		String middleIntial = null;

		if (!getPREFIX().isEmpty()) {
			if (getPREFIX().contains(".")) {
				prefix = getPREFIX();
			} else {
				prefix = getPREFIX() + ".";
			}
		}

		if (!getMI().isEmpty()) {
			if (getMI().contains(".")) {
				middleIntial = getMI();
			} else {
				middleIntial = getMI() + ".";
			}
		}

		if (!getPREFIX().isEmpty() && !getLASTNAME().isEmpty()) {
			signature = signature + prefix + " ";
			if (!this.getFIRSTNAME().isEmpty()) signature = signature + this.getFIRSTNAME() + " ";
			if (!this.getMI().isEmpty()) signature = signature + middleIntial + " ";
			signature = signature + this.getLASTNAME() + "\n";
			
		} else if (getPREFIX().isEmpty() && !getLASTNAME().isEmpty()) {
			if (!this.getFIRSTNAME().isEmpty()) signature = signature + this.getFIRSTNAME() + " ";
			if (!this.getMI().isEmpty()) signature = signature + middleIntial + " ";
			signature = signature + this.getLASTNAME() + "\n";
		}
		
		/*
		 * format signature Address
		 */
		
		if (!getADDRESS1().isEmpty()) signature = signature + getADDRESS1() + "\n";
		
		/*
		 * format signature City State Zip
		 */
		
		if (!getADDRESS2().isEmpty() && !getSTATE().isEmpty() && !getZIP().isEmpty() && !getPLUS4().isEmpty()) {
			signature = signature + this.getADDRESS2() + " " + this.getSTATE() + " " + this.getZIP() + "-" + this.getPLUS4() + "\n";
		} else if (!getADDRESS2().isEmpty() && !getSTATE().isEmpty() && !getZIP().isEmpty()) {
			signature = signature + this.getADDRESS2() + " " + this.getSTATE() + " " + this.getZIP() + "\n";
		}

		/*
		 * format signature Telephone
		 */
		
		if (!getTELEPHONE().isEmpty()) signature = signature + "phone " + getTELEPHONE() + "\n";
				
		return signature;
		
	}

    public String getAddressUrl() {

	    String addressUrl = null;

	    addressUrl = getADDRESS1() + " " + getADDRESS2() + " " + getSTATE() + " " + getZIP() + "-" + getPLUS4();
	    addressUrl = addressUrl.replaceAll(" ", "+").toLowerCase();

	    return addressUrl;

    }
}
