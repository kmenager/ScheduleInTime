package io.github.kmenager.scheduleintime.ui;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventParcelable implements Parcelable{

	private String formationId;
	private Date startTime;
	private Date endTime;
	private List<String> labels = new ArrayList<>();

	/**
	 * Class create for passing data through different fragment class
	 * @param formationId the formation id
	 * @param labels the labels of the course
	 * @param startTime the start time of the course
	 * @param endTime the end of the course
	 */
	public EventParcelable(String formationId, List<String> labels, Date startTime, Date endTime) {
		this.formationId = formationId;
		this.labels = labels;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public EventParcelable() {
		this.formationId = "null";
		this.labels = null;
		this.startTime = null;
		this.endTime = null;
	}
	
	public EventParcelable(Parcel in) {
		readFromParcel(in);
	}
	
	public void setFormationId(String formationId) {
        this.formationId = formationId;
    }

    public String getFormationId() {
        return formationId;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public void addLabel(String label) {
        labels.add(label);
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<String> getLabels() {
        return labels;
    }
	
	public void readFromParcel(Parcel in) {
		formationId = in.readString();
		in.readList(labels, null);
		if(startTime != null && endTime != null){
			startTime = new Date(Long.parseLong(in.readString()));
			endTime = new Date(Long.parseLong(in.readString()));
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(formationId);
		dest.writeList(labels);
		if(startTime != null && endTime != null){
			dest.writeString(String.valueOf(startTime.getTime()));
			dest.writeString(String.valueOf(endTime.getTime()));
		}

	}

	public static final Parcelable.Creator<EventParcelable> CREATOR =
			new Parcelable.Creator<EventParcelable>() {
		public EventParcelable createFromParcel(Parcel in) {
			return new EventParcelable(in);
		}

		public EventParcelable[] newArray(int size) {
			return new EventParcelable[size];
		}
	};

}
