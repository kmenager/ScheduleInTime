package io.github.kmenager.scheduleintime.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import io.github.kmenager.scheduleintime.R;

public class OneWeekView extends Fragment{

	private ArrayList<EventParcelable> eventParce;
	private int weekOfYear;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* Getting args from bundle */
		Bundle data = getArguments();
		eventParce = data.getParcelableArrayList("events");
		weekOfYear = data.getInt("weekOfYear");
	}

	public class MyExpandableListAdapter extends BaseExpandableListAdapter {

		private String[] groups;
		private SparseArray<Vector<Vector<String>>> weekEvents;


		/**
		 * Initialize groups and save all events from a week in a SparseArray
		 * All event in a day are save in Vector themselves save in Vector of weekEvents
		 */
		public MyExpandableListAdapter() {
			Calendar cal = Calendar.getInstance();
			groups = new String[5];
			boolean firstInitGroup = true;
			Vector<String> mchildren;
			Vector<Vector<String>> mondayEvent    = new Vector<>();
			Vector<Vector<String>> tuesdayEvent   = new Vector<>();
			Vector<Vector<String>> wednesdayEvent = new Vector<>();
			Vector<Vector<String>> thursdayEvent  = new Vector<>();
			Vector<Vector<String>> fridayEvent    = new Vector<>();
			weekEvents = new SparseArray<>();

			cal.setFirstDayOfWeek(Calendar.MONDAY);
			EventParcelable ev1 = eventParce.get(0);

			Calendar refdate = Calendar.getInstance();
			refdate.set(Calendar.HOUR_OF_DAY, 6);
			
			if(!ev1.getFormationId().equals("null")) {
				for(EventParcelable ev : eventParce){
					cal.setTime(ev.getStartTime());
					
					if(firstInitGroup) {

						cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
						groups[Calendar.MONDAY - 2] = (String) android.text.format.DateFormat.format("EEEE"+ " d " + "MMMM", cal);
						cal.setTime(ev.getStartTime());

						cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
						groups[Calendar.TUESDAY - 2] = (String) android.text.format.DateFormat.format("EEEE"+ " d " + "MMMM", cal);
						cal.setTime(ev.getStartTime());

						cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
						groups[Calendar.WEDNESDAY - 2] = (String) android.text.format.DateFormat.format("EEEE"+ " d " + "MMMM", cal);
						cal.setTime(ev.getStartTime());

						cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
						groups[Calendar.THURSDAY - 2] = (String) android.text.format.DateFormat.format("EEEE"+ " d " + "MMMM", cal);
						cal.setTime(ev.getStartTime());

						cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
						groups[Calendar.FRIDAY - 2] = (String) android.text.format.DateFormat.format("EEEE"+ " d " + "MMMM", cal);
						cal.setTime(ev.getStartTime());
						firstInitGroup = false;
					}

					mchildren = new Vector<>();
					if(refdate.get(Calendar.HOUR_OF_DAY) == cal.get(Calendar.HOUR_OF_DAY)) {
						List<String> labels = ev.getLabels();
						mchildren.add(labels.get(0));
					} else {
						mchildren.add((String) android.text.format.DateFormat.format("kk"+ "'h'" + "mm", cal));
						cal.setTime(ev.getEndTime());
						mchildren.add((String) android.text.format.DateFormat.format("kk"+ "'h'" + "mm", cal));
						List<String> labels = ev.getLabels();
						for(String label : labels){
							mchildren.add(label);
						}
					}
					
					
					switch (cal.get(Calendar.DAY_OF_WEEK)) {
					case Calendar.MONDAY:
						mondayEvent.add(mchildren);
						break;

					case Calendar.TUESDAY:
						tuesdayEvent.add(mchildren);
						break;

					case Calendar.WEDNESDAY:
						wednesdayEvent.add(mchildren);
						break;

					case Calendar.THURSDAY:
						thursdayEvent.add(mchildren);
						break;

					case Calendar.FRIDAY:
						fridayEvent.add(mchildren);
						break;

					default:
						break;
					}
				}		

				weekEvents.put(Calendar.MONDAY - 2, mondayEvent);
				weekEvents.put(Calendar.TUESDAY - 2, tuesdayEvent);
				weekEvents.put(Calendar.WEDNESDAY - 2, wednesdayEvent);
				weekEvents.put(Calendar.THURSDAY - 2, thursdayEvent);
				weekEvents.put(Calendar.FRIDAY - 2, fridayEvent);
			}
			else {
				cal.set(Calendar.WEEK_OF_YEAR, weekOfYear);
				Date date = cal.getTime();
				if(firstInitGroup) {

					cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
					groups[Calendar.MONDAY - 2] = (String) android.text.format.DateFormat.format("EEEE"+ " d " + "MMMM", cal);
					cal.setTime(date);

					cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
					groups[Calendar.TUESDAY - 2] = (String) android.text.format.DateFormat.format("EEEE"+ " d " + "MMMM", cal);
					cal.setTime(date);

					cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
					groups[Calendar.WEDNESDAY - 2] = (String) android.text.format.DateFormat.format("EEEE"+ " d " + "MMMM", cal);
					cal.setTime(date);

					cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
					groups[Calendar.THURSDAY - 2] = (String) android.text.format.DateFormat.format("EEEE"+ " d " + "MMMM", cal);
					cal.setTime(date);

					cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
					groups[Calendar.FRIDAY - 2] = (String) android.text.format.DateFormat.format("EEEE"+ " d " + "MMMM", cal);
					cal.setTime(date);
					firstInitGroup = false;
				}
				weekEvents.put(Calendar.MONDAY - 2, null);
				weekEvents.put(Calendar.TUESDAY - 2, null);
				weekEvents.put(Calendar.WEDNESDAY - 2, null);
				weekEvents.put(Calendar.THURSDAY - 2, null);
				weekEvents.put(Calendar.FRIDAY - 2, null);
			}
		}

		@Override
		public Object getChild(int positionGroup, int positionChild) {
			return weekEvents.get(positionGroup).get(positionChild);
		}

		@Override
		public long getChildId(int positionGroup, int positionChild) {
			return positionChild;
		}

		/**
		 * Return view
		 */
		@Override
		public View getChildView(int positionGroup, int positionChild, boolean arg2, View arg3,
				ViewGroup arg4) {

			View inflatedView = null;
			Vector<Vector<String>> days = weekEvents.get(positionGroup);
			if (days == null) {
				inflatedView = View.inflate(getActivity().getApplicationContext(),
						R.layout.empty_layout, null);

			} else {
				Vector<String> mchildren = days.get(positionChild);
				if (mchildren.size() == 6) {
					inflatedView = View.inflate(getActivity().getApplicationContext(),
							R.layout.child_layout, null);


					TextView tvStartTime =(TextView)inflatedView.findViewById(R.id.start_time);
					tvStartTime.setText(mchildren.get(0));

					TextView tvEndTime =(TextView)inflatedView.findViewById(R.id.end_time);
					tvEndTime.setText(mchildren.get(1));

					TextView tvSubject =(TextView)inflatedView.findViewById(R.id.subject);
					tvSubject.setText(mchildren.get(2));

					TextView tvTeacher =(TextView)inflatedView.findViewById(R.id.teacher);
					tvTeacher.setText(mchildren.get(3));

					TextView tvClassroom =(TextView)inflatedView.findViewById(R.id.classroom);
					tvClassroom.setText(mchildren.get(5));

				}
				else if (mchildren.size() > 6) {
					//Special view if we have exam or something else like LastProject Reunion
					inflatedView = View.inflate(getActivity().getApplicationContext(),
							R.layout.special_child_layout, null);

					TextView tvStartTime =(TextView)inflatedView.findViewById(R.id.start_time_spe);
					tvStartTime.setText(mchildren.get(0));

					TextView tvEndTime =(TextView)inflatedView.findViewById(R.id.end_time_spe);
					tvEndTime.setText(mchildren.get(1));

					TextView tvSubject =(TextView)inflatedView.findViewById(R.id.subject_spe);
					tvSubject.setText(mchildren.get(2));

					TextView tvTeacher =(TextView)inflatedView.findViewById(R.id.teacher_spe);
					tvTeacher.setText(mchildren.get(3));

					TextView tvClassroom =(TextView)inflatedView.findViewById(R.id.classroom_spe);
					tvClassroom.setText(mchildren.get(5));

					TextView tvExamen =(TextView)inflatedView.findViewById(R.id.type_spe);
					tvExamen.setText(mchildren.get(6));
				} else if (mchildren.size() < 6) {
					inflatedView = View.inflate(getActivity().getApplicationContext(),
							R.layout.empty_layout, null);
					
					TextView tvFreeTime = (TextView) inflatedView.findViewById(R.id.free_time);
					tvFreeTime.setText(getResources().getString(R.string.entreprise_txt));
				}
			}
			if (inflatedView != null)
				inflatedView.setBackgroundColor(getResources().getColor(R.color.child_backgroud_color));
			return inflatedView;
		}

		@Override
		public int getChildrenCount(int positionGroup) {
			if(weekEvents.get(positionGroup) == null)
				return 1;
			else
				return weekEvents.get(positionGroup).size();
		}

		@Override
		public Object getGroup(int positionGroup) {
			return groups[positionGroup];
		}

		@Override
		public int getGroupCount() {
			return groups.length;
		}

		@Override
		public long getGroupId(int arg0) {
			return arg0;
		}

		@Override
		public View getGroupView(int positionGroup, boolean arg1, View arg2,
				ViewGroup parent) {
			View inflatedView = View.inflate(getActivity().getApplicationContext(),
					R.layout.groups_layout, null);

			inflatedView.setPadding(100, 20, 0, 20);
			inflatedView.setBackgroundColor(getResources().getColor(R.color.group_background_color));
			TextView tv = (TextView) inflatedView.findViewById(R.id.group);
			tv.setText(groups[positionGroup]);
			return inflatedView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			return false;
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		View v = inflater.inflate(R.layout.one_week_layout, container,false);
		ExpandableListView elv = (ExpandableListView) v.findViewById(R.id.list);
		elv.setAdapter(new MyExpandableListAdapter());		
		return elv;

	}
}
