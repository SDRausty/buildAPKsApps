package fr.isima.chuckNorrisFactsV2.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class Fact implements Parcelable,Comparable<Fact>{
	
	private int id;
	private String fact;
	private String author;
	
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFact() {
		return fact;
	}
	public void setFact(String fact) {
		this.fact = fact;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	@Override
	public String toString() {
		return "Fact [id=" + id + ", fact=" + fact + ", author=" + author + "]";
	}
	public Fact(int id, String fact, String author) {
		super();
		this.id = id;
		this.fact = fact;
		this.author = author;
	}

	
	// //////////////////////////////////////////////
		// ////
		// //// PARCEL
		// ////
		// ///////////////////////////////////////////////
		public static final Parcelable.Creator<Fact> CREATOR = new Parcelable.Creator<Fact>() {
			public Fact createFromParcel(Parcel in) {
				return new Fact(in);
			}

			public Fact[] newArray(int size) {
				return new Fact[size];
			}
		};

		public Fact(Parcel in) {
			readFromParcel(in);
		}

		
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		
		public void writeToParcel(Parcel parcel, int flags) {
			parcel.writeInt(id);
			parcel.writeString(fact);
			parcel.writeString(author);
		}

		private void readFromParcel(Parcel parcel) {
			id = parcel.readInt();
			fact = parcel.readString();
			author = parcel.readString();
		}
		/////////////////////////////////////////////////////////////
		
		public int compareTo(Fact otherFact) {
			return (this.getId() > otherFact.getId()) ? 1 : -1;
		}
}





