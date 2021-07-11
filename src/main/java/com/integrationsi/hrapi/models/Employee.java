package com.integrationsi.hrapi.models;

public class Employee {
	

	public  static final String CDSTDO = "ZY";
	
    private String matcle;
    private String name;
    private String establishmentCode;
    private String establishmentLabel;
    private String teamCode;
    private String teamLabel;
    private String contract;
    

    public String getMatcle() {
        return matcle;
    }

    public void setMatcle(String matcle) {
        this.matcle = matcle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getTeamLabel() {
        return teamLabel;
    }

    public void setTeamLabel(String teamLabel) {
        this.teamLabel = teamLabel;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public String getEstablishmentCode() {
        return establishmentCode;
    }

    public void setEstablishmentCode(String establishmentCode) {
        this.establishmentCode = establishmentCode;
    }

    public String getEstablishmentLabel() { 	
        return establishmentLabel;
    }

    public void setEstablishmentLabel(String establishmentLabel) {
        this.establishmentLabel = establishmentLabel;
    }

	

}