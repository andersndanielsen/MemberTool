package hioa.mappe3.s180475;

public class Payment {

	private String memberID;
	private String regDate;
	private String year;
	private String typeOfMembership;
	
	public Payment(String id, String date, String y, String type){
		memberID = id;
		regDate = date;
		year = y;
		typeOfMembership = type;
	}
	public String getMemberID(){
		return memberID;
	}
	public String getRegDate(){
		return regDate;
	}
	public String getYear(){
		return year;
	}
	public String getTypeOfMembership(){
		return typeOfMembership;
	}
}
