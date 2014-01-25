package hioa.mappe3.s180475;

import java.io.Serializable;

public class Member implements Serializable{
	private static final long serialVersionUID = 1L;
	private String id, firstname, lastname, start_membership, birth, streetadr, postnr, city, email, tlf, info;
	private boolean paidThisYear;

	public Member(String ident, String fname, String lname, String start, String birthday, 
			String street, String post, String c, String mail, String teleph, String informa){
		id = ident;
		firstname = fname;
		lastname = lname;
		start_membership = start;
		birth = birthday;
		streetadr = street;
		postnr = post;
		city = c;
		email = mail;
		tlf = teleph;
		info = informa;
		paidThisYear = false;
	}
	
	public String getId(){
		return id;
	}	
	public String getFirstname(){
		return firstname;
	}
	public String getLastname(){
		return lastname;
	}
	public String getStart(){
		return start_membership;
	}
	public String getBirth(){
		return birth;
	}
	public String getStreetadr(){
		return streetadr;
	}
	public String getPostnr(){
		return postnr;
	}
	public String getCity(){
		return city;
	}
	public String getEmail(){
		return email;
	}
	public String getTlf(){
		return tlf;
	}
	public String getInfo(){
		return info;
	}	
	public boolean getPaidThisYear(){
		return paidThisYear;
	}
	
	public void setPaidThisYear(boolean paid){
		paidThisYear = paid;
	}
}
